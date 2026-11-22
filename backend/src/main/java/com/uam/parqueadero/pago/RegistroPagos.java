package com.uam.parqueadero.pago;

/** Puerto para guardar un pago (principios I y D): la caja no conoce la base de datos. */
public interface RegistroPagos {

    /**
     * Guarda el pago y deja el cobro en estado PAGADO, todo en la misma transacción.
     *
     * @throws CobroNoPagableException si el cobro dejó de estar pendiente mientras tanto
     */
    void registrar(Pago pago);
}
