package com.uam.parqueadero.admin;

/** La configuración solicitada no cumple una regla de administración. */
public class ConfiguracionInvalidaException extends RuntimeException {

    public ConfiguracionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
