package com.uam.parqueadero.entrada;

import com.uam.parqueadero.salida.ClasificadorPlacas;

/**
 * Primer paso: la placa debe ser de carro (AAA123) o de moto (AAA12A). Si lo es, deja el tipo
 * de vehículo en el contexto (CA-4 y CA-5); si no, rechaza la lectura (CA-6).
 * Reutiliza el clasificador del carril de salida, que lee los formatos de {@code tipo_vehiculo}.
 */
public class ValidarFormatoPaso extends PasoEntrada {

    static final String MOTIVO = "Formato de placa no válido";

    private final ClasificadorPlacas clasificador;

    public ValidarFormatoPaso(ClasificadorPlacas clasificador) {
        this.clasificador = clasificador;
    }

    @Override
    protected void aplicar(ContextoEntrada contexto) {
        clasificador.clasificar(contexto.placa())
                .ifPresentOrElse(contexto::tipo, () -> contexto.rechazar(MOTIVO));
    }
}