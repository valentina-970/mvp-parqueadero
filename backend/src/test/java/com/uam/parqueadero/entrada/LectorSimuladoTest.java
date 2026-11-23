package com.uam.parqueadero.entrada;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.uam.parqueadero.entrada.infraestructura.LectorSimulado;

/** Pruebas del adaptador del simulador (CA-3). */
class LectorSimuladoTest {

    private final LectorPlacas lector = new LectorSimulado();

    @Test
    void normalizaLaPlacaYConservaLaOriginal() {
        LecturaPlaca lectura = lector.leer(new SolicitudIngreso(" xyz-12d ", new BigDecimal("0.90")));

        assertEquals("XYZ12D", lectura.placa());
        assertEquals(" xyz-12d ", lectura.placaLeida());
        assertEquals(new BigDecimal("0.90"), lectura.confianza());
    }
}