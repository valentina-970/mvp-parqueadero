package com.uam.parqueadero.cupo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

/** Pruebas del modelo de cupo (HU-07, CA-1, CA-4 y CA-6). */
class CupoTest {

    private Cupo carros(int capacidad, int ocupados) {
        return new Cupo(UUID.randomUUID(), "CARRO", "Carro", capacidad, ocupados);
    }

    /** CA-6: la pantalla muestra los cupos disponibles. */
    @Test
    void losDisponiblesSonLaCapacidadMenosLosOcupados() {
        Cupo cupo = carros(40, 28);

        assertEquals(12, cupo.disponibles());
        assertFalse(cupo.lleno());
    }

    /** CP-07-01: queda 1 cupo y entra un carro → los disponibles quedan en 0 y el tipo se llena. */
    @Test
    void alOcuparseElUltimoLugarElCupoQuedaLleno() {
        Cupo antes = carros(40, 39);
        Cupo despues = carros(40, 40);

        assertEquals(1, antes.disponibles());
        assertFalse(antes.lleno());
        assertEquals(0, despues.disponibles());
        assertTrue(despues.lleno());
    }

    @Test
    void siSeBajoLaCapacidadPorDebajoDeLosOcupadosNoHayDisponiblesNegativos() {
        Cupo cupo = carros(2, 3);

        assertEquals(0, cupo.disponibles());
        assertTrue(cupo.lleno());
    }

    /** CA-4: cada tipo tiene su propio cupo, así que uno lleno no afecta al otro. */
    @Test
    void cadaTipoTieneSuPropioCupo() {
        Cupo carrosLlenos = carros(40, 40);
        Cupo motos = new Cupo(UUID.randomUUID(), "MOTO", "Moto", 20, 6);

        assertTrue(carrosLlenos.lleno());
        assertFalse(motos.lleno());
        assertEquals(14, motos.disponibles());
    }
}
