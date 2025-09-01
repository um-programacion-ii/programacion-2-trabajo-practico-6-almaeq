package com.example.sistemaMicroservicios.dataService.exception;

public class ValidacionNegocioException extends RuntimeException {
    public ValidacionNegocioException(String message) {
        super(message);
    }
}
