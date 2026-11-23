package com.uam.parqueadero.salida;

import java.util.UUID;

/** Tipo de vehículo deducido del formato de la placa (por ejemplo CARRO o MOTO). */
public record TipoVehiculoDetectado(UUID id, String codigo, String nombre) {
}
