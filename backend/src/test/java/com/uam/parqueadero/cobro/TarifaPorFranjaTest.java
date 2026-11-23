package com.uam.parqueadero.cobro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Pruebas de la estrategia de tarifas con las franjas por defecto (SG-03): diurna de
 * 06:00 a 17:59 y nocturna de 18:00 a 05:59. No usa base de datos.
 */
class TarifaPorFranjaTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private final UUID sede = UUID.randomUUID();
    private final UUID carro = UUID.randomUUID();
    private final TarifaVigente diurna = new TarifaVigente(UUID.randomUUID(), UUID.randomUUID(),
            LocalTime.of(6, 0), LocalTime.of(17, 59), new BigDecimal("5000"));
    private final TarifaVigente nocturna = new TarifaVigente(UUID.randomUUID(), UUID.randomUUID(),
            LocalTime.of(18, 0), LocalTime.of(5, 59), new BigDecimal("3000"));

    private final PoliticaTarifa politica =
            new TarifaPorFranja((sedeId, tipo, momento) -> List.of(diurna, nocturna), Clock.system(ZONA));

    /** Instante de una hora local en Colombia. */
    private static Instant a(int hora, int minuto, int segundo) {
        return ZonedDateTime.of(2026, 11, 12, hora, minuto, segundo, 0, ZONA).toInstant();
    }

    private BigDecimal valorA(int hora, int minuto, int segundo) {
        return politica.calcular(sede, carro, a(hora, minuto, segundo)).orElseThrow().valor();
    }

    /** CP-02-01 (éxito): carro a las 10:00 → tarifa diurna. */
    @Test
    void aLasDiezAplicaLaTarifaDiurna() {
        TarifaAplicada tarifa = politica.calcular(sede, carro, a(10, 0, 0)).orElseThrow();

        assertEquals(diurna.tarifaId(), tarifa.tarifaId());
        assertEquals(diurna.franjaId(), tarifa.franjaId());
        assertEquals(new BigDecimal("5000"), tarifa.valor());
    }

    /** CP-02-02 (éxito): carro a las 20:00 → tarifa nocturna. */
    @Test
    void aLasOchoDeLaNocheAplicaLaTarifaNocturna() {
        assertEquals(new BigDecimal("3000"), valorA(20, 0, 0));
    }

    /** CP-02-03 y SG-03: los bordes de cada franja, incluidos los segundos dentro del último minuto. */
    @Test
    void losBordesDeLasFranjasSeRespetan() {
        assertEquals(new BigDecimal("3000"), valorA(5, 59, 30));
        assertEquals(new BigDecimal("5000"), valorA(6, 0, 0));
        assertEquals(new BigDecimal("5000"), valorA(17, 59, 59));
        assertEquals(new BigDecimal("3000"), valorA(18, 0, 0));
        assertEquals(new BigDecimal("3000"), valorA(0, 0, 0));
    }

    @Test
    void sinTarifasConfiguradasNoHayTarifa() {
        PoliticaTarifa sinTarifas = new TarifaPorFranja((sedeId, tipo, momento) -> List.of(), Clock.system(ZONA));

        assertTrue(sinTarifas.calcular(sede, carro, a(10, 0, 0)).isEmpty());
    }
}
