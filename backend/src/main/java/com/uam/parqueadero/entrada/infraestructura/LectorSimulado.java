package com.uam.parqueadero.entrada.infraestructura;

import org.springframework.stereotype.Component;

import com.uam.parqueadero.comun.Placa;
import com.uam.parqueadero.entrada.LecturaPlaca;
import com.uam.parqueadero.entrada.LectorPlacas;
import com.uam.parqueadero.entrada.SolicitudIngreso;

/**
 * Adaptador del simulador (patrón Adapter): convierte lo que envía el simulador en una
 * {@link LecturaPlaca}, normalizando la placa (CA-3). El simulador ya envía la confianza
 * entre 0 y 1; el adaptador de una cámara que la envíe de 0 a 100 la convertiría aquí.
 */
@Component
public class LectorSimulado implements LectorPlacas {

    @Override
    public LecturaPlaca leer(SolicitudIngreso solicitud) {
        return new LecturaPlaca(Placa.normalizar(solicitud.placa()), solicitud.placa(), solicitud.confianza());
    }
}