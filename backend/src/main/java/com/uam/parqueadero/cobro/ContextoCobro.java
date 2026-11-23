package com.uam.parqueadero.cobro;

import com.uam.parqueadero.entrada.IngresoRegistrado;

/**
 * Datos que recorren la cadena que decide si un ingreso genera cobro. Un paso puede dejar
 * la tarifa que aplica o descartar el cobro con un motivo.
 */

public class ContextoCobro {

    private final IngresoRegistrado ingreso;
    private TarifaAplicada tarifa;
    private String movimientoOmiso;

    public ContextoCobro(IngresoRegistrado ingreso) {
        this.ingreso = ingreso;
    }

    public IngresoRegistrado ingreso() {
        return ingreso;
    }

    public TarifaAplicada tarifa() {
        return tarifa;
    }

    public void tarifa(TarifaAplicada tarifa) {
        this.tarifa = tarifa;
    }
    
    /** Descarta el cobro; la cadena se detiene en el paso que llama a este método. */

    public void omitir(String motivo) {
        this.movimientoOmiso = motivo;
    }

    public boolean omitido(){
        return movimientoOmiso != null;
    }

    public String motivoOmision(){
        return movimientoOmiso;
    }

}
