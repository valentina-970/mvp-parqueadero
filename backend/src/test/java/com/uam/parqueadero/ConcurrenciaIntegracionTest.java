package com.uam.parqueadero;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import com.uam.parqueadero.comun.SedeActual;
import com.uam.parqueadero.entrada.EstadoIngreso;
import com.uam.parqueadero.entrada.OperacionCarrilEntrada;
import com.uam.parqueadero.entrada.ResultadoIngreso;
import com.uam.parqueadero.entrada.SolicitudIngreso;
import com.uam.parqueadero.pago.CajaServicio;
import com.uam.parqueadero.pago.ResultadoPago;

/** OBS-5: comprueba los bloqueos de entrada y caja con transacciones reales e independientes. */
@SpringBootTest
@Import(ConcurrenciaIntegracionTest.SedeDePrueba.class)
class ConcurrenciaIntegracionTest {

    private static final UUID SEDE = UUID.randomUUID();
    private static final String OPERADOR = "tmp-" + SEDE;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private OperacionCarrilEntrada entrada;

    @Autowired
    private CajaServicio caja;

    @BeforeEach
    void prepararSede() {
        UUID carro = jdbc.queryForObject("SELECT id FROM tipo_vehiculo WHERE codigo = 'CARRO'", UUID.class);
        jdbc.update("INSERT INTO sede (id, nombre, activa) VALUES (?, ?, false)", SEDE, SEDE.toString());
        jdbc.update("INSERT INTO cupo (sede_id, tipo_vehiculo_id, capacidad) VALUES (?, ?, 1)", SEDE, carro);
        jdbc.update("""
                INSERT INTO tarifa (sede_id, tipo_vehiculo_id, franja_id, valor, vigente_desde)
                SELECT ?, ?, id, CASE codigo WHEN 'DIURNA' THEN 5000 ELSE 3000 END, now() - interval '1 day'
                FROM franja WHERE codigo IN ('DIURNA', 'NOCTURNA')
                """, SEDE, carro);
        jdbc.update("""
                INSERT INTO usuario (nombre_usuario, contrasena_hash, rol)
                VALUES (?, 'usuario de prueba sin acceso por contraseña', 'OPERADOR')
                """, OPERADOR);
    }

    @AfterEach
    void retirarSede() {
        // Los hilos confirman sus propias transacciones; se borra solo la sede aislada de la prueba.
        jdbc.update("DELETE FROM pago WHERE cobro_id IN (SELECT id FROM cobro WHERE sede_id = ?)", SEDE);
        jdbc.update("DELETE FROM cobro WHERE sede_id = ?", SEDE);
        jdbc.update("DELETE FROM estancia WHERE sede_id = ?", SEDE);
        jdbc.update("DELETE FROM evento_acceso WHERE sede_id = ?", SEDE);
        jdbc.update("DELETE FROM tarifa WHERE sede_id = ?", SEDE);
        jdbc.update("DELETE FROM cupo WHERE sede_id = ?", SEDE);
        jdbc.update("DELETE FROM sede WHERE id = ?", SEDE);
        jdbc.update("DELETE FROM usuario WHERE nombre_usuario = ?", OPERADOR);
    }

    @Test
    void dosLecturasDeLaMismaPlacaCreanUnaSolaEstanciaYUnSoloCobro() throws Exception {
        List<ResultadoIngreso> resultados = simultaneos(() -> ingresar("QCO101"), () -> ingresar("QCO101"));

        ResultadoIngreso rechazo = verificarIngresoUnico(resultados);
        assertFalse(rechazo.lleno(), "el anti-passback decide antes que el cupo");
    }

    @Test
    void dosPlacasNoPuedenOcuparElUltimoCupo() throws Exception {
        List<ResultadoIngreso> resultados = simultaneos(() -> ingresar("QCO102"), () -> ingresar("QCO103"));

        ResultadoIngreso rechazo = verificarIngresoUnico(resultados);
        assertTrue(rechazo.lleno());
    }

    @Test
    void dosPagosSimultaneosDevuelvenElMismoPago() throws Exception {
        String placa = "QCO104";
        assertEquals(EstadoIngreso.PERMITIDO, ingresar(placa).resultado());

        List<ResultadoPago> resultados = simultaneos(() -> pagar(placa), () -> pagar(placa));

        assertEquals(1, resultados.stream().filter(ResultadoPago::nuevo).count());
        assertEquals(resultados.get(0).pago().id(), resultados.get(1).pago().id());
        assertEquals(1, jdbc.queryForObject("""
                SELECT COUNT(*) FROM pago p JOIN cobro c ON c.id = p.cobro_id WHERE c.sede_id = ?
                """, Integer.class, SEDE));
    }

    private ResultadoIngreso verificarIngresoUnico(List<ResultadoIngreso> resultados) {
        assertEquals(1, resultados.stream().filter(r -> r.resultado() == EstadoIngreso.PERMITIDO).count());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM estancia WHERE sede_id = ?", Integer.class, SEDE));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM cobro WHERE sede_id = ?", Integer.class, SEDE));
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM evento_acceso WHERE sede_id = ?", Integer.class, SEDE));
        return resultados.stream().filter(r -> r.resultado() == EstadoIngreso.RECHAZADO).findFirst().orElseThrow();
    }

    private ResultadoIngreso ingresar(String placa) {
        return entrada.procesar(new SolicitudIngreso(placa, BigDecimal.ONE));
    }

    private ResultadoPago pagar(String placa) {
        return caja.pagar(placa, new BigDecimal("5000"), OPERADOR).resultado();
    }

    private static <T> List<T> simultaneos(Callable<T> primera, Callable<T> segunda) throws Exception {
        CyclicBarrier inicio = new CyclicBarrier(2);
        try (var hilos = Executors.newFixedThreadPool(2)) {
            Future<T> resultadoPrimero = hilos.submit(() -> {
                inicio.await(5, TimeUnit.SECONDS);
                return primera.call();
            });
            Future<T> resultadoSegundo = hilos.submit(() -> {
                inicio.await(5, TimeUnit.SECONDS);
                return segunda.call();
            });
            return List.of(resultadoPrimero.get(10, TimeUnit.SECONDS), resultadoSegundo.get(10, TimeUnit.SECONDS));
        }
    }

    /** La segunda sede es exclusiva de este contexto de pruebas. */
    @TestConfiguration
    static class SedeDePrueba {
        @Bean
        @Primary
        SedeActual sedeParaConcurrencia() {
            return () -> SEDE;
        }
    }
}
