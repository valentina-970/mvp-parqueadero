package com.uam.parqueadero.cupo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.uam.parqueadero.comun.SedeActual;
import com.uam.parqueadero.entrada.EstadoIngreso;
import com.uam.parqueadero.entrada.OperacionCarrilEntrada;
import com.uam.parqueadero.entrada.ResultadoIngreso;
import com.uam.parqueadero.entrada.SolicitudIngreso;
import com.uam.parqueadero.salida.OperacionCarrilSalida;
import com.uam.parqueadero.salida.SolicitudSalida;

/**
 * Prueba de integración del control de cupos con la base de datos real (HU-07, CA-3, CA-5 y CA-7).
 * Cada prueba corre dentro de una transacción que se revierte al terminar, así que no deja datos.
 * Necesita la base levantada, igual que el arranque completo de la aplicación.
 */
@SpringBootTest
@Transactional
class CuposIntegracionTest {

    @Autowired
    private OperacionCarrilEntrada entrada;

    @Autowired
    private OperacionCarrilSalida salida;

    @Autowired
    private CuposDeLaSede cupos;

    @Autowired
    private SedeActual sedeActual;

    @Autowired
    private JdbcTemplate jdbc;

    private UUID sede;
    private UUID carro;
    private UUID moto;

    @BeforeEach
    void preparar() {
        sede = sedeActual.id();
        carro = tipo("CARRO");
        moto = tipo("MOTO");
        // El cupo de carros queda con un solo lugar libre, sin importar lo que haya adentro
        int adentro = cupos.deTipo(sede, carro).orElseThrow().ocupados();
        jdbc.update("UPDATE cupo SET capacidad = ? WHERE sede_id = ? AND tipo_vehiculo_id = ?",
                adentro + 1, sede, carro);
    }

    private UUID tipo(String codigo) {
        return jdbc.queryForObject("SELECT id FROM tipo_vehiculo WHERE codigo = ?", UUID.class, codigo);
    }

    private static String placaDeCarro() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return "Q" + (char) ('A' + r.nextInt(26)) + (char) ('A' + r.nextInt(26)) + (100 + r.nextInt(900));
    }

    private static String placaDeMoto() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return "Q" + (char) ('A' + r.nextInt(26)) + (char) ('A' + r.nextInt(26)) + (10 + r.nextInt(90))
                + (char) ('A' + r.nextInt(26));
    }

    private ResultadoIngreso entra(String placa) {
        return entrada.procesar(new SolicitudIngreso(placa, new BigDecimal("0.95")));
    }

    private int contar(String tabla, String placa) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + tabla + " WHERE placa = ?", Integer.class, placa);
    }

    /** CA-3, CA-4 y CA-7: sin cupo no hay ingreso ni cobro, la moto sigue entrando y el rechazo queda en eventos. */
    @Test
    void conElCupoLlenoNoHayIngresoNiCobroYElRechazoQuedaEnEventos() {
        String primero = placaDeCarro();
        String segundo = placaDeCarro();
        String unaMoto = placaDeMoto();

        assertEquals(EstadoIngreso.PERMITIDO, entra(primero).resultado());
        assertEquals(0, cupos.deTipo(sede, carro).orElseThrow().disponibles());

        ResultadoIngreso lleno = entra(segundo);

        assertEquals(EstadoIngreso.RECHAZADO, lleno.resultado());
        assertTrue(lleno.lleno());
        assertEquals(0, contar("estancia", segundo), "no se abre la estancia");
        assertEquals(0, contar("cobro", segundo), "no se genera cobro");
        assertEquals("LLENO: no hay cupos disponibles para carros", jdbc.queryForObject(
                "SELECT motivo FROM evento_acceso WHERE placa = ? AND carril = 'ENTRADA'", String.class, segundo));
        assertEquals("RECHAZADO", jdbc.queryForObject(
                "SELECT resultado FROM evento_acceso WHERE placa = ?", String.class, segundo));
        // El tipo lleno no afecta al otro
        assertEquals(EstadoIngreso.PERMITIDO, entra(unaMoto).resultado());
    }

    /** CA-5: cada ingreso ocupa un cupo y cada salida con la barrera abierta lo libera. */
    @Test
    void laSalidaLiberaElCupoYElSiguienteCarroPuedeEntrar() {
        String adentro = placaDeCarro();
        String esperando = placaDeCarro();
        assertEquals(EstadoIngreso.PERMITIDO, entra(adentro).resultado());
        assertTrue(entra(esperando).lleno(), "con el cupo lleno no entra");

        // El carro de adentro paga y sale
        jdbc.update("UPDATE cobro SET estado = 'PAGADO' WHERE placa = ?", adentro);
        assertTrue(salida.procesar(new SolicitudSalida(adentro, new BigDecimal("0.95"))).abre());

        assertEquals(1, cupos.deTipo(sede, carro).orElseThrow().disponibles(), "la salida liberó el cupo");
        ResultadoIngreso siguiente = entra(esperando);
        assertEquals(EstadoIngreso.PERMITIDO, siguiente.resultado());
        assertFalse(siguiente.lleno());
    }
}
