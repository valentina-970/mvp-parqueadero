package com.uam.parqueadero.salida;

/** Estados del cobro; coinciden con la restricción {@code ck_cobro_estado} de la base. */
public enum EstadoCobro {
    PENDIENTE,
    PAGADO,
    VENCIDO,
    ANULADO
}
