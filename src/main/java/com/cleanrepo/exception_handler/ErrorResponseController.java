package com.cleanrepo.exception_handler;

import com.cleanrepo.account.exception.GenericAccountException;
import com.cleanrepo.auth.exception.GenericAuthenticationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
class ErrorResponseController {

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException exception){
        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();

        List<ErrorDto> errors = violations.stream()
                .map(violation -> new ErrorDto(getConstraintViolationField(violation), violation.getMessage()))
                .collect(Collectors.toList());

        var errorResponse = ErrorResponse.builder().errors(errors).build();
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            GenericAuthenticationException.class,
            GenericAccountException.class
    })
    ResponseEntity<?> handleBadRequestException(RuntimeException exception){
        List<ErrorDto> errors = List.of(ErrorDto.builder()
                .message(exception.getMessage())
                .build());
        var errorResponse = ErrorResponse.builder().errors(errors).build();
        return ResponseEntity.badRequest().body(errorResponse);
    }

    private String getConstraintViolationField(ConstraintViolation<?> violation) {
        String[] propertyPath = violation.getPropertyPath().toString().split("\\.");
        return propertyPath.length > 0 ? propertyPath[propertyPath.length - 1] : "unknown";
    }
}
