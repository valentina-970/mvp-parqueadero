package com.uam.parqueadero.salida.infraestructura;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.uam.parqueadero.salida.ClasificadorPlacas;
import com.uam.parqueadero.salida.TipoVehiculoDetectado;

/**
 * Clasifica la placa con las expresiones regulares guardadas en {@code tipo_vehiculo.patron_placa}.
 * Agregar un tipo de vehículo nuevo es insertar una fila: este código no cambia (principio O).
 */
@Repository
public class ClasificadorPlacasJdbc implements ClasificadorPlacas {

    private record Tipo(TipoVehiculoDetectado tipo, Pattern patron) {
    }

    private final JdbcTemplate jdbc;

    public ClasificadorPlacasJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<TipoVehiculoDetectado> clasificar(String placaNormalizada) {
        List<Tipo> tipos = jdbc.query(
                "SELECT id, codigo, nombre, patron_placa FROM tipo_vehiculo",
                (rs, fila) -> new Tipo(
                        new TipoVehiculoDetectado(
                                rs.getObject("id", UUID.class), rs.getString("codigo"), rs.getString("nombre")),
                        Pattern.compile(rs.getString("patron_placa"))));
        return tipos.stream()
                .filter(t -> t.patron().matcher(placaNormalizada).matches())
                .map(Tipo::tipo)
                .findFirst();
    }
}
