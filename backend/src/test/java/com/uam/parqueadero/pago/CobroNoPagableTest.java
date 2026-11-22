package com.uam.parqueadero.pago;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/** Pruebas de los estados desde los que no se puede pagar: ANULADO y VENCIDO (HU-03, CA-7). */
class CobroNoPagableTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final DatosPago DATOS =
            new DatosPago(new BigDecimal("5000"), UUID.randomUUID(), "operador1",
                    Instant.parse("2026-11-14T15:15:00Z"), ZONA);

    private CobroPorPagar cobro(String estado) {
        return new CobroPorPagar(UUID.randomUUID(), "ABC123", "Carro", LocalDate.of(2026, 11, 14), "DIURNA",
                new BigDecimal("5000.00"), estado);
    }

    /** CP-03-05 (error): cobro Anulado → pago rechazado con el motivo. */
    @Test
    void unCobroAnuladoNoSePuedePagarYMuestraElMotivo() {
        CobroPorPagar cobro = cobro("ANULADO");

        CobroNoPagableException e = assertThrows(CobroNoPagableException.class, () -> cobro.pagar(DATOS));

        assertEquals("El cobro está anulado y no se puede pagar", e.getMessage());
        assertEquals("ANULADO", cobro.estado());
    }

    @Test
    void unCobroVencidoNoSePuedePagarYMuestraElMotivo() {
        CobroPorPagar cobro = cobro("VENCIDO");

        CobroNoPagableException e = assertThrows(CobroNoPagableException.class, () -> cobro.pagar(DATOS));

        assertEquals("El cobro está vencido: terminó su día operativo sin pagarse", e.getMessage());
        assertEquals("VENCIDO", cobro.estado());
    }

    @Test
    void unEstadoDesconocidoSeRechazaAlCargarElCobro() {
        assertThrows(IllegalArgumentException.class, () -> cobro("OTRO"));
    }
}
