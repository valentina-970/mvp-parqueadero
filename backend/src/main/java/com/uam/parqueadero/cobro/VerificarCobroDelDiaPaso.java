package com.uam.parqueadero.cobro;

import com.uam.parqueadero.entrada.IngresoRegistrado;

/**
 * Primer paso: si el vehículo ya tiene cobro en este día operativo, no se crea otro (CA-5).
 * La base de datos también lo impide con {@code uq_cobro_placa_sede_dia} (CA-6).
 */

public class VerificarCobroDelDiaPaso extends PasoCobro{

    static final String MOTIVO = "Ya tiene cobro een este día operativo";

    private final RegistroCobros cobros;

    public VerificarCobroDelDiaPaso(RegistroCobros cobros) {
        this.cobros = cobros;
    }

    @Override
    protected void aplicar(ContextoCobro contexto){
        IngresoRegistrado ingreso = contexto.ingreso();
        if(cobros.existe(ingreso.sedeId(), ingreso.placa(),ingreso.diaOperativo())){
            contexto.omitir(MOTIVO);
        }
    }

}
