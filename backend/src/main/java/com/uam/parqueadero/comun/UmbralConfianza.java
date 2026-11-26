package com.uam.parqueadero.comun;

import java.math.BigDecimal;

/**
 * Puerto para leer el umbral de confianza configurado (parámetro {@code umbral_confianza}).
 * Lo usan los dos carriles (HU-06, CA-7) y lo edita el administrador (HU-08, CA-8).
 */
public interface UmbralConfianza {

    BigDecimal valor();
}