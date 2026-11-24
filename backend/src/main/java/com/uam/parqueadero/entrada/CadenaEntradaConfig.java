package com.uam.parqueadero.entrada;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.uam.parqueadero.comun.ClasificadorPlacas;

/** Arma la cadena de validaciones del ingreso en el orden en que se aplican. */
@Configuration
public class CadenaEntradaConfig {

    @Bean
    PasoEntrada cadenaEntrada(ClasificadorPlacas clasificador, UmbralConfianza umbral,
                              BloqueoCupos bloqueo, EstanciasEntrada estancias) {
        PasoEntrada primero = new ValidarFormatoPaso(clasificador);
        primero.enlazar(new VerificarConfianzaPaso(umbral))
               .enlazar(new BloquearCupoPaso(bloqueo))
               .enlazar(new VerificarNoAdentroPaso(estancias));
        return primero;
    }
}