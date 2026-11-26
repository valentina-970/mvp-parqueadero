package com.uam.parqueadero.salida;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/** Filtro de confianza del carril de salida (HU-06, CA-7). */
class VerificarConfianzaSalidaPasoTest {

    private final VerificarConfianzaSalidaPaso paso = new VerificarConfianzaSalidaPaso(() -> new BigDecimal("0.80"));

    private static SolicitudSalida solicitud(String confianza) {
        return new SolicitudSalida("ABC123", new BigDecimal(confianza));
    }

    /** CP-06-05 (éxito): salida con confianza 0,50 → se pide validación antes de decidir la barrera. */
    @Test
    void unaSalidaConConfianzaBajaQuedaPendiente() {
        ContextoSalida contexto = new ContextoSalida(solicitud("0.50"), "ABC123", UUID.randomUUID(), Instant.now());

        paso.aplicar(contexto);

        assertTrue(contexto.pendiente());
        assertEquals(VerificarConfianzaSalidaPaso.MOTIVO, contexto.motivoPendiente());
    }

    /** CA-2 aplicado a la salida: una confianza igual al umbral se procesa de forma automática. */
    @Test
    void unaConfianzaIgualAlUmbralSigueAutomatica() {
        ContextoSalida contexto = new ContextoSalida(solicitud("0.80"), "ABC123", UUID.randomUUID(), Instant.now());

        paso.aplicar(contexto);

        assertFalse(contexto.pendiente());
    }

    @Test
    void unaSalidaQueValidoElOperadorNoVuelveAQuedarPendiente() {
        ContextoSalida contexto = ContextoSalida.manual(solicitud("0.50"), "ABC123", UUID.randomUUID(), Instant.now());

        paso.aplicar(contexto);

        assertFalse(contexto.pendiente());
    }
}