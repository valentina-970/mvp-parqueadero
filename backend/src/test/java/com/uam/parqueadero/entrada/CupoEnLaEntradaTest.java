package com.uam.parqueadero.entrada;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import com.uam.parqueadero.comun.ClasificadorPlacas;
import com.uam.parqueadero.comun.SedeActual;
import com.uam.parqueadero.comun.TipoVehiculoDetectado;
import com.uam.parqueadero.cupo.Cupo;
import com.uam.parqueadero.cupo.CuposDeLaSede;
import com.uam.parqueadero.entrada.infraestructura.LectorSimulado;

/**
 * Pruebas del control de cupos con la cadena real de la entrada (HU-07, CP-07-01 a CP-07-05).
 * Los cupos son un doble en memoria que cuenta como la base: ocupados = estancias abiertas.
 */
class CupoEnLaEntradaTest {

    /** Cupos en memoria: cada ingreso permitido ocupa un lugar y {@link #liberar} imita una salida. */
    private static class CuposEnMemoria implements CuposDeLaSede {
        private final Map<UUID, Cupo> porTipo = new HashMap<>();

        void definir(TipoVehiculoDetectado tipo, int capacidad, int ocupados) {
            porTipo.put(tipo.id(), new Cupo(tipo.id(), tipo.codigo(), tipo.nombre(), capacidad, ocupados));
        }

        void ocupar(UUID tipoId) {
            Cupo c = porTipo.get(tipoId);
            porTipo.put(tipoId, new Cupo(c.tipoVehiculoId(), c.codigo(), c.nombre(), c.capacidad(), c.ocupados() + 1));
        }

        void liberar(UUID tipoId) {
            Cupo c = porTipo.get(tipoId);
            porTipo.put(tipoId, new Cupo(c.tipoVehiculoId(), c.codigo(), c.nombre(), c.capacidad(), c.ocupados() - 1));
        }

        @Override
        public List<Cupo> todos(UUID sedeId) {
            return List.copyOf(porTipo.values());
        }

        @Override
        public Optional<Cupo> deTipo(UUID sedeId, UUID tipoVehiculoId) {
            return Optional.ofNullable(porTipo.get(tipoVehiculoId));
        }
    }

    private final ClasificadorPlacas clasificador = mock(ClasificadorPlacas.class);
    private final EventosEntrada eventos = mock(EventosEntrada.class);
    private final EstanciasEntrada estancias = mock(EstanciasEntrada.class);
    private final ApplicationEventPublisher publicador = mock(ApplicationEventPublisher.class);
    private final CuposEnMemoria cupos = new CuposEnMemoria();
    private final TipoVehiculoDetectado carro = new TipoVehiculoDetectado(UUID.randomUUID(), "CARRO", "Carro");
    private final TipoVehiculoDetectado moto = new TipoVehiculoDetectado(UUID.randomUUID(), "MOTO", "Moto");
    private OperacionCarrilEntrada operacion;

    @BeforeEach
    void preparar() {
        UUID sede = UUID.randomUUID();
        SedeActual sedeActual = () -> sede;
        Clock reloj = Clock.fixed(Instant.parse("2026-11-24T15:00:00Z"), ZoneId.of("America/Bogota"));
        PasoEntrada cadena = new ValidarFormatoPaso(clasificador);
        cadena.enlazar(new VerificarConfianzaPaso(() -> new BigDecimal("0.80")))
                .enlazar(new BloquearCupoPaso((s, t) -> { }))
                .enlazar(new VerificarNoAdentroPaso(estancias))
                .enlazar(new VerificarCupoPaso(cupos));
        operacion = new OperacionCarrilEntrada(
                new LectorSimulado(), cadena, eventos, estancias, publicador, sedeActual, reloj);

        when(clasificador.clasificar("ABC123")).thenReturn(Optional.of(carro));
        when(clasificador.clasificar("DEF456")).thenReturn(Optional.of(carro));
        when(clasificador.clasificar("XYZ12D")).thenReturn(Optional.of(moto));
        when(eventos.registrar(any())).thenReturn(UUID.randomUUID());
        // Abrir la estancia ocupa un lugar, igual que contar las estancias abiertas en la base
        when(estancias.abrir(any(), any(), any(), any(), any())).thenAnswer(inv -> {
            cupos.ocupar(inv.getArgument(2));
            return UUID.randomUUID();
        });
    }

