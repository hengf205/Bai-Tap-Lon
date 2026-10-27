package com.example.qlkytucxa.dto;

import com.example.qlkytucxa.entity.Role;
import com.example.qlkytucxa.entity.UserStatus;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO dùng cho form tạo/sửa người dùng.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private Long id;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập từ 3-50 ký tự")
    private String username;

    // Chỉ bắt buộc khi tạo mới (null khi update mà không đổi mật khẩu)
    @Size(min = 6, message = "Mật khẩu ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @Email(message = "Email không đúng định dạng")
    private String email;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotNull(message = "Vai trò không được để trống")
    private Role role;

    private UserStatus status;
}
