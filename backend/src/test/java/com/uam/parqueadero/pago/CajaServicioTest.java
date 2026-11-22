package com.uam.parqueadero.pago;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.uam.parqueadero.comun.SedeActual;

/** Pruebas de la caja: búsqueda del cobro (CA-1 y CA-8) y registro del pago (CA-2 a CA-5). */
class CajaServicioTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    // 14 de noviembre de 2026, 10:15 hora de Colombia → día operativo del 14
    private static final Instant AHORA = Instant.parse("2026-11-14T15:15:00Z");
    private static final LocalDate HOY = LocalDate.of(2026, 11, 14);

    private final BuscadorDeCobros buscador = mock(BuscadorDeCobros.class);
    private final RegistroPagos pagos = mock(RegistroPagos.class);
    private final OperadoresCaja operadores = mock(OperadoresCaja.class);
    private final UUID sede = UUID.randomUUID();
    private final UUID operadorId = UUID.randomUUID();
    private final SedeActual sedeActual = () -> sede;
    private final CajaServicio caja =
            new CajaServicio(buscador, pagos, operadores, sedeActual, Clock.fixed(AHORA, ZONA));

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

    /** CP-03-01 (éxito): el pago se guarda con el operador de la sesión y el cobro queda pagado. */
    @Test
    void pagarGuardaElPagoConElOperadorDeLaSesion() {
        when(buscador.buscar(sede, "ABC123", HOY)).thenReturn(Optional.of(cobro("PENDIENTE")));
        when(operadores.idDe("operador1")).thenReturn(operadorId);

        PagoDeCaja resultado = caja.pagar("abc-123", new BigDecimal("10000"), "operador1");

        ArgumentCaptor<Pago> guardado = ArgumentCaptor.forClass(Pago.class);
        verify(pagos).registrar(guardado.capture());
        assertEquals(operadorId, guardado.getValue().operadorId());
        assertEquals(AHORA, guardado.getValue().pagadoEn());
        assertEquals(0, new BigDecimal("5000").compareTo(guardado.getValue().cambio()));
        assertEquals("ABC123", resultado.placa());
    }

    /** CP-03-04 (error): con efectivo insuficiente no se guarda ningún pago. */
    @Test
    void conEfectivoInsuficienteNoSeGuardaNada() {
        when(buscador.buscar(sede, "ABC123", HOY)).thenReturn(Optional.of(cobro("PENDIENTE")));
        when(operadores.idDe("operador1")).thenReturn(operadorId);

        assertThrows(ValorInsuficienteException.class,
                () -> caja.pagar("ABC123", new BigDecimal("3000"), "operador1"));

        verify(pagos, never()).registrar(any());
    }

    /** CP-03-06 (error): pagar una placa sin cobro responde que no hay cobro pendiente. */
    @Test
    void pagarUnaPlacaSinCobroLoInforma() {
        when(buscador.buscar(sede, "ZZZ999", HOY)).thenReturn(Optional.empty());

        assertThrows(CobroNoEncontradoException.class,
                () -> caja.pagar("ZZZ999", new BigDecimal("5000"), "operador1"));

        verify(pagos, never()).registrar(any());
    }
}
