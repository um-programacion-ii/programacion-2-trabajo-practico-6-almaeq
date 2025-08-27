package com.example.sistemaMicroservicios.dataService.exception;

public class CategoriaDuplicadaException extends RuntimeException {
    public CategoriaDuplicadaException(String message) {
        super(message);
    }
}
