package com.uam.parqueadero.salida;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Arma la cadena de validaciones de la salida en el orden en que se aplican. */
@Configuration
public class CadenaSalidaConfig {

    @Bean
    PasoSalida cadenaSalida(ClasificadorPlacas clasificador) {
        PasoSalida primero = new ValidarPlacaPaso(clasificador);
        // Los pasos siguientes se enlazan aquí: primero.enlazar(...).enlazar(...)
        return primero;
    }
}
