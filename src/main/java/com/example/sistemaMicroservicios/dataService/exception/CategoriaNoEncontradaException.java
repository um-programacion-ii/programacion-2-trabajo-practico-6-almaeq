package com.example.sistemaMicroservicios.dataService.exception;

public class CategoriaNoEncontradaException extends RuntimeException {
    public CategoriaNoEncontradaException(String message) {
        super(message);
    }
}
