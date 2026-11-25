package com.uam.parqueadero.entrada;

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

import com.uam.parqueadero.comun.TipoVehiculoDetectado;
import com.uam.parqueadero.cupo.Cupo;
import com.uam.parqueadero.cupo.CuposDeLaSede;

/** Pruebas del paso que verifica el cupo antes de abrir la barrera (HU-07, CA-2, CA-3 y CA-4). */
class VerificarCupoPasoTest {

    private final CuposDeLaSede cupos = mock(CuposDeLaSede.class);
    private final VerificarCupoPaso paso = new VerificarCupoPaso(cupos);
    private final UUID sede = UUID.randomUUID();
    private final TipoVehiculoDetectado carro = new TipoVehiculoDetectado(UUID.randomUUID(), "CARRO", "Carro");

    private ContextoEntrada contexto() {
        ContextoEntrada ctx = new ContextoEntrada(
                new LecturaPlaca("ABC123", "ABC123", new BigDecimal("0.95")), sede, Instant.now());
        ctx.tipo(carro);
        return ctx;
    }

    @Test
    void conLugarDisponibleDejaPasarElIngreso() {
        when(cupos.deTipo(sede, carro.id())).thenReturn(Optional.of(new Cupo(carro.id(), "CARRO", "Carro", 40, 12)));
        ContextoEntrada ctx = contexto();

        paso.ejecutar(ctx);

        assertFalse(ctx.decidido());
        assertEquals(EstadoIngreso.PERMITIDO, ctx.estado());
        assertFalse(ctx.lleno());
    }

    /** CA-3: sin cupo → LLENO, la barrera no abre. */
    @Test
    void sinCupoRechazaElIngresoComoLleno() {
        when(cupos.deTipo(sede, carro.id())).thenReturn(Optional.of(new Cupo(carro.id(), "CARRO", "Carro", 40, 40)));
        ContextoEntrada ctx = contexto();

        paso.ejecutar(ctx);

        assertTrue(ctx.decidido());
        assertEquals(EstadoIngreso.RECHAZADO, ctx.estado());
        assertTrue(ctx.lleno());
        assertEquals("LLENO: no hay cupos disponibles para carros", ctx.motivo());
    }

    /** Un tipo al que la sede no le definió cupo no se admite hasta que el administrador le dé uno. */
    @Test
    void unTipoSinCupoDefinidoSeTrataComoLleno() {
        when(cupos.deTipo(sede, carro.id())).thenReturn(Optional.empty());
        ContextoEntrada ctx = contexto();

        paso.ejecutar(ctx);

        assertTrue(ctx.lleno());
        assertEquals(EstadoIngreso.RECHAZADO, ctx.estado());
    }

    /** CA-4: la pregunta es siempre por el tipo del vehículo que entra, no por los demás. */
    @Test
    void consultaElCupoDelTipoDelVehiculoQueEntra() {
        TipoVehiculoDetectado moto = new TipoVehiculoDetectado(UUID.randomUUID(), "MOTO", "Moto");
        when(cupos.deTipo(sede, carro.id())).thenReturn(Optional.of(new Cupo(carro.id(), "CARRO", "Carro", 40, 40)));
        when(cupos.deTipo(sede, moto.id())).thenReturn(Optional.of(new Cupo(moto.id(), "MOTO", "Moto", 20, 6)));
        ContextoEntrada deMoto = new ContextoEntrada(
                new LecturaPlaca("XYZ12D", "XYZ12D", new BigDecimal("0.95")), sede, Instant.now());
        deMoto.tipo(moto);

        paso.ejecutar(deMoto);

        assertFalse(deMoto.lleno(), "los carros llenos no afectan a las motos");
    }
}
