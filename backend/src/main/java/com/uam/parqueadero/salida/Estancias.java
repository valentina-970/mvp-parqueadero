package com.uam.parqueadero.salida;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto con lo que el carril de salida necesita saber de las estancias.
 * Solo expone lo que la salida usa (principio I); el dominio no conoce el ORM (principio D).
 */
public interface Estancias {

    /** @return la estancia abierta de la placa en la sede, o vacío si no tiene ingreso registrado */
    Optional<EstanciaAbierta> buscarAbierta(UUID sedeId, String placa);
}
