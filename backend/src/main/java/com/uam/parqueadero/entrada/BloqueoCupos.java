package com.uam.parqueadero.entrada;

import java.util.UUID;

/**
 * Puerto para atender de a uno los ingresos de un mismo tipo de vehículo en una sede.
 * Mientras dure la transacción del ingreso, otro ingreso del mismo tipo espera su turno.
 */
public interface BloqueoCupos {

    /** Bloquea el cupo del tipo de vehículo hasta que termine la transacción actual. */
    void bloquear(UUID sedeId, UUID tipoVehiculoId);
}