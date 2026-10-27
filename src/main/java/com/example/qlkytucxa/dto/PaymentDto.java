package com.example.qlkytucxa.dto;

import com.example.qlkytucxa.entity.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO dùng cho form thanh toán hóa đơn.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDto {

    private Long id;

    @NotNull(message = "Hóa đơn không được để trống")
    private Long invoiceId;
    private String invoiceCode;   // hiển thị
    private String tenantName;    // hiển thị
    private BigDecimal invoiceTotal; // tổng tiền hóa đơn (chỉ đọc)

    private String paymentCode;   // tự sinh

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime paymentDate;

    private String note;
}
