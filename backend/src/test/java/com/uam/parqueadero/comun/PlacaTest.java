package com.uam.parqueadero.comun;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PlacaTest {

    @Test
    void normalizaMayusculasEspaciosYGuiones() {
        assertEquals("ABC123", Placa.normalizar(" abc-123 "));
    }

    @Test
    void unaPlacaNulaQuedaVacia() {
        assertEquals("", Placa.normalizar(null));
    }
}
