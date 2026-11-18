package com.uam.parqueadero.salida;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Pruebas del mecanismo de la cadena: orden de los pasos y corte al rechazar. */
class PasoSalidaTest {

    /** Paso de prueba que anota su nombre y puede rechazar. */
    private static class PasoAnotador extends PasoSalida {
        private final String nombre;
        private final List<String> orden;
        private final boolean rechaza;

        PasoAnotador(String nombre, List<String> orden, boolean rechaza) {
            this.nombre = nombre;
            this.orden = orden;
            this.rechaza = rechaza;
        }

        @Override
        protected void aplicar(ContextoSalida contexto) {
            orden.add(nombre);
            if (rechaza) {
                contexto.rechazar("rechazado por " + nombre);
            }
        }
    }

    private ContextoSalida contexto() {
        return new ContextoSalida(new SolicitudSalida("ABC123", BigDecimal.ONE), "ABC123", Instant.now());
    }

    @Test
    void ejecutaLosPasosEnElOrdenEnlazado() {
        List<String> orden = new ArrayList<>();
        PasoSalida primero = new PasoAnotador("uno", orden, false);
        primero.enlazar(new PasoAnotador("dos", orden, false)).enlazar(new PasoAnotador("tres", orden, false));

        primero.ejecutar(contexto());

        assertEquals(List.of("uno", "dos", "tres"), orden);
    }

    @Test
    void siUnPasoRechazaLaCadenaSeDetiene() {
        List<String> orden = new ArrayList<>();
        PasoSalida primero = new PasoAnotador("uno", orden, false);
        primero.enlazar(new PasoAnotador("dos", orden, true)).enlazar(new PasoAnotador("tres", orden, false));
        ContextoSalida ctx = contexto();

        primero.ejecutar(ctx);

        assertEquals(List.of("uno", "dos"), orden);
        assertTrue(ctx.rechazada());
        assertEquals("rechazado por dos", ctx.motivoRechazo());
    }
}
