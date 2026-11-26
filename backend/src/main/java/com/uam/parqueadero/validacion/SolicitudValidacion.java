package com.uam.parqueadero.validacion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Lo que envía el operador al validar una lectura (HU-06, CA-4): la misma placa leída para
 * confirmarla, o la placa correcta para corregirla.
 */
public record SolicitudValidacion(@NotBlank @Size(max = 20) @Schema(example = "ABC123") String placa) {
}