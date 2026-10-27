package com.example.qlkytucxa.dto;

import com.example.qlkytucxa.entity.Gender;
import com.example.qlkytucxa.entity.TenantStatus;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO dùng cho form tạo/sửa thông tin người thuê.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantDto {

    private Long id;

    // User account liên kết (có thể null nếu chưa có account)
    private Long userId;
    private String username;

    @Size(max = 20, message = "Mã sinh viên tối đa 20 ký tự")
    private String studentCode;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String fullName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private Gender gender;

    @Size(max = 20, message = "Số CMND/CCCD tối đa 20 ký tự")
    private String identityCard;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ (10-11 chữ số)")
    private String phone;

    @Email(message = "Email không đúng định dạng")
    private String email;

    @Size(max = 200, message = "Quê quán tối đa 200 ký tự")
    private String hometown;

    @Size(max = 150, message = "Trường học tối đa 150 ký tự")
    private String school;

    @Size(max = 100, message = "Ngành học tối đa 100 ký tự")
    private String major;

    private String avatar;

    private TenantStatus status;

    // Thông tin hiển thị thêm
    private String currentRoomCode;
    private String buildingName;
}
