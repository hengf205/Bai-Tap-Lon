package com.example.qlkytucxa.dto.validation;

import com.example.qlkytucxa.dto.UtilityMeterDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class UtilityReadingsValidator implements ConstraintValidator<ValidUtilityReadings, UtilityMeterDto> {
    @Override
    public boolean isValid(UtilityMeterDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }
        boolean valid = true;
        context.disableDefaultConstraintViolation();
        valid &= validatePair(dto.getElectricityOld(), dto.getElectricityNew(), "electricityNew", context);
        valid &= validatePair(dto.getWaterOld(), dto.getWaterNew(), "waterNew", context);
        return valid;
    }

    private boolean validatePair(BigDecimal oldValue, BigDecimal newValue, String property,
                                 ConstraintValidatorContext context) {
        if (oldValue == null || newValue == null || newValue.compareTo(oldValue) >= 0) {
            return true;
        }
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(property)
                .addConstraintViolation();
        return false;
    }
}
