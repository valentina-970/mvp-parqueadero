package com.uam.parqueadero.pago;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Cobro tal como lo muestra la pantalla de la caja. */
public record CobroConsulta(
        String placa,
        String tipoVehiculo,
        LocalDate diaOperativo,
        String franja,
        String tarifaAplicada,
        BigDecimal valor,
        String estado) {

    static CobroConsulta de(CobroPorPagar cobro) {
        return new CobroConsulta(
                cobro.placa(), cobro.tipoVehiculo(), cobro.diaOperativo(), cobro.franja(),
                cobro.tarifaAplicada(), cobro.valor(), cobro.estado());
    }
}
