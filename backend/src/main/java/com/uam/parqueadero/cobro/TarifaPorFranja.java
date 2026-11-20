package com.uam.parqueadero.cobro;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * Estrategia del MVP: tarifa plana según el tipo de vehículo y la franja (diurna o nocturna)
 * de la hora del primer ingreso (SG-02 y SG-03). Las franjas y los valores son datos de la
 * base, así que cambiarlos no exige cambiar este código.
 */

@Component
public class TarifaPorFranja implements PoliticaTarifa{

    private final TarifasVigentes tarifas;
    private final Clock reloj;

    public TarifaPorFranja(TarifasVigentes tarifas, Clock reloj) {
        this.tarifas = tarifas;
        this.reloj = reloj;
    }

    @Override
    public Optional<TarifaAplicada> calcular(UUID sedeId, UUID tipoVehiculoId, Instant momento) {
        LocalTime hora = momento.atZone(reloj.getZone()).toLocalTime().truncatedTo(ChronoUnit.MINUTES);
        return tarifas.buscar(sedeId, tipoVehiculoId, momento).stream()
                .filter(tarifa -> tarifa.cubre(hora))
                .findFirst()
                .map(tarifa -> new TarifaAplicada(tarifa.tarifaId(),tarifa.franjaId(), tarifa.valor()));
    }

}
