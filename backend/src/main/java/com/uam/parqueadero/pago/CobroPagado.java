package com.uam.parqueadero.pago;

/**
 * Estado PAGADO: pagar otra vez no crea un segundo pago, devuelve el que ya existe
 * (HU-03, CA-6). Esto vuelve idempotente el registro del pago: repetir la petición, por un
 * reintento o un doble clic, da el mismo resultado.
 */
public class CobroPagado implements EstadoDelCobro {

    @Override
    public String nombre() {
        return "PAGADO";
    }

    @Override
    public ResultadoPago pagar(CobroPorPagar cobro, DatosPago datos) {
        Pago existente = cobro.pagoRegistrado();
        if (existente == null) {
            throw new IllegalStateException("El cobro " + cobro.id() + " está pagado pero no tiene un pago registrado");
        }
        return ResultadoPago.existente(existente);
    }
}
