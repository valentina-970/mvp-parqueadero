package com.uam.parqueadero.pago;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.uam.parqueadero.comun.SedeActual;

/** Pruebas de la búsqueda del cobro en la caja (HU-03, CA-1 y CA-8). */
class CajaServicioTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    // 14 de noviembre de 2026, 10:15 hora de Colombia → día operativo del 14
    private static final Instant AHORA = Instant.parse("2026-11-14T15:15:00Z");
    private static final LocalDate HOY = LocalDate.of(2026, 11, 14);

    private final BuscadorDeCobros buscador = mock(BuscadorDeCobros.class);
    private final UUID sede = UUID.randomUUID();
    private final SedeActual sedeActual = () -> sede;
    private final CajaServicio caja = new CajaServicio(buscador, sedeActual, Clock.fixed(AHORA, ZONA));

    private CobroPorPagar cobro(String estado) {
        return new CobroPorPagar(UUID.randomUUID(), "ABC123", "Carro", HOY, "DIURNA",
                new BigDecimal("5000.00"), estado);
    }

    /** CA-1: el operador busca por placa el cobro del día operativo. */
    @Test
    void encuentraElCobroDelDiaOperativoPorPlaca() {
        CobroPorPagar esperado = cobro("PENDIENTE");
        when(buscador.buscar(sede, "ABC123", HOY)).thenReturn(Optional.of(esperado));

        assertEquals(esperado, caja.consultar("ABC123"));
    }

    /** La placa se normaliza antes de buscar: mayúsculas, sin espacios ni guiones. */
    @Test
    void normalizaLaPlacaAntesDeBuscar() {
        when(buscador.buscar(sede, "ABC123", HOY)).thenReturn(Optional.of(cobro("PENDIENTE")));

        caja.consultar(" abc-123 ");

        verify(buscador).buscar(sede, "ABC123", HOY);
    }

    /** CP-03-06 (error): placa sin cobro pendiente → "No hay cobro pendiente para esta placa". */
    @Test
    void sinCobroLanzaElMensajeDeNoHayCobroPendiente() {
        when(buscador.buscar(sede, "ZZZ999", HOY)).thenReturn(Optional.empty());

        CobroNoEncontradoException e = assertThrows(CobroNoEncontradoException.class, () -> caja.consultar("ZZZ999"));

        assertEquals("No hay cobro pendiente para esta placa", e.getMessage());
    }

    @Test
    void laTarifaAplicadaSeMuestraConTipoFranjaYValor() {
        assertEquals("Carro diurna · $5.000", cobro("PENDIENTE").tarifaAplicada());
    }
}
