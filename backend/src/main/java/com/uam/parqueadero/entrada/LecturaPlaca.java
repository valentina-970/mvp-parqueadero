package com.uam.parqueadero.entrada;

import java.math.BigDecimal;

/**
 * Lectura de una placa en el formato que entiende el carril, sin importar qué dispositivo la hizo.
 *
 * @param placa       placa normalizada: mayúsculas, sin espacios ni guiones (CA-3)
 * @param placaLeida  placa tal como llegó del dispositivo, para el registro de eventos
 * @param confianza   nivel de confianza entre 0 y 1
 */
public record LecturaPlaca(String placa, String placaLeida, BigDecimal confianza) {
}