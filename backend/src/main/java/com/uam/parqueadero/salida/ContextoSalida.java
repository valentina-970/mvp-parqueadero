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
    private final boolean manual;
    private TipoVehiculoDetectado tipo;
    private EstanciaAbierta estancia;
    private String motivoRechazo;
    private String motivoAutorizacion = "Salida autorizada";
    private BigDecimal valorPendiente;
    private String motivoPendiente;

    public ContextoSalida(SolicitudSalida solicitud, String placaNormalizada, UUID sedeId, Instant ahora) {
        this(solicitud, placaNormalizada, sedeId, ahora, false);
    }

    private ContextoSalida(SolicitudSalida solicitud, String placaNormalizada, UUID sedeId, Instant ahora,boolean manual) {
        this.solicitud = solicitud;
        this.placa = placaNormalizada;
        this.sedeId = sedeId;
        this.ahora = ahora;
        this.manual = manual;
    }

    /** Contexto de una salida cuya placa ya confirmó o corrigió el operador (HU-06). */
    public static ContextoSalida manual(SolicitudSalida solicitud, String placaNormalizada, UUID sedeId,Instant ahora) {
        return new ContextoSalida(solicitud, placaNormalizada, sedeId, ahora, true);
    }

    /** {@code true} si la placa la validó el operador; entonces no se vuelve a exigir confianza. */
    public boolean manual() {
        return manual;
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

    /**
     * Deja la salida esperando la validación del operador (HU-06, CA-7): la barrera no se mueve
     * y la cadena se detiene en el paso que llama a este método.
     */
    public void dejarPendiente(String motivo) {
        this.motivoPendiente = motivo;
    }

    public boolean pendiente() {
        return motivoPendiente != null;
    }

    public String motivoPendiente() {
        return motivoPendiente;
    }
}
