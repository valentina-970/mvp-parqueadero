package com.uam.parqueadero.comun;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Busca en la base la sede configurada (por defecto "Principal"). */
@Repository
public class SedeActualJdbc implements SedeActual {

    private final JdbcTemplate jdbc;
    private final String nombre;

    public SedeActualJdbc(JdbcTemplate jdbc, @Value("${app.sede.nombre:Principal}") String nombre) {
        this.jdbc = jdbc;
        this.nombre = nombre;
    }

    @Override
    public UUID id() {
        return jdbc.queryForObject("SELECT id FROM sede WHERE nombre = ?", UUID.class, nombre);
    }
}
