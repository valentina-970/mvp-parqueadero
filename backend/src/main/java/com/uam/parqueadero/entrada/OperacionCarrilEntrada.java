package com.uam.parqueadero.entrada;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uam.parqueadero.comun.DiaOperativo;
import com.uam.parqueadero.comun.SedeActual;

/**
 * Fachada del carril de entrada (patrón Facade): una sola operación que orquesta la lectura,
 * las reglas de la cadena, el registro del evento, la estancia y el aviso de ingreso.
 * El controlador solo conoce esta clase.
 */
@Service
public class OperacionCarrilEntrada {

    private final LectorPlacas lector;
    private final PasoEntrada cadena;
    private final EventosEntrada eventos;
    private final EstanciasEntrada estancias;
    private final ApplicationEventPublisher publicador;
    private final SedeActual sede;
    private final Clock reloj;

    public OperacionCarrilEntrada(LectorPlacas lector, PasoEntrada cadena, EventosEntrada eventos,
                                  EstanciasEntrada estancias, ApplicationEventPublisher publicador,
                                  SedeActual sede, Clock reloj) {
        this.lector = lector;
        this.cadena = cadena;
        this.eventos = eventos;
        this.estancias = estancias;
        this.publicador = publicador;
        this.sede = sede;
        this.reloj = reloj;
    }

    /**
     * Procesa una lectura del carril de entrada y devuelve el resultado con su motivo.
     * Toda lectura con placa válida queda en el registro de eventos. Una placa con formato
     * inválido no se registra (CA-6): el evento exige el tipo de vehículo.
     * Solo un ingreso permitido abre la estancia y avisa a los demás módulos.
     */
    @Transactional
    public ResultadoIngreso procesar(SolicitudIngreso solicitud) {
        LecturaPlaca lectura = lector.leer(solicitud);
        ContextoEntrada contexto = new ContextoEntrada(lectura, sede.id(), reloj.instant());

        cadena.ejecutar(contexto);

        if (contexto.tipo() != null) {
            LocalDate dia = DiaOperativo.de(contexto.ahora(), reloj.getZone());
            UUID eventoId = eventos.registrar(new EventoEntrada(
                    contexto.sedeId(),
                    contexto.placa(),
                    lectura.placaLeida(),
                    contexto.tipo().id(),
                    lectura.confianza(),
                    contexto.estado(),
                    contexto.motivo(),
                    contexto.ahora(),
                    dia));

            if (contexto.estado() == EstadoIngreso.PERMITIDO) {
                UUID estanciaId = estancias.abrir(
                        contexto.sedeId(), contexto.placa(), contexto.tipo().id(), eventoId, contexto.ahora());
                publicador.publishEvent(new IngresoRegistrado(
                        estanciaId, eventoId, contexto.sedeId(), contexto.placa(), contexto.tipo().id(),
                        contexto.ahora(), dia));
            }
        }

        return new ResultadoIngreso(
                contexto.estado(),
                contexto.motivo(),
                contexto.placa(),
                contexto.tipo() == null ? null : contexto.tipo().nombre(),
                lectura.confianza(),
                contexto.ahora());
    }
}