package com.example.sistemaMicroservicios.dataService.exception;

public class InventarioNoEncontradoException extends RuntimeException {
    public InventarioNoEncontradoException(String message) {
        super(message);
    }
}
