package com.uam.parqueadero.entrada.infraestructura;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.uam.parqueadero.entrada.EventoEntrada;
import com.uam.parqueadero.entrada.EventosEntrada;

/** Implementación del puerto {@link EventosEntrada} sobre la tabla {@code evento_acceso}. */
@Repository
public class EventosEntradaJdbc implements EventosEntrada {

    private final JdbcTemplate jdbc;
    private final Clock reloj;

    public EventosEntradaJdbc(JdbcTemplate jdbc, Clock reloj) {
        this.jdbc = jdbc;
        this.reloj = reloj;
    }

    @Override
    public UUID registrar(EventoEntrada evento) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO evento_acceso (id, sede_id, carril, placa, placa_leida, tipo_vehiculo_id,
                    confianza, tipo_lectura, resultado, motivo, ocurrido_en, dia_operativo)
                VALUES (?, ?, 'ENTRADA', ?, ?, ?, ?, 'AUTOMATICA', ?, ?, ?, ?)
                """,
                id, evento.sedeId(), evento.placa(), evento.placaLeida(), evento.tipoVehiculoId(),
                evento.confianza(), evento.resultado().name(), evento.motivo(),
                OffsetDateTime.ofInstant(evento.ocurridoEn(), reloj.getZone()), evento.diaOperativo());
        return id;
    }
}