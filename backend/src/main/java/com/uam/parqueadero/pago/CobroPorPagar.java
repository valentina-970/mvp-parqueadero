package com.uam.parqueadero.pago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.uam.parqueadero.comun.Dinero;

/**
 * Cobro de un vehículo tal como lo ve la caja (contexto del patrón State). Su estado decide
 * qué pasa cuando se intenta pagar.
 */
public class CobroPorPagar {

    private final UUID id;
    private final String placa;
    private final String tipoVehiculo;
    private final LocalDate diaOperativo;
    private final String franja;
    private final BigDecimal valor;
    private EstadoDelCobro estado;

    /**
     * @param tipoVehiculo nombre del tipo de vehículo (por ejemplo "Carro")
     * @param franja       código de la franja del primer ingreso (por ejemplo "DIURNA")
     * @param valor        valor guardado en el cobro cuando se creó (RN-07)
     * @param estado       estado guardado: PENDIENTE, PAGADO, VENCIDO o ANULADO
     */
    public CobroPorPagar(UUID id, String placa, String tipoVehiculo, LocalDate diaOperativo,
                         String franja, BigDecimal valor, String estado) {
        this.id = id;
        this.placa = placa;
        this.tipoVehiculo = tipoVehiculo;
        this.diaOperativo = diaOperativo;
        this.franja = franja;
        this.valor = valor;
        this.estado = EstadoDelCobro.desde(estado);
    }

    public UUID id() {
        return id;
    }

    public String placa() {
        return placa;
    }

    public String tipoVehiculo() {
        return tipoVehiculo;
    }

    public LocalDate diaOperativo() {
        return diaOperativo;
    }

    public String franja() {
        return franja;
    }

    public BigDecimal valor() {
        return valor;
    }

    /** Nombre del estado actual del cobro. */
    public String estado() {
        return estado.nombre();
    }

    /** Texto de la tarifa con la que se calculó el cobro, por ejemplo "Carro diurna · $5.000". */
    public String tarifaAplicada() {
        return tipoVehiculo + " " + franja.toLowerCase() + " · " + Dinero.formatear(valor);
    }

    /** Intenta pagar el cobro; el estado actual decide si se puede (CA-4, CA-6 y CA-7). */
    public ResultadoPago pagar(DatosPago datos) {
        return estado.pagar(this, datos);
    }

    /** Lo llama el estado cuando el cobro cambia de estado. */
    void cambiarEstado(EstadoDelCobro nuevo) {
        this.estado = nuevo;
    }
}
