package com.uam.parqueadero.salida;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/** Pruebas del paso que verifica el ingreso registrado (HU-04, CA-5). */
class VerificarIngresoPasoTest {

    private final Estancias estancias = mock(Estancias.class);
    private final VerificarIngresoPaso paso = new VerificarIngresoPaso(estancias);
    private final UUID sede = UUID.randomUUID();

    private ContextoSalida contexto() {
        return new ContextoSalida(new SolicitudSalida("ABC123", BigDecimal.ONE), "ABC123", sede, Instant.now());
    }

    /** CP-04-05 (error): placa sin ingreso registrado → NO ABRE con "Sin ingreso registrado". */
    @Test
    void sinIngresoRegistradoSeRechaza() {
        when(estancias.buscarAbierta(sede, "ABC123")).thenReturn(Optional.empty());
        ContextoSalida ctx = contexto();

        paso.ejecutar(ctx);

        assertTrue(ctx.rechazada());
        assertEquals("Sin ingreso registrado", ctx.motivoRechazo());
    }

    @Test
    void conIngresoRegistradoGuardaLaEstanciaYContinua() {
        EstanciaAbierta estancia = new EstanciaAbierta(UUID.randomUUID(), Instant.now());
        when(estancias.buscarAbierta(sede, "ABC123")).thenReturn(Optional.of(estancia));
        ContextoSalida ctx = contexto();

        paso.ejecutar(ctx);

        assertFalse(ctx.rechazada());
        assertEquals(estancia, ctx.estancia());
    }
}
