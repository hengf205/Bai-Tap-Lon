package com.example.qlkytucxa.dto.validation;

import com.example.qlkytucxa.dto.ChangePasswordDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordConfirmationValidator implements ConstraintValidator<ValidPasswordConfirmation, ChangePasswordDto> {
    @Override
    public boolean isValid(ChangePasswordDto dto, ConstraintValidatorContext context) {
        if (dto == null || dto.getNewPassword() == null || dto.getConfirmPassword() == null) {
            return true;
        }
        boolean valid = dto.getNewPassword().equals(dto.getConfirmPassword());
        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
        }
        return valid;
    }
}
