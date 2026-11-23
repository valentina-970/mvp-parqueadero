package com.uam.parqueadero.cobro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.uam.parqueadero.entrada.IngresoRegistrado;

/**
 * Pruebas del generador con la cadena real y los puertos simulados. Verifican cuándo se
 * crea el cobro y con qué datos, sin base de datos.
 */
class GeneradorCobroTest {

    private static final Instant DIEZ_AM = Instant.parse("2026-11-12T15:00:00Z"); // 10:00 en Colombia
    private static final LocalDate DIA = LocalDate.of(2026, 11, 12);

    private final RegistroCobros cobros = mock(RegistroCobros.class);
    private final PoliticaTarifa politica = mock(PoliticaTarifa.class);
    private final UUID sede = UUID.randomUUID();
    private final UUID carro = UUID.randomUUID();
    private final UUID moto = UUID.randomUUID();
    private final UUID eventoId = UUID.randomUUID();
    private final UUID cobroId = UUID.randomUUID();
    private final TarifaAplicada diurnaCarro =
            new TarifaAplicada(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("5000"));
    private GeneradorCobro generador;

    @BeforeEach
    void preparar() {
        PasoCobro cadena = new VerificarCobroDelDiaPaso(cobros);
        cadena.enlazar(new AplicarTarifaPaso(politica)).enlazar(new VerificarTarifaCeroPaso());
        generador = new GeneradorCobro(cadena, cobros);

        when(politica.calcular(sede, carro, DIEZ_AM)).thenReturn(Optional.of(diurnaCarro));
        when(politica.calcular(sede, moto, DIEZ_AM)).thenReturn(Optional.of(
                new TarifaAplicada(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("0.00"))));
        when(cobros.crear(any())).thenReturn(cobroId);
    }

    private IngresoRegistrado ingreso(String placa, UUID tipo) {
        return new IngresoRegistrado(UUID.randomUUID(), eventoId, sede, placa, tipo, DIEZ_AM, DIA);
    }

    /** CP-02-01 y CA-7 (éxito): el primer ingreso del día crea el cobro con su tarifa y su valor. */
    @Test
    void elPrimerIngresoDelDiaCreaElCobro() {
        Optional<UUID> resultado = generador.generar(ingreso("ABC123", carro));

        assertEquals(Optional.of(cobroId), resultado);
        ArgumentCaptor<NuevoCobro> cobro = ArgumentCaptor.forClass(NuevoCobro.class);
        verify(cobros).crear(cobro.capture());
        assertEquals("ABC123", cobro.getValue().placa());
        assertEquals(sede, cobro.getValue().sedeId());
        assertEquals(new BigDecimal("5000"), cobro.getValue().valor());
        assertEquals(diurnaCarro.tarifaId(), cobro.getValue().tarifaId());
        assertEquals(diurnaCarro.franjaId(), cobro.getValue().franjaId());
        assertEquals(eventoId, cobro.getValue().ingresoEventoId());
    }

    /** CA-2: el cobro usa el día operativo del ingreso, no la fecha del calendario. */
    @Test
    void elCobroQuedaEnElDiaOperativoDelIngreso() {
        generador.generar(ingreso("ABC123", carro));

        ArgumentCaptor<NuevoCobro> cobro = ArgumentCaptor.forClass(NuevoCobro.class);
        verify(cobros).crear(cobro.capture());
        assertEquals(DIA, cobro.getValue().diaOperativo());
    }

    /** CP-02-04 (éxito): una moto con tarifa $0 no genera cobro. */
    @Test
    void unaMotoConTarifaCeroNoGeneraCobro() {
        Optional<UUID> resultado = generador.generar(ingreso("XYZ12D", moto));

        assertTrue(resultado.isEmpty());
        verify(cobros, never()).crear(any());
    }

    /** CP-02-05 (éxito): si ya hay cobro del día, volver a entrar no crea otro. */
    @Test
    void unSegundoIngresoElMismoDiaNoCreaOtroCobro() {
        when(cobros.existe(sede, "ABC123", DIA)).thenReturn(true);

        Optional<UUID> resultado = generador.generar(ingreso("ABC123", carro));

        assertTrue(resultado.isEmpty());
        verify(cobros, never()).crear(any());
        verify(politica, never()).calcular(any(), any(), any());
    }

    /** Sin tarifa configurada no se crea cobro (y el ingreso no falla). */
    @Test
    void sinTarifaNoSeCreaCobro() {
        when(politica.calcular(sede, carro, DIEZ_AM)).thenReturn(Optional.empty());

        assertTrue(generador.generar(ingreso("ABC123", carro)).isEmpty());
        verify(cobros, never()).crear(any());
    }
}