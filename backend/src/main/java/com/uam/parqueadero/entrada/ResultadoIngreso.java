package com.uam.parqueadero.entrada;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Respuesta del carril de entrada; es lo que muestra la pantalla del operador (CA-8).
 *
 * @param tipoVehiculo  nombre del tipo ("Carro" o "Moto"), o {@code null} si la placa no es válida
 * @param lleno         {@code true} si se rechazó porque no hay cupo para ese tipo (la pantalla muestra LLENO)
 */
public record ResultadoIngreso(
        EstadoIngreso resultado,
        String motivo,
        String placa,
        String tipoVehiculo,
        BigDecimal confianza,
        Instant ocurridoEn,
        boolean lleno) {
}