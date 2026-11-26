package com.uam.parqueadero.validacion;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uam.parqueadero.comun.LecturaPendiente;
import com.uam.parqueadero.comun.SedeActual;
import com.uam.parqueadero.entrada.EstadoIngreso;
import com.uam.parqueadero.entrada.OperacionCarrilEntrada;
import com.uam.parqueadero.entrada.ResultadoIngreso;
import com.uam.parqueadero.entrada.UltimoIngreso;
import com.uam.parqueadero.salida.OperacionCarrilSalida;
import com.uam.parqueadero.salida.ResultadoSalida;
import com.uam.parqueadero.salida.UltimaSalida;

/**
 * Validación manual de lecturas dudosas (HU-06). Busca la lectura pendiente y se la entrega a
 * la fachada de su carril, que vuelve a aplicar sus reglas con la placa del operador. Así la
 * validación no repite ninguna regla de la entrada ni de la salida (CA-5 y CA-7).
 */

@Service
public class ValidacionManual {

    private final LecturasPendientes pendientes;
    private final OperacionCarrilEntrada entrada;
    private final OperacionCarrilSalida salida;
    private final UltimoIngreso ultimoIngreso;
    private final UltimaSalida ultimaSalida;
    private final SedeActual sede;

    public ValidacionManual(LecturasPendientes pendientes, OperacionCarrilEntrada entrada,OperacionCarrilSalida salida, UltimoIngreso ultimoIngreso,UltimaSalida ultimaSalida, SedeActual sede) {
        this.pendientes = pendientes;
        this.entrada = entrada;
        this.salida = salida;
        this.ultimoIngreso = ultimoIngreso;
        this.ultimaSalida = ultimaSalida;
        this.sede = sede;
    }

    /** Lecturas de la sede que esperan al operador (CA-3); {@code carril} puede ser {@code null}. */
    public List<LecturaPendiente> pendientes(String carril) {
        return pendientes.listar(sede.id(), carril);
    }

    /**
     * Confirma o corrige la placa de una lectura pendiente (CA-4) y deja el resultado en la
     * pantalla de su carril.
     *
     * @throws LecturaNoPendienteException si la lectura no existe o ya fue validada
     * @throws PlacaNoValidaException si la placa digitada no tiene formato válido; sigue pendiente
     */
    @Transactional
    public ResultadoValidacion validar(UUID eventoId, String placa, String usuario) {
        LecturaPendiente pendiente = pendientes.tomar(eventoId).orElseThrow(LecturaNoPendienteException::new);

        if ("ENTRADA".equals(pendiente.carril())) {
            ResultadoIngreso resultado = entrada.validar(pendiente, placa, usuario);
            exigirFormato(resultado.tipoVehiculo(), resultado.motivo());
            ultimoIngreso.guardar(resultado);
            return new ResultadoValidacion("ENTRADA", resultado.placa(),
                    resultado.resultado() == EstadoIngreso.PERMITIDO, resultado.motivo());
        }

        ResultadoSalida resultado = salida.validar(pendiente, placa, usuario);
        exigirFormato(resultado.tipoVehiculo(), resultado.motivo());
        ultimaSalida.guardar(resultado);
        return new ResultadoValidacion("SALIDA", resultado.placa(), resultado.abre(), resultado.motivo());
    }

    /** Sin tipo de vehículo la placa no pasó la validación de formato y no se guardó nada. */
    private static void exigirFormato(String tipoVehiculo, String motivo) {
        if (tipoVehiculo == null) {
            throw new PlacaNoValidaException(motivo);
        }
    }
}
