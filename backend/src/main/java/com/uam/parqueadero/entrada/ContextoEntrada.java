package com.uam.parqueadero.entrada;

import java.time.Instant;
import java.util.UUID;

import com.uam.parqueadero.comun.TipoVehiculoDetectado;

/**
 * Datos que recorren la cadena de validaciones del ingreso. Cada paso lee lo que necesita,
 * agrega lo que descubre y, si corresponde, decide el resultado con un motivo.
 */
public class ContextoEntrada {

    static final String MOTIVO_REGISTRADO = "Ingreso registrado";

    private final LecturaPlaca lectura;
    private final UUID sedeId;
    private final Instant ahora;
    private TipoVehiculoDetectado tipo;
    private EstadoIngreso estado = EstadoIngreso.PERMITIDO;
    private String motivo = MOTIVO_REGISTRADO;
    private boolean decidido;

    public ContextoEntrada(LecturaPlaca lectura, UUID sedeId, Instant ahora) {
        this.lectura = lectura;
        this.sedeId = sedeId;
        this.ahora = ahora;
    }

    public LecturaPlaca lectura() {
        return lectura;
    }

    /** Placa ya normalizada. */
    public String placa() {
        return lectura.placa();
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

    /** Rechaza la lectura; la cadena se detiene en el paso que llama a este método. */
    public void rechazar(String motivo) {
        decidir(EstadoIngreso.RECHAZADO, motivo);
    }

    /** Deja la lectura para validación manual; la cadena también se detiene. */
    public void dejarPendiente(String motivo) {
        decidir(EstadoIngreso.PENDIENTE_VALIDACION, motivo);
    }

    private void decidir(EstadoIngreso estado, String motivo) {
        this.estado = estado;
        this.motivo = motivo;
        this.decidido = true;
    }

    /** {@code true} si algún paso ya decidió el resultado y la cadena no debe seguir. */
    public boolean decidido() {
        return decidido;
    }

    public EstadoIngreso estado() {
        return estado;
    }

    public String motivo() {
        return motivo;
    }
}