    private ResultadoIngreso entra(String placa) {
        return operacion.procesar(new SolicitudIngreso(placa, new BigDecimal("0.95")));
    }

    /** CP-07-01 (éxito): queda 1 cupo de carro; entra un carro → ingresa y los disponibles quedan en 0. */
    @Test
    void elUltimoCupoSeOcupaYQuedaEnCero() {
        cupos.definir(carro, 40, 39);
        cupos.definir(moto, 20, 0);

        ResultadoIngreso resultado = entra("ABC123");

        assertEquals(EstadoIngreso.PERMITIDO, resultado.resultado());
        assertFalse(resultado.lleno());
        assertEquals(0, cupos.deTipo(null, carro.id()).orElseThrow().disponibles());
    }

    /** CP-07-02 (éxito): carros llenos; entra una moto con cupo → la moto ingresa. */
    @Test
    void conLosCarrosLlenosLaMotoIngresa() {
        cupos.definir(carro, 40, 40);
        cupos.definir(moto, 20, 6);

        ResultadoIngreso resultado = entra("XYZ12D");

        assertEquals(EstadoIngreso.PERMITIDO, resultado.resultado());
        assertEquals(13, cupos.deTipo(null, moto.id()).orElseThrow().disponibles(), "la moto ocupó un lugar de los 14 libres");
    }

    /** CP-07-03 (éxito): carros llenos; sale un carro → se libera un cupo y el siguiente puede entrar. */
    @Test
    void alSalirUnCarroSeLiberaSuCupoYEntraElSiguiente() {
        cupos.definir(carro, 40, 40);
        cupos.definir(moto, 20, 0);
        assertTrue(entra("ABC123").lleno(), "con los carros llenos no entra");

        cupos.liberar(carro.id()); // sale un carro: su estancia se cierra y deja de contar

        assertEquals(EstadoIngreso.PERMITIDO, entra("DEF456").resultado());
    }

    /** CP-07-04 (error): carros llenos; entra un carro → LLENO, la barrera no abre y no hay ingreso ni cobro. */
    @Test
    void conLosCarrosLlenosElIngresoSeRechazaComoLleno() {
        cupos.definir(carro, 40, 40);
        cupos.definir(moto, 20, 0);

        ResultadoIngreso resultado = entra("ABC123");

        assertEquals(EstadoIngreso.RECHAZADO, resultado.resultado());
        assertTrue(resultado.lleno());
        assertEquals("LLENO: no hay cupos disponibles para carros", resultado.motivo());
        verify(estancias, never()).abrir(any(), any(), any(), any(), any());
        // Sin IngresoRegistrado nadie genera el cobro (HU-02 escucha ese aviso)
        verify(publicador, never()).publishEvent(any(IngresoRegistrado.class));
    }

    /** CP-07-05 (error): el rechazo por cupo aparece en el registro de eventos. */
    @Test
    void elRechazoPorCupoQuedaEnElRegistroDeEventos() {
        cupos.definir(carro, 40, 40);

        entra("ABC123");

        ArgumentCaptor<EventoEntrada> evento = ArgumentCaptor.forClass(EventoEntrada.class);
        verify(eventos).registrar(evento.capture());
        assertEquals(EstadoIngreso.RECHAZADO, evento.getValue().resultado());
        assertTrue(evento.getValue().motivo().startsWith("LLENO"));
        assertEquals("ABC123", evento.getValue().placa());
    }
}
