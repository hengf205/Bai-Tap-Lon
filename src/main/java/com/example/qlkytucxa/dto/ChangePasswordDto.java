package com.example.qlkytucxa.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import com.example.qlkytucxa.dto.validation.PasswordConfirmationValidator;
import com.example.qlkytucxa.dto.validation.ValidPasswordConfirmation;

/**
 * DTO dùng cho form đổi mật khẩu.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidPasswordConfirmation
public class ChangePasswordDto {

    @NotBlank(message = "Mật khẩu hiện tại không được để trống")
    private String currentPassword;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, message = "Mật khẩu mới ít nhất 6 ký tự")
    private String newPassword;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;
}
