package com.uam.parqueadero.salida;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uam.parqueadero.comun.DiaOperativo;
import com.uam.parqueadero.comun.Placa;
import com.uam.parqueadero.comun.SedeActual;

/**
 * Fachada del carril de salida (patrón Facade): una sola operación que orquesta la
 * normalización de la placa, las reglas de la cadena, el registro del evento y el resultado
 * para el operador. El controlador solo conoce esta clase.
 */
@Service
public class OperacionCarrilSalida {

    private final PasoSalida cadena;
    private final RegistroEventos eventos;
    private final SedeActual sede;
    private final Clock reloj;

    public OperacionCarrilSalida(PasoSalida cadena, RegistroEventos eventos, SedeActual sede, Clock reloj) {
        this.cadena = cadena;
        this.eventos = eventos;
        this.sede = sede;
        this.reloj = reloj;
    }

    /**
     * Procesa un intento de salida y devuelve la decisión con su motivo.
     * Todo intento con una placa válida queda en el registro de eventos. Una placa con formato
     * inválido no se registra, porque el evento exige el tipo de vehículo (SG-06).
     */
    @Transactional
    public ResultadoSalida procesar(SolicitudSalida solicitud) {
        ContextoSalida contexto = new ContextoSalida(
                solicitud, Placa.normalizar(solicitud.placa()), sede.id(), reloj.instant());

        cadena.ejecutar(contexto);

        boolean abre = !contexto.rechazada();
        String motivo = abre ? contexto.motivoAutorizacion() : contexto.motivoRechazo();
        registrarEvento(contexto, abre, motivo);

        return new ResultadoSalida(
                abre,
                motivo,
                contexto.placa(),
                contexto.tipo() == null ? null : contexto.tipo().nombre(),
                contexto.valorPendiente(),
                solicitud.confianza(),
                contexto.ahora());
    }

    private void registrarEvento(ContextoSalida contexto, boolean permitido, String motivo) {
        if (contexto.tipo() == null) {
            return;
        }
        eventos.registrarSalida(new EventoSalida(
                contexto.sedeId(),
                contexto.placa(),
                contexto.solicitud().placa(),
                contexto.tipo().id(),
                contexto.solicitud().confianza(),
                permitido,
                motivo,
                contexto.ahora(),
                DiaOperativo.de(contexto.ahora(), reloj.getZone())));
    }
}
