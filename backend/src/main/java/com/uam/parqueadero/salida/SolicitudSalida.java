package com.uam.parqueadero.salida;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Lectura que llega al carril de salida desde el simulador o la carga de una imagen.
 *
 * @param placa     placa tal como la leyó el lector, sin normalizar
 * @param confianza nivel de confianza de la lectura, entre 0 y 1
 */
public record SolicitudSalida(
        @NotNull @Schema(example = "ABC123") String placa,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") @Schema(example = "0.95") BigDecimal confianza) {
}
