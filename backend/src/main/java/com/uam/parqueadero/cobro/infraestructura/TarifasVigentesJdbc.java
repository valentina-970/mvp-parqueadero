package com.uam.parqueadero.cobro.infraestructura;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.uam.parqueadero.cobro.TarifaVigente;
import com.uam.parqueadero.cobro.TarifasVigentes;

/** Implementación del puerto {@link TarifasVigentes} sobre las tablas {@code tarifa} y {@code franja}. */
@Repository
public class TarifasVigentesJdbc implements TarifasVigentes {

    private final JdbcTemplate jdbc;
    private final Clock reloj;

    public TarifasVigentesJdbc(JdbcTemplate jdbc, Clock reloj) {
        this.jdbc = jdbc;
        this.reloj = reloj;
    }

    @Override
    public List<TarifaVigente> buscar(UUID sedeId, UUID tipoVehiculoId, Instant momento) {
        OffsetDateTime instante = OffsetDateTime.ofInstant(momento, reloj.getZone());
        return jdbc.query("""
                SELECT t.id, t.franja_id, f.hora_inicio, f.hora_fin, t.valor
                FROM tarifa t
                JOIN franja f ON f.id = t.franja_id
                WHERE t.sede_id = ? AND t.tipo_vehiculo_id = ?
                  AND t.vigente_desde <= ?
                  AND (t.vigente_hasta IS NULL OR t.vigente_hasta > ?)
                ORDER BY t.vigente_desde DESC
                """,
                (rs, fila) -> new TarifaVigente(
                        rs.getObject("id", UUID.class),
                        rs.getObject("franja_id", UUID.class),
                        rs.getObject("hora_inicio", LocalTime.class),
                        rs.getObject("hora_fin", LocalTime.class),
                        rs.getBigDecimal("valor")),
                sedeId, tipoVehiculoId, instante, instante);
    }
}