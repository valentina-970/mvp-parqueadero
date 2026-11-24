package com.uam.parqueadero.consulta;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import com.uam.parqueadero.comun.Placa;

/**
 * Filtros de una consulta de eventos o de cobros (HU-09). Todos son opcionales y se combinan
 * entre sí (CA-3 y CA-4). Los resultados se entregan en páginas de 20 (CA-5).
 *
 * @param placa   placa normalizada, o {@code null} para no filtrar
 * @param desde   primer día incluido, o {@code null}
 * @param hasta   último día incluido, o {@code null}
 * @param estado  estado del cobro (solo en la consulta de cobros), o {@code null}
 * @param pagina  número de página, empezando en 0
 */
public record FiltroConsulta(String placa, LocalDate desde, LocalDate hasta, String estado, int pagina) {

    public static final int TAMANO_PAGINA = 20;
    static final String RANGO_INVALIDO = "La fecha final no puede ser anterior a la inicial";
    static final String ESTADO_INVALIDO = "Estado de cobro no válido";
    private static final Set<String> ESTADOS = Set.of("PENDIENTE", "PAGADO", "VENCIDO", "ANULADO");

    /**
     * Arma el filtro con lo que llega en la URL: normaliza la placa igual que los carriles,
     * convierte los textos vacíos en {@code null} y no deja páginas negativas.
     */
    public static FiltroConsulta de(String placa, LocalDate desde, LocalDate hasta, String estado,
                                    Integer pagina) {
        String placaNormalizada = placa == null || placa.isBlank() ? null : Placa.normalizar(placa);
        String estadoNormalizado = estado == null || estado.isBlank() ? null : estado.trim().toUpperCase();
        int numeroPagina = pagina == null ? 0 : Math.max(0, pagina);
        return new FiltroConsulta(placaNormalizada, desde, hasta, estadoNormalizado, numeroPagina);
    }

    /** @return el motivo por el que el filtro no se puede consultar, o vacío si es válido (CA-7) */
    public Optional<String> error() {
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            return Optional.of(RANGO_INVALIDO);
        }
        if (estado != null && !ESTADOS.contains(estado)) {
            return Optional.of(ESTADO_INVALIDO);
        }
        return Optional.empty();
    }

    /** Cuántos registros se saltan para llegar a esta página. */
    public int desplazamiento() {
        return pagina * TAMANO_PAGINA;
    }
}