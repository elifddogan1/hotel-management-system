package com.example.hotel_management.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(org.springframework.web.method.annotation.HandlerMethodValidationException.class)
public org.springframework.http.ResponseEntity<String> handleMethodValidationException(
        org.springframework.web.method.annotation.HandlerMethodValidationException ex) {
    return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
            .body("Validation failed: " + ex.getMessage());
}

@org.springframework.web.bind.annotation.ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
public org.springframework.http.ResponseEntity<String> handleArgumentNotValidException(
        org.springframework.web.bind.MethodArgumentNotValidException ex) {
    return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
            .body("Validation failed: " + ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
}
}