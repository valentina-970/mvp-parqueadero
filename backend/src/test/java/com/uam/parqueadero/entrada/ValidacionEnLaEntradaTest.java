package com.uam.parqueadero.entrada;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import com.uam.parqueadero.comun.ClasificadorPlacas;
import com.uam.parqueadero.comun.LecturaPendiente;
import com.uam.parqueadero.comun.SedeActual;
import com.uam.parqueadero.comun.TipoVehiculoDetectado;
import com.uam.parqueadero.entrada.infraestructura.LectorSimulado;

/** Validación manual en el carril de entrada (HU-06), con la cadena real y los puertos simulados. */
class ValidacionEnLaEntradaTest {

    private static final Instant AHORA = Instant.parse("2026-11-25T15:00:00Z");

    private final ClasificadorPlacas clasificador = mock(ClasificadorPlacas.class);
    private final EventosEntrada eventos = mock(EventosEntrada.class);
    private final EstanciasEntrada estancias = mock(EstanciasEntrada.class);
    private final ApplicationEventPublisher publicador = mock(ApplicationEventPublisher.class);
    private final UUID sede = UUID.randomUUID();
    private final UUID eventoPendiente = UUID.randomUUID();
    private final TipoVehiculoDetectado carro = new TipoVehiculoDetectado(UUID.randomUUID(), "CARRO", "Carro");
    private OperacionCarrilEntrada operacion;

    @BeforeEach
    void preparar() {
        SedeActual sedeActual = () -> sede;
        PasoEntrada cadena = new ValidarFormatoPaso(clasificador);
        cadena.enlazar(new VerificarConfianzaPaso(() -> new BigDecimal("0.80")))
                .enlazar(new BloquearCupoPaso((s, t) -> { }))
                .enlazar(new VerificarNoAdentroPaso(estancias));
        operacion = new OperacionCarrilEntrada(new LectorSimulado(), cadena, eventos, estancias, publicador,
                sedeActual, Clock.fixed(AHORA, ZoneId.of("America/Bogota")));

        when(clasificador.clasificar("ABC123")).thenReturn(Optional.of(carro));
        when(clasificador.clasificar("AB12")).thenReturn(Optional.empty());
        when(estancias.abrir(any(), any(), any(), any(), any())).thenReturn(UUID.randomUUID());
    }

    private LecturaPendiente pendiente(String placaLeida) {
        return new LecturaPendiente(eventoPendiente, "ENTRADA", placaLeida, placaLeida, "Carro",
                new BigDecimal("0.60"), AHORA.minusSeconds(30));
    }

    /** CP-06-01 (éxito): confianza 0,60; el operador confirma → ingreso como lectura manual con su usuario. */
    @Test
    void elOperadorConfirmaLaPlacaYElIngresoSeRegistra() {
        ResultadoIngreso resultado = operacion.validar(pendiente("ABC123"), "ABC123", "operador1");

        assertEquals(EstadoIngreso.PERMITIDO, resultado.resultado());
        assertTrue(resultado.manual());
        verify(eventos).resolverManual(eq(eventoPendiente), any(), eq("operador1"));
        verify(eventos, never()).registrar(any());
        verify(estancias).abrir(sede, "ABC123", carro.id(), eventoPendiente, AHORA);
        verify(publicador).publishEvent(any(IngresoRegistrado.class));
    }

    /** CP-06-02 (éxito): se leyó ABC128 y el operador corrige a ABC123 → el evento queda con la placa corregida. */
    @Test
    void elOperadorCorrigeLaPlaca() {
        ResultadoIngreso resultado = operacion.validar(pendiente("ABC128"), "abc-123", "operador1");

        assertEquals("ABC123", resultado.placa());
        ArgumentCaptor<EventoEntrada> evento = ArgumentCaptor.forClass(EventoEntrada.class);
        verify(eventos).resolverManual(eq(eventoPendiente), evento.capture(), eq("operador1"));
        assertEquals("ABC123", evento.getValue().placa());
        assertEquals("ABC128", evento.getValue().placaLeida());
        assertEquals(new BigDecimal("0.60"), evento.getValue().confianza());
    }

    /** CP-06-06 (error): el operador digita AB12 → formato no válido; no se guarda nada y sigue pendiente. */
    @Test
    void unaPlacaDigitadaSinFormatoNoResuelveLaLectura() {
        ResultadoIngreso resultado = operacion.validar(pendiente("ABC123"), "AB12", "operador1");

        assertEquals(EstadoIngreso.RECHAZADO, resultado.resultado());
        assertEquals("Formato de placa no válido", resultado.motivo());
        verify(eventos, never()).resolverManual(any(), any(), any());
        verify(estancias, never()).abrir(any(), any(), any(), any(), any());
    }

    /** Las demás reglas siguen valiendo: si el vehículo ya está adentro, la lectura queda resuelta como rechazo. */
    @Test
    void unVehiculoQueYaEstaAdentroSeRechazaAunqueLoValideElOperador() {
        when(estancias.hayAbierta(sede, "ABC123")).thenReturn(true);

        ResultadoIngreso resultado = operacion.validar(pendiente("ABC123"), "ABC123", "operador1");

        assertEquals(EstadoIngreso.RECHAZADO, resultado.resultado());
        ArgumentCaptor<EventoEntrada> evento = ArgumentCaptor.forClass(EventoEntrada.class);
        verify(eventos).resolverManual(eq(eventoPendiente), evento.capture(), eq("operador1"));
        assertEquals(EstadoIngreso.RECHAZADO, evento.getValue().resultado());
        verify(estancias, never()).abrir(any(), any(), any(), any(), any());
    }
}
