package com.example.dataService.controller;

import com.example.dataService.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackages = "com.example.dataService.controller")
public class GlobalExceptionHandler {

    /**
     * Maneja todas las excepciones de "recurso no encontrado".
     * Devuelve el mensaje de la excepción y un estado HTTP 404 Not Found.
     */
    @ExceptionHandler({
            ProductoNoEncontradoException.class,
            CategoriaNoEncontradaException.class,
            InventarioNoEncontradoException.class
    })
    public ResponseEntity<String> handleNotFoundException(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja excepciones de validación de negocio (ej. stock insuficiente).
     * Devuelve el mensaje de la excepción y un estado HTTP 400 Bad Request.
     */
    @ExceptionHandler(ValidacionNegocioException.class)
    public ResponseEntity<String> handleValidationException(ValidacionNegocioException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja excepciones por recursos duplicados (ej. crear categoría con nombre existente).
     * Devuelve el mensaje de la excepción y un estado HTTP 409 Conflict.
     */
    @ExceptionHandler(CategoriaDuplicadaException.class)
    public ResponseEntity<String> handleDuplicateResourceException(CategoriaDuplicadaException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    /**
     * Manejador genérico para cualquier otra excepción no controlada.
     * Devuelve un mensaje genérico y un estado HTTP 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        // Por seguridad, no se expone el mensaje de errores internos inesperados.
        return new ResponseEntity<>("Ocurrió un error inesperado en el servidor.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
