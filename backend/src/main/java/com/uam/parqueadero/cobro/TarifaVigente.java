package com.uam.parqueadero.cobro;

import java.math.BigDecimal;
import java.util.UUID;
import java.time.LocalTime;

/**
 * Una tarifa vigente junto con el horario de su franja, tal como sale de la base de datos.
 *
 * @param horaInicio  primera hora de la franja (por ejemplo 06:00)
 * @param horaFin     última hora de la franja, incluida (por ejemplo 17:59)
 */

public record TarifaVigente(
    UUID tarifaId,
    UUID franjaId,
    LocalTime horaInicio,
    LocalTime horaFin,
    BigDecimal valor
) {

    /**
     * Indica si la hora cae dentro de la franja. Si la franja cruza la medianoche
     * (18:00 a 05:59), la hora cuenta si es posterior al inicio o anterior al fin.
     *
     * @param hora  hora en minutos exactos (sin segundos)
     */
    
    public boolean cubre(LocalTime hora){
        if (!horaInicio.isAfter(horaFin)){
            return !hora.isBefore(horaInicio) && !hora.isAfter(horaFin);
        }
        return !hora.isBefore(horaInicio) || !hora.isAfter(horaFin);
    }


}
