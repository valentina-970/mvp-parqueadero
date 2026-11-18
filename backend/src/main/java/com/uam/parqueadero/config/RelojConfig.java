package com.uam.parqueadero.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Reloj del sistema en la zona horaria de Colombia. El dominio recibe un {@link Clock}
 * (abstracción) en vez de llamar a la hora del sistema, así las pruebas fijan la hora.
 */
@Configuration
public class RelojConfig {

    public static final ZoneId ZONA = ZoneId.of("America/Bogota");

    @Bean
    Clock reloj() {
        return Clock.system(ZONA);
    }
}
