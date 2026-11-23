package com.uam.parqueadero.cobro;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Estrategia para calcular la tarifa de un ingreso (Patrón Strategy). La del MVP es
 * {@link TarifaPorFranja}: tarifa plana por día según el tipo de vehículo y la franja
 * de la hora de entrada (SG-01 y SG-02). Otra forma de cobrar, por ejemplo por horas,
 * sería otra implementación sin tocar la generación del cobro (principio O).
 */

public interface PoliticaTarifa{

    /**
     * @param momento   hora del primer ingreso del día operativo
     * @return la tarifa que aplica, o vacío si no hay ninguna configurada
     */
    Optional<TarifaAplicada> calcular(UUID sedeId, UUID tipoVehiculoId, Instant momento);
}