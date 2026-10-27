package com.example.qlkytucxa.dto.validation;

import com.example.qlkytucxa.dto.ContractDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ContractDatesValidator implements ConstraintValidator<ValidContractDates, ContractDto> {
    @Override
    public boolean isValid(ContractDto dto, ConstraintValidatorContext context) {
        if (dto == null || dto.getStartDate() == null || dto.getEndDate() == null) {
            return true; // @NotNull reports missing values on the individual fields.
        }
        boolean valid = dto.getEndDate().isAfter(dto.getStartDate());
        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("endDate")
                    .addConstraintViolation();
        }
        return valid;
    }
}
