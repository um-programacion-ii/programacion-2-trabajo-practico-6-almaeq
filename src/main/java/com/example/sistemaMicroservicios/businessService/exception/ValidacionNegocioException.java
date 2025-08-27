package com.example.sistemaMicroservicios.businessService.exception;

public class ValidacionNegocioException extends RuntimeException {
    public ValidacionNegocioException(String message) {
        super(message);
    }
}
