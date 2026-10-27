package com.example.qlkytucxa.service.impl;

import com.example.qlkytucxa.entity.*;
import com.example.qlkytucxa.exception.DuplicateResourceException;
import com.example.qlkytucxa.exception.InvalidOperationException;
import com.example.qlkytucxa.repository.InvoiceRepository;
import com.example.qlkytucxa.repository.BedRepository;
import com.example.qlkytucxa.repository.ContractRepository;
import com.example.qlkytucxa.repository.RoomRepository;
import com.example.qlkytucxa.repository.TenantRepository;
import com.example.qlkytucxa.repository.NotificationRepository;
import com.example.qlkytucxa.repository.PaymentRepository;
import com.example.qlkytucxa.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentServiceImpl extends AbstractCrudService<Payment> implements PaymentService {
    private final PaymentRepository payments;
    private final InvoiceRepository invoices;
    private final ContractRepository contracts;
    private final BedRepository beds;
    private final RoomRepository rooms;
    private final TenantRepository tenants;
    private final NotificationRepository notifications;
    public PaymentServiceImpl(PaymentRepository payments, InvoiceRepository invoices, ContractRepository contracts,
                              BedRepository beds, RoomRepository rooms, TenantRepository tenants,
                              NotificationRepository notifications) {
        super(payments, "Thanh toán"); this.payments = payments; this.invoices = invoices; this.contracts = contracts;
        this.beds = beds; this.rooms = rooms; this.tenants = tenants; this.notifications = notifications;
    }

    @Override @Transactional public Payment pay(Payment payment) {
        if (payment.getPaymentCode() == null || payment.getPaymentCode().isBlank()) {
            payment.setPaymentCode("TT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        payments.findByPaymentCode(payment.getPaymentCode()).ifPresent(p -> { throw new DuplicateResourceException("Mã thanh toán đã tồn tại"); });
        Invoice invoice = invoices.findByIdForUpdate(payment.getInvoice().getId()).orElseThrow(() -> new InvalidOperationException("Hóa đơn không tồn tại"));
        if (invoice.getStatus() == InvoiceStatus.PAID || invoice.getStatus() == InvoiceStatus.CANCELLED) throw new InvalidOperationException("Hóa đơn không còn được thanh toán");
        if (invoice.getContract().getStatus() == ContractStatus.PENDING && invoice.getDueDate() != null
                && invoice.getDueDate().isBefore(java.time.LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")))) {
            throw new InvalidOperationException("Hóa đơn đã quá hạn thanh toán.");
        }
        BigDecimal amount = payment.getAmount();
        if (amount == null || amount.signum() <= 0) throw new InvalidOperationException("Số tiền thanh toán phải lớn hơn 0");
        payment.setInvoice(invoice);
        if (payment.getPaymentDate() == null) payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus(PaymentStatus.SUCCESS);
        Payment saved = payments.save(payment);
        // Hệ thống hiện không hỗ trợ thanh toán từng phần: chỉ đánh dấu PAID khi đủ số tiền.
        BigDecimal totalPaid = payments.findByInvoiceIdOrderByPaymentDateDesc(invoice.getId()).stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalPaid.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoices.save(invoice);
            activatePendingRental(invoice);
        } else {
            payments.delete(saved);
            throw new InvalidOperationException("Số tiền phải thanh toán đủ tổng hóa đơn");
        }
        return saved;
    }

    private void activatePendingRental(Invoice invoice) {
        Contract contract = invoice.getContract();
        if (contract.getStatus() != ContractStatus.PENDING) return;
        if (contracts.existsByTenantIdAndStatusAndIdNot(contract.getTenant().getId(), ContractStatus.ACTIVE, contract.getId())) {
            throw new InvalidOperationException("Người thuê đã có hợp đồng đang hiệu lực.");
        }
        if (contract.getBed() == null) throw new InvalidOperationException("Hợp đồng chưa được gán giường.");
        Bed bed = beds.findByIdForUpdate(contract.getBed().getId())
                .orElseThrow(() -> new InvalidOperationException("Giường thuê không còn tồn tại."));
        if (!bed.getRoom().getId().equals(contract.getRoom().getId())) {
            throw new InvalidOperationException("Giường trong hợp đồng không thuộc phòng đã đăng ký.");
        }
        if (bed.getStatus() != BedStatus.RESERVED) throw new InvalidOperationException("Giường thuê không còn được giữ chỗ.");
        bed.setStatus(BedStatus.OCCUPIED);
        beds.save(bed);
        contract.setStatus(ContractStatus.ACTIVE);
        contracts.save(contract);

        Tenant tenant = tenants.findByIdWithUser(contract.getTenant().getId())
                .orElseThrow(() -> new InvalidOperationException("Hồ sơ người thuê không tồn tại."));
        tenant.setStatus(TenantStatus.ACTIVE);
        tenants.save(tenant);
        refreshRoomStatus(contract.getRoom());
        notifications.save(Notification.builder().user(tenant.getUser()).title("Hoàn tất thủ tục thuê phòng")
                .content("Hóa đơn đã được thanh toán. Hợp đồng " + contract.getContractCode() + " đang có hiệu lực.")
                .type(NotificationType.PAYMENT).isRead(false).build());
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
