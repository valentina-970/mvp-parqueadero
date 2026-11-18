package com.uam.parqueadero.salida;

import java.math.BigDecimal;
import java.util.UUID;

/** Lo que la salida necesita saber del cobro de un día operativo: su estado y su valor. */
public record CobroDia(UUID id, EstadoCobro estado, BigDecimal valor) {
}
