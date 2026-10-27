package com.example.qlkytucxa.dto;

import com.example.qlkytucxa.entity.InvoiceStatus;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO dùng cho form tạo/sửa hóa đơn.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDto {

    private Long id;

    @NotNull(message = "Hợp đồng không được để trống")
    private Long contractId;
    private String tenantName;   // hiển thị
    private String roomCode;     // hiển thị

    @Size(max = 30, message = "Mã hóa đơn tối đa 30 ký tự")
    private String invoiceCode;  // tự sinh nếu để trống

    @NotNull(message = "Tháng thanh toán không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate billingMonth;

    @DecimalMin(value = "0.0", message = "Tiền phòng không được âm")
    private BigDecimal roomPrice;

    @DecimalMin(value = "0.0", message = "Tiền điện không được âm")
    private BigDecimal electricityAmount;

    @DecimalMin(value = "0.0", message = "Tiền nước không được âm")
    private BigDecimal waterAmount;

    @DecimalMin(value = "0.0", message = "Tiền dịch vụ không được âm")
    private BigDecimal serviceAmount;

    @DecimalMin(value = "0.0", message = "Khoản khác không được âm")
    private BigDecimal otherAmount;

    @DecimalMin(value = "0.0", message = "Giảm giá không được âm")
    private BigDecimal discount;

    // Tự động tính = phòng + điện + nước + dịch vụ + khác - giảm giá
    private BigDecimal totalAmount;

    @NotNull(message = "Hạn thanh toán không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    private InvoiceStatus status;

    // Utility meter liên quan (để tự động điền tiền điện nước)
    private Long utilityMeterId;
}
