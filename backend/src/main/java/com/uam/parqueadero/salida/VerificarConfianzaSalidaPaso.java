package com.uam.parqueadero.salida;

import com.uam.parqueadero.comun.UmbralConfianza;

/**
 * Segundo paso de la salida: una lectura con confianza menor que el umbral no decide la
 * barrera; queda esperando la validación del operador (HU-06, CA-7). Una lectura que ya
 * validó el operador no vuelve a pasar por este filtro.
 */
public class VerificarConfianzaSalidaPaso extends PasoSalida {

    static final String MOTIVO = "Confianza baja: requiere validación manual";

    private final UmbralConfianza umbral;

    public VerificarConfianzaSalidaPaso(UmbralConfianza umbral) {
        this.umbral = umbral;
    }

    @Override
    protected void aplicar(ContextoSalida contexto) {
        if (!contexto.manual() && contexto.solicitud().confianza().compareTo(umbral.valor()) < 0) {
            contexto.dejarPendiente(MOTIVO);
        }
    }
}