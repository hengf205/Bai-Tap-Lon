package com.example.qlkytucxa.controller;

import com.example.qlkytucxa.dto.DashboardDto;
import com.example.qlkytucxa.dto.TenantDashboardDto;
import com.example.qlkytucxa.entity.*;
import com.example.qlkytucxa.exception.ResourceNotFoundException;
import com.example.qlkytucxa.repository.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {
    private final BuildingRepository buildings;
    private final RoomRepository rooms;
    private final TenantRepository tenants;
    private final InvoiceRepository invoices;
    private final IncidentRepository incidents;
    private final ContractRepository contracts;
    private final NotificationRepository notifications;

    public DashboardController(BuildingRepository buildings, RoomRepository rooms, TenantRepository tenants,
                               InvoiceRepository invoices, IncidentRepository incidents, ContractRepository contracts,
                               NotificationRepository notifications) {
        this.buildings = buildings; this.rooms = rooms; this.tenants = tenants; this.invoices = invoices;
        this.incidents = incidents; this.contracts = contracts;
        this.notifications = notifications;
    }

    @GetMapping({"/admin/dashboard", "/manager/dashboard"})
    public String adminDashboard(Model model) {
        LocalDate today = LocalDate.now();
        DashboardDto dto = DashboardDto.builder()
                .totalBuildings(buildings.count())
                .totalRooms(rooms.count())
                .availableRooms(rooms.countByStatus(RoomStatus.AVAILABLE))
                .occupiedRooms(rooms.countByStatus(RoomStatus.OCCUPIED) + rooms.countByStatus(RoomStatus.FULL))
                .maintenanceRooms(rooms.countByStatus(RoomStatus.MAINTENANCE))
                .totalTenants(tenants.count())
                .activeTenants(tenants.countByStatus(TenantStatus.ACTIVE))
                .unpaidInvoices(invoices.countByStatus(InvoiceStatus.UNPAID))
                .overdueInvoices(invoices.findOverdueInvoices(today).size())
                .pendingIncidents(incidents.countByStatus(IncidentStatus.PENDING) + incidents.countByStatus(IncidentStatus.PROCESSING))
                .activeContracts(contracts.countByStatus(ContractStatus.ACTIVE))
                .currentMonthRevenue(invoices.sumRevenueByMonthAndYear(today.getMonthValue(), today.getYear()))
                .currentMonthUtility(invoices.sumUtilityByMonthAndYear(today.getMonthValue(), today.getYear()))
                .currentYear(today.getYear()).currentMonth(today.getMonthValue())
                .monthlyRevenueData(monthlyRevenue(today.getYear()))
                .roomRevenueData(monthlyValues(invoices.getMonthlyRoomRevenue(today.getYear())))
                .serviceRevenueData(monthlyValues(invoices.getMonthlyServiceRevenue(today.getYear())))
                .monthLabels(List.of("T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"))
                .roomStatusData(List.of(rooms.countByStatus(RoomStatus.AVAILABLE),
                        rooms.countByStatus(RoomStatus.OCCUPIED), rooms.countByStatus(RoomStatus.FULL),
                        rooms.countByStatus(RoomStatus.MAINTENANCE)))
                .roomStatusLabels(List.of("Trống", "Đang ở", "Đầy", "Bảo trì"))
                .tenantsByBuilding(tenantsByBuilding())
                .build();
        model.addAttribute("dashboard", dto);
        return "admin/dashboard";
    }

    @GetMapping("/tenant/dashboard")
    public String tenantDashboard(@AuthenticationPrincipal User user, Model model) {
        Tenant tenant = tenants.findByUsername(user.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Hồ sơ người thuê", -1L));
        Contract contract = contracts.findByTenantIdAndStatus(tenant.getId(), ContractStatus.ACTIVE).orElse(null);
        Contract pendingRental = contracts.findByTenantIdOrderByStartDateDesc(tenant.getId()).stream()
                .filter(c -> c.getStatus() == ContractStatus.PENDING).findFirst().orElse(null);
        Invoice pendingRentalInvoice = invoices.findByTenantUsername(user.getUsername()).stream()
                .filter(i -> i.getContract().getStatus() == ContractStatus.PENDING)
                .findFirst().orElse(null);
        Invoice currentInvoice = invoices.findByTenantUsername(user.getUsername()).stream()
                .filter(i -> i.getBillingMonth() != null && i.getBillingMonth().getMonthValue() == LocalDate.now().getMonthValue()
                        && i.getBillingMonth().getYear() == LocalDate.now().getYear())
                .findFirst().orElse(null);
        TenantDashboardDto dto = TenantDashboardDto.builder()
                .fullName(tenant.getFullName()).studentCode(tenant.getStudentCode()).avatar(tenant.getAvatar())
                .roomCode(contract == null ? null : contract.getRoom().getRoomCode())
                .buildingName(contract == null ? null : contract.getRoom().getBuildingName())
                .floorInfo(contract == null ? null : String.valueOf(contract.getRoom().getFloor().getFloorNumber()))
                .contractCode(contract == null ? null : contract.getContractCode())
                .contractEndDate(contract == null ? null : contract.getEndDate())
                .contractStatus(contract == null ? null : contract.getStatus().name())
                .monthlyPrice(contract == null ? null : contract.getMonthlyPrice())
                .currentInvoiceCode(currentInvoice == null ? null : currentInvoice.getInvoiceCode())
                .currentInvoiceTotal(currentInvoice == null ? null : currentInvoice.getTotalAmount())
                .dueDate(currentInvoice == null ? null : currentInvoice.getDueDate())
                .invoiceStatus(currentInvoice == null ? null : currentInvoice.getStatus().name())
                .isOverdue(currentInvoice != null && currentInvoice.isOverdue())
                .pendingIncidentsCount(incidents.findByTenantIdOrderByCreatedAtDesc(tenant.getId()).stream()
                        .filter(i -> i.getStatus() == IncidentStatus.PENDING || i.getStatus() == IncidentStatus.PROCESSING).count())
                .unreadNotifications(notifications.countByUserIdAndIsRead(user.getId(), false))
                .build();
        model.addAttribute("dashboard", dto);
        model.addAttribute("pendingRental", pendingRental);
        model.addAttribute("pendingRentalInvoice", pendingRentalInvoice);
        return "tenant/dashboard";
    }

    private List<BigDecimal> monthlyRevenue(int year) {
        return monthlyValues(invoices.getMonthlyRevenue(year));
    }

    private List<BigDecimal> monthlyValues(List<Object[]> rows) {
        List<BigDecimal> values = new ArrayList<>(java.util.Collections.nCopies(12, java.math.BigDecimal.ZERO));
        for (Object[] row : rows) {
            int month = ((Number) row[0]).intValue();
            values.set(month - 1, (BigDecimal) row[1]);
        }
        return values;
    }

    private Map<String, Long> tenantsByBuilding() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : contracts.countActiveTenantsByBuilding()) {
            result.put((String) row[0], ((Number) row[1]).longValue());
        }
        return result;
    }
}
