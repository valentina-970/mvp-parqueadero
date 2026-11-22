package com.uam.parqueadero.pago;

/** El cobro está en un estado desde el que no se puede pagar; el mensaje explica el motivo. */
public class CobroNoPagableException extends RuntimeException {

    public CobroNoPagableException(String motivo) {
        super(motivo);
    }
}
