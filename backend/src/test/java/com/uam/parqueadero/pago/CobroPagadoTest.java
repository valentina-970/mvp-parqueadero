package com.uam.parqueadero.pago;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/** Pruebas del estado PAGADO: el pago es idempotente (HU-03, CA-6). */
class CobroPagadoTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final Instant AHORA = Instant.parse("2026-11-14T15:15:00Z");
    private static final LocalDate DIA = LocalDate.of(2026, 11, 14);

    private DatosPago datos(String recibido, Instant cuando) {
        return new DatosPago(new BigDecimal(recibido), UUID.randomUUID(), "operador2", cuando, ZONA);
    }

    private CobroPorPagar cobroPendiente() {
        return new CobroPorPagar(UUID.randomUUID(), "ABC123", "Carro", DIA, "DIURNA",
                new BigDecimal("5000.00"), "PENDIENTE");
    }

    /** CP-03-02 (éxito): pagar dos veces el mismo cobro devuelve el mismo pago; no hay duplicado. */
    @Test
    void pagarDosVecesDevuelveElMismoPago() {
        CobroPorPagar cobro = cobroPendiente();

        ResultadoPago primero = cobro.pagar(datos("10000", AHORA));
        ResultadoPago segundo = cobro.pagar(datos("20000", AHORA.plusSeconds(30)));

        assertTrue(primero.nuevo());
        assertFalse(segundo.nuevo());
        assertSame(primero.pago(), segundo.pago());
        assertEquals("PAGADO", cobro.estado());
    }

    /** Un cobro cargado de la base ya pagado devuelve el pago con el que se cargó. */
    @Test
    void unCobroCargadoYaPagadoDevuelveSuPago() {
        Pago existente = new Pago(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("5000.00"),
                new BigDecimal("5000"), BigDecimal.ZERO, UUID.randomUUID(), "operador1",
                AHORA, Instant.parse("2026-11-15T10:59:59Z"));
        CobroPorPagar cobro = new CobroPorPagar(existente.cobroId(), "ABC123", "Carro", DIA, "DIURNA",
                new BigDecimal("5000.00"), "PAGADO").conPago(existente);

        ResultadoPago resultado = cobro.pagar(datos("5000", AHORA.plusSeconds(5)));

        assertSame(existente, resultado.pago());
        assertFalse(resultado.nuevo());
    }

    @Test
    void unCobroPagadoSinPagoEsUnaInconsistencia() {
        CobroPorPagar cobro = new CobroPorPagar(UUID.randomUUID(), "ABC123", "Carro", DIA, "DIURNA",
                new BigDecimal("5000.00"), "PAGADO");

        assertThrows(IllegalStateException.class, () -> cobro.pagar(datos("5000", AHORA)));
    }
}
