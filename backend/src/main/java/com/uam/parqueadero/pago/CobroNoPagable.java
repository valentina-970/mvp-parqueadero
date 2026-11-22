package com.uam.parqueadero.pago;

/** Estado desde el que no se puede pagar: el intento se rechaza con el estado como motivo. */
public class CobroNoPagable implements EstadoDelCobro {

    private final String nombre;

    public CobroNoPagable(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String nombre() {
        return nombre;
    }

    @Override
    public ResultadoPago pagar(CobroPorPagar cobro, DatosPago datos) {
        throw new CobroNoPagableException("El cobro está " + nombre.toLowerCase() + " y no se puede pagar");
    }
}
