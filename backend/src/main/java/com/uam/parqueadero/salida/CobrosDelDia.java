package com.uam.parqueadero.salida;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de solo lectura con el cobro de un vehículo en un día operativo. El carril de salida
 * no puede crear ni modificar cobros (principio I): eso es responsabilidad de HU-02 y HU-03.
 */
public interface CobrosDelDia {

    /** @return el cobro de la placa en ese día operativo, o vacío si no existe (por ejemplo una moto) */
    Optional<CobroDia> buscar(UUID sedeId, String placa, LocalDate diaOperativo);
}
