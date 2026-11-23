package com.uam.parqueadero.pago;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Pago en efectivo que registra el operador.
 *
 * @param placa         placa del vehículo (se normaliza)
 * @param valorRecibido efectivo que entregó el cliente
 */
public record SolicitudPago(
        @NotBlank @Schema(example = "ABC123") String placa,
        @NotNull @DecimalMin("0.0") @Schema(example = "10000") BigDecimal valorRecibido) {
}
