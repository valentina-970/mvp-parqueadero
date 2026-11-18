package com.uam.parqueadero.entrada;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Aviso de que un vehículo entró (patrón Observer). Lo publica el carril de entrada; quien
 * necesite reaccionar, como la generación del cobro (HU-02), lo escucha con
 * {@code @EventListener} sin que el carril sepa que existe.
 */
public record IngresoRegistrado(
        UUID estanciaId,
        UUID sedeId,
        String placa,
        UUID tipoVehiculoId,
        Instant ingresoEn,
        LocalDate diaOperativo) {
}