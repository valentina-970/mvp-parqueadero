package com.uam.parqueadero.pago;

import java.util.UUID;

/** Puerto para saber quién es el operador que recibe el dinero (HU-03, CA-3). */
public interface OperadoresCaja {

    /** Identificador del usuario que inició sesión con ese nombre. */
    UUID idDe(String nombreUsuario);
}
