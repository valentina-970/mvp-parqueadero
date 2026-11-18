package com.uam.parqueadero.entrada;

import java.math.BigDecimal;

/** Puerto para leer el umbral de confianza configurado (parámetro {@code umbral_confianza}). */
public interface UmbralConfianza {

    BigDecimal valor();
}