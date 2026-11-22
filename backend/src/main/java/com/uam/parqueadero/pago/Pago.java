package com.uam.parqueadero.pago;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Pago en efectivo de un cobro (HU-03).
 *
 * @param valor            valor del cobro
 * @param valorRecibido    efectivo que entregó el cliente
 * @param cambio           lo que se devuelve al cliente
 * @param operadorNombre   usuario del operador que recibió el pago
 * @param paseVigenteHasta fin del día operativo, 05:59 (RN-03)
 */
public record Pago(
        UUID id,
        UUID cobroId,
        BigDecimal valor,
        BigDecimal valorRecibido,
        BigDecimal cambio,
        UUID operadorId,
        String operadorNombre,
        Instant pagadoEn,
        Instant paseVigenteHasta) {
}
