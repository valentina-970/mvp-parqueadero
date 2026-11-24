package com.uam.parqueadero.entrada;

/**
 * Paso que serializa los ingresos del mismo tipo de vehículo. Sin él, dos lecturas
 * simultáneas de la misma placa pasarían a la vez por {@link VerificarNoAdentroPaso} y la
 * segunda fallaría con un error 500 al abrir la estancia. Con el bloqueo, la segunda espera,
 * ve la estancia de la primera y recibe un rechazo limpio. HU-07 cuenta los cupos con este
 * mismo bloqueo.
 */
public class BloquearCupoPaso extends PasoEntrada {

    private final BloqueoCupos bloqueo;

    public BloquearCupoPaso(BloqueoCupos bloqueo) {
        this.bloqueo = bloqueo;
    }

    @Override
    protected void aplicar(ContextoEntrada contexto) {
        bloqueo.bloquear(contexto.sedeId(), contexto.tipo().id());
    }
}