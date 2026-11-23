package com.uam.parqueadero.comun;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Día operativo del parqueadero: va de las 06:00 a las 05:59 del día siguiente (S-02).
 * Un instante a las 02:00 del 12 de noviembre pertenece al día operativo del 11.
 */
public final class DiaOperativo {

    private static final int HORA_INICIO = 6;
    private static final LocalTime ULTIMO_INSTANTE = LocalTime.of(5, 59, 59);

    private DiaOperativo() {
    }

    /** @return la fecha del día operativo al que pertenece el instante en la zona dada */
    public static LocalDate de(Instant instante, ZoneId zona) {
        return instante.atZone(zona).minusHours(HORA_INICIO).toLocalDate();
    }

    /**
     * @return el último instante del día operativo: las 05:59:59 del día calendario siguiente.
     *         Es hasta cuando vale un pase pagado (RN-03).
     */
    public static Instant fin(LocalDate diaOperativo, ZoneId zona) {
        return diaOperativo.plusDays(1).atTime(ULTIMO_INSTANTE).atZone(zona).toInstant();
    }
}
