package com.uam.parqueadero.consulta.infraestructura;

import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.uam.parqueadero.consulta.CobroConsultado;
import com.uam.parqueadero.consulta.ConsultaCobros;
import com.uam.parqueadero.consulta.FiltroConsulta;
import com.uam.parqueadero.consulta.Pagina;

/** Implementación del puerto {@link ConsultaCobros} sobre la tabla {@code cobro}. */
@Repository
public class ConsultaCobrosJdbc implements ConsultaCobros {

    private static final String TABLAS = """
             FROM cobro c
            JOIN franja f ON f.id = c.franja_id
            JOIN tarifa ta ON ta.id = c.tarifa_id""";

    private final JdbcTemplate jdbc;

    public ConsultaCobrosJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Pagina<CobroConsultado> buscar(FiltroConsulta filtro) {
        // Los cobros se filtran por día operativo, que ya es una fecha
        CondicionesSql condiciones = new CondicionesSql()
                .siHay(filtro.placa(), "c.placa = ?")
                .siHay(filtro.desde(), "c.dia_operativo >= ?")
                .siHay(filtro.hasta(), "c.dia_operativo <= ?")
                .siHay(filtro.estado(), "c.estado = ?");

        Long total = jdbc.queryForObject("SELECT COUNT(*)" + TABLAS + condiciones.where(),
                Long.class, condiciones.valores());

        List<CobroConsultado> cobros = jdbc.query(
                "SELECT c.placa, c.dia_operativo, f.codigo AS franja, c.valor, ta.valor AS tarifa, c.estado"
                        + TABLAS + condiciones.where()
                        + " ORDER BY c.dia_operativo DESC, c.creado_en DESC LIMIT ? OFFSET ?",
                (rs, fila) -> new CobroConsultado(
                        rs.getString("placa"),
                        rs.getObject("dia_operativo", LocalDate.class),
                        rs.getString("franja"),
                        rs.getBigDecimal("valor"),
                        rs.getBigDecimal("tarifa"),
                        rs.getString("estado")),
                condiciones.valores(FiltroConsulta.TAMANO_PAGINA, filtro.desplazamiento()));

        return Pagina.de(cobros, filtro.pagina(), total == null ? 0 : total);
    }
}