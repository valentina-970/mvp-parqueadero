package com.uam.parqueadero.cupo.infraestructura;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.uam.parqueadero.cupo.Cupo;
import com.uam.parqueadero.cupo.CuposDeLaSede;

/**
 * Implementación del puerto {@link CuposDeLaSede}: la capacidad sale de la tabla {@code cupo} y
 * los ocupados se cuentan con las estancias abiertas ({@code salida_en IS NULL}).
 */
@Repository
public class CuposDeLaSedeJdbc implements CuposDeLaSede {

    private static final String CONSULTA = """
            SELECT tv.id, tv.codigo, tv.nombre, c.capacidad,
                   (SELECT COUNT(*) FROM estancia e
                     WHERE e.sede_id = c.sede_id AND e.tipo_vehiculo_id = c.tipo_vehiculo_id
                       AND e.salida_en IS NULL) AS ocupados
            FROM cupo c
            JOIN tipo_vehiculo tv ON tv.id = c.tipo_vehiculo_id
            WHERE c.sede_id = ?
            """;

    private static final RowMapper<Cupo> FILA = (rs, n) -> new Cupo(
            rs.getObject("id", UUID.class),
            rs.getString("codigo"),
            rs.getString("nombre"),
            rs.getInt("capacidad"),
            rs.getInt("ocupados"));

    private final JdbcTemplate jdbc;

    public CuposDeLaSedeJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Cupo> todos(UUID sedeId) {
        return jdbc.query(CONSULTA + " ORDER BY tv.codigo", FILA, sedeId);
    }

    @Override
    public Optional<Cupo> deTipo(UUID sedeId, UUID tipoVehiculoId) {
        return jdbc.query(CONSULTA + " AND c.tipo_vehiculo_id = ?", FILA, sedeId, tipoVehiculoId)
                .stream().findFirst();
    }
}
