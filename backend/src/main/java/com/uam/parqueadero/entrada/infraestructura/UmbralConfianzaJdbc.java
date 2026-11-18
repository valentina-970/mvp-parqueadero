package com.uam.parqueadero.entrada.infraestructura;

import java.math.BigDecimal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.uam.parqueadero.entrada.UmbralConfianza;

/** Lee el umbral de la tabla {@code parametro}; si no existe, usa 0,80 (supuesto del enunciado). */
@Repository
public class UmbralConfianzaJdbc implements UmbralConfianza {

    static final BigDecimal POR_DEFECTO = new BigDecimal("0.80");

    private final JdbcTemplate jdbc;

    public UmbralConfianzaJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public BigDecimal valor() {
        return jdbc.query("SELECT valor FROM parametro WHERE clave = 'umbral_confianza'",
                        (rs, fila) -> new BigDecimal(rs.getString("valor")))
                .stream().findFirst().orElse(POR_DEFECTO);
    }
}