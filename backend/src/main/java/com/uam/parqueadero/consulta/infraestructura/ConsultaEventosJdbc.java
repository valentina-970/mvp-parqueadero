package com.uam.parqueadero.consulta.infraestructura;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.uam.parqueadero.consulta.ConsultaEventos;
import com.uam.parqueadero.consulta.EventoConsultado;
import com.uam.parqueadero.consulta.FiltroConsulta;
import com.uam.parqueadero.consulta.Pagina;

/** Implementación del puerto {@link ConsultaEventos} sobre la tabla {@code evento_acceso}. */
@Repository
public class ConsultaEventosJdbc implements ConsultaEventos {

    private static final String TABLAS = """
             FROM evento_acceso e
            JOIN tipo_vehiculo t ON t.id = e.tipo_vehiculo_id
            JOIN sede s ON s.id = e.sede_id""";

    private final JdbcTemplate jdbc;
    private final Clock reloj;

    public ConsultaEventosJdbc(JdbcTemplate jdbc, Clock reloj) {
        this.jdbc = jdbc;
        this.reloj = reloj;
    }

    @Override
    public Pagina<EventoConsultado> buscar(FiltroConsulta filtro) {
        ZoneId zona = reloj.getZone();
        // Las fechas son días de Colombia: "hasta" incluye todo ese día, hasta antes de la medianoche
        CondicionesSql condiciones = new CondicionesSql()
                .siHay(filtro.placa(), "e.placa = ?")
                .siHay(inicioDel(filtro.desde(), zona), "e.ocurrido_en >= ?")
                .siHay(inicioDel(filtro.hasta() == null ? null : filtro.hasta().plusDays(1), zona), "e.ocurrido_en < ?");

        Long total = jdbc.queryForObject("SELECT COUNT(*)" + TABLAS + condiciones.where(),
                Long.class, condiciones.valores());

        List<EventoConsultado> eventos = jdbc.query(
                "SELECT e.placa, t.nombre AS tipo, e.carril, e.ocurrido_en, s.nombre AS sede, e.tipo_lectura,"
                        + " e.resultado, e.motivo" + TABLAS + condiciones.where()
                        + " ORDER BY e.ocurrido_en DESC LIMIT ? OFFSET ?",
                (rs, fila) -> new EventoConsultado(
                        rs.getString("placa"),
                        rs.getString("tipo"),
                        rs.getString("carril"),
                        rs.getObject("ocurrido_en", OffsetDateTime.class).toInstant(),
                        rs.getString("sede"),
                        rs.getString("tipo_lectura"),
                        rs.getString("resultado"),
                        rs.getString("motivo")),
                condiciones.valores(FiltroConsulta.TAMANO_PAGINA, filtro.desplazamiento()));

        return Pagina.de(eventos, filtro.pagina(), total == null ? 0 : total);
    }

    private static OffsetDateTime inicioDel(LocalDate dia, ZoneId zona) {
        return dia == null ? null : dia.atStartOfDay(zona).toOffsetDateTime();
    }
}