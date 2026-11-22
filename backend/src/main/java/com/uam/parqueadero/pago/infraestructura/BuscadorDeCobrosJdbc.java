package com.uam.parqueadero.pago.infraestructura;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.uam.parqueadero.pago.BuscadorDeCobros;
import com.uam.parqueadero.pago.CobroPorPagar;
import com.uam.parqueadero.pago.Pago;

/**
 * Implementación del puerto {@link BuscadorDeCobros} sobre las tablas {@code cobro},
 * {@code tipo_vehiculo}, {@code franja}, {@code pago} y {@code usuario}.
 */
@Repository
public class BuscadorDeCobrosJdbc implements BuscadorDeCobros {

    private static final String CONSULTA = """
            SELECT c.id, c.placa, tv.nombre AS tipo, c.dia_operativo, f.codigo AS franja, c.valor, c.estado,
                   p.id AS pago_id, p.valor AS pago_valor, p.valor_recibido, p.cambio,
                   p.recibido_por_id, u.nombre_usuario, p.pagado_en, p.pase_vigente_hasta
            FROM cobro c
            JOIN tipo_vehiculo tv ON tv.id = c.tipo_vehiculo_id
            JOIN franja f ON f.id = c.franja_id
            LEFT JOIN pago p ON p.cobro_id = c.id
            LEFT JOIN usuario u ON u.id = p.recibido_por_id
            WHERE c.sede_id = ? AND c.placa = ?
              AND (c.dia_operativo = ? OR (c.dia_operativo = ? AND c.estado = 'PENDIENTE'))
            ORDER BY c.dia_operativo DESC
            LIMIT 1
            """;

    private final JdbcTemplate jdbc;

    public BuscadorDeCobrosJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<CobroPorPagar> buscar(UUID sedeId, String placa, LocalDate diaOperativo) {
        return consultar(CONSULTA, sedeId, placa, diaOperativo);
    }

    @Override
    public Optional<CobroPorPagar> buscarParaPagar(UUID sedeId, String placa, LocalDate diaOperativo) {
        // OF c bloquea solo la fila del cobro; el pago y el usuario se unen por la izquierda
        return consultar(CONSULTA + " FOR UPDATE OF c", sedeId, placa, diaOperativo);
    }

    private Optional<CobroPorPagar> consultar(String sql, UUID sedeId, String placa, LocalDate dia) {
        return jdbc.query(sql, (rs, fila) -> aCobro(rs), sedeId, placa, dia, dia.minusDays(1))
                .stream().findFirst();
    }

    private static CobroPorPagar aCobro(ResultSet rs) throws SQLException {
        CobroPorPagar cobro = new CobroPorPagar(
                rs.getObject("id", UUID.class),
                rs.getString("placa"),
                rs.getString("tipo"),
                rs.getObject("dia_operativo", LocalDate.class),
                rs.getString("franja"),
                rs.getBigDecimal("valor"),
                rs.getString("estado"));
        UUID pagoId = rs.getObject("pago_id", UUID.class);
        if (pagoId == null) {
            return cobro;
        }
        return cobro.conPago(new Pago(
                pagoId,
                cobro.id(),
                rs.getBigDecimal("pago_valor"),
                rs.getBigDecimal("valor_recibido"),
                rs.getBigDecimal("cambio"),
                rs.getObject("recibido_por_id", UUID.class),
                rs.getString("nombre_usuario"),
                rs.getObject("pagado_en", OffsetDateTime.class).toInstant(),
                rs.getObject("pase_vigente_hasta", OffsetDateTime.class).toInstant()));
    }
}
