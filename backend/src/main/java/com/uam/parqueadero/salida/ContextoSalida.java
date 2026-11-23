package com.uam.parqueadero.salida;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.uam.parqueadero.comun.TipoVehiculoDetectado;

/**
 * Datos que recorren la cadena de validaciones de la salida. Cada paso lee lo
 * que
 * necesita, agrega lo que descubre y, si corresponde, rechaza la salida con un
 * motivo.
 */
public class ContextoSalida {

    private final SolicitudSalida solicitud;
    private final Instant ahora;
    private final String placa;
    private final UUID sedeId;
    private TipoVehiculoDetectado tipo;
    private EstanciaAbierta estancia;
    private String motivoRechazo;
    private String motivoAutorizacion = "Salida autorizada";
    private BigDecimal valorPendiente;

    public ContextoSalida(SolicitudSalida solicitud, String placaNormalizada, UUID sedeId, Instant ahora) {
        this.solicitud = solicitud;
        this.placa = placaNormalizada;
        this.sedeId = sedeId;
        this.ahora = ahora;
    }

    public SolicitudSalida solicitud() {
        return solicitud;
    }

    /** Placa ya normalizada (mayúsculas, sin espacios ni guiones). */
    public String placa() {
        return placa;
    }

    public UUID sedeId() {
        return sedeId;
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

    /** Estancia abierta del vehículo; la deja el paso que verifica el ingreso. */
    public EstanciaAbierta estancia() {
        return estancia;
    }

    public void estancia(EstanciaAbierta estancia) {
        this.estancia = estancia;
    }

    public BigDecimal valorPendiente() {
        return valorPendiente;
    }

    public void valorPendiente(BigDecimal valorPendiente) {
        this.valorPendiente = valorPendiente;
    }

    /**
     * Deja el motivo con el que se autoriza la salida (por ejemplo "Cobro del día
     * pagado").
     */
    public void autorizar(String motivo) {
        this.motivoAutorizacion = motivo;
    }

    public String motivoAutorizacion() {
        return motivoAutorizacion;
    }

    /**
     * Marca la salida como rechazada; la cadena se detiene en el paso que llama a
     * este método.
     */
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
