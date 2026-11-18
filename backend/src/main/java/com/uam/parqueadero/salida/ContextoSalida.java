package com.uam.parqueadero.salida;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Datos que recorren la cadena de validaciones de la salida. Cada paso lee lo que
 * necesita, agrega lo que descubre y, si corresponde, rechaza la salida con un motivo.
 */
public class ContextoSalida {

    private final SolicitudSalida solicitud;
    private final Instant ahora;
    private final String placa;
    private TipoVehiculoDetectado tipo;
    private String motivoRechazo;
    private BigDecimal valorPendiente;

    public ContextoSalida(SolicitudSalida solicitud, String placaNormalizada, Instant ahora) {
        this.solicitud = solicitud;
        this.placa = placaNormalizada;
        this.ahora = ahora;
    }

    public SolicitudSalida solicitud() {
        return solicitud;
    }

    /** Placa ya normalizada (mayúsculas, sin espacios ni guiones). */
    public String placa() {
        return placa;
    }

    public Instant ahora() {
        return ahora;
    }

    public TipoVehiculoDetectado tipo() {
        return tipo;
    }

    public void tipo(TipoVehiculoDetectado tipo) {
        this.tipo = tipo;
    }

    public BigDecimal valorPendiente() {
        return valorPendiente;
    }

    public void valorPendiente(BigDecimal valorPendiente) {
        this.valorPendiente = valorPendiente;
    }

    /** Marca la salida como rechazada; la cadena se detiene en el paso que llama a este método. */
    public void rechazar(String motivo) {
        this.motivoRechazo = motivo;
    }

    public boolean rechazada() {
        return motivoRechazo != null;
    }

    public String motivoRechazo() {
        return motivoRechazo;
    }
}
