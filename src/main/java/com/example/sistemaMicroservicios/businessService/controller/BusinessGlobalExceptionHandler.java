package com.example.sistemaMicroservicios.businessService.controller;

import com.example.sistemaMicroservicios.businessService.exception.MicroserviceCommunicationException;
import com.example.sistemaMicroservicios.businessService.exception.ValidacionNegocioException;
import com.example.sistemaMicroservicios.dataService.exception.CategoriaNoEncontradaException;
import com.example.sistemaMicroservicios.dataService.exception.InventarioNoEncontradoException;
import com.example.sistemaMicroservicios.dataService.exception.ProductoNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;



@ControllerAdvice(basePackages = "com.example.sistemaMicroservicios.businessService.controller")
public class BusinessGlobalExceptionHandler {

    /**
     * Maneja excepciones de "recurso no encontrado" que se originan en el business service.
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
     * Maneja excepciones de validación de lógica de negocio (ej. precio negativo, stock insuficiente).
     * Devuelve el mensaje de la excepción y un estado HTTP 400 Bad Request.
     */
    @ExceptionHandler(ValidacionNegocioException.class)
    public ResponseEntity<String> handleValidationException(ValidacionNegocioException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja errores de comunicación con otros microservicios (errores de Feign).
     * Devuelve un mensaje genérico y un estado HTTP 503 Service Unavailable,
     * que indica que el servicio no está disponible temporalmente.
     */
    @ExceptionHandler(MicroserviceCommunicationException.class)
    public ResponseEntity<String> handleMicroserviceCommunicationException(MicroserviceCommunicationException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Manejador genérico para cualquier otra excepción no controlada en el business service.
     * Devuelve un mensaje genérico y un estado HTTP 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        // Por seguridad, no se expone el mensaje de errores internos inesperados.
        // Se podría registrar el error completo en los logs.
        // log.error("Error no controlado en Business Service:", ex);
        return new ResponseEntity<>("Ocurrió un error inesperado en el servicio.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
