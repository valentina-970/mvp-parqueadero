package com.uam.parqueadero.cupo;

import java.util.UUID;

/**
 * Cupo de un tipo de vehículo en la sede: cuántos caben y cuántos hay adentro.
 * Los vehículos adentro no se guardan en ningún contador: son las estancias abiertas, así que
 * el valor nunca queda desfasado y cada salida libera su cupo sola (HU-07, CA-5).
 *
 * @param capacidad cupo máximo configurado para el tipo
 * @param ocupados  vehículos de ese tipo que están adentro ahora
 */
public record Cupo(
        UUID tipoVehiculoId,
        String codigo,
        String nombre,
        int capacidad,
        int ocupados) {

    /** Lugares libres; nunca negativo, aunque la capacidad se haya bajado con vehículos adentro. */
    public int disponibles() {
        return Math.max(capacidad - ocupados, 0);
    }

    /** {@code true} si no queda ningún lugar para este tipo de vehículo. */
    public boolean lleno() {
        return ocupados >= capacidad;
    }
}
