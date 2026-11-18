package com.uam.parqueadero.salida;

import java.time.Clock;

import org.springframework.stereotype.Service;

import com.uam.parqueadero.comun.Placa;

/**
 * Fachada del carril de salida (patrón Facade): una sola operación que orquesta la
 * normalización de la placa, las reglas de la cadena y el resultado para el operador.
 * El controlador solo conoce esta clase.
 */
@Service
public class OperacionCarrilSalida {

    private final PasoSalida cadena;
    private final Clock reloj;

    public OperacionCarrilSalida(PasoSalida cadena, Clock reloj) {
        this.cadena = cadena;
        this.reloj = reloj;
    }

    /** Procesa un intento de salida y devuelve la decisión con su motivo. */
    public ResultadoSalida procesar(SolicitudSalida solicitud) {
        ContextoSalida contexto = new ContextoSalida(
                solicitud, Placa.normalizar(solicitud.placa()), reloj.instant());

        cadena.ejecutar(contexto);

        return new ResultadoSalida(
                !contexto.rechazada(),
                contexto.rechazada() ? contexto.motivoRechazo() : "Salida autorizada",
                contexto.placa(),
                contexto.tipo() == null ? null : contexto.tipo().nombre(),
                contexto.valorPendiente(),
                solicitud.confianza(),
                contexto.ahora());
    }
}
