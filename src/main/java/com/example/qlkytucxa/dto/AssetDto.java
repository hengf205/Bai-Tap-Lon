package com.example.qlkytucxa.dto;

import com.example.qlkytucxa.entity.AssetCondition;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class AssetDto {
    private Long id;
    @NotNull(message = "Phòng không được để trống") private Long roomId;
    @NotBlank(message = "Tên tài sản không được để trống") @Size(max = 100) private String assetName;
    @NotNull(message = "Số lượng không được để trống") @Min(value = 1, message = "Số lượng phải lớn hơn 0") private Integer quantity;
    @NotNull(message = "Tình trạng không được để trống") private AssetCondition conditionStatus;
    @DateTimeFormat(pattern = "yyyy-MM-dd") private LocalDate purchaseDate;
    private String note;
}
