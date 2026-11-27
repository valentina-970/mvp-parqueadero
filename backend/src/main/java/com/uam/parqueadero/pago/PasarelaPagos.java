package com.uam.parqueadero.pago;

import java.math.BigDecimal;
import java.util.UUID;

/** Puerto para solicitar pagos externos sin acoplar el dominio a un proveedor. */
public interface PasarelaPagos {

    /**
     * Solicita el pago de un cobro. El adaptador debe reutilizar la misma operación al
     * recibir de nuevo su identificador de intento, sin duplicar el cargo.
     */
    Resultado solicitar(Solicitud solicitud);

    /** Datos del intento; el valor está expresado en pesos colombianos. */
    record Solicitud(UUID intentoId, UUID cobroId, BigDecimal valor) {
    }

    /** Identificador de la operación en el proveedor y su estado al responder. */
    record Resultado(String referencia, Estado estado) {
    }

    /** Estados que el dominio reconoce en una respuesta de pasarela. */
    enum Estado {
        PENDIENTE, CONFIRMADO, RECHAZADO
    }
}
