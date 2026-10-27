package com.example.qlkytucxa.dto;

import com.example.qlkytucxa.entity.ContractStatus;
import com.example.qlkytucxa.dto.validation.ValidContractDates;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO dùng cho form tạo/sửa hợp đồng.
 * Bao gồm validation nghiệp vụ: ngày kết thúc phải sau ngày bắt đầu.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidContractDates
public class ContractDto {

    private Long id;

    @NotNull(message = "Người thuê không được để trống")
    private Long tenantId;
    private String tenantName;  // hiển thị

    @NotNull(message = "Phòng không được để trống")
    private Long roomId;
    private String roomCode;    // hiển thị

    private Long bedId;
    private String bedCode;     // hiển thị

    @Size(max = 30, message = "Mã hợp đồng tối đa 30 ký tự")
    private String contractCode;  // tự động sinh nếu để trống

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @NotNull(message = "Tiền phòng hàng tháng không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Tiền phòng phải lớn hơn 0")
    private BigDecimal monthlyPrice;

    @DecimalMin(value = "0.0", message = "Tiền cọc không được âm")
    private BigDecimal deposit;

    private ContractStatus status;

    private String note;
}
