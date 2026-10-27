package com.example.qlkytucxa.dto;

import com.example.qlkytucxa.entity.ServiceStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServiceDto {
    private Long id;
    @NotBlank(message = "Tên dịch vụ không được để trống") @Size(max = 100) private String serviceName;
    @Size(max = 50) private String unit;
    @NotNull(message = "Giá dịch vụ không được để trống") @DecimalMin(value = "0.0", message = "Giá dịch vụ không được âm") private BigDecimal price;
    @NotNull(message = "Trạng thái không được để trống") private ServiceStatus status;
    private String description;
}
