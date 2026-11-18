package com.uam.parqueadero.comun;

import java.util.UUID;

/**
 * Puerto que indica en qué sede opera este sistema. El MVP tiene una sola sede,
 * pero cada registro guarda su sede para poder crecer a varias.
 */
public interface SedeActual {

    UUID id();
}
