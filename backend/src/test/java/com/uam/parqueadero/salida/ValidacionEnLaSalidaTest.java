package com.uam.parqueadero.salida;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import com.uam.parqueadero.comun.ClasificadorPlacas;
import com.uam.parqueadero.comun.LecturaPendiente;
import com.uam.parqueadero.comun.SedeActual;
import com.uam.parqueadero.comun.TipoVehiculoDetectado;

/** Validación manual en el carril de salida (HU-06, CA-7), con la cadena real y los puertos simulados. */
class ValidacionEnLaSalidaTest {

    private static final Instant AHORA = Instant.parse("2026-11-25T23:00:00Z");

    private final ClasificadorPlacas clasificador = mock(ClasificadorPlacas.class);
    private final Estancias estancias = mock(Estancias.class);
    private final CobrosDelDia cobros = mock(CobrosDelDia.class);
    private final RegistroEventos eventos = mock(RegistroEventos.class);
    private final Barrera barrera = mock(Barrera.class);
    private final UUID sede = UUID.randomUUID();
    private final UUID estanciaId = UUID.randomUUID();
    private final UUID eventoPendiente = UUID.randomUUID();
    private final TipoVehiculoDetectado carro = new TipoVehiculoDetectado(UUID.randomUUID(), "CARRO", "Carro");
    private OperacionCarrilSalida operacion;

    @BeforeEach
    void preparar() {
        Clock reloj = Clock.fixed(AHORA, ZoneId.of("America/Bogota"));
        SedeActual sedeActual = () -> sede;
        PasoSalida cadena = new ValidarPlacaPaso(clasificador);
        cadena.enlazar(new VerificarConfianzaSalidaPaso(() -> new BigDecimal("0.80")))
                .enlazar(new VerificarIngresoPaso(estancias))
                .enlazar(new VerificarDeudaPaso(cobros, reloj));
        operacion = new OperacionCarrilSalida(cadena, eventos, estancias, barrera, sedeActual, reloj);

        when(clasificador.clasificar("ABC123")).thenReturn(Optional.of(carro));
        when(clasificador.clasificar("AB12")).thenReturn(Optional.empty());
        when(eventos.registrarSalida(any())).thenReturn(UUID.randomUUID());
        when(estancias.buscarAbierta(any(), any()))
                .thenReturn(Optional.of(new EstanciaAbierta(estanciaId, AHORA.minusSeconds(3600))));
        when(cobros.buscar(any(), any(), any())).thenReturn(Optional.empty());
    }

    /** CP-06-05 (éxito): confianza 0,50 → la barrera no se mueve y el intento queda pendiente. */
    @Test
    void unaLecturaDudosaNoDecideLaBarrera() {
        ResultadoSalida resultado = operacion.procesar(new SolicitudSalida("ABC123", new BigDecimal("0.50")));

        assertTrue(resultado.pendiente());
        assertFalse(resultado.abre());
        assertEquals(EstadoBarrera.CERRADA, resultado.barrera());
        ArgumentCaptor<EventoSalida> evento = ArgumentCaptor.forClass(EventoSalida.class);
        verify(eventos).registrarSalida(evento.capture());
        assertTrue(evento.getValue().pendiente());
        verify(estancias, never()).cerrar(any(), any(), any());
        verify(barrera, never()).abrir();
    }

    /** El operador confirma la placa: la salida se decide con las reglas de siempre y queda como lectura manual. */
    @Test
    void alConfirmarLaPlacaLaBarreraAbre() {
        LecturaPendiente pendiente = new LecturaPendiente(eventoPendiente, "SALIDA", "ABC123", "ABC123", "Carro",
                new BigDecimal("0.50"), AHORA.minusSeconds(20));

        ResultadoSalida resultado = operacion.validar(pendiente, "ABC123", "operador1");

        assertTrue(resultado.abre());
        assertTrue(resultado.manual());
        verify(eventos).resolverManual(eq(eventoPendiente), any(), eq("operador1"));
        verify(estancias).cerrar(estanciaId, eventoPendiente, AHORA);
        verify(barrera).abrir();
    }

    /** CA-5 en la salida: una placa digitada sin formato no resuelve la lectura ni mueve la barrera. */
    @Test
    void unaPlacaDigitadaSinFormatoNoResuelveLaSalida() {
        LecturaPendiente pendiente = new LecturaPendiente(eventoPendiente, "SALIDA", "ABC123", "ABC123", "Carro",
                new BigDecimal("0.50"), AHORA.minusSeconds(20));

        ResultadoSalida resultado = operacion.validar(pendiente, "AB12", "operador1");

        assertFalse(resultado.abre());
        verify(eventos, never()).resolverManual(any(), any(), any());
        verify(barrera, never()).abrir();
    }
}
