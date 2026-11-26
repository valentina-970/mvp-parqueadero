package com.uam.parqueadero.cobro;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Datos de un cobro que se va a crear al registrar un ingreso (HU-02). Lo arma
 * {@link GeneradorCobro} y lo guarda el puerto {@link RegistroCobros}; el cobro nace pendiente.
 *
 * @param sedeId           sede donde ocurrió el ingreso
 * @param placa            placa normalizada del vehículo
 * @param tipoVehiculoId   tipo de vehículo, para la tarifa
 * @param diaOperativo     día operativo del ingreso (de 06:00 a 05:59, S-02)
 * @param franjaId         franja en la que entró el vehículo (diurna o nocturna)
 * @param tarifaId         tarifa vigente con la que se calculó el valor (RN-07)
 * @param valor            valor a pagar; no se recalcula aunque la tarifa cambie después
 * @param ingresoEventoId  evento de acceso del ingreso que originó el cobro
 * @param creadoEn         momento en que se creó el cobro
 */
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
