package com.uam.parqueadero.salida;

import java.time.Clock;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.uam.parqueadero.comun.DiaOperativo;
import com.uam.parqueadero.comun.LecturaPendiente;
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
     * Si la confianza es baja, la barrera no se mueve hasta que valide el operador (HU-06).
     */
    @Transactional
    public ResultadoSalida procesar(SolicitudSalida solicitud) {
        ContextoSalida contexto = new ContextoSalida(
                solicitud, Placa.normalizar(solicitud.placa()), sede.id(), reloj.instant());
        return decidir(contexto, eventos::registrarSalida);
    }

    /**
     * Valida un intento de salida que estaba pendiente (HU-06, CA-7). La placa que confirma o
     * corrige el operador recorre la misma cadena, salvo el filtro de confianza (CA-5), y el
     * evento pendiente queda resuelto como lectura manual con su usuario (CA-6). Si la placa
     * digitada no tiene un formato válido, no se guarda nada y la lectura sigue pendiente.
     */
    @Transactional
    public ResultadoSalida validar(LecturaPendiente pendiente, String placaDigitada, String usuario) {
        SolicitudSalida original = new SolicitudSalida(pendiente.placaLeida(), pendiente.confianza());
        ContextoSalida contexto = ContextoSalida.manual(
                original, Placa.normalizar(placaDigitada), sede.id(), reloj.instant());
        return decidir(contexto, evento -> {
            eventos.resolverManual(pendiente.eventoId(), evento, usuario);
            return pendiente.eventoId();
        });
    }

    /**
     * Pasos comunes a la lectura automática y a la manual: aplica la cadena, guarda el evento
     * con la estrategia recibida, cierra la estancia si la barrera abre y mueve la barrera.
     */
    private ResultadoSalida decidir(ContextoSalida contexto, Function<EventoSalida, UUID> guardarEvento) {
        cadena.ejecutar(contexto);

        boolean pendiente = contexto.pendiente();
        boolean abre = !contexto.rechazada() && !pendiente;
        String motivo = pendiente ? contexto.motivoPendiente()
                : abre ? contexto.motivoAutorizacion() : contexto.motivoRechazo();
        UUID eventoId = registrarEvento(contexto, abre, motivo, guardarEvento);

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
                contexto.solicitud().confianza(),
                contexto.ahora(),
                pendiente,
                contexto.manual());
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
    private UUID registrarEvento(ContextoSalida contexto, boolean permitido, String motivo,
                                 Function<EventoSalida, UUID> guardarEvento) {
        if (contexto.tipo() == null) {
            return null;
        }
        return guardarEvento.apply(new EventoSalida(
                contexto.sedeId(),
                contexto.placa(),
                contexto.solicitud().placa(),
                contexto.tipo().id(),
                contexto.solicitud().confianza(),
                permitido,
                contexto.pendiente(),
                motivo,
                contexto.ahora(),
                DiaOperativo.de(contexto.ahora(), reloj.getZone())));
    }
}