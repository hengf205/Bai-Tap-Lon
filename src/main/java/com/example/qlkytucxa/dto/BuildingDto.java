package com.example.qlkytucxa.dto;

import com.example.qlkytucxa.entity.BuildingStatus;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO dùng cho form tạo/sửa tòa nhà.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingDto {

    private Long id;

    @NotBlank(message = "Mã tòa nhà không được để trống")
    @Size(max = 20, message = "Mã tòa nhà tối đa 20 ký tự")
    private String buildingCode;

    @NotBlank(message = "Tên tòa nhà không được để trống")
    @Size(max = 100, message = "Tên tòa nhà tối đa 100 ký tự")
    private String buildingName;

    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String address;

    @NotNull(message = "Số tầng không được để trống")
    @Min(value = 1, message = "Số tầng phải lớn hơn 0")
    @Max(value = 100, message = "Số tầng tối đa 100")
    private Integer numberOfFloors;

    private String description;

    private BuildingStatus status;
}
