package com.uam.parqueadero.entrada;

/**
 * Puerto del lector de placas (patrón Adapter). Cada dispositivo tiene su adaptador, que
 * traduce lo que envía al formato {@link LecturaPlaca}. Hoy el único es el simulador;
 * una cámara real sería otra implementación y el carril no cambiaría (principio L).
 */
public interface LectorPlacas {

    LecturaPlaca leer(SolicitudIngreso solicitud);
}