package com.uam.parqueadero.entrada.infraestructura;

import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.uam.parqueadero.entrada.BloqueoCupos;

/**
 * Bloquea la fila de {@code cupo} del tipo de vehículo con {@code FOR UPDATE}. La fila queda
 * bloqueada hasta que termina la transacción del ingreso, igual que en la salida y la caja.
 */
@Repository
public class BloqueoCuposJdbc implements BloqueoCupos {

    private final JdbcTemplate jdbc;

    public BloqueoCuposJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void bloquear(UUID sedeId, UUID tipoVehiculoId) {
        jdbc.query("SELECT id FROM cupo WHERE sede_id = ? AND tipo_vehiculo_id = ? FOR UPDATE",
                (rs, fila) -> rs.getObject("id", UUID.class), sedeId, tipoVehiculoId);
    }
}