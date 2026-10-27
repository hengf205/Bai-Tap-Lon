package com.example.qlkytucxa.dto;

import com.example.qlkytucxa.entity.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class TenantRegistrationDto {
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3 đến 50 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Vui lòng nhập lại mật khẩu")
    private String confirmPassword;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có 10 hoặc 11 chữ số")
    private String phone;

    @NotBlank(message = "Mã sinh viên hoặc mã người thuê không được để trống")
    @Size(max = 20, message = "Mã không được vượt quá 20 ký tự")
    private String studentCode;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;
    private Gender gender;
    private String identityCard;
    private String hometown;
    private String school;
    private String major;

    @NotNull(message = "Vui lòng chọn tòa nhà")
    private Long buildingId;
    @NotNull(message = "Vui lòng chọn phòng")
    private Long roomId;
    @NotNull(message = "Vui lòng chọn giường")
    private Long bedId;
}
