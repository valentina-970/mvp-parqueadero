package com.uam.parqueadero.salida.infraestructura;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.uam.parqueadero.salida.EventoSalida;
import com.uam.parqueadero.salida.RegistroEventos;

/** Implementación del puerto {@link RegistroEventos} sobre la tabla {@code evento_acceso}. */
@Repository
public class RegistroEventosJdbc implements RegistroEventos {

    private final JdbcTemplate jdbc;
    private final Clock reloj;

    public RegistroEventosJdbc(JdbcTemplate jdbc, Clock reloj) {
        this.jdbc = jdbc;
        this.reloj = reloj;
    }

    @Override
    public UUID registrarSalida(EventoSalida evento) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO evento_acceso (id, sede_id, carril, placa, placa_leida, tipo_vehiculo_id,
                    confianza, tipo_lectura, resultado, motivo, ocurrido_en, dia_operativo)
                VALUES (?, ?, 'SALIDA', ?, ?, ?, ?, 'AUTOMATICA', ?, ?, ?, ?)
                """,
                id, evento.sedeId(), evento.placa(), evento.placaLeida(), evento.tipoVehiculoId(),
                evento.confianza(), resultado(evento), evento.motivo(),
                OffsetDateTime.ofInstant(evento.ocurridoEn(), reloj.getZone()), evento.diaOperativo());
        return id;
    }

    @Override
    public void resolverManual(UUID eventoId, EventoSalida evento, String usuario) {
        jdbc.update("""
                UPDATE evento_acceso
                SET placa = ?, tipo_vehiculo_id = ?, tipo_lectura = 'MANUAL', resultado = ?, motivo = ?,
                    validado_por_id = (SELECT id FROM usuario WHERE nombre_usuario = ?)
                WHERE id = ?
                """,
                evento.placa(), evento.tipoVehiculoId(), resultado(evento), evento.motivo(), usuario, eventoId);
    }

    private static String resultado(EventoSalida evento) {
        if (evento.pendiente()) {
            return "PENDIENTE_VALIDACION";
        }
        return evento.permitido() ? "PERMITIDO" : "RECHAZADO";
    }
}
