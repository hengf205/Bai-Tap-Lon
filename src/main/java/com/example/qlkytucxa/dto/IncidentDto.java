package com.example.qlkytucxa.dto;

import com.example.qlkytucxa.entity.IncidentPriority;
import com.example.qlkytucxa.entity.IncidentStatus;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO dùng cho form báo sự cố / báo hỏng thiết bị.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentDto {

    private Long id;

    @NotNull(message = "Phòng không được để trống")
    private Long roomId;
    private String roomCode;   // hiển thị

    private Long tenantId;
    private String tenantName; // hiển thị

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 200, message = "Tiêu đề tối đa 200 ký tự")
    private String title;

    @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
    private String description;

    private String image;  // đường dẫn ảnh upload

    @NotNull(message = "Mức độ ưu tiên không được để trống")
    private IncidentPriority priority;

    private IncidentStatus status;

    private String resolvedNote; // ghi chú khi đóng sự cố
}
