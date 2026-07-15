package com.diettracker.api;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class FieldValidationException extends ApiException {
    private final Map<String, String> fieldErrors;

    public FieldValidationException(String code, String message, Map<String, String> fieldErrors) {
        super(HttpStatus.BAD_REQUEST, code, message);
        this.fieldErrors = Map.copyOf(fieldErrors);
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
