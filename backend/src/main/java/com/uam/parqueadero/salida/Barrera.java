package com.uam.parqueadero.salida;

/**
 * Puerto de la barrera de salida. El MVP usa una barrera simulada; una barrera física
 * implementaría esta misma interfaz sin cambiar el flujo del carril (principios D y L).
 */
public interface Barrera {

    void abrir();

    void cerrar();

    EstadoBarrera estado();
}
