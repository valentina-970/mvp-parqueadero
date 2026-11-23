package com.uam.parqueadero.entrada;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Lectura que envía el simulador (o, más adelante, una cámara) al carril de entrada (HU-01, CA-1).
 *
 * @param placa      placa tal como se leyó; puede traer espacios, guiones o minúsculas
 * @param confianza  nivel de confianza de la lectura, entre 0 y 1
 */
public record SolicitudIngreso(
        @NotNull @Size(max = 20) @Schema(example = "ABC123") String placa,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") @Schema(example = "0.95") BigDecimal confianza) {
}