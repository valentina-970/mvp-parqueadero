package com.uam.parqueadero.pago;

/**
 * Estado de un cobro (patrón State). Cada estado decide qué pasa cuando se intenta pagar,
 * así el cobro no necesita una cadena de condicionales por estado y agregar un estado nuevo
 * es agregar una clase (principio O).
 */
public interface EstadoDelCobro {

    /** Nombre del estado tal como se guarda en la base: PENDIENTE, PAGADO, VENCIDO o ANULADO. */
    String nombre();

    /** Intenta pagar el cobro; si el estado lo permite, cambia el estado del cobro. */
    ResultadoPago pagar(CobroPorPagar cobro, DatosPago datos);

    /** Crea el estado que corresponde al nombre guardado en la base. */
    static EstadoDelCobro desde(String nombre) {
        return switch (nombre) {
            case "PENDIENTE" -> new CobroPendiente();
            case "PAGADO" -> new CobroPagado();
            default -> new CobroNoPagable(nombre);
        };
    }
}
