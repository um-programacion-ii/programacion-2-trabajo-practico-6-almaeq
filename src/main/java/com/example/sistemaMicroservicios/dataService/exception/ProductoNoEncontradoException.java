package com.example.sistemaMicroservicios.dataService.exception;

public class ProductoNoEncontradoException extends RuntimeException {
    public ProductoNoEncontradoException(String message) {
        super(message);
    }
}
