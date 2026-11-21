package com.uam.parqueadero.cobro;

import com.uam.parqueadero.entrada.IngresoRegistrado;

/**
 * Segundo paso: calcula la tarifa con la estrategia configurada (CA-3). Si no hay ninguna
 * tarifa vigente para ese tipo y franja, no se crea cobro y queda el aviso en el log.
 */

public class AplicarTarifaPaso extends PasoCobro {

    static final String MOTIVO = "No hay tarifa vigente para el tipo de vehiculo y la franja";

    private final PoliticaTarifa politica;

    public AplicarTarifaPaso(PoliticaTarifa politica) {
        this.politica = politica;
    }

    @Override
    protected void aplicar(ContextoCobro contexto){
        IngresoRegistrado ingreso = contexto.ingreso();
        politica.calcular(ingreso.sedeId(), ingreso.tipoVehiculoId(), ingreso.ingresoEn())
        .ifPresentOrElse(contexto :: tarifa, () -> contexto.omitir(MOTIVO));
    }


}
