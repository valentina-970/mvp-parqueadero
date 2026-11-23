package com.uam.parqueadero.salida;

import com.uam.parqueadero.comun.ClasificadorPlacas;

/**
 * 
 * /**
 * Primer paso: la placa debe tener un formato válido (AAA123 o AAA12A, SG-06).
 * Si lo tiene, deja en el contexto el tipo de vehículo.
 */
public class ValidarPlacaPaso extends PasoSalida {

    static final String MOTIVO = "Formato de placa no válido";

    private final ClasificadorPlacas clasificador;

    public ValidarPlacaPaso(ClasificadorPlacas clasificador) {
        this.clasificador = clasificador;
    }

    @Override
    protected void aplicar(ContextoSalida contexto) {
        clasificador.clasificar(contexto.placa())
                .ifPresentOrElse(contexto::tipo, () -> contexto.rechazar(MOTIVO));
    }
}
