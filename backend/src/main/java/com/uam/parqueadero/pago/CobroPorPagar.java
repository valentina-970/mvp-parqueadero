package com.uam.parqueadero.pago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.uam.parqueadero.comun.Dinero;

/**
 * Cobro de un vehículo tal como lo ve la caja: lo que el operador necesita para cobrarlo.
 *
 * @param tipoVehiculo nombre del tipo de vehículo (por ejemplo "Carro")
 * @param franja       código de la franja del primer ingreso (por ejemplo "DIURNA")
 * @param valor        valor guardado en el cobro cuando se creó (RN-07)
 * @param estado       estado del cobro: PENDIENTE, PAGADO, VENCIDO o ANULADO
 */
public record CobroPorPagar(
        UUID id,
        String placa,
        String tipoVehiculo,
        LocalDate diaOperativo,
        String franja,
        BigDecimal valor,
        String estado) {

    /** Texto de la tarifa con la que se calculó el cobro, por ejemplo "Carro diurna · $5.000". */
    public String tarifaAplicada() {
        return tipoVehiculo + " " + franja.toLowerCase() + " · " + Dinero.formatear(valor);
    }
}
