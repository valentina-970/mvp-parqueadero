package com.uam.parqueadero.validacion;

/**
 * Resultado de validar una lectura: qué decidió el carril con la placa del operador.
 *
 * @param carril     {@code ENTRADA} o {@code SALIDA}
 * @param placa      placa con la que quedó el evento
 * @param permitido  {@code true} si el ingreso se registró o la barrera de salida abrió
 * @param motivo     explicación para el operador
 */
public record ResultadoValidacion(String carril, String placa, boolean permitido, String motivo) {
}
