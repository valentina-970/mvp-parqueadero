package com.uam.parqueadero.validacion;

/** La placa que digitó el operador no tiene un formato válido (HU-06, CA-5); la lectura sigue pendiente. */
public class PlacaNoValidaException extends RuntimeException {

    public PlacaNoValidaException(String motivo) {
        super(motivo);
    }
}