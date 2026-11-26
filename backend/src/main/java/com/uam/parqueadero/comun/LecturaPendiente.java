package com.uam.parqueadero.comun;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Lectura de un carril que quedó esperando la validación del operador (HU-06).
 *
 * @param eventoId      evento de acceso que la registró como {@code PENDIENTE_VALIDACION}
 * @param carril        {@code ENTRADA} o {@code SALIDA}
 * @param placaLeida    placa tal como la envió el lector
 * @param placa         placa normalizada que se leyó
 * @param tipoVehiculo  nombre del tipo de vehículo de esa placa ("Carro" o "Moto")
 * @param confianza     confianza de la lectura, entre 0 y 1
 */
public record LecturaPendiente(
        UUID eventoId,
        String carril,
        String placaLeida,
        String placa,
        String tipoVehiculo,
        BigDecimal confianza,
        Instant ocurridoEn) {
}
