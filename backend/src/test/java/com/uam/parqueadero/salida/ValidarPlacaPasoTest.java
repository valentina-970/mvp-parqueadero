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

/** Pruebas del primer paso de la cadena: formato y clasificación de la placa. */
class ValidarPlacaPasoTest {

    private final ClasificadorPlacas clasificador = mock(ClasificadorPlacas.class);
    private final ValidarPlacaPaso paso = new ValidarPlacaPaso(clasificador);

    private ContextoSalida contexto(String placa) {
        return new ContextoSalida(new SolicitudSalida(placa, new BigDecimal("0.95")), placa, UUID.randomUUID(), Instant.now());
    }

    @Test
    void unaPlacaValidaQuedaClasificada() {
        TipoVehiculoDetectado carro = new TipoVehiculoDetectado(UUID.randomUUID(), "CARRO", "Carro");
        when(clasificador.clasificar("ABC123")).thenReturn(Optional.of(carro));
        ContextoSalida ctx = contexto("ABC123");

        paso.ejecutar(ctx);

        assertFalse(ctx.rechazada());
        assertEquals(carro, ctx.tipo());
    }

    @Test
    void unaPlacaConFormatoInvalidoSeRechaza() {
        when(clasificador.clasificar("AB1234")).thenReturn(Optional.empty());
        ContextoSalida ctx = contexto("AB1234");

        paso.ejecutar(ctx);

        assertTrue(ctx.rechazada());
        assertEquals("Formato de placa no válido", ctx.motivoRechazo());
    }

    @Test
    void unaPlacaVaciaSeRechaza() {
        when(clasificador.clasificar("")).thenReturn(Optional.empty());
        ContextoSalida ctx = contexto("");

        paso.ejecutar(ctx);

        assertTrue(ctx.rechazada());
    }
}
