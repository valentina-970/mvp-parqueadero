package com.uam.parqueadero.pago;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Convierte los rechazos de la caja en respuestas JSON con un mensaje para el operador. */
@RestControllerAdvice(basePackageClasses = CajaController.class)
public class ErroresCajaAdvice {

    @ExceptionHandler(CobroNoEncontradoException.class)
    ResponseEntity<Map<String, String>> noEncontrado(CobroNoEncontradoException e) {
        return respuesta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    private static ResponseEntity<Map<String, String>> respuesta(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("mensaje", mensaje));
    }
}
