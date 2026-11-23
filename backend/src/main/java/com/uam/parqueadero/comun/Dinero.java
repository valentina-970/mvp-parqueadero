package com.uam.parqueadero.comun;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/** Formato de los valores en pesos para los mensajes al operador. */
public final class Dinero {

    private Dinero() {
    }

    /** Da formato colombiano sin decimales: 5000 se muestra como "$5.000". */
    public static String formatear(BigDecimal valor) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setGroupingSeparator('.');
        return "$" + new DecimalFormat("#,##0", simbolos).format(valor);
    }
}
