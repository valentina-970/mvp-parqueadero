package com.uam.parqueadero.cobro;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.uam.parqueadero.entrada.IngresoRegistrado;

/**
 * Genera el cobro del día operativo cuando un vehículo entra (HU-02). Escucha el aviso
 * {@link IngresoRegistrado} del carril de entrada (patrón Observer): el carril no sabe que
 * esta clase existe. Se ejecuta dentro de la misma transacción del ingreso, así que si el
 * cobro falla, el ingreso tampoco queda registrado.
 */

@Component
public class GeneradorCobro {

    private static final Logger LOG = LoggerFactory.getLogger(GeneradorCobro.class);

    private final PasoCobro cadena;
    private final RegistroCobros cobros;

    public GeneradorCobro(PasoCobro cadena, RegistroCobros cobros){
        this.cadena = cadena;
        this.cobros = cobros;
    }

    @EventListener
    public void alIngresar(IngresoRegistrado ingreso){
        generar(ingreso);
    }

    /**
     * Aplica las reglas y, si corresponde, crea el cobro en estado Pendiente.
     *
     * @return el id del cobro creado, o vacío si el ingreso no genera cobro
     */
    public Optional<UUID> generar(IngresoRegistrado ingreso){
        ContextoCobro contexto = new ContextoCobro(ingreso);
        cadena.ejecutar(contexto);

        if(contexto.omitido()){
            LOG.info("Sin cobro para {}: {}", ingreso.placa(), ingreso.diaOperativo(), contexto.motivoOmision());
            return Optional.empty();
        }

        TarifaAplicada tarifa = contexto.tarifa();
        return Optional.of(cobros.crear(new NuevoCobro(ingreso.sedeId(),ingreso.placa(),ingreso.tipoVehiculoId(),ingreso.diaOperativo(),tarifa.franjaId(),tarifa.tarifaId(),tarifa.valor(),ingreso.eventoIngresoId(),ingreso.ingresoEn())));
    }

}
