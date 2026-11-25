package com.uam.parqueadero.cupo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de solo lectura con los cupos de una sede (principios I y D). Lo usa el carril de
 * entrada para saber si hay lugar y la pantalla para mostrar los cupos; HU-08 lo reutiliza para
 * saber cuántos vehículos hay adentro antes de bajar una capacidad.
 */
public interface CuposDeLaSede {

    /** Cupos de todos los tipos de vehículo de la sede, ordenados por código del tipo. */
    List<Cupo> todos(UUID sedeId);

    /** Cupo de un tipo de vehículo, o vacío si la sede no le definió cupo. */
    Optional<Cupo> deTipo(UUID sedeId, UUID tipoVehiculoId);
}
