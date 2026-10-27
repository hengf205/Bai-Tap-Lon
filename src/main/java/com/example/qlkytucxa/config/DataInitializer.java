package com.example.qlkytucxa.config;

import com.example.qlkytucxa.entity.*;
import com.example.qlkytucxa.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Creates a small, relational demo dataset once, after Hibernate has initialized the schema. */
@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository users;
    private final BuildingRepository buildings;
    private final FloorRepository floors;
    private final RoomRepository rooms;
    private final BedRepository beds;
    private final TenantRepository tenants;
    private final ContractRepository contracts;
    private final UtilityMeterRepository meters;
    private final InvoiceRepository invoices;
    private final PaymentRepository payments;
    private final ServiceRepository services;
    private final RoomServiceRepository roomServices;
    private final AssetRepository assets;
    private final IncidentRepository incidents;
    private final NotificationRepository notifications;
    private final PasswordEncoder passwordEncoder;
    private final BigDecimal electricityPrice;
    private final BigDecimal waterPrice;

    public DataInitializer(UserRepository users, BuildingRepository buildings, FloorRepository floors,
                           RoomRepository rooms, BedRepository beds, TenantRepository tenants,
                           ContractRepository contracts, UtilityMeterRepository meters,
                           InvoiceRepository invoices, PaymentRepository payments, ServiceRepository services,
                           RoomServiceRepository roomServices, AssetRepository assets,
                           IncidentRepository incidents, NotificationRepository notifications,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.electricity-price:3500}") BigDecimal electricityPrice,
                           @Value("${app.water-price:15000}") BigDecimal waterPrice) {
        this.users = users; this.buildings = buildings; this.floors = floors; this.rooms = rooms; this.beds = beds;
        this.tenants = tenants; this.contracts = contracts; this.meters = meters; this.invoices = invoices;
        this.payments = payments; this.services = services; this.roomServices = roomServices; this.assets = assets;
        this.incidents = incidents; this.notifications = notifications; this.passwordEncoder = passwordEncoder;
        this.electricityPrice = electricityPrice; this.waterPrice = waterPrice;
    }

    @Override
    @Transactional
    public void run(String... args) {
        User admin = ensureUser("admin", "admin123", "Quản trị viên", "admin@qlkytucxa.local", Role.ROLE_ADMIN);
        ensureUser("manager", "manager123", "Quản lý ký túc xá UTC", "manager@qlkytucxa.local", Role.ROLE_MANAGER);
        User tenantUser = ensureUser("tenant", "tenant123", "Nguyễn Minh Anh", "tenant@qlkytucxa.local", Role.ROLE_TENANT);

        if (buildings.count() > 0) return;

        Building buildingA = createBuilding("A", "Khu ký túc xá UTC - Tòa A", "Khu ký túc xá Trường Đại học Giao thông Vận tải");
        Building buildingB = createBuilding("B", "Khu ký túc xá UTC - Tòa B", "Khu ký túc xá Trường Đại học Giao thông Vận tải");
        List<Room> allRooms = new ArrayList<>();
        createRooms(buildingA, allRooms);
        createRooms(buildingB, allRooms);

        Tenant tenantA = createTenant(tenantUser, "SV2026001", "Nguyễn Minh Anh", "0901000001", "tenant@qlkytucxa.local");
        Tenant tenantB = createTenant(null, "SV2026002", "Trần Quốc Bảo", "0901000002", "bao@example.local");
        Tenant tenantC = createTenant(null, "SV2026003", "Lê Thu Hà", "0901000003", "ha@example.local");
        Tenant tenantD = createTenant(null, "SV2026004", "Phạm Gia Huy", "0901000004", "huy@example.local");
        List<Tenant> sampleTenants = List.of(tenantA, tenantB, tenantC, tenantD);

        List<Contract> activeContracts = new ArrayList<>();
        for (int index = 0; index < sampleTenants.size(); index++) {
            Room room = allRooms.get(index);
            Bed bed = beds.findByRoomId(room.getId()).get(0);
            bed.setStatus(BedStatus.OCCUPIED);
            beds.save(bed);
            room.setStatus(RoomStatus.OCCUPIED);
            rooms.save(room);
            Contract contract = Contract.builder().tenant(sampleTenants.get(index)).room(room).bed(bed)
                    .contractCode(String.format("HD-SEED-%03d", index + 1)).startDate(LocalDate.now().minusMonths(2))
                    .endDate(LocalDate.now().plusMonths(10)).monthlyPrice(room.getPrice())
                    .deposit(room.getPrice()).status(ContractStatus.ACTIVE).note("Hợp đồng dữ liệu mẫu").build();
            activeContracts.add(contracts.save(contract));
        }

        createInvoice(activeContracts.get(0), "HDN-SEED-PAID", LocalDate.now().minusMonths(1).withDayOfMonth(1),
                new BigDecimal("120000"), new BigDecimal("80000"), InvoiceStatus.PAID, true);
        createInvoice(activeContracts.get(0), "HDN-SEED-UNPAID", LocalDate.now().withDayOfMonth(1),
                new BigDecimal("135000"), new BigDecimal("90000"), InvoiceStatus.UNPAID, false);
        createInvoice(activeContracts.get(1), "HDN-SEED-002", LocalDate.now().withDayOfMonth(1),
                new BigDecimal("98000"), new BigDecimal("65000"), InvoiceStatus.UNPAID, false);

        UtilityMeter meter = UtilityMeter.builder().room(allRooms.get(0))
                .electricityOld(new BigDecimal("1200")).electricityNew(new BigDecimal("1234"))
                .waterOld(new BigDecimal("410")).waterNew(new BigDecimal("416"))
                .electricityPrice(electricityPrice).waterPrice(waterPrice).recordingDate(LocalDate.now().withDayOfMonth(1)).build();
        meters.save(meter);

        Service internet = services.save(Service.builder().serviceName("Internet").unit("phòng/tháng")
                .price(new BigDecimal("100000")).status(ServiceStatus.ACTIVE).description("Dịch vụ Internet tốc độ cao").build());
        Service cleaning = services.save(Service.builder().serviceName("Vệ sinh khu vực chung").unit("phòng/tháng")
                .price(new BigDecimal("50000")).status(ServiceStatus.ACTIVE).description("Vệ sinh khu vực chung định kỳ").build());
        roomServices.save(RoomService.builder().room(allRooms.get(0)).service(internet).quantity(1).build());
        roomServices.save(RoomService.builder().room(allRooms.get(0)).service(cleaning).quantity(1).build());

        assets.save(Asset.builder().room(allRooms.get(0)).assetName("Bàn học").quantity(4)
                .conditionStatus(AssetCondition.GOOD).purchaseDate(LocalDate.now().minusYears(1)).note("Dữ liệu minh họa").build());
        assets.save(Asset.builder().room(allRooms.get(0)).assetName("Quạt trần").quantity(2)
                .conditionStatus(AssetCondition.FAIR).purchaseDate(LocalDate.now().minusYears(2)).build());

        Incident incident = incidents.save(Incident.builder().room(allRooms.get(0)).tenant(tenantA)
                .title("Vòi nước bị rò rỉ").description("Nước rỉ nhẹ tại khu vực bồn rửa.")
                .priority(IncidentPriority.MEDIUM).status(IncidentStatus.PROCESSING).build());
        incidents.save(Incident.builder().room(allRooms.get(1)).tenant(tenantB)
                .title("Đèn phòng không sáng").description("Đèn trần cần được kiểm tra.")
                .priority(IncidentPriority.LOW).status(IncidentStatus.PENDING).build());
        incidents.save(Incident.builder().room(allRooms.get(2)).tenant(tenantC)
                .title("Điều hòa hoạt động yếu").description("Đã vệ sinh bộ lọc, chờ kiểm tra tiếp.")
                .priority(IncidentPriority.HIGH).status(IncidentStatus.RESOLVED).resolvedAt(LocalDateTime.now().minusDays(1)).build());

        notifications.save(Notification.builder().user(tenantUser).title("Chào mừng đến với hệ thống")
                .content("Tài khoản mẫu đã sẵn sàng. Bạn có thể xem phòng, hợp đồng và hóa đơn tại bảng điều khiển.")
                .type(NotificationType.GENERAL).isRead(false).build());
    }

    private User ensureUser(String username, String rawPassword, String fullName, String email, Role role) {
        return users.findByUsername(username).orElseGet(() -> users.save(User.builder().username(username)
                .password(passwordEncoder.encode(rawPassword)).fullName(fullName).email(email).role(role)
                .status(UserStatus.ACTIVE).build()));
    }

    private Building createBuilding(String code, String name, String address) {
        Building building = buildings.save(Building.builder().buildingCode(code).buildingName(name).address(address)
                .numberOfFloors(3).description("Tòa nhà dữ liệu mẫu").status(BuildingStatus.ACTIVE).build());
        for (int floorNumber = 1; floorNumber <= 3; floorNumber++) {
            floors.save(Floor.builder().building(building).floorNumber(floorNumber)
                    .description("Tầng " + floorNumber).build());
        }
        return building;
    }

    private void createRooms(Building building, List<Room> allRooms) {
        for (Floor floor : floors.findByBuildingIdOrderByFloorNumberAsc(building.getId())) {
            for (int roomNumber = 1; roomNumber <= 5; roomNumber++) {
                String roomCode = building.getBuildingCode() + "-" + floor.getFloorNumber() + String.format("%02d", roomNumber);
                Room room = rooms.save(Room.builder().floor(floor).roomCode(roomCode).roomType(RoomType.QUAD)
                        .capacity(4).price(new BigDecimal("1800000")).status(RoomStatus.AVAILABLE)
                        .description("Phòng 4 người, dữ liệu mẫu").build());
                allRooms.add(room);
                for (int bedNumber = 1; bedNumber <= 4; bedNumber++) {
                    beds.save(Bed.builder().room(room).bedCode(roomCode + "-G" + bedNumber).status(BedStatus.AVAILABLE).build());
                }
            }
        }
    }

    private Tenant createTenant(User user, String studentCode, String fullName, String phone, String email) {
        Tenant tenant = Tenant.builder().user(user).studentCode(studentCode).fullName(fullName)
                .dateOfBirth(LocalDate.of(2005, 5, 15)).gender(Gender.OTHER)
                .identityCard("079" + studentCode.substring(studentCode.length() - 7))
                .phone(phone).email(email).hometown("Thành phố Hồ Chí Minh")
                .school("Đại học Giao thông Vận tải").major("Công nghệ thông tin").status(TenantStatus.ACTIVE).build();
        Tenant saved = tenants.save(tenant);
        if (user != null) {
            user.setTenant(saved);
            users.save(user);
        }
        return saved;
    }

    private void createInvoice(Contract contract, String code, LocalDate billingMonth,
                               BigDecimal electricity, BigDecimal water, InvoiceStatus status, boolean createPayment) {
        Invoice invoice = Invoice.builder().contract(contract).invoiceCode(code).billingMonth(billingMonth)
                .roomPrice(contract.getMonthlyPrice()).electricityAmount(electricity).waterAmount(water)
                .serviceAmount(new BigDecimal("150000")).otherAmount(BigDecimal.ZERO).discount(BigDecimal.ZERO)
                .dueDate(billingMonth.plusDays(10)).status(status).build();
        invoice.calculateTotal();
        Invoice saved = invoices.save(invoice);
        if (createPayment) {
            payments.save(Payment.builder().invoice(saved).paymentCode("TT-SEED-PAID").amount(saved.getTotalAmount())
                    .paymentMethod(PaymentMethod.BANK_TRANSFER).paymentDate(LocalDateTime.now().minusDays(5))
                    .status(PaymentStatus.SUCCESS).note("Thanh toán dữ liệu mẫu").build());
        }
    }
}
