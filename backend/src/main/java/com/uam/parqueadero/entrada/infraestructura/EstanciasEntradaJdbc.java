package com.uam.parqueadero.entrada.infraestructura;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.uam.parqueadero.entrada.EstanciasEntrada;

/** Implementación del puerto {@link EstanciasEntrada} sobre la tabla {@code estancia}. */
@Repository
public class EstanciasEntradaJdbc implements EstanciasEntrada {

    private final JdbcTemplate jdbc;
    private final Clock reloj;

    public EstanciasEntradaJdbc(JdbcTemplate jdbc, Clock reloj) {
        this.jdbc = jdbc;
        this.reloj = reloj;
    }

    @Override
    public boolean hayAbierta(UUID sedeId, String placa) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM estancia WHERE sede_id = ? AND placa = ? AND salida_en IS NULL)",
                Boolean.class, sedeId, placa));
    }

    @Override
    public UUID abrir(UUID sedeId, String placa, UUID tipoVehiculoId, UUID eventoIngresoId, Instant ingresoEn) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO estancia (id, sede_id, placa, tipo_vehiculo_id, ingreso_evento_id, ingreso_en)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                id, sedeId, placa, tipoVehiculoId, eventoIngresoId,
                OffsetDateTime.ofInstant(ingresoEn, reloj.getZone()));
        return id;
    }
}