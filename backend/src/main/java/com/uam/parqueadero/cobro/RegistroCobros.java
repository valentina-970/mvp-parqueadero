package com.uam.parqueadero.cobro;

import java.time.LocalDate;
import java.util.UUID;

/** Puerto de los cobros desde el punto de vista de su generación. */

public interface RegistroCobros {

    /** {@code true} si la placa ya tiene cobro en ese día operativo y sede (SG-01). */

    boolean existe(UUID sedeId, String placa, LocalDate diaOperativo);

    /**
     * Crea el cobro en estado Pendiente.
     *
     * @return el id del cobro creado
     */
    
    UUID crear(NuevoCobro cobro);

}
