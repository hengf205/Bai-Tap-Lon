package com.example.qlkytucxa.service;

import com.example.qlkytucxa.dto.TenantRegistrationDto;
import com.example.qlkytucxa.entity.*;
import com.example.qlkytucxa.exception.DuplicateResourceException;
import com.example.qlkytucxa.exception.InvalidOperationException;
import com.example.qlkytucxa.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
public class TenantRentalService {
    private final UserRepository users;
    private final TenantRepository tenants;
    private final RoomRepository rooms;
    private final BedRepository beds;
    private final ContractRepository contracts;
    private final NotificationRepository notifications;
    private final PasswordEncoder passwordEncoder;
    private final CampusSettingsService campusSettings;

    public TenantRentalService(UserRepository users, TenantRepository tenants, RoomRepository rooms,
                               BedRepository beds, ContractRepository contracts,
                               NotificationRepository notifications, PasswordEncoder passwordEncoder,
                               CampusSettingsService campusSettings) {
        this.users = users;
        this.tenants = tenants;
        this.rooms = rooms;
        this.beds = beds;
        this.contracts = contracts;
        this.notifications = notifications;
        this.passwordEncoder = passwordEncoder;
        this.campusSettings = campusSettings;
    }

    @Transactional
    public void register(TenantRegistrationDto form) {
        String username = form.getUsername().trim();
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        String studentCode = form.getStudentCode().trim();
        if (users.existsByUsername(username)) throw new DuplicateResourceException("Tên đăng nhập đã được sử dụng.");
        if (users.existsByEmail(email)) throw new DuplicateResourceException("Email đã được sử dụng.");
        if (tenants.existsByStudentCode(studentCode)) throw new DuplicateResourceException("Mã sinh viên/người thuê đã tồn tại.");
        if (!form.getPassword().equals(form.getConfirmPassword())) throw new InvalidOperationException("Mật khẩu nhập lại không khớp.");

        Room room = rooms.findByIdWithFloorAndBuilding(form.getRoomId())
                .orElseThrow(() -> new InvalidOperationException("Phòng đã chọn không tồn tại."));
        if (!room.getFloor().getBuilding().getId().equals(form.getBuildingId())) {
            throw new InvalidOperationException("Phòng không thuộc tòa nhà đã chọn.");
        }
        if (room.getFloor().getBuilding().getStatus() != BuildingStatus.ACTIVE || room.getStatus() == RoomStatus.MAINTENANCE) {
            throw new InvalidOperationException("Tòa nhà hoặc phòng hiện không nhận đăng ký.");
        }

        Bed bed = beds.findByIdForUpdate(form.getBedId())
                .orElseThrow(() -> new InvalidOperationException("Giường đã chọn không tồn tại."));
        if (!bed.getRoom().getId().equals(room.getId()) || bed.getStatus() != BedStatus.AVAILABLE) {
            throw new InvalidOperationException("Giường vừa được người khác chọn hoặc không thuộc phòng này. Vui lòng tải lại danh sách.");
        }
        User user = users.save(User.builder().username(username).password(passwordEncoder.encode(form.getPassword()))
                .fullName(form.getFullName().trim()).email(email).phone(form.getPhone().trim())
                .role(Role.ROLE_TENANT).status(UserStatus.ACTIVE).build());
        Tenant tenant = tenants.save(Tenant.builder().user(user).studentCode(studentCode)
                .fullName(form.getFullName().trim()).dateOfBirth(form.getDateOfBirth()).gender(form.getGender())
                .identityCard(form.getIdentityCard()).phone(form.getPhone().trim()).email(email)
                .hometown(form.getHometown()).school(campusSettings.getSettings().getInstitutionName()).major(form.getMajor())
                .status(TenantStatus.PENDING).build());

        LocalDate startDate = LocalDate.now();
        Contract contract = Contract.builder().tenant(tenant).room(room).bed(bed)
                .contractCode("YC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .startDate(startDate).endDate(startDate.plusYears(1)).monthlyPrice(room.getPrice())
                .deposit(room.getPrice()).status(ContractStatus.PENDING)
                .note("Yêu cầu thuê do người dùng đăng ký; chờ quản trị viên duyệt.").build();
        contracts.save(contract);
        bed.setStatus(BedStatus.RESERVED);
        beds.save(bed);
        refreshRoomStatus(room);
        notifications.save(Notification.builder().user(user).title("Đã nhận yêu cầu thuê phòng")
                .content("Yêu cầu thuê phòng " + room.getRoomCode() + " đã được gửi và đang chờ quản trị viên duyệt.")
                .type(NotificationType.CONTRACT).isRead(false).build());
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
