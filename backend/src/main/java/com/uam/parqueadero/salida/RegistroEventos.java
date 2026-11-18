package com.uam.parqueadero.salida;

import java.util.UUID;

/** Puerto para dejar constancia de cada intento de salida en el registro de eventos. */
public interface RegistroEventos {

    /** Guarda el intento de salida y devuelve el identificador del evento creado. */
    UUID registrarSalida(EventoSalida evento);
}
