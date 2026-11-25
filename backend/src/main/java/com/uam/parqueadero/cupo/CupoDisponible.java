package com.uam.parqueadero.cupo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Cupo de un tipo de vehículo tal como lo muestra la pantalla.
 *
 * @param codigo      código del tipo, por ejemplo CARRO o MOTO
 * @param nombre      nombre para la interfaz
 * @param capacidad   cupo máximo
 * @param ocupados    vehículos adentro
 * @param disponibles lugares libres
 * @param lleno       {@code true} si ya no cabe ningún vehículo de ese tipo
 */
public record CupoDisponible(
        @Schema(example = "CARRO") String codigo,
        @Schema(example = "Carro") String nombre,
        int capacidad,
        int ocupados,
        int disponibles,
        boolean lleno) {

    static CupoDisponible de(Cupo cupo) {
        return new CupoDisponible(
                cupo.codigo(), cupo.nombre(), cupo.capacidad(), cupo.ocupados(), cupo.disponibles(), cupo.lleno());
    }
}
