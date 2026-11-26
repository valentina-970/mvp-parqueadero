package com.uam.parqueadero.entrada;

import com.uam.parqueadero.comun.UmbralConfianza;

/**
 * Segundo paso: si la confianza es igual o mayor que el umbral, la lectura sigue como
 * automática (CA-7). Si es menor, queda pendiente de validación manual (HU-06, CA-1 y CA-2).
 * Una lectura que ya validó el operador no vuelve a pasar por este filtro.
 */
public class VerificarConfianzaPaso extends PasoEntrada {

    static final String MOTIVO = "Confianza baja: requiere validación manual";

    private final UmbralConfianza umbral;

    public VerificarConfianzaPaso(UmbralConfianza umbral) {
        this.umbral = umbral;
    }

    @Override
    protected void aplicar(ContextoEntrada contexto) {
        if (!contexto.manual() && contexto.lectura().confianza().compareTo(umbral.valor()) < 0) {
            contexto.dejarPendiente(MOTIVO);
        }
    }
}