package com.uam.parqueadero.cobro;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record NuevoCobro(
    UUID sedeId,
    String placa,
    UUID tipoVehiculoId,
    LocalDate diaOperativo,
    UUID franjaId,
    UUID tarifaId,
    BigDecimal valor,
    UUID ingresoEventoId,
    Instant creadoEn
) {

}
