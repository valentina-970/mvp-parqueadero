package com.uam.parqueadero.validacion;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Convierte los rechazos de la validación manual en respuestas JSON con un mensaje para el operador. */
@RestControllerAdvice(basePackageClasses = ValidacionController.class)
public class ErroresValidacionAdvice {

    @ExceptionHandler(LecturaNoPendienteException.class)
    ResponseEntity<Map<String, String>> noPendiente(LecturaNoPendienteException e) {
        return respuesta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(PlacaNoValidaException.class)
    ResponseEntity<Map<String, String>> placaNoValida(PlacaNoValidaException e) {
        return respuesta(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
    }

    private static ResponseEntity<Map<String, String>> respuesta(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("mensaje", mensaje));
    }
}
