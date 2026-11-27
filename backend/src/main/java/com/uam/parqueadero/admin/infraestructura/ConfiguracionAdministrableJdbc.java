package com.uam.parqueadero.admin.infraestructura;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.uam.parqueadero.admin.ConfiguracionAdministrable;
import com.uam.parqueadero.admin.ConfiguracionParqueadero.Horarios;
import com.uam.parqueadero.admin.ConfiguracionParqueadero.Tarifas;

/** Persiste los cambios de administración en PostgreSQL. */
@Repository
public class ConfiguracionAdministrableJdbc implements ConfiguracionAdministrable {

    private final JdbcTemplate jdbc;

    public ConfiguracionAdministrableJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Tarifas> tarifas(UUID sede) {
        return jdbc.query("""
                SELECT tv.id, tv.codigo, tv.nombre,
                       MAX(t.valor) FILTER (WHERE f.codigo = 'DIURNA') AS diurna,
                       MAX(t.valor) FILTER (WHERE f.codigo = 'NOCTURNA') AS nocturna
                FROM tipo_vehiculo tv
                LEFT JOIN tarifa t ON t.tipo_vehiculo_id = tv.id AND t.sede_id = ? AND t.vigente_hasta IS NULL
                LEFT JOIN franja f ON f.id = t.franja_id
                GROUP BY tv.id, tv.codigo, tv.nombre
                ORDER BY tv.codigo
                """, (rs, fila) -> new Tarifas(rs.getObject("id", UUID.class), rs.getString("codigo"),
                rs.getString("nombre"), rs.getBigDecimal("diurna"), rs.getBigDecimal("nocturna")), sede);
    }

    @Override
    public Horarios horarios() {
        return jdbc.queryForObject("""
                SELECT d.hora_inicio AS inicio_diurno, d.hora_fin AS fin_diurno,
                       n.hora_inicio AS inicio_nocturno, n.hora_fin AS fin_nocturno
                FROM franja d CROSS JOIN franja n
                WHERE d.codigo = 'DIURNA' AND n.codigo = 'NOCTURNA'
                """, (rs, fila) -> new Horarios(rs.getObject("inicio_diurno", LocalTime.class),
                rs.getObject("fin_diurno", LocalTime.class), rs.getObject("inicio_nocturno", LocalTime.class),
                rs.getObject("fin_nocturno", LocalTime.class)));
    }

    @Override
    public String bloquearTipo(UUID tipo) {
        return jdbc.queryForList("SELECT codigo FROM tipo_vehiculo WHERE id = ? FOR NO KEY UPDATE",
                String.class, tipo).stream().findFirst().orElse(null);
    }

    @Override
    public void reemplazarTarifa(UUID sede, UUID tipo, String franja, BigDecimal valor, OffsetDateTime instante) {
        UUID franjaId = jdbc.queryForObject("SELECT id FROM franja WHERE codigo = ?", UUID.class, franja);
        List<BigDecimal> valores = jdbc.queryForList("""
                SELECT valor FROM tarifa
                WHERE sede_id = ? AND tipo_vehiculo_id = ? AND franja_id = ? AND vigente_hasta IS NULL
                """, BigDecimal.class, sede, tipo, franjaId);
        if (!valores.isEmpty() && valores.getFirst().compareTo(valor) == 0) {
            return;
        }
        jdbc.update("""
                UPDATE tarifa SET vigente_hasta = ?
                WHERE sede_id = ? AND tipo_vehiculo_id = ? AND franja_id = ? AND vigente_hasta IS NULL
                """, instante, sede, tipo, franjaId);
        jdbc.update("""
                INSERT INTO tarifa (sede_id, tipo_vehiculo_id, franja_id, valor, vigente_desde)
                VALUES (?, ?, ?, ?, ?)
                """, sede, tipo, franjaId, valor, instante);
    }

    @Override
    public void guardarHorarios(Horarios horarios) {
        jdbc.update("""
                UPDATE franja
                SET hora_inicio = CASE codigo WHEN 'DIURNA' THEN CAST(? AS time) ELSE CAST(? AS time) END,
                    hora_fin = CASE codigo WHEN 'DIURNA' THEN CAST(? AS time) ELSE CAST(? AS time) END
                WHERE codigo IN ('DIURNA', 'NOCTURNA')
                """, horarios.inicioDiurno(), horarios.inicioNocturno(), horarios.finDiurno(), horarios.finNocturno());
    }

}
