package com.uam.parqueadero.comun;

import java.util.Locale;

/** Utilidades para las placas de los vehículos. */
public final class Placa {

    private Placa() {
    }

    /**
     * Normaliza la placa antes de validarla: mayúsculas, sin espacios ni guiones.
     *
     * @param leida texto tal como lo envió el lector; puede ser nulo
     * @return la placa normalizada, o una cadena vacía si no había texto
     */
    public static String normalizar(String leida) {
        if (leida == null) {
            return "";
        }
        return leida.toUpperCase(Locale.ROOT).replaceAll("[\\s-]", "");
    }
}
