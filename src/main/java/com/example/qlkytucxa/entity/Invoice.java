package com.example.qlkytucxa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Entity hóa đơn hàng tháng.
 * Bao gồm tiền phòng, điện, nước, dịch vụ và các khoản khác.
 */
@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ManyToOne: Nhiều hóa đơn thuộc một hợp đồng.
     */
    @NotNull(message = "Hợp đồng không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Contract contract;

    @NotBlank(message = "Mã hóa đơn không được để trống")
    @Column(name = "invoice_code", unique = true, nullable = false, length = 30)
    private String invoiceCode;

    @NotNull(message = "Tháng thanh toán không được để trống")
    @Column(name = "billing_month", nullable = false)
    private LocalDate billingMonth;

    @DecimalMin(value = "0.0", message = "Tiền phòng không được âm")
    @Column(name = "room_price", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal roomPrice = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Tiền điện không được âm")
    @Column(name = "electricity_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal electricityAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Tiền nước không được âm")
    @Column(name = "water_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal waterAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Tiền dịch vụ không được âm")
    @Column(name = "service_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal serviceAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Khoản khác không được âm")
    @Column(name = "other_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal otherAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Giảm giá không được âm")
    @Column(name = "discount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @NotNull(message = "Hạn thanh toán không được để trống")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.UNPAID;

    // ========================
    // Relationships
    // ========================

    /**
     * Một hóa đơn có nhiều lần thanh toán (payment).
     */
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Payment> payments;

    /**
     * Tự động tính tổng tiền trước khi lưu.
     * Tổng = Phòng + Điện + Nước + Dịch vụ + Khác - Giảm giá
     */
    @PrePersist
    @PreUpdate
    public void calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        if (roomPrice != null) total = total.add(roomPrice);
        if (electricityAmount != null) total = total.add(electricityAmount);
        if (waterAmount != null) total = total.add(waterAmount);
        if (serviceAmount != null) total = total.add(serviceAmount);
        if (otherAmount != null) total = total.add(otherAmount);
        if (discount != null) total = total.subtract(discount);
        this.totalAmount = total.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total;
    }

    /**
     * Kiểm tra hóa đơn quá hạn chưa thanh toán.
     */
    @Transient
    public boolean isOverdue() {
        return status == InvoiceStatus.UNPAID
                && dueDate != null
                && dueDate.isBefore(LocalDate.now());
    }
}
