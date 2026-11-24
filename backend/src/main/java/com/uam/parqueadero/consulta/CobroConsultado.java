package com.uam.parqueadero.consulta;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Un cobro tal como lo muestra la consulta (CA-2).
 *
 * @param franja          código de la franja (DIURNA o NOCTURNA)
 * @param tarifaAplicada  valor de la tarifa con la que se calculó el cobro
 */
public record CobroConsultado(
        String placa,
        LocalDate diaOperativo,
        String franja,
        BigDecimal valor,
        BigDecimal tarifaAplicada,
        String estado) {
}