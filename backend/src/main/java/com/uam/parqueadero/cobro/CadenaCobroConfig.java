package com.uam.parqueadero.cobro;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Arma la cadena que decide si un ingreso genera cobro, en el orden en que se aplica. */

@Configuration
public class CadenaCobroConfig {

    @Bean
    PasoCobro cadenaCobro(RegistroCobros cobros, PoliticaTarifa politica){
        PasoCobro primero = new VerificarCobroDelDiaPaso(cobros);
        primero.enlazar(new AplicarTarifaPaso(politica))
            .enlazar(new VerificarTarifaCeroPaso());
        return primero;
    }

}
