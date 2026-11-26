package com.uam.parqueadero.entrada;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/** El filtro de confianza de la entrada usa el umbral configurado (HU-06, CA-8). */
class VerificarConfianzaPasoTest {

    private final VerificarConfianzaPaso paso = new VerificarConfianzaPaso(() -> new BigDecimal("0.90"));

    private static LecturaPlaca lectura(String confianza) {
        return new LecturaPlaca("ABC123", "ABC123", new BigDecimal(confianza));
    }

    /** CP-06-04 (éxito): umbral cambiado a 0,90; lectura con 0,85 → pasa a validación manual. */
    @Test
    void conElUmbralEnNoventaUnaLecturaDeOchentaYCincoQuedaPendiente() {
        ContextoEntrada contexto = new ContextoEntrada(lectura("0.85"), UUID.randomUUID(), Instant.now());

        paso.aplicar(contexto);

        assertEquals(EstadoIngreso.PENDIENTE_VALIDACION, contexto.estado());
    }

    /** Una lectura que ya validó el operador no vuelve a quedar pendiente por su confianza. */
    @Test
    void unaLecturaManualNoSeDetienePorLaConfianza() {
        ContextoEntrada contexto = ContextoEntrada.manual(lectura("0.50"), UUID.randomUUID(), Instant.now());

        paso.aplicar(contexto);

        assertFalse(contexto.decidido());
    }
}
