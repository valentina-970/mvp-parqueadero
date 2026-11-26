package com.uam.parqueadero.entrada;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.uam.parqueadero.comun.ClasificadorPlacas;
import com.uam.parqueadero.comun.UmbralConfianza;
import com.uam.parqueadero.cupo.CuposDeLaSede;

/** Arma la cadena de validaciones del ingreso en el orden en que se aplican. */
@Configuration
public class CadenaEntradaConfig {

    @Bean
    PasoEntrada cadenaEntrada(ClasificadorPlacas clasificador, UmbralConfianza umbral,BloqueoCupos bloqueo, EstanciasEntrada estancias, CuposDeLaSede cupos) {
        PasoEntrada primero = new ValidarFormatoPaso(clasificador);
        primero.enlazar(new VerificarConfianzaPaso(umbral))
               .enlazar(new BloquearCupoPaso(bloqueo))
               .enlazar(new VerificarNoAdentroPaso(estancias))
               .enlazar(new VerificarCupoPaso(cupos));
        return primero;
    }
}