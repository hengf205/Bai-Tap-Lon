package com.example.qlkytucxa.dto;

import com.example.qlkytucxa.dto.validation.ValidUtilityReadings;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO dùng cho form nhập chỉ số điện nước.
 * Có validation: chỉ số mới >= chỉ số cũ.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidUtilityReadings
public class UtilityMeterDto {

    private Long id;

    @NotNull(message = "Phòng không được để trống")
    private Long roomId;
    private String roomCode;  // hiển thị

    @NotNull(message = "Chỉ số điện cũ không được để trống")
    @DecimalMin(value = "0.0", message = "Chỉ số điện cũ không được âm")
    private BigDecimal electricityOld;

    @NotNull(message = "Chỉ số điện mới không được để trống")
    @DecimalMin(value = "0.0", message = "Chỉ số điện mới không được âm")
    private BigDecimal electricityNew;

    @NotNull(message = "Chỉ số nước cũ không được để trống")
    @DecimalMin(value = "0.0", message = "Chỉ số nước cũ không được âm")
    private BigDecimal waterOld;

    @NotNull(message = "Chỉ số nước mới không được để trống")
    @DecimalMin(value = "0.0", message = "Chỉ số nước mới không được âm")
    private BigDecimal waterNew;

    @NotNull(message = "Giá điện không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá điện phải lớn hơn 0")
    private BigDecimal electricityPrice;

    @NotNull(message = "Giá nước không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá nước phải lớn hơn 0")
    private BigDecimal waterPrice;

    @NotNull(message = "Ngày ghi chỉ số không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate recordingDate;

    // Kết quả tính toán (readonly)
    private BigDecimal electricityUsage;
    private BigDecimal electricityAmount;
    private BigDecimal waterUsage;
    private BigDecimal waterAmount;
}
