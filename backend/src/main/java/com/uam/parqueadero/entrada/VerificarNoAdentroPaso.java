package com.uam.parqueadero.entrada;

/**
 * Tercer paso: un vehículo que ya está adentro no puede volver a entrar sin haber salido.
 * La base de datos también lo impide con el índice {@code uq_estancia_abierta}.
 */
public class VerificarNoAdentroPaso extends PasoEntrada {

    static final String MOTIVO = "El vehículo ya tiene un ingreso registrado";

    private final EstanciasEntrada estancias;

    public VerificarNoAdentroPaso(EstanciasEntrada estancias) {
        this.estancias = estancias;
    }

    @Override
    protected void aplicar(ContextoEntrada contexto) {
        if (estancias.hayAbierta(contexto.sedeId(), contexto.placa())) {
            contexto.rechazar(MOTIVO);
        }
    }
}