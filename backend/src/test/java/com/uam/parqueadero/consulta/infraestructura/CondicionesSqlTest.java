package com.uam.parqueadero.consulta.infraestructura;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Pruebas del armado del WHERE con filtros opcionales (HU-09, CA-3 y CA-4). */
class CondicionesSqlTest {

    @Test
    void sinFiltrosNoHayWhere() {
        CondicionesSql condiciones = new CondicionesSql().siHay(null, "c.placa = ?");

        assertEquals("", condiciones.where());
        assertArrayEquals(new Object[] {}, condiciones.valores());
    }

    /** CP-09-03: los filtros que llegan se combinan con AND, en orden y con sus valores. */
    @Test
    void combinaLosFiltrosQueLleganConAnd() {
        CondicionesSql condiciones = new CondicionesSql()
                .siHay("ABC123", "c.placa = ?")
                .siHay(null, "c.dia_operativo >= ?")
                .siHay("PENDIENTE", "c.estado = ?");

        assertEquals(" WHERE c.placa = ? AND c.estado = ?", condiciones.where());
        assertArrayEquals(new Object[] {"ABC123", "PENDIENTE"}, condiciones.valores());
    }

    @Test
    void agregaLaPaginacionAlFinalDeLosValores() {
        CondicionesSql condiciones = new CondicionesSql().siHay("ABC123", "c.placa = ?");

        assertArrayEquals(new Object[] {"ABC123", 20, 40}, condiciones.valores(20, 40));
    }
}