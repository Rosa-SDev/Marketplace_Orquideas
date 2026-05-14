package com.orquicombeima.proyecto_orquideas.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Errores de negocio esperados (recurso no encontrado, stock insuficiente, etc.)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        log.error("RuntimeException capturada: {}", ex.getMessage(), ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // Errores de parámetros en URL inválidos
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(MethodArgumentTypeMismatchException ex) {
        log.error("Parámetro inválido '{}': {}", ex.getName(), ex.getValue(), ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", "El parámetro '" + ex.getName() + "' tiene un valor inválido: " + ex.getValue());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Cualquier error inesperado no capturado antes
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        log.error("Excepción no manejada (500): {}", ex.getMessage(), ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", "Ocurrió un error interno en el servidor");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}