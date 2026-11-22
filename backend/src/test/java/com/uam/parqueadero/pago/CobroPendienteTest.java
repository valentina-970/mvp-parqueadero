package com.uam.parqueadero.pago;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/** Pruebas del estado PENDIENTE: pagar con cambio, pase vigente y valor insuficiente (HU-03). */
class CobroPendienteTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final Instant AHORA = Instant.parse("2026-11-14T15:15:00Z"); // 10:15 en Colombia
    private static final LocalDate DIA = LocalDate.of(2026, 11, 14);
    private static final UUID OPERADOR = UUID.randomUUID();

    private CobroPorPagar cobroPendiente() {
        return new CobroPorPagar(UUID.randomUUID(), "ABC123", "Carro", DIA, "DIURNA",
                new BigDecimal("5000.00"), "PENDIENTE");
    }

    private DatosPago datos(String recibido) {
        return new DatosPago(new BigDecimal(recibido), OPERADOR, "operador1", AHORA, ZONA);
    }

    /** CP-03-01 (éxito): cobro de $5.000 y se reciben $10.000 → pagado, cambio de $5.000 y pase hasta las 05:59. */
    @Test
    void pagarConMasEfectivoDaCambioYDejaElPase() {
        CobroPorPagar cobro = cobroPendiente();

        ResultadoPago resultado = cobro.pagar(datos("10000"));

        assertTrue(resultado.nuevo());
        Pago pago = resultado.pago();
        assertEquals(0, new BigDecimal("5000").compareTo(pago.cambio()));
        assertEquals(0, new BigDecimal("5000").compareTo(pago.valor()));
        assertEquals(0, new BigDecimal("10000").compareTo(pago.valorRecibido()));
        assertEquals("PAGADO", cobro.estado());
        // El pase vale hasta las 05:59:59 del día siguiente al día operativo (RN-03)
        assertEquals(Instant.parse("2026-11-15T10:59:59Z"), pago.paseVigenteHasta());
    }

    /** CA-3: el pago guarda el valor, la fecha y hora y el operador que lo recibió. */
    @Test
    void elPagoGuardaLaHoraYElOperador() {
        Pago pago = cobroPendiente().pagar(datos("5000")).pago();

        assertEquals(AHORA, pago.pagadoEn());
        assertEquals(OPERADOR, pago.operadorId());
        assertEquals("operador1", pago.operadorNombre());
    }

    @Test
    void conElValorExactoNoHayCambio() {
        Pago pago = cobroPendiente().pagar(datos("5000")).pago();

        assertEquals(0, BigDecimal.ZERO.compareTo(pago.cambio()));
    }

    /** CP-03-04 (error): cobro de $5.000 y se reciben $3.000 → rechazado por valor insuficiente. */
    @Test
    void conValorInsuficienteSeRechazaYSigueEnPendiente() {
        CobroPorPagar cobro = cobroPendiente();

        ValorInsuficienteException e =
                assertThrows(ValorInsuficienteException.class, () -> cobro.pagar(datos("3000")));

        assertEquals("Valor recibido insuficiente: faltan $2.000", e.getMessage());
        assertEquals("PENDIENTE", cobro.estado());
    }
}
