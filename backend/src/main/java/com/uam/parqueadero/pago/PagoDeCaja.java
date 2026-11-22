package com.uam.parqueadero.pago;

/** Pago hecho en la caja: la placa del vehículo y el resultado del intento. */
public record PagoDeCaja(String placa, ResultadoPago resultado) {
}
