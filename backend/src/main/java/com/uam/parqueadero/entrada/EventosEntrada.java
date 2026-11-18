package com.uam.parqueadero.entrada;

import java.util.UUID;

/** Puerto del registro de eventos para las lecturas del carril de entrada. */
public interface EventosEntrada {

    /** @return el id del evento creado */
    UUID registrar(EventoEntrada evento);
}