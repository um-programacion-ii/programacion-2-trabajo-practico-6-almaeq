package com.example.sistemaMicroservicios.businessService.exception;

public class CategoriaNoEncontradaException extends RuntimeException {
    public CategoriaNoEncontradaException(String message) {
        super(message);
    }
}
