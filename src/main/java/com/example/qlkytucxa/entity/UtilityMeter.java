package com.example.qlkytucxa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity bản ghi chỉ số điện nước theo phòng.
 * Mỗi lần nhập chỉ số tạo một bản ghi mới.
 */
@Entity
@Table(name = "utility_meters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilityMeter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ManyToOne: Một phòng có nhiều bản ghi chỉ số.
     */
    @NotNull(message = "Phòng không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Room room;

    // --- Chỉ số điện ---
    @NotNull(message = "Chỉ số điện cũ không được để trống")
    @DecimalMin(value = "0.0", message = "Chỉ số điện cũ không được âm")
    @Column(name = "electricity_old", nullable = false, precision = 10, scale = 2)
    private BigDecimal electricityOld;

    @NotNull(message = "Chỉ số điện mới không được để trống")
    @DecimalMin(value = "0.0", message = "Chỉ số điện mới không được âm")
    @Column(name = "electricity_new", nullable = false, precision = 10, scale = 2)
    private BigDecimal electricityNew;

    // --- Chỉ số nước ---
    @NotNull(message = "Chỉ số nước cũ không được để trống")
    @DecimalMin(value = "0.0", message = "Chỉ số nước cũ không được âm")
    @Column(name = "water_old", nullable = false, precision = 10, scale = 2)
    private BigDecimal waterOld;

    @NotNull(message = "Chỉ số nước mới không được để trống")
    @DecimalMin(value = "0.0", message = "Chỉ số nước mới không được âm")
    @Column(name = "water_new", nullable = false, precision = 10, scale = 2)
    private BigDecimal waterNew;

    // --- Giá đơn vị ---
    @NotNull(message = "Giá điện không được để trống")
    @Column(name = "electricity_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal electricityPrice;

    @NotNull(message = "Giá nước không được để trống")
    @Column(name = "water_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal waterPrice;

    @NotNull(message = "Ngày ghi chỉ số không được để trống")
    @Column(name = "recording_date", nullable = false)
    private LocalDate recordingDate;

    // ========================
    // Computed fields (Transient)
    // ========================

    /**
     * Tính số điện tiêu thụ.
     */
    @Transient
    public BigDecimal getElectricityUsage() {
        return electricityNew.subtract(electricityOld);
    }

    /**
     * Tính tiền điện = (mới - cũ) × giá điện.
     */
    @Transient
    public BigDecimal getElectricityAmount() {
        return getElectricityUsage().multiply(electricityPrice);
    }

    /**
     * Tính số nước tiêu thụ.
     */
    @Transient
    public BigDecimal getWaterUsage() {
        return waterNew.subtract(waterOld);
    }

    /**
     * Tính tiền nước = (mới - cũ) × giá nước.
     */
    @Transient
    public BigDecimal getWaterAmount() {
        return getWaterUsage().multiply(waterPrice);
    }
}
