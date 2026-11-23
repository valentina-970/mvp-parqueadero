package com.uam.parqueadero.pago;

/**
 * Resultado de intentar pagar un cobro.
 *
 * @param nuevo {@code true} si el pago se acaba de crear; {@code false} si ya existía
 */
public record ResultadoPago(Pago pago, boolean nuevo) {

    static ResultadoPago creado(Pago pago) {
        return new ResultadoPago(pago, true);
    }

    static ResultadoPago existente(Pago pago) {
        return new ResultadoPago(pago, false);
    }
}
