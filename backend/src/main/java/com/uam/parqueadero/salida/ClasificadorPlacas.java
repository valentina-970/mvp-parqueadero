package com.uam.parqueadero.salida;

import java.util.Optional;

/**
 * Puerto que deduce el tipo de vehículo a partir de la placa normalizada.
 * El dominio de la salida depende de esta abstracción, no de la base de datos (principio D).
 */
public interface ClasificadorPlacas {

    /** @return el tipo de vehículo, o vacío si la placa no cumple ningún formato válido */
    Optional<TipoVehiculoDetectado> clasificar(String placaNormalizada);
}
