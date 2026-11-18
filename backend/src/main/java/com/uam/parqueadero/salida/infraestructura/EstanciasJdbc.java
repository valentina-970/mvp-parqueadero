package com.uam.parqueadero.salida.infraestructura;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.uam.parqueadero.salida.EstanciaAbierta;
import com.uam.parqueadero.salida.Estancias;

/** Implementación del puerto {@link Estancias} sobre la tabla {@code estancia}. */
@Repository
public class EstanciasJdbc implements Estancias {

    private final JdbcTemplate jdbc;
    private final Clock reloj;

    public EstanciasJdbc(JdbcTemplate jdbc, Clock reloj) {
        this.jdbc = jdbc;
        this.reloj = reloj;
    }

    @Override
    public Optional<EstanciaAbierta> buscarAbierta(UUID sedeId, String placa) {
        return jdbc.query(
                "SELECT id, ingreso_en FROM estancia WHERE sede_id = ? AND placa = ? AND salida_en IS NULL",
                (rs, fila) -> new EstanciaAbierta(
                        rs.getObject("id", UUID.class),
                        rs.getObject("ingreso_en", OffsetDateTime.class).toInstant()),
                sedeId, placa).stream().findFirst();
    }

    @Override
    public void cerrar(UUID estanciaId, UUID eventoSalidaId, Instant salidaEn) {
        jdbc.update("UPDATE estancia SET salida_en = ?, salida_evento_id = ? WHERE id = ? AND salida_en IS NULL",
                OffsetDateTime.ofInstant(salidaEn, reloj.getZone()), eventoSalidaId, estanciaId);
    }
}
