package com.iroomclass.springbackend.common.validation;

import com.iroomclass.springbackend.common.ValidationConstants;
import jakarta.validation.Constraint;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 한국 휴대폰 번호 형식 검증 어노테이션
 * 
 * <p>형식: 010-1234-5678</p>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {})
@Pattern(regexp = ValidationConstants.PHONE_NUMBER_PATTERN, 
         message = ValidationConstants.INVALID_PHONE_FORMAT)
public @interface ValidPhoneNumber {
    
    String message() default ValidationConstants.INVALID_PHONE_FORMAT;
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}