package com.uam.parqueadero.salida;

/**
 * Segundo paso: el vehículo debe tener un ingreso registrado (estancia abierta).
 * Si no lo tiene, la barrera no abre y se avisa al operador (HU-04, CA-5).
 */
public class VerificarIngresoPaso extends PasoSalida {

    static final String MOTIVO = "Sin ingreso registrado";

    private final Estancias estancias;

    public VerificarIngresoPaso(Estancias estancias) {
        this.estancias = estancias;
    }

    @Override
    protected void aplicar(ContextoSalida contexto) {
        estancias.buscarAbierta(contexto.sedeId(), contexto.placa())
                .ifPresentOrElse(contexto::estancia, () -> contexto.rechazar(MOTIVO));
    }
}
