package com.uam.parqueadero.pago;

import java.math.BigDecimal;

import com.uam.parqueadero.comun.Dinero;

/** El efectivo recibido es menor que el cobro: el pago se rechaza (HU-03, CA-2). */
public class ValorInsuficienteException extends RuntimeException {

    public ValorInsuficienteException(BigDecimal faltante) {
        super("Valor recibido insuficiente: faltan " + Dinero.formatear(faltante));
    }
}
