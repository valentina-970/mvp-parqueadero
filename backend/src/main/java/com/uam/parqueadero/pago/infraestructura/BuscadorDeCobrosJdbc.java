package com.uam.parqueadero.pago.infraestructura;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.uam.parqueadero.pago.BuscadorDeCobros;
import com.uam.parqueadero.pago.CobroPorPagar;

/** Implementación del puerto {@link BuscadorDeCobros} sobre las tablas {@code cobro}, {@code tipo_vehiculo} y {@code franja}. */
@Repository
public class BuscadorDeCobrosJdbc implements BuscadorDeCobros {

    private final JdbcTemplate jdbc;

    public BuscadorDeCobrosJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<CobroPorPagar> buscar(UUID sedeId, String placa, LocalDate diaOperativo) {
        return jdbc.query("""
                SELECT c.id, c.placa, tv.nombre AS tipo, c.dia_operativo, f.codigo AS franja, c.valor, c.estado
                FROM cobro c
                JOIN tipo_vehiculo tv ON tv.id = c.tipo_vehiculo_id
                JOIN franja f ON f.id = c.franja_id
                WHERE c.sede_id = ? AND c.placa = ?
                  AND (c.dia_operativo = ? OR (c.dia_operativo = ? AND c.estado = 'PENDIENTE'))
                ORDER BY c.dia_operativo DESC
                LIMIT 1
                """,
                (rs, fila) -> new CobroPorPagar(
                        rs.getObject("id", UUID.class),
                        rs.getString("placa"),
                        rs.getString("tipo"),
                        rs.getObject("dia_operativo", LocalDate.class),
                        rs.getString("franja"),
                        rs.getBigDecimal("valor"),
                        rs.getString("estado")),
                sedeId, placa, diaOperativo, diaOperativo.minusDays(1)).stream().findFirst();
    }
}
