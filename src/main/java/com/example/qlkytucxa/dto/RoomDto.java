package com.example.qlkytucxa.dto;

import com.example.qlkytucxa.entity.RoomStatus;
import com.example.qlkytucxa.entity.RoomType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO dùng cho form tạo/sửa phòng.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDto {

    private Long id;

    @NotNull(message = "Tầng không được để trống")
    private Long floorId;

    // Hiển thị thông tin tòa nhà (chỉ đọc, không validate)
    private String buildingName;
    private Integer floorNumber;

    @NotBlank(message = "Mã phòng không được để trống")
    @Size(max = 20, message = "Mã phòng tối đa 20 ký tự")
    private String roomCode;

    @NotNull(message = "Loại phòng không được để trống")
    private RoomType roomType;

    @NotNull(message = "Sức chứa không được để trống")
    @Min(value = 1, message = "Sức chứa phải lớn hơn 0")
    @Max(value = 20, message = "Sức chứa tối đa 20 người")
    private Integer capacity;

    @NotNull(message = "Giá phòng không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phòng phải lớn hơn 0")
    private BigDecimal price;

    private RoomStatus status;

    private String description;

    // Thống kê (chỉ hiển thị, không dùng trong form)
    private long totalBeds;
    private long availableBeds;
}
