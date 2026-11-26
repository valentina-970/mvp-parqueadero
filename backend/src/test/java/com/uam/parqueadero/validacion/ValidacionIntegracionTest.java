package com.uam.parqueadero.validacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.uam.parqueadero.entrada.EstadoIngreso;
import com.uam.parqueadero.entrada.OperacionCarrilEntrada;
import com.uam.parqueadero.entrada.ResultadoIngreso;
import com.uam.parqueadero.entrada.SolicitudIngreso;

/**
 * Prueba de integración de la validación manual con la base de datos real (HU-06, CP-06-01).
 * Corre dentro de una transacción que se revierte al terminar, así que no deja datos.
 * Necesita la base levantada, igual que el arranque completo de la aplicación.
 */
@SpringBootTest
@Transactional
class ValidacionIntegracionTest {

    @Autowired
    private OperacionCarrilEntrada entrada;

    @Autowired
    private ValidacionManual validacion;

    @Autowired
    private JdbcTemplate jdbc;

    @Value("${app.usuarios.operador.nombre}")
    private String operador;

    private static String placaDeCarro() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return "V" + (char) ('A' + r.nextInt(26)) + (char) ('A' + r.nextInt(26)) + (100 + r.nextInt(900));
    }

    /** CP-06-01: confianza 0,60; el operador confirma → ingreso, estancia y cobro, con lectura manual y su usuario. */
    @Test
    void elIngresoConfirmadoQuedaComoLecturaManualConElUsuario() {
        String placa = placaDeCarro();
        ResultadoIngreso lectura = entrada.procesar(new SolicitudIngreso(placa, new BigDecimal("0.60")));
        assertEquals(EstadoIngreso.PENDIENTE_VALIDACION, lectura.resultado());

        UUID eventoId = validacion.pendientes("ENTRADA").stream()
                .filter(p -> p.placa().equals(placa))
                .findFirst().orElseThrow().eventoId();

        ResultadoValidacion validada = validacion.validar(eventoId, placa, operador);

        assertTrue(validada.permitido());
        Map<String, Object> evento = jdbc.queryForMap("""
                SELECT e.resultado, e.tipo_lectura, u.nombre_usuario
                FROM evento_acceso e JOIN usuario u ON u.id = e.validado_por_id
                WHERE e.id = ?""", eventoId);
        assertEquals("PERMITIDO", evento.get("resultado"));
        assertEquals("MANUAL", evento.get("tipo_lectura"));
        assertEquals(operador, evento.get("nombre_usuario"));
        assertEquals(1, jdbc.queryForObject(
                "SELECT COUNT(*) FROM estancia WHERE ingreso_evento_id = ? AND salida_en IS NULL", Integer.class, eventoId));
        assertEquals(1, jdbc.queryForObject(
                "SELECT COUNT(*) FROM cobro WHERE ingreso_evento_id = ?", Integer.class, eventoId));
        assertTrue(validacion.pendientes("ENTRADA").stream().noneMatch(p -> p.eventoId().equals(eventoId)));
    }
}