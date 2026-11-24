package com.uam.parqueadero.consulta;

import java.util.List;

/**
 * Una página de resultados, con lo necesario para que la pantalla muestre la paginación.
 *
 * @param pagina          número de esta página, empezando en 0
 * @param totalPaginas    cuántas páginas hay en total
 * @param totalElementos  cuántos registros cumplen el filtro
 */
public record Pagina<T>(List<T> contenido, int pagina, int totalPaginas, long totalElementos) {

    public static <T> Pagina<T> de(List<T> contenido, int pagina, long totalElementos) {
        int totalPaginas = (int) ((totalElementos + FiltroConsulta.TAMANO_PAGINA - 1) / FiltroConsulta.TAMANO_PAGINA);
        return new Pagina<>(contenido, pagina, totalPaginas, totalElementos);
    }
}