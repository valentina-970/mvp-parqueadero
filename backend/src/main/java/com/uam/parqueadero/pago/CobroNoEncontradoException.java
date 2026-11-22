package com.uam.parqueadero.pago;

/** La placa no tiene un cobro en el día operativo (HU-03, CA-8). */
public class CobroNoEncontradoException extends RuntimeException {

    public CobroNoEncontradoException() {
        super("No hay cobro pendiente para esta placa");
    }
}
