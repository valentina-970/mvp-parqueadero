package com.uam.parqueadero.facturacion;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Puerto de emisión electrónica para una futura integración con un proveedor. */
public interface FacturacionElectronica {

    /** Solicita la factura de un pago confirmado; el ID del pago identifica la emisión. */
    FacturaEmitida emitir(PagoFacturable pago);

    /** Datos operativos del pago ya confirmado, con el valor en pesos colombianos. */
    record PagoFacturable(UUID pagoId, UUID cobroId, BigDecimal valor, Instant pagadoEn) {
    }

    /** Referencia y fecha de emisión que devuelve el proveedor de facturación. */
    record FacturaEmitida(String referencia, Instant emitidaEn) {
    }
}
