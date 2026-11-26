package com.uam.parqueadero.entrada;

import java.util.UUID;

/** Puerto del registro de eventos para las lecturas del carril de entrada. */
public interface EventosEntrada {

    /** @return el id del evento creado */
    UUID registrar(EventoEntrada evento);

    /**
     * Resuelve una lectura que estaba pendiente (HU-06): el mismo evento pasa a lectura manual,
     * con la placa que validó el operador, el resultado final y el usuario que la validó (CA-6).
     */
    void resolverManual(UUID eventoId, EventoEntrada evento, String usuario);
}