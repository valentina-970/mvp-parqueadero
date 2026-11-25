package com.uam.parqueadero.entrada;

import com.uam.parqueadero.cupo.CuposDeLaSede;

/**
 * Último paso de la cadena: debe quedar lugar para el tipo de vehículo (HU-07, CA-2). Va después
 * de {@link BloquearCupoPaso}, así que mientras se cuenta nadie más puede ocupar el último lugar.
 * Si el tipo está lleno, el ingreso se rechaza con "LLENO": la barrera no abre, no se registra la
 * estancia ni se genera cobro, pero el rechazo queda en el registro de eventos (CA-3 y CA-7).
 * Cada tipo tiene su propio cupo, así que un tipo lleno no afecta al otro (CA-4).
 */
public class VerificarCupoPaso extends PasoEntrada {

    static final String MOTIVO_LLENO = "LLENO: no hay cupos disponibles para ";

    private final CuposDeLaSede cupos;

    public VerificarCupoPaso(CuposDeLaSede cupos) {
        this.cupos = cupos;
    }

    @Override
    protected void aplicar(ContextoEntrada contexto) {
        boolean hayLugar = cupos.deTipo(contexto.sedeId(), contexto.tipo().id())
                .map(cupo -> !cupo.lleno())
                // Un tipo sin cupo definido no se admite hasta que el administrador le asigne uno
                .orElse(false);
        if (!hayLugar) {
            contexto.rechazarPorCupo(MOTIVO_LLENO + contexto.tipo().nombre().toLowerCase() + "s");
        }
    }
}
