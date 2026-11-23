package com.uam.parqueadero.comun;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

class DiaOperativoTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    /** Un instante a las 02:00 del 12 de noviembre pertenece al día operativo del 11. */
    @Test
    void laMadrugadaPerteneceAlDiaAnterior() {
        Instant dosAm = Instant.parse("2026-11-12T07:00:00Z"); // 02:00 en Colombia
        assertEquals(LocalDate.of(2026, 11, 11), DiaOperativo.de(dosAm, ZONA));
    }

    @Test
    void alas0559SigueElDiaAnteriorYAlas0600EmpiezaElNuevo() {
        assertEquals(LocalDate.of(2026, 11, 11), DiaOperativo.de(Instant.parse("2026-11-12T10:59:00Z"), ZONA));
        assertEquals(LocalDate.of(2026, 11, 12), DiaOperativo.de(Instant.parse("2026-11-12T11:00:00Z"), ZONA));
    }

    @Test
    void formateaPesosConSeparadorDeMiles() {
        assertEquals("$5.000", Dinero.formatear(new BigDecimal("5000.00")));
        assertEquals("$1.250.000", Dinero.formatear(new BigDecimal("1250000")));
    }

    /** El día operativo termina a las 05:59:59 del día calendario siguiente (RN-03). */
    @Test
    void elDiaOperativoTerminaALas0559DelDiaSiguiente() {
        Instant fin = DiaOperativo.fin(LocalDate.of(2026, 11, 14), ZONA);

        assertEquals(Instant.parse("2026-11-15T10:59:59Z"), fin); // 05:59:59 en Colombia
        assertEquals(LocalDate.of(2026, 11, 14), DiaOperativo.de(fin, ZONA));
    }
}
