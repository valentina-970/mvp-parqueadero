package com.uam.parqueadero.validacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.uam.parqueadero.comun.LecturaPendiente;
import com.uam.parqueadero.entrada.EstadoIngreso;
import com.uam.parqueadero.entrada.OperacionCarrilEntrada;
import com.uam.parqueadero.entrada.ResultadoIngreso;
import com.uam.parqueadero.entrada.UltimoIngreso;
import com.uam.parqueadero.salida.EstadoBarrera;
import com.uam.parqueadero.salida.OperacionCarrilSalida;
import com.uam.parqueadero.salida.ResultadoSalida;
import com.uam.parqueadero.salida.UltimaSalida;

/** El servicio entrega cada lectura pendiente a la fachada de su carril (HU-06, CA-7). */
class ValidacionManualTest {

    private final LecturasPendientes pendientes = mock(LecturasPendientes.class);
    private final OperacionCarrilEntrada entrada = mock(OperacionCarrilEntrada.class);
    private final OperacionCarrilSalida salida = mock(OperacionCarrilSalida.class);
    private final UltimoIngreso ultimoIngreso = mock(UltimoIngreso.class);
    private final UltimaSalida ultimaSalida = mock(UltimaSalida.class);
    private final UUID eventoId = UUID.randomUUID();
    private final ValidacionManual validacion = new ValidacionManual(
            pendientes, entrada, salida, ultimoIngreso, ultimaSalida, UUID::randomUUID);

    private LecturaPendiente pendiente(String carril) {
        return new LecturaPendiente(eventoId, carril, "ABC123", "ABC123", "Carro",
                new BigDecimal("0.60"), Instant.now());
    }

    private static ResultadoIngreso ingreso(EstadoIngreso estado, String tipo, String motivo) {
        return new ResultadoIngreso(estado, motivo, "ABC123", tipo, new BigDecimal("0.60"), Instant.now(), false, true);
    }

    @Test
    void unaLecturaQueYaNoEstaPendienteNoSeValida() {
        when(pendientes.tomar(eventoId)).thenReturn(Optional.empty());

        assertThrows(LecturaNoPendienteException.class, () -> validacion.validar(eventoId, "ABC123", "operador1"));
    }

    @Test
    void unaLecturaDeLaEntradaLaDecideElCarrilDeEntrada() {
        ResultadoIngreso resultado = ingreso(EstadoIngreso.PERMITIDO, "Carro", "Ingreso registrado");
        when(pendientes.tomar(eventoId)).thenReturn(Optional.of(pendiente("ENTRADA")));
        when(entrada.validar(any(), any(), any())).thenReturn(resultado);

        ResultadoValidacion validada = validacion.validar(eventoId, "ABC123", "operador1");

        assertEquals("ENTRADA", validada.carril());
        assertTrue(validada.permitido());
        verify(ultimoIngreso).guardar(resultado);
        verify(salida, never()).validar(any(), any(), any());
    }

    /** CP-06-06 (error): la placa digitada no tiene formato → error y la pantalla del carril no cambia. */
    @Test
    void unaPlacaSinFormatoSeRechazaSinTocarLaPantallaDelCarril() {
        when(pendientes.tomar(eventoId)).thenReturn(Optional.of(pendiente("ENTRADA")));
        when(entrada.validar(any(), any(), any()))
                .thenReturn(ingreso(EstadoIngreso.RECHAZADO, null, "Formato de placa no válido"));

        PlacaNoValidaException error = assertThrows(PlacaNoValidaException.class,
                () -> validacion.validar(eventoId, "AB12", "operador1"));

        assertEquals("Formato de placa no válido", error.getMessage());
        verify(ultimoIngreso, never()).guardar(any());
    }

    @Test
    void unaLecturaDeLaSalidaLaDecideElCarrilDeSalida() {
        ResultadoSalida resultado = new ResultadoSalida(true, "Salida autorizada", "ABC123", "Carro", null,
                EstadoBarrera.ABIERTA, new BigDecimal("0.60"), Instant.now(), false, true);
        when(pendientes.tomar(eventoId)).thenReturn(Optional.of(pendiente("SALIDA")));
        when(salida.validar(any(), any(), any())).thenReturn(resultado);

        ResultadoValidacion validada = validacion.validar(eventoId, "ABC123", "operador1");

        assertEquals("SALIDA", validada.carril());
        assertTrue(validada.permitido());
        verify(ultimaSalida).guardar(resultado);
        verify(entrada, never()).validar(any(), any(), any());
    }
}