package com.uam.parqueadero.cobro.infraestructura;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.uam.parqueadero.cobro.NuevoCobro;
import com.uam.parqueadero.cobro.RegistroCobros;

/** Implementación del puerto {@link RegistroCobros} sobre la tabla {@code cobro}. */
@Repository
public class RegistroCobrosJdbc implements RegistroCobros {

    private final JdbcTemplate jdbc;
    private final Clock reloj;

    public RegistroCobrosJdbc(JdbcTemplate jdbc, Clock reloj) {
        this.jdbc = jdbc;
        this.reloj = reloj;
    }

    @Override
    public boolean existe(UUID sedeId, String placa, LocalDate diaOperativo) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM cobro WHERE sede_id = ? AND placa = ? AND dia_operativo = ?)",
                Boolean.class, sedeId, placa, diaOperativo));
    }

    @Override
    public UUID crear(NuevoCobro cobro) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO cobro (id, sede_id, placa, tipo_vehiculo_id, dia_operativo, franja_id,
                    tarifa_id, valor, estado, ingreso_evento_id, creado_en)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'PENDIENTE', ?, ?)
                """,
                id, cobro.sedeId(), cobro.placa(), cobro.tipoVehiculoId(), cobro.diaOperativo(),
                cobro.franjaId(), cobro.tarifaId(), cobro.valor(), cobro.ingresoEventoId(),
                OffsetDateTime.ofInstant(cobro.creadoEn(), reloj.getZone()));
        return id;
    }
}
