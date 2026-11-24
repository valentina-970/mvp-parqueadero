package com.uam.parqueadero.consulta.infraestructura;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Arma el {@code WHERE} de una consulta solo con los filtros que llegaron (CA-4). Las
 * condiciones son textos fijos escritos en el código y los valores van siempre como
 * parámetros {@code ?}, así que lo que escribe el usuario nunca se pega al SQL.
 */
public final class CondicionesSql {

    private final List<String> condiciones = new ArrayList<>();
    private final List<Object> valores = new ArrayList<>();

    /** Agrega la condición solo si el valor no es {@code null}. */
    public CondicionesSql siHay(Object valor, String condicion) {
        if (valor != null) {
            condiciones.add(condicion);
            valores.add(valor);
        }
        return this;
    }

    /** @return {@code " WHERE a = ? AND b = ?"}, o vacío si no hay ningún filtro */
    public String where() {
        return condiciones.isEmpty() ? "" : " WHERE " + String.join(" AND ", condiciones);
    }

    /** @return los valores de las condiciones, en orden, seguidos de los extra (por ejemplo LIMIT y OFFSET) */
    public Object[] valores(Object... extra) {
        List<Object> todos = new ArrayList<>(valores);
        todos.addAll(Arrays.asList(extra));
        return todos.toArray();
    }
}