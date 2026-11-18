package com.uam.parqueadero.salida;

import java.time.Instant;
import java.util.UUID;

/** Vehículo que está dentro del parqueadero: su estancia no tiene hora de salida. */
public record EstanciaAbierta(UUID id, Instant ingresoEn) {
}
