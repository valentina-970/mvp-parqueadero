package com.uam.parqueadero.salida;

import java.time.Clock;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.uam.parqueadero.comun.DiaOperativo;
import com.uam.parqueadero.comun.Placa;
import com.uam.parqueadero.comun.SedeActual;

/**
 * Fachada del carril de salida (patrón Facade): una sola operación que orquesta la
 * normalización de la placa, las reglas de la cadena, el registro del evento, la estancia
 * y la barrera. El controlador solo conoce esta clase.
 */
@Service
public class OperacionCarrilSalida {

    private final PasoSalida cadena;
    private final RegistroEventos eventos;
    private final Estancias estancias;
    private final Barrera barrera;
    private final SedeActual sede;
    private final Clock reloj;

    public OperacionCarrilSalida(PasoSalida cadena, RegistroEventos eventos, Estancias estancias,
                                 Barrera barrera, SedeActual sede, Clock reloj) {
        this.cadena = cadena;
        this.eventos = eventos;
        this.estancias = estancias;
        this.barrera = barrera;
        this.sede = sede;
        this.reloj = reloj;
    }

    /**
     * Procesa un intento de salida y devuelve la decisión con su motivo.
     * Todo intento con una placa válida queda en el registro de eventos. Una placa con formato
     * inválido no se registra, porque el evento exige el tipo de vehículo (SG-06).
     * Si la barrera abre, la estancia se cierra: el vehículo queda fuera y libera su cupo.
     */
    @Transactional
    public ResultadoSalida procesar(SolicitudSalida solicitud) {
        ContextoSalida contexto = new ContextoSalida(
                solicitud, Placa.normalizar(solicitud.placa()), sede.id(), reloj.instant());

        cadena.ejecutar(contexto);

        boolean abre = !contexto.rechazada();
        String motivo = abre ? contexto.motivoAutorizacion() : contexto.motivoRechazo();
        UUID eventoId = registrarEvento(contexto, abre, motivo);

        if (abre) {
            estancias.cerrar(contexto.estancia().id(), eventoId, contexto.ahora());
        }
        moverBarrera(abre);

        return new ResultadoSalida(
                abre,
                motivo,
                contexto.placa(),
                contexto.tipo() == null ? null : contexto.tipo().nombre(),
                contexto.valorPendiente(),
                abre ? EstadoBarrera.ABIERTA : EstadoBarrera.CERRADA,
                solicitud.confianza(),
                contexto.ahora());
    }

    /**
     * Mueve la barrera solo cuando la transacción se confirma: si el guardado falla, la barrera
     * no debe haber abierto. Sin transacción activa (por ejemplo en las pruebas) se mueve de inmediato.
     */
    private void moverBarrera(boolean abrir) {
        Runnable accion = abrir ? barrera::abrir : barrera::cerrar;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    accion.run();
                }
            });
        } else {
            accion.run();
        }
    }

    /** @return el id del evento, o {@code null} si la placa no es válida y no se puede registrar */
    private UUID registrarEvento(ContextoSalida contexto, boolean permitido, String motivo) {
        if (contexto.tipo() == null) {
            return null;
        }
        return eventos.registrarSalida(new EventoSalida(
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
