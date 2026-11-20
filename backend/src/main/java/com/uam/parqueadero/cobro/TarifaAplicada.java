package com.uam.parqueadero.cobro;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Tarifa que le corresponde a un ingreso: cuál es, de qué franja y cuánto valo.
 * El cobro copia estos datos al crearse, así que un cambio porterior de tarifa no lo modifica
 * (RN-07).
 */

public record TarifaAplicada(
    UUID tarifaId,
    UUID franjaId,
    BigDecimal valor
){}