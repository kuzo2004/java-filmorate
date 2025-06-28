package ru.yandex.practicum.filmorate.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    // Обработка дублированных данных ValidationExceptionDuplicate (email, login - д.б. уникальны)
    @ExceptionHandler(ValidationExceptionDuplicate.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptionDuplicate(ValidationExceptionDuplicate e) {
        log.warn("Ошибка валидации: {}", e.getMessage());
        return ResponseEntity
                .status(400)    // 400
                .body(Map.of("error", e.getMessage()));
    }

    // Обработка ошибок валидации (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Ошибки валидации полей: {}", errors);
        return ResponseEntity
                .badRequest()    // 400
                .body(errors);
    }

    // Обработка ошибок от кастомных валидаторов (ConstraintViolationException)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException e) {
        Map<String, String> errors = new HashMap<>();
        e.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Ошибки валидации: {}", errors);
        return ResponseEntity
                .badRequest()    // 400
                .body(errors);
    }
}