package com.example.qlkytucxa.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = UtilityReadingsValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUtilityReadings {
    String message() default "Chỉ số mới phải lớn hơn hoặc bằng chỉ số cũ";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
