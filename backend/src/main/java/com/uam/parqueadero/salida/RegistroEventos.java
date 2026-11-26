package com.uam.parqueadero.salida;

import java.util.UUID;

/** Puerto para dejar constancia de cada intento de salida en el registro de eventos. */
public interface RegistroEventos {

    /** Guarda el intento de salida y devuelve el identificador del evento creado. */
    UUID registrarSalida(EventoSalida evento);

    /**
     * Resuelve un intento de salida que estaba pendiente (HU-06): el mismo evento pasa a lectura
     * manual, con la placa que validó el operador, el resultado final y su usuario (CA-6).
     */
    void resolverManual(UUID eventoId, EventoSalida evento, String usuario);
}