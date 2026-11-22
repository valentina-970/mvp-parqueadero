package com.uam.parqueadero.pago;

/** Estado ANULADO: el cobro ya no se cobra, así que no se puede pagar (HU-03, CA-7). */
public class CobroAnulado implements EstadoDelCobro {

    static final String MOTIVO = "El cobro está anulado y no se puede pagar";

    @Override
    public String nombre() {
        return "ANULADO";
    }

    @Override
    public ResultadoPago pagar(CobroPorPagar cobro, DatosPago datos) {
        throw new CobroNoPagableException(MOTIVO);
    }
}
