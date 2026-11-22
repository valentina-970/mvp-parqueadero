package com.uam.parqueadero.pago;

import java.math.BigDecimal;
import java.time.Instant;

/** Respuesta de la caja al registrar un pago. */
public record PagoRegistrado(
        String placa,
        BigDecimal valorCobro,
        BigDecimal valorRecibido,
        BigDecimal cambio,
        String estadoCobro,
        Instant pagadoEn,
        Instant paseVigenteHasta,
        String recibidoPor) {

    static PagoRegistrado de(PagoDeCaja pagoDeCaja) {
        Pago pago = pagoDeCaja.resultado().pago();
        return new PagoRegistrado(
                pagoDeCaja.placa(), pago.valor(), pago.valorRecibido(), pago.cambio(), "PAGADO",
                pago.pagadoEn(), pago.paseVigenteHasta(), pago.operadorNombre());
    }
}
