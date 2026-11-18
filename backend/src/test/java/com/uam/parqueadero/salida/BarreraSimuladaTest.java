package com.uam.parqueadero.salida;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.uam.parqueadero.salida.infraestructura.BarreraSimulada;

class BarreraSimuladaTest {

    private final Barrera barrera = new BarreraSimulada();

    @Test
    void empiezaCerrada() {
        assertEquals(EstadoBarrera.CERRADA, barrera.estado());
    }

    @Test
    void abreYCierra() {
        barrera.abrir();
        assertEquals(EstadoBarrera.ABIERTA, barrera.estado());

        barrera.cerrar();
        assertEquals(EstadoBarrera.CERRADA, barrera.estado());
    }
}
