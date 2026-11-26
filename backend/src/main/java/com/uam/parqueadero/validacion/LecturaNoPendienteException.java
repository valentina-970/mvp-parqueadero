package com.uam.parqueadero.validacion;

/** La lectura no existe o ya la validó otro operador. */
public class LecturaNoPendienteException extends RuntimeException {

    static final String MENSAJE = "La lectura ya fue validada o no existe";

    public LecturaNoPendienteException() {
        super(MENSAJE);
    }
}
