package com.uam.parqueadero.consulta;

import java.time.Instant;

/** Un evento de acceso tal como lo muestra la consulta (CA-1). */
public record EventoConsultado(
        String placa,
        String tipoVehiculo,
        String carril,
        Instant ocurridoEn,
        String sede,
        String tipoLectura,
        String resultado,
        String motivo) {
}