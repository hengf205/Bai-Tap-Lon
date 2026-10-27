package com.example.qlkytucxa.service;

import com.example.qlkytucxa.entity.*;
import com.example.qlkytucxa.exception.InvalidOperationException;
import com.example.qlkytucxa.exception.ResourceNotFoundException;
import com.example.qlkytucxa.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class RentalRequestService {
    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final ContractRepository contracts;
    private final BedRepository beds;
    private final RoomRepository rooms;
    private final TenantRepository tenants;
    private final InvoiceRepository invoices;
    private final InvoiceService invoiceService;
    private final NotificationRepository notifications;

    public RentalRequestService(ContractRepository contracts, BedRepository beds, RoomRepository rooms,
                                TenantRepository tenants, InvoiceRepository invoices,
                                InvoiceService invoiceService, NotificationRepository notifications) {
        this.contracts = contracts;
        this.beds = beds;
        this.rooms = rooms;
        this.tenants = tenants;
        this.invoices = invoices;
        this.invoiceService = invoiceService;
        this.notifications = notifications;
    }

    @Transactional
    public void approve(Long contractId) {
        Contract contract = contracts.findByIdForUpdate(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu thuê", contractId));
        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new InvalidOperationException("Chỉ có thể duyệt yêu cầu đang chờ xử lý.");
        }
        if (!invoices.findByContractIdOrderByBillingMonthDesc(contractId).isEmpty()) {
            throw new InvalidOperationException("Yêu cầu này đã có hóa đơn. Vui lòng không tạo hóa đơn đăng ký trùng.");
        }
        if (contracts.existsByTenantIdAndStatusAndIdNot(contract.getTenant().getId(), ContractStatus.ACTIVE, contractId)) {
            throw new InvalidOperationException("Người thuê đã có hợp đồng đang hiệu lực.");
        }
        if (contract.getBed() == null) throw new InvalidOperationException("Yêu cầu thuê chưa có giường.");
        Tenant tenant = tenants.findByIdWithUser(contract.getTenant().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Hồ sơ người thuê", contract.getTenant().getId()));
        if (tenant.getUser() == null) throw new InvalidOperationException("Người thuê chưa có tài khoản để nhận hóa đơn trực tuyến.");
        Bed bed = beds.findByIdForUpdate(contract.getBed().getId())
                .orElseThrow(() -> new InvalidOperationException("Giường không còn tồn tại."));
        if (!bed.getRoom().getId().equals(contract.getRoom().getId())) {
            throw new InvalidOperationException("Giường của yêu cầu không thuộc phòng đã đăng ký.");
        }
        if (bed.getStatus() != BedStatus.RESERVED && bed.getStatus() != BedStatus.AVAILABLE) {
            throw new InvalidOperationException("Giường đã được sử dụng hoặc không còn khả dụng.");
        }
        if (bed.getStatus() == BedStatus.AVAILABLE) {
            bed.setStatus(BedStatus.RESERVED);
            beds.save(bed);
        }
        LocalDate today = LocalDate.now(APP_ZONE);
        Invoice invoice = Invoice.builder().contract(contract)
                .billingMonth(today.withDayOfMonth(1)).roomPrice(contract.getMonthlyPrice())
                .electricityAmount(java.math.BigDecimal.ZERO).waterAmount(java.math.BigDecimal.ZERO)
                .serviceAmount(java.math.BigDecimal.ZERO).otherAmount(contract.getDeposit())
                .discount(java.math.BigDecimal.ZERO).dueDate(today).status(InvoiceStatus.UNPAID).build();
        invoiceService.create(invoice);

        notifications.save(Notification.builder().user(tenant.getUser()).title("Yêu cầu thuê đã được duyệt")
                .content("Yêu cầu thuê " + contract.getRoom().getRoomCode() + " đã được duyệt. Hóa đơn tiền phòng và tiền cọc đã có trong mục Hóa đơn; vui lòng thanh toán trong ngày " + today + ".")
                .type(NotificationType.INVOICE).isRead(false).build());
        refreshRoomStatus(contract.getRoom());
    }

    @Transactional
    public void reject(Long contractId) {
        Contract contract = contracts.findByIdForUpdate(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu thuê", contractId));
        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new InvalidOperationException("Chỉ có thể từ chối yêu cầu đang chờ xử lý.");
        }
        contract.setStatus(ContractStatus.CANCELLED);
        cancelOutstandingInvoices(contract);
        releaseReservedBed(contract);
        tenants.findByIdWithUser(contract.getTenant().getId()).ifPresent(tenant -> {
            if (tenant.getStatus() == TenantStatus.PENDING) tenant.setStatus(TenantStatus.INACTIVE);
            if (tenant.getUser() != null) {
                notifications.save(Notification.builder().user(tenant.getUser()).title("Yêu cầu thuê chưa được chấp thuận")
                        .content("Yêu cầu thuê phòng " + contract.getRoom().getRoomCode() + " chưa được chấp thuận. Vui lòng liên hệ ban quản lý để biết thêm thông tin.")
                        .type(NotificationType.CONTRACT).isRead(false).build());
            }
        });
        contracts.save(contract);
        refreshRoomStatus(contract.getRoom());
    }

    @Transactional
    public int cancelExpiredRequests() {
        LocalDate today = LocalDate.now(APP_ZONE);
        List<Invoice> expired = invoices.findExpiredRentalInvoices(InvoiceStatus.UNPAID, ContractStatus.PENDING, today);
        for (Invoice invoice : expired) {
            Contract contract = invoice.getContract();
            invoice.setStatus(InvoiceStatus.CANCELLED);
            contract.setStatus(ContractStatus.CANCELLED);
            releaseReservedBed(contract);
            tenants.findByIdWithUser(contract.getTenant().getId()).ifPresent(tenant -> {
                if (tenant.getStatus() == TenantStatus.PENDING) tenant.setStatus(TenantStatus.INACTIVE);
                if (tenant.getUser() != null) {
                    notifications.save(Notification.builder().user(tenant.getUser()).title("Yêu cầu thuê đã hết hạn")
                            .content("Hóa đơn cho phòng " + contract.getRoom().getRoomCode() + " chưa được thanh toán trong ngày quy định. Yêu cầu thuê đã tự hủy và giường đã được trả lại.")
                            .type(NotificationType.CONTRACT).isRead(false).build());
                }
            });
            contracts.save(contract);
            invoices.save(invoice);
            refreshRoomStatus(contract.getRoom());
        }
        return expired.size();
    }

    private void cancelOutstandingInvoices(Contract contract) {
        invoices.findByContractIdOrderByBillingMonthDesc(contract.getId()).stream()
                .filter(invoice -> invoice.getStatus() == InvoiceStatus.UNPAID)
                .forEach(invoice -> {
                    invoice.setStatus(InvoiceStatus.CANCELLED);
                    invoices.save(invoice);
                });
    }

    private void releaseReservedBed(Contract contract) {
        if (contract.getBed() == null) return;
        Bed bed = beds.findByIdForUpdate(contract.getBed().getId()).orElse(null);
        if (bed != null && bed.getStatus() == BedStatus.RESERVED) {
            bed.setStatus(BedStatus.AVAILABLE);
            beds.save(bed);
        }
    }

    private void refreshRoomStatus(Room room) {
        if (room.getStatus() == RoomStatus.MAINTENANCE) return;
        long occupied = beds.countByRoomIdAndStatus(room.getId(), BedStatus.OCCUPIED);
        long reserved = beds.countByRoomIdAndStatus(room.getId(), BedStatus.RESERVED);
        room.setStatus(occupied + reserved == 0 ? RoomStatus.AVAILABLE
                : (occupied + reserved >= room.getCapacity() ? RoomStatus.FULL : RoomStatus.OCCUPIED));
        rooms.save(room);
    }
}
