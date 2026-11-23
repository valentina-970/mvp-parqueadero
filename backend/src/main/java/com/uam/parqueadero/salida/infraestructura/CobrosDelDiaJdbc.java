package com.uam.parqueadero.salida.infraestructura;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.uam.parqueadero.salida.CobroDia;
import com.uam.parqueadero.salida.CobrosDelDia;
import com.uam.parqueadero.salida.EstadoCobro;

/** Implementación del puerto {@link CobrosDelDia} sobre la tabla {@code cobro}. */
@Repository
public class CobrosDelDiaJdbc implements CobrosDelDia {

    private final JdbcTemplate jdbc;

    public CobrosDelDiaJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<CobroDia> buscar(UUID sedeId, String placa, LocalDate diaOperativo) {
        return jdbc.query(
                "SELECT id, estado, valor FROM cobro WHERE sede_id = ? AND placa = ? AND dia_operativo = ?",
                (rs, fila) -> new CobroDia(
                        rs.getObject("id", UUID.class),
                        EstadoCobro.valueOf(rs.getString("estado")),
                        rs.getBigDecimal("valor")),
                sedeId, placa, diaOperativo).stream().findFirst();
    }
}
