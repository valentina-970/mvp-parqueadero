package com.uam.parqueadero.cobro;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Puerto para leer las tarifas vigentes de un tipo de vehículo en una sede, con su franja. */

public interface TarifasVigentes {

    /** @return las tarifas vigentes en ese momento, una por franja */
    List<TarifaVigente> buscar(UUID sedeId, UUID tipoVehiculoId, Instant momento);

}
