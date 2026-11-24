package com.uam.parqueadero.consulta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

/** Pruebas de los filtros de la consulta (HU-09). */
class FiltroConsultaTest {

    private static final LocalDate DIA_10 = LocalDate.of(2026, 11, 10);
    private static final LocalDate DIA_12 = LocalDate.of(2026, 11, 12);

    @Test
    void normalizaLaPlacaYConvierteLosVaciosEnNull() {
        FiltroConsulta filtro = FiltroConsulta.de(" abc-123 ", null, null, "  ", null);

        assertEquals("ABC123", filtro.placa());
        assertNull(filtro.estado());
        assertEquals(0, filtro.pagina());
        assertTrue(filtro.error().isEmpty());
    }

    /** CP-09-06 (error): la fecha final anterior a la inicial no se consulta. */
    @Test
    void unRangoInvertidoNoEsValido() {
        FiltroConsulta filtro = FiltroConsulta.de(null, DIA_12, DIA_10, null, 0);

        assertEquals(Optional.of(FiltroConsulta.RANGO_INVALIDO), filtro.error());
    }

    @Test
    void unRangoDeUnSoloDiaEsValido() {
        assertTrue(FiltroConsulta.de(null, DIA_10, DIA_10, null, 0).error().isEmpty());
    }

    @Test
    void elEstadoSeAceptaEnMinusculasYSeRechazaSiNoExiste() {
        assertEquals("PENDIENTE", FiltroConsulta.de(null, null, null, "pendiente", 0).estado());
        assertEquals(Optional.of(FiltroConsulta.ESTADO_INVALIDO),
                FiltroConsulta.de(null, null, null, "REGALADO", 0).error());
    }

    /** CA-5: páginas de 20; una página negativa se trata como la primera. */
    @Test
    void calculaElDesplazamientoDeCadaPagina() {
        assertEquals(40, FiltroConsulta.de(null, null, null, null, 2).desplazamiento());
        assertEquals(0, FiltroConsulta.de(null, null, null, null, -3).desplazamiento());
    }
}