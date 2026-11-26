package com.uam.parqueadero.validacion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.uam.parqueadero.comun.LecturaPendiente;

/** Puerto para encontrar las lecturas que esperan la validación del operador (HU-06). */
public interface LecturasPendientes {

    /**
     * @param carril {@code ENTRADA}, {@code SALIDA} o {@code null} para los dos
     * @return las lecturas pendientes de la sede, de la más antigua a la más reciente
     */
    List<LecturaPendiente> listar(UUID sedeId, String carril);

    /**
     * Toma una lectura pendiente para validarla y la bloquea hasta el final de la transacción,
     * para que dos operadores no la validen al mismo tiempo.
     *
     * @return la lectura, o vacío si no existe o ya fue validada
     */
    Optional<LecturaPendiente> tomar(UUID eventoId);
}