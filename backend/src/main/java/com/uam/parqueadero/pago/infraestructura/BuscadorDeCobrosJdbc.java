package com.uam.parqueadero.pago.infraestructura;

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

    private static final String CONSULTA_COBRO = """
            SELECT c.id, c.placa, tv.nombre AS tipo, c.dia_operativo, f.codigo AS franja, c.valor, c.estado
            FROM cobro c
            JOIN tipo_vehiculo tv ON tv.id = c.tipo_vehiculo_id
            JOIN franja f ON f.id = c.franja_id
            WHERE c.sede_id = ? AND c.placa = ?
              AND (c.dia_operativo = ? OR (c.dia_operativo = ? AND c.estado = 'PENDIENTE'))
            ORDER BY c.dia_operativo DESC
            LIMIT 1
            """;

    private static final String CONSULTA_PAGO = """
            SELECT p.id, p.valor, p.valor_recibido, p.cambio, p.recibido_por_id, u.nombre_usuario,
                   p.pagado_en, p.pase_vigente_hasta
            FROM pago p
            JOIN usuario u ON u.id = p.recibido_por_id
            WHERE p.cobro_id = ?
            """;

    private final JdbcTemplate jdbc;

    public BuscadorDeCobrosJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<CobroPorPagar> buscar(UUID sedeId, String placa, LocalDate diaOperativo) {
        return consultar(CONSULTA_COBRO, sedeId, placa, diaOperativo);
    }

    @Override
    public Optional<CobroPorPagar> buscarParaPagar(UUID sedeId, String placa, LocalDate diaOperativo) {
        // OF c bloquea solo la fila del cobro; las demás tablas solo se leen
        return consultar(CONSULTA_COBRO + " FOR UPDATE OF c", sedeId, placa, diaOperativo);
    }

    private Optional<CobroPorPagar> consultar(String sql, UUID sedeId, String placa, LocalDate dia) {
        return jdbc.query(sql, (rs, fila) -> new CobroPorPagar(
                        rs.getObject("id", UUID.class),
                        rs.getString("placa"),
                        rs.getString("tipo"),
                        rs.getObject("dia_operativo", LocalDate.class),
                        rs.getString("franja"),
                        rs.getBigDecimal("valor"),
                        rs.getString("estado")),
                        sedeId, placa, dia, dia.minusDays(1))
                .stream().findFirst().map(this::conSuPago);
    }

    /**
     * Si el cobro está pagado, trae su pago. Va en una consulta aparte, después del bloqueo:
     * un pago que esperó el bloqueo debe ver el pago que acaba de confirmar la transacción anterior,
     * y unirlo en la misma consulta devolvería el estado anterior (vacío).
     */
    private CobroPorPagar conSuPago(CobroPorPagar cobro) {
        if (!"PAGADO".equals(cobro.estado())) {
            return cobro;
        }
        return jdbc.query(CONSULTA_PAGO, (rs, fila) -> new Pago(
                        rs.getObject("id", UUID.class),
                        cobro.id(),
                        rs.getBigDecimal("valor"),
                        rs.getBigDecimal("valor_recibido"),
                        rs.getBigDecimal("cambio"),
                        rs.getObject("recibido_por_id", UUID.class),
                        rs.getString("nombre_usuario"),
                        rs.getObject("pagado_en", OffsetDateTime.class).toInstant(),
                        rs.getObject("pase_vigente_hasta", OffsetDateTime.class).toInstant()),
                        cobro.id())
                .stream().findFirst().map(cobro::conPago).orElse(cobro);
    }
}
