package com.uam.parqueadero.pago;

/** Estado VENCIDO: el cobro siguió pendiente al terminar su día operativo (SG-05); no se paga aquí. */
public class CobroVencido implements EstadoDelCobro {

    static final String MOTIVO = "El cobro está vencido: terminó su día operativo sin pagarse";

    @Override
    public String nombre() {
        return "VENCIDO";
    }

    @Override
    public ResultadoPago pagar(CobroPorPagar cobro, DatosPago datos) {
        throw new CobroNoPagableException(MOTIVO);
    }
}
