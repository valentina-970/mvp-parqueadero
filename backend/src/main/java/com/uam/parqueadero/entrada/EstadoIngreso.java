package com.uam.parqueadero.entrada;

/** Resultado de una lectura en el carril de entrada. Son los mismos valores de {@code evento_acceso.resultado}. */
public enum EstadoIngreso {
    /** El ingreso quedó registrado como lectura automática. */
    PERMITIDO,
    /** La confianza no alcanzó el umbral: el operador debe validar la placa (HU-06). */
    PENDIENTE_VALIDACION,
    /** La lectura no se acepta: formato inválido o vehículo que ya está adentro. */
    RECHAZADO
}