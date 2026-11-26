package com.uam.parqueadero.salida;

import java.math.BigDecimal;
import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Respuesta del carril de salida: si la barrera abre y por qué.
 *
 * @param abre           {@code true} si la barrera abre
 * @param motivo         explicación para el operador, por ejemplo "Cobro pendiente por $5.000"
 * @param placa          placa normalizada
 * @param tipoVehiculo   nombre del tipo de vehículo, o {@code null} si la placa no es válida
 * @param valorPendiente valor del cobro pendiente, solo cuando la salida se niega por deuda
 * @param barrera        posición de la barrera después del intento
 * @param confianza      confianza de la lectura recibida
 * @param ocurridoEn     momento del intento de salida
* @param pendiente      {@code true} si la confianza fue baja y la salida espera la validación del operador (HU-06)
 * @param manual         {@code true} si la placa la validó el operador (HU-06)
 */
public record ResultadoSalida(
        boolean abre,
        String motivo,
        String placa,
        @Schema(nullable = true) String tipoVehiculo,
        @Schema(nullable = true) BigDecimal valorPendiente,
        EstadoBarrera barrera,
        BigDecimal confianza,
        Instant ocurridoEn,
        boolean pendiente,
        boolean manual) {
}
