package com.uam.parqueadero.salida;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Arma la cadena de validaciones de la salida en el orden en que se aplican. */
@Configuration
public class CadenaSalidaConfig {

    @Bean
    PasoSalida cadenaSalida(ClasificadorPlacas clasificador, Estancias estancias,
                            CobrosDelDia cobros, Clock reloj) {
        PasoSalida primero = new ValidarPlacaPaso(clasificador);
        primero.enlazar(new VerificarIngresoPaso(estancias))
               .enlazar(new VerificarDeudaPaso(cobros, reloj));
        // Los pasos siguientes se enlazan aquí, a continuación del último
        return primero;
    }
}
