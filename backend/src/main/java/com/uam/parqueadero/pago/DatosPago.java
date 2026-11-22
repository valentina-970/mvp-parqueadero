package com.uam.parqueadero.pago;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Lo que el estado de un cobro necesita para registrar un pago: el efectivo recibido,
 * quién lo recibió y cuándo.
 */
public record DatosPago(
        BigDecimal valorRecibido,
        UUID operadorId,
        String operadorNombre,
        Instant ahora,
        ZoneId zona) {
}
