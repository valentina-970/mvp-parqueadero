package com.uam.parqueadero.salida;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Intento de salida que queda en el registro de eventos (HU-04, CA-8).
 *
 * @param permitido     {@code true} si la barrera abrió; {@code false} si fue rechazado
 * @param placaLeida    placa tal como la envió el lector, antes de normalizar
 * @param diaOperativo  día operativo al que pertenece el intento
 */
public record EventoSalida(
        UUID sedeId,
        String placa,
        String placaLeida,
        UUID tipoVehiculoId,
        BigDecimal confianza,
        boolean permitido,
        String motivo,
        Instant ocurridoEn,
        LocalDate diaOperativo) {
}
