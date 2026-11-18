package com.uam.parqueadero.salida;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.uam.parqueadero.comun.SedeActual;

/**
 * Pruebas de la fachada con la cadena real y los puertos simulados. Verifican el flujo
 * completo del carril de salida sin base de datos.
 */
class OperacionCarrilSalidaTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final Instant AHORA = Instant.parse("2026-11-14T23:15:00Z"); // 18:15 en Colombia
    private static final LocalDate HOY = LocalDate.of(2026, 11, 14);

    private final ClasificadorPlacas clasificador = mock(ClasificadorPlacas.class);
    private final Estancias estancias = mock(Estancias.class);
    private final CobrosDelDia cobros = mock(CobrosDelDia.class);
    private final RegistroEventos eventos = mock(RegistroEventos.class);
    private final UUID sede = UUID.randomUUID();
    private final TipoVehiculoDetectado carro = new TipoVehiculoDetectado(UUID.randomUUID(), "CARRO", "Carro");
    private final TipoVehiculoDetectado moto = new TipoVehiculoDetectado(UUID.randomUUID(), "MOTO", "Moto");
    private OperacionCarrilSalida operacion;

    @BeforeEach
    void preparar() {
        Clock reloj = Clock.fixed(AHORA, ZONA);
        SedeActual sedeActual = () -> sede;
        PasoSalida cadena = new ValidarPlacaPaso(clasificador);
        cadena.enlazar(new VerificarIngresoPaso(estancias)).enlazar(new VerificarDeudaPaso(cobros, reloj));
        operacion = new OperacionCarrilSalida(cadena, eventos, sedeActual, reloj);

        when(clasificador.clasificar("ABC123")).thenReturn(Optional.of(carro));
        when(clasificador.clasificar("XYZ12D")).thenReturn(Optional.of(moto));
        when(clasificador.clasificar("AB1234")).thenReturn(Optional.empty());
        when(estancias.buscarAbierta(any(), any()))
                .thenReturn(Optional.of(new EstanciaAbierta(UUID.randomUUID(), AHORA.minusSeconds(3600))));
        when(cobros.buscar(any(), any(), any())).thenReturn(Optional.empty());
    }

    private SolicitudSalida solicitud(String placa) {
        return new SolicitudSalida(placa, new BigDecimal("0.95"));
    }

    /** CP-04-03 (éxito): una salida permitida queda en el registro con su resultado y motivo. */
    @Test
    void unaSalidaPermitidaQuedaRegistrada() {
        ResultadoSalida resultado = operacion.procesar(solicitud(" abc-123 "));

        assertTrue(resultado.abre());
        ArgumentCaptor<EventoSalida> evento = ArgumentCaptor.forClass(EventoSalida.class);
        verify(eventos).registrarSalida(evento.capture());
        assertTrue(evento.getValue().permitido());
        assertEquals("Sin cobro del día", evento.getValue().motivo());
        assertEquals("ABC123", evento.getValue().placa());
        assertEquals(" abc-123 ", evento.getValue().placaLeida());
        assertEquals(sede, evento.getValue().sedeId());
        assertEquals(AHORA, evento.getValue().ocurridoEn());
        assertEquals(HOY, evento.getValue().diaOperativo());
    }

    /** CP-04-03 (éxito): una salida negada también queda registrada, con el motivo. */
    @Test
    void unaSalidaNegadaQuedaRegistradaConSuMotivo() {
        when(cobros.buscar(sede, "ABC123", HOY))
                .thenReturn(Optional.of(new CobroDia(UUID.randomUUID(), EstadoCobro.PENDIENTE, new BigDecimal("5000"))));

        ResultadoSalida resultado = operacion.procesar(solicitud("ABC123"));

        assertFalse(resultado.abre());
        assertEquals("Cobro pendiente por $5.000", resultado.motivo());
        assertEquals(new BigDecimal("5000"), resultado.valorPendiente());
        ArgumentCaptor<EventoSalida> evento = ArgumentCaptor.forClass(EventoSalida.class);
        verify(eventos).registrarSalida(evento.capture());
        assertFalse(evento.getValue().permitido());
        assertEquals("Cobro pendiente por $5.000", evento.getValue().motivo());
    }

    /** CP-04-05 (error): placa sin ingreso → NO ABRE, y el intento queda registrado. */
    @Test
    void sinIngresoNoAbreYQuedaRegistrado() {
        when(estancias.buscarAbierta(sede, "ABC123")).thenReturn(Optional.empty());

        ResultadoSalida resultado = operacion.procesar(solicitud("ABC123"));

        assertFalse(resultado.abre());
        assertEquals("Sin ingreso registrado", resultado.motivo());
        verify(eventos).registrarSalida(any());
    }

    /** CP-04-01 (éxito): moto sin cobro → ABRE. */
    @Test
    void unaMotoSinCobroAbre() {
        ResultadoSalida resultado = operacion.procesar(solicitud("XYZ12D"));

        assertTrue(resultado.abre());
        assertEquals("Moto", resultado.tipoVehiculo());
    }

    /** Una placa inválida se rechaza sin registrar evento: el evento exige el tipo de vehículo. */
    @Test
    void unaPlacaInvalidaSeRechazaSinRegistrarEvento() {
        ResultadoSalida resultado = operacion.procesar(solicitud("AB1234"));

        assertFalse(resultado.abre());
        assertEquals("Formato de placa no válido", resultado.motivo());
        assertNull(resultado.tipoVehiculo());
        verify(eventos, never()).registrarSalida(any());
    }
}
