package com.uam.parqueadero.entrada;

import java.time.Instant;
import java.util.UUID;

/** Puerto de las estancias desde el punto de vista del carril de entrada. */
public interface EstanciasEntrada {

    /** {@code true} si el vehículo tiene una estancia abierta en la sede (ya está adentro). */
    boolean hayAbierta(UUID sedeId, String placa);

    /**
     * Abre la estancia del vehículo que acaba de entrar.
     *
     * @return el id de la estancia creada
     */
    UUID abrir(UUID sedeId, String placa, UUID tipoVehiculoId, UUID eventoIngresoId, Instant ingresoEn);
}