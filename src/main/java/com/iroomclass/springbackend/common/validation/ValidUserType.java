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
 * 사용자 타입 검증 어노테이션
 * 
 * <p>허용되는 값: STUDENT, TEACHER</p>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {})
@Pattern(regexp = ValidationConstants.USER_TYPE_PATTERN, 
         message = ValidationConstants.INVALID_USER_TYPE)
public @interface ValidUserType {
    
    String message() default ValidationConstants.INVALID_USER_TYPE;
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}