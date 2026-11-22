package com.uam.parqueadero.cobro;

/** Tercer paso: una tarifa de $0, como la de las motos, no genera cobro (RN-01, CA-4). */

public class VerificarTarifaCeroPaso extends PasoCobro{

    static final String MOTIVO = "La tarifa de $0, no genera cobro";

    @Override
    protected void aplicar(ContextoCobro contexto){
        if(contexto.tarifa().valor().signum() == 0){
            contexto.omitir(MOTIVO);
        }
    }
}
