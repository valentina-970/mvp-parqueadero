package com.uam.parqueadero.validacion.infraestructura;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.uam.parqueadero.comun.LecturaPendiente;
import com.uam.parqueadero.validacion.LecturasPendientes;

/** Implementación del puerto {@link LecturasPendientes} sobre la tabla {@code evento_acceso}. */
@Repository
public class LecturasPendientesJdbc implements LecturasPendientes {

    private static final String CONSULTA = """
            SELECT e.id, e.carril, e.placa_leida, e.placa, t.nombre AS tipo, e.confianza, e.ocurrido_en
            FROM evento_acceso e
            JOIN tipo_vehiculo t ON t.id = e.tipo_vehiculo_id
            WHERE e.resultado = 'PENDIENTE_VALIDACION'""";

    private final JdbcTemplate jdbc;

    public LecturasPendientesJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<LecturaPendiente> listar(UUID sedeId, String carril) {
        if (carril == null) {
            return jdbc.query(CONSULTA + " AND e.sede_id = ? ORDER BY e.ocurrido_en",
                    LecturasPendientesJdbc::aLectura, sedeId);
        }
        return jdbc.query(CONSULTA + " AND e.sede_id = ? AND e.carril = ? ORDER BY e.ocurrido_en",
                LecturasPendientesJdbc::aLectura, sedeId, carril);
    }

    @Override
    public Optional<LecturaPendiente> tomar(UUID eventoId) {
        // FOR UPDATE OF e: bloquea solo la fila del evento, no la del tipo de vehículo
        return jdbc.query(CONSULTA + " AND e.id = ? FOR UPDATE OF e",
                        LecturasPendientesJdbc::aLectura, eventoId)
                .stream().findFirst();
    }

    private static LecturaPendiente aLectura(ResultSet rs, int fila) throws SQLException {
        return new LecturaPendiente(
                rs.getObject("id", UUID.class),
                rs.getString("carril"),
                rs.getString("placa_leida"),
                rs.getString("placa"),
                rs.getString("tipo"),
                rs.getBigDecimal("confianza"),
                rs.getObject("ocurrido_en", OffsetDateTime.class).toInstant());
    }
}