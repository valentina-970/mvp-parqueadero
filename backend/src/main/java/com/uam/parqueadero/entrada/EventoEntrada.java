package com.uam.parqueadero.entrada;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Lectura del carril de entrada que queda en el registro de eventos (CA-2).
 *
 * @param placaLeida    placa tal como la envió el lector, antes de normalizar
 * @param diaOperativo  día operativo al que pertenece la lectura
 */
public record EventoEntrada(
        UUID sedeId,
        String placa,
        String placaLeida,
        UUID tipoVehiculoId,
        BigDecimal confianza,
        EstadoIngreso resultado,
        String motivo,
        Instant ocurridoEn,
        LocalDate diaOperativo) {
}