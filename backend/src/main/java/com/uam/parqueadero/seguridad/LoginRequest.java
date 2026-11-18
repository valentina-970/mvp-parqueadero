package com.uam.parqueadero.seguridad;

import jakarta.validation.constraints.NotBlank;

/** Cuerpo de la petición de inicio de sesión. */
public record LoginRequest(
        @NotBlank String usuario,
        @NotBlank String contrasena) {
}
