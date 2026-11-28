package com.uam.parqueadero.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.uam.parqueadero.admin.ConfiguracionParqueadero.Horarios;
import com.uam.parqueadero.comun.UmbralConfianza;
import com.uam.parqueadero.entrada.EstadoIngreso;
import com.uam.parqueadero.entrada.OperacionCarrilEntrada;
import com.uam.parqueadero.entrada.SolicitudIngreso;

/** Verifica que los cambios de HU-08 afectan la operación sin alterar los cobros existentes. */
@SpringBootTest
@Transactional
class AdministracionIntegracionTest {

    @Autowired
    private AdministrarParqueadero administracion;

    @Autowired
    private OperacionCarrilEntrada entrada;

    @Autowired
    private UmbralConfianza umbral;

    @Autowired
    private JdbcTemplate jdbc;

    /** CP-08-03: el cobro anterior mantiene el valor y la tarifa con que se emitió. */
    @Test
    void conservaCobroAnteriorYAplicaTarifaNueva() {
        assertEquals(EstadoIngreso.PERMITIDO, entrada.procesar(lectura("QHU801")).resultado());
        BigDecimal valorAnterior = valorCobrado("QHU801");
        UUID tarifaAnterior = tarifaUsada("QHU801");
        var carro = administracion.tarifas().stream().filter(t -> t.codigo().equals("CARRO")).findFirst().orElseThrow();
        BigDecimal aumento = new BigDecimal("1000.00");

        administracion.cambiarTarifas(carro.tipoId(), carro.diurna().add(aumento), carro.nocturna().add(aumento));
        assertEquals(EstadoIngreso.PERMITIDO, entrada.procesar(lectura("QHU802")).resultado());

        assertEquals(valorAnterior, valorCobrado("QHU801"));
        assertEquals(tarifaAnterior, tarifaUsada("QHU801"));
        assertEquals(valorAnterior.add(aumento), valorCobrado("QHU802"));
        assertNotEquals(tarifaAnterior, tarifaUsada("QHU802"));
    }

    @Test
    void actualizaAmbosHorariosEnLaBase() {
        Horarios horarios = new Horarios(LocalTime.parse("07:00"), LocalTime.parse("19:29"),
                LocalTime.parse("19:30"), LocalTime.parse("06:59"));

        administracion.cambiarHorarios(horarios);

        assertEquals(horarios, administracion.consultar().horarios());
    }

    /** El cupo queda igual a los carros que ya están adentro, así no depende de que la base esté vacía. */
    @Test
    void configuraLaCapacidadQueUsaElCarril() {
        var carro = administracion.tarifas().stream().filter(t -> t.codigo().equals("CARRO")).findFirst().orElseThrow();
        int adentro = administracion.consultar().cupos().stream()
                .filter(c -> c.codigo().equals("CARRO")).findFirst().orElseThrow().ocupados();

        administracion.cambiarCupo(carro.tipoId(), adentro);

        var resultado = entrada.procesar(lectura("QHU803"));
        assertEquals(EstadoIngreso.RECHAZADO, resultado.resultado());
        assertTrue(resultado.lleno());
    }

    @Test
    void usaUmbralNuevoParaLasSiguientesLecturas() {
        administracion.cambiarUmbral(new BigDecimal("0.96"));

        assertEquals(new BigDecimal("0.96"), umbral.valor());
        assertEquals(EstadoIngreso.PENDIENTE_VALIDACION, entrada.procesar(lectura("QHU804")).resultado());
    }

    private static SolicitudIngreso lectura(String placa) {
        return new SolicitudIngreso(placa, new BigDecimal("0.95"));
    }

    private BigDecimal valorCobrado(String placa) {
        return jdbc.queryForObject("SELECT valor FROM cobro WHERE placa = ?", BigDecimal.class, placa);
    }

    private UUID tarifaUsada(String placa) {
        return jdbc.queryForObject("SELECT tarifa_id FROM cobro WHERE placa = ?", UUID.class, placa);
    }
}
