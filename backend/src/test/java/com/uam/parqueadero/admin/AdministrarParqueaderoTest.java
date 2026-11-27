package com.uam.parqueadero.admin;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.uam.parqueadero.admin.ConfiguracionParqueadero.Horarios;
import com.uam.parqueadero.comun.SedeActual;

class AdministrarParqueaderoTest {

    private final UUID sedeId = UUID.randomUUID();
    private final UUID tipoId = UUID.randomUUID();
    private final ConfiguracionAdministrable datos = mock(ConfiguracionAdministrable.class);
    private final SedeActual sede = () -> sedeId;
    private final Clock reloj = Clock.fixed(Instant.parse("2026-11-26T15:00:00Z"), ZoneOffset.UTC);
    private final AdministrarParqueadero administracion =
            new AdministrarParqueadero(datos, sede, reloj);

    @BeforeEach
    void preparar() {
        when(datos.bloquearTipo(tipoId)).thenReturn("CARRO");
    }

    /** CP-08-01: permite tarifa de carro diurna de $5.000 y nocturna de $3.000. */
    @Test
    void guardaTarifasValidasDeCarro() {
        administracion.cambiarTarifas(tipoId, new BigDecimal("5000"), new BigDecimal("3000"));

        verify(datos).reemplazarTarifa(eq(sedeId), eq(tipoId), eq("DIURNA"), eq(new BigDecimal("5000")), any());
        verify(datos).reemplazarTarifa(eq(sedeId), eq(tipoId), eq("NOCTURNA"), eq(new BigDecimal("3000")), any());
    }

    /** CP-08-02: la moto conserva la excepción de tarifa gratuita en ambas franjas. */
    @Test
    void permiteTarifasEnCeroParaMoto() {
        when(datos.bloquearTipo(tipoId)).thenReturn("MOTO");

        administracion.cambiarTarifas(tipoId, BigDecimal.ZERO, BigDecimal.ZERO);

        verify(datos).reemplazarTarifa(eq(sedeId), eq(tipoId), eq("DIURNA"), eq(BigDecimal.ZERO), any());
        verify(datos).reemplazarTarifa(eq(sedeId), eq(tipoId), eq("NOCTURNA"), eq(BigDecimal.ZERO), any());
    }

    /** CP-08-04 y CP-08-05: rechaza nocturna igual o superior a la diurna. */
    @ParameterizedTest
    @ValueSource(strings = {"5000", "6000"})
    void rechazaTarifaNocturnaInvalida(String valor) {
        var error = assertThrows(ConfiguracionInvalidaException.class,
                () -> administracion.cambiarTarifas(tipoId, new BigDecimal("5000"), new BigDecimal(valor)));

        assertTrue(error.getMessage().contains("RN-02"));
        verify(datos, never()).reemplazarTarifa(any(), any(), any(), any(), any());
    }

    /** CP-08-06: rechaza tarifas negativas. */
    @Test
    void rechazaTarifaNegativa() {
        var error = assertThrows(ConfiguracionInvalidaException.class,
                () -> administracion.cambiarTarifas(tipoId, new BigDecimal("-1000"), BigDecimal.ZERO));

        assertTrue(error.getMessage().contains("negativas"));
        verify(datos, never()).reemplazarTarifa(any(), any(), any(), any(), any());
    }

    /** CP-08-07: rechaza tanto un minuto compartido como un minuto sin cubrir. */
    @ParameterizedTest
    @ValueSource(strings = {"18:00", "18:02"})
    void rechazaFranjasConSolapamientoOHuecos(String inicioNocturno) {
        Horarios propuestos = new Horarios(LocalTime.parse("06:00"), LocalTime.parse("18:00"),
                LocalTime.parse(inicioNocturno), LocalTime.parse("05:59"));

        assertThrows(ConfiguracionInvalidaException.class, () -> administracion.cambiarHorarios(propuestos));
        verify(datos, never()).guardarHorarios(any());
    }

    @Test
    void guardaFranjasQueCubrenMedianoche() {
        Horarios propuestos = new Horarios(LocalTime.parse("07:00"), LocalTime.parse("19:29"),
                LocalTime.parse("19:30"), LocalTime.parse("06:59"));

        administracion.cambiarHorarios(propuestos);

        verify(datos).guardarHorarios(propuestos);
    }

}
