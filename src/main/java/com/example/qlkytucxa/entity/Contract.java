package com.example.qlkytucxa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Entity hợp đồng thuê phòng.
 * Liên kết giữa Tenant, Room và Bed.
 */
@Entity
@Table(name = "contracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ManyToOne: Nhiều hợp đồng thuộc một người thuê.
     */
    @NotNull(message = "Người thuê không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Tenant tenant;

    /**
     * ManyToOne: Nhiều hợp đồng thuộc một phòng (theo thời điểm khác nhau).
     */
    @NotNull(message = "Phòng không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Room room;

    /**
     * ManyToOne: Nhiều hợp đồng thuộc một giường (theo thời điểm khác nhau).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bed_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Bed bed;

    @NotBlank(message = "Mã hợp đồng không được để trống")
    @Column(name = "contract_code", unique = true, nullable = false, length = 30)
    private String contractCode;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull(message = "Tiền phòng hàng tháng không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Tiền phòng phải lớn hơn 0")
    @Column(name = "monthly_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyPrice;

    @DecimalMin(value = "0.0", message = "Tiền cọc không được âm")
    @Column(name = "deposit", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal deposit = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ContractStatus status = ContractStatus.PENDING;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // ========================
    // Relationships
    // ========================

    /**
     * Một hợp đồng có nhiều hóa đơn.
     */
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Invoice> invoices;

    /**
     * Kiểm tra hợp đồng còn hiệu lực không.
     */
    @Transient
    public boolean isActive() {
        return status == ContractStatus.ACTIVE;
    }

    /**
     * Kiểm tra hợp đồng đã quá hạn chưa.
     */
    @Transient
    public boolean isExpiredByDate() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }
}
