package com.uam.parqueadero.salida;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/** Pruebas de la regla RN-06: la barrera abre solo si el cobro del día está pagado o no existe. */
class VerificarDeudaPasoTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    // 14 de noviembre de 2026, 18:15 hora de Colombia → día operativo del 14
    private static final Instant AHORA = Instant.parse("2026-11-14T23:15:00Z");
    private static final LocalDate HOY = LocalDate.of(2026, 11, 14);

    private final CobrosDelDia cobros = mock(CobrosDelDia.class);
    private final VerificarDeudaPaso paso = new VerificarDeudaPaso(cobros, Clock.fixed(AHORA, ZONA));
    private final UUID sede = UUID.randomUUID();

    private ContextoSalida contexto() {
        return new ContextoSalida(new SolicitudSalida("ABC123", BigDecimal.ONE), "ABC123", sede, AHORA);
    }

    private CobroDia cobro(EstadoCobro estado) {
        return new CobroDia(UUID.randomUUID(), estado, new BigDecimal("5000.00"));
    }

    /** CP-04-02 (éxito): carro con cobro Pagado → ABRE. */
    @Test
    void conCobroPagadoAbre() {
        when(cobros.buscar(sede, "ABC123", HOY)).thenReturn(Optional.of(cobro(EstadoCobro.PAGADO)));
        ContextoSalida ctx = contexto();

        paso.ejecutar(ctx);

        assertFalse(ctx.rechazada());
        assertEquals("Cobro del día pagado", ctx.motivoAutorizacion());
    }

    /** CP-04-01 (éxito): una moto no tiene cobro (tarifa $0) → ABRE. */
    @Test
    void sinCobroDelDiaAbre() {
        when(cobros.buscar(sede, "ABC123", HOY)).thenReturn(Optional.empty());
        ContextoSalida ctx = contexto();

        paso.ejecutar(ctx);

        assertFalse(ctx.rechazada());
        assertEquals("Sin cobro del día", ctx.motivoAutorizacion());
    }

    /** CP-04-04 (error): carro con cobro Pendiente → NO ABRE con el motivo y el valor. */
    @Test
    void conCobroPendienteNoAbreYMuestraElValor() {
        when(cobros.buscar(sede, "ABC123", HOY)).thenReturn(Optional.of(cobro(EstadoCobro.PENDIENTE)));
        ContextoSalida ctx = contexto();

        paso.ejecutar(ctx);

        assertTrue(ctx.rechazada());
        assertEquals("Cobro pendiente por $5.000", ctx.motivoRechazo());
        assertEquals(new BigDecimal("5000.00"), ctx.valorPendiente());
    }

    @Test
    void conCobroVencidoNoAbre() {
        when(cobros.buscar(sede, "ABC123", HOY)).thenReturn(Optional.of(cobro(EstadoCobro.VENCIDO)));
        ContextoSalida ctx = contexto();

        paso.ejecutar(ctx);

        assertTrue(ctx.rechazada());
        assertEquals("Cobro vencido por $5.000", ctx.motivoRechazo());
    }

    @Test
    void unCobroAnuladoNoEsDeudaYAbre() {
        when(cobros.buscar(sede, "ABC123", HOY)).thenReturn(Optional.of(cobro(EstadoCobro.ANULADO)));
        ContextoSalida ctx = contexto();

        paso.ejecutar(ctx);

        assertFalse(ctx.rechazada());
    }

    /** Si el vehículo cruzó las 06:00 y el nuevo día no tiene cobro, cuenta el del día en que entró. */
    @Test
    void siCruzoElDiaOperativoRevisaElCobroDelIngreso() {
        LocalDate ayer = HOY.minusDays(1);
        when(cobros.buscar(sede, "ABC123", HOY)).thenReturn(Optional.empty());
        when(cobros.buscar(sede, "ABC123", ayer)).thenReturn(Optional.of(cobro(EstadoCobro.PENDIENTE)));
        ContextoSalida ctx = contexto();
        // Ingresó el 13 de noviembre a las 20:00 (día operativo del 13)
        ctx.estancia(new EstanciaAbierta(UUID.randomUUID(), Instant.parse("2026-11-14T01:00:00Z")));

        paso.ejecutar(ctx);

        assertTrue(ctx.rechazada());
        assertEquals("Cobro pendiente por $5.000", ctx.motivoRechazo());
    }
}
