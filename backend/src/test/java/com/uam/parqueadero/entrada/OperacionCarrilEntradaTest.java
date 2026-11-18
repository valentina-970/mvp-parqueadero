package com.uam.parqueadero.entrada;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.springframework.context.ApplicationEventPublisher;

import com.uam.parqueadero.comun.SedeActual;
import com.uam.parqueadero.entrada.infraestructura.LectorSimulado;
import com.uam.parqueadero.salida.ClasificadorPlacas;
import com.uam.parqueadero.salida.TipoVehiculoDetectado;

/**
 * Pruebas de la fachada con la cadena real, el lector simulado y los puertos simulados.
 * Verifican el flujo completo del carril de entrada sin base de datos.
 */
class OperacionCarrilEntradaTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final Instant AHORA = Instant.parse("2026-11-14T13:30:00Z"); // 8:30 en Colombia
    private static final LocalDate HOY = LocalDate.of(2026, 11, 14);

    private final ClasificadorPlacas clasificador = mock(ClasificadorPlacas.class);
    private final EventosEntrada eventos = mock(EventosEntrada.class);
    private final EstanciasEntrada estancias = mock(EstanciasEntrada.class);
    private final ApplicationEventPublisher publicador = mock(ApplicationEventPublisher.class);
    private final UUID sede = UUID.randomUUID();
    private final UUID eventoId = UUID.randomUUID();
    private final UUID estanciaId = UUID.randomUUID();
    private final TipoVehiculoDetectado carro = new TipoVehiculoDetectado(UUID.randomUUID(), "CARRO", "Carro");
    private final TipoVehiculoDetectado moto = new TipoVehiculoDetectado(UUID.randomUUID(), "MOTO", "Moto");
    private OperacionCarrilEntrada operacion;

    @BeforeEach
    void preparar() {
        Clock reloj = Clock.fixed(AHORA, ZONA);
        SedeActual sedeActual = () -> sede;
        UmbralConfianza umbral = () -> new BigDecimal("0.80");
        PasoEntrada cadena = new ValidarFormatoPaso(clasificador);
        cadena.enlazar(new VerificarConfianzaPaso(umbral)).enlazar(new VerificarNoAdentroPaso(estancias));
        operacion = new OperacionCarrilEntrada(
                new LectorSimulado(), cadena, eventos, estancias, publicador, sedeActual, reloj);

        when(clasificador.clasificar("ABC123")).thenReturn(Optional.of(carro));
        when(clasificador.clasificar("XYZ12D")).thenReturn(Optional.of(moto));
        when(clasificador.clasificar("AB1234")).thenReturn(Optional.empty());
        when(clasificador.clasificar("")).thenReturn(Optional.empty());
        when(eventos.registrar(any())).thenReturn(eventoId);
        when(estancias.abrir(any(), any(), any(), any(), any())).thenReturn(estanciaId);
    }

    private SolicitudIngreso solicitud(String placa, String confianza) {
        return new SolicitudIngreso(placa, new BigDecimal(confianza));
    }

    /** CP-01-01 (éxito): carro con confianza alta → ingreso registrado como lectura automática. */
    @Test
    void unCarroConConfianzaAltaQuedaRegistrado() {
        ResultadoIngreso resultado = operacion.procesar(solicitud("ABC123", "0.95"));

        assertEquals(EstadoIngreso.PERMITIDO, resultado.resultado());
        assertEquals("Carro", resultado.tipoVehiculo());
        assertEquals(AHORA, resultado.ocurridoEn());
        ArgumentCaptor<EventoEntrada> evento = ArgumentCaptor.forClass(EventoEntrada.class);
        verify(eventos).registrar(evento.capture());
        assertEquals(EstadoIngreso.PERMITIDO, evento.getValue().resultado());
        assertEquals(sede, evento.getValue().sedeId());
        assertEquals(carro.id(), evento.getValue().tipoVehiculoId());
        assertEquals(new BigDecimal("0.95"), evento.getValue().confianza());
        assertEquals(HOY, evento.getValue().diaOperativo());
        verify(estancias).abrir(sede, "ABC123", carro.id(), eventoId, AHORA);
    }

    /** CP-01-02 (éxito): placa de moto → ingreso registrado como moto. */
    @Test
    void unaMotoQuedaRegistradaComoMoto() {
        ResultadoIngreso resultado = operacion.procesar(solicitud("XYZ12D", "0.90"));

        assertEquals(EstadoIngreso.PERMITIDO, resultado.resultado());
        assertEquals("Moto", resultado.tipoVehiculo());
    }

    /** CP-01-03 (éxito): la placa se normaliza antes de validarse y se guarda también como se leyó. */
    @Test
    void laPlacaSeNormalizaAntesDeRegistrarse() {
        ResultadoIngreso resultado = operacion.procesar(solicitud(" abc-123 ", "0.95"));

        assertEquals("ABC123", resultado.placa());
        ArgumentCaptor<EventoEntrada> evento = ArgumentCaptor.forClass(EventoEntrada.class);
        verify(eventos).registrar(evento.capture());
        assertEquals("ABC123", evento.getValue().placa());
        assertEquals(" abc-123 ", evento.getValue().placaLeida());
    }

    /** CP-01-04 (error): formato inválido → rechazo con su mensaje y sin registro. */
    @Test
    void unaPlacaConFormatoInvalidoSeRechazaSinRegistrar() {
        ResultadoIngreso resultado = operacion.procesar(solicitud("AB1234", "0.95"));

        assertEquals(EstadoIngreso.RECHAZADO, resultado.resultado());
        assertEquals("Formato de placa no válido", resultado.motivo());
        assertNull(resultado.tipoVehiculo());
        verify(eventos, never()).registrar(any());
        verify(estancias, never()).abrir(any(), any(), any(), any(), any());
    }

    /** CP-01-05 (error): placa vacía → rechazo sin registro. */
    @Test
    void unaPlacaVaciaSeRechazaSinRegistrar() {
        ResultadoIngreso resultado = operacion.procesar(solicitud("", "0.95"));

        assertEquals(EstadoIngreso.RECHAZADO, resultado.resultado());
        verify(eventos, never()).registrar(any());
    }

    /** CA-7: con confianza menor que el umbral la lectura queda pendiente y no abre estancia. */
    @Test
    void conConfianzaBajaQuedaPendienteDeValidacion() {
        ResultadoIngreso resultado = operacion.procesar(solicitud("ABC123", "0.79"));

        assertEquals(EstadoIngreso.PENDIENTE_VALIDACION, resultado.resultado());
        ArgumentCaptor<EventoEntrada> evento = ArgumentCaptor.forClass(EventoEntrada.class);
        verify(eventos).registrar(evento.capture());
        assertEquals(EstadoIngreso.PENDIENTE_VALIDACION, evento.getValue().resultado());
        verify(estancias, never()).abrir(any(), any(), any(), any(), any());
        verify(publicador, never()).publishEvent(any(Object.class));
    }

    /** CA-7: una confianza igual al umbral cuenta como lectura automática. */
    @Test
    void unaConfianzaIgualAlUmbralEsAutomatica() {
        ResultadoIngreso resultado = operacion.procesar(solicitud("ABC123", "0.80"));

        assertEquals(EstadoIngreso.PERMITIDO, resultado.resultado());
    }

    /** Un vehículo que ya está adentro no vuelve a entrar; el intento queda registrado. */
    @Test
    void unVehiculoQueYaEstaAdentroSeRechaza() {
        when(estancias.hayAbierta(sede, "ABC123")).thenReturn(true);

        ResultadoIngreso resultado = operacion.procesar(solicitud("ABC123", "0.95"));

        assertEquals(EstadoIngreso.RECHAZADO, resultado.resultado());
        assertEquals("El vehículo ya tiene un ingreso registrado", resultado.motivo());
        verify(eventos).registrar(any());
        verify(estancias, never()).abrir(any(), any(), any(), any(), any());
    }

    /** Observer: un ingreso permitido avisa a los demás módulos (lo usará HU-02). */
    @Test
    void unIngresoPermitidoPublicaElAviso() {
        operacion.procesar(solicitud("ABC123", "0.95"));

        ArgumentCaptor<IngresoRegistrado> aviso = ArgumentCaptor.forClass(IngresoRegistrado.class);
        verify(publicador).publishEvent(aviso.capture());
        assertEquals(estanciaId, aviso.getValue().estanciaId());
        assertEquals("ABC123", aviso.getValue().placa());
        assertEquals(carro.id(), aviso.getValue().tipoVehiculoId());
        assertEquals(HOY, aviso.getValue().diaOperativo());
    }
}