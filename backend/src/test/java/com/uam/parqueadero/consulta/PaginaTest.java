package com.uam.parqueadero.consulta;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

/** CP-09-04: más de 20 resultados se reparten en páginas de 20. */
class PaginaTest {

    @Test
    void calculaElTotalDePaginas() {
        assertEquals(0, Pagina.de(List.of(), 0, 0).totalPaginas());
        assertEquals(1, Pagina.de(List.of(), 0, 20).totalPaginas());
        assertEquals(2, Pagina.de(List.of(), 0, 21).totalPaginas());
    }
}