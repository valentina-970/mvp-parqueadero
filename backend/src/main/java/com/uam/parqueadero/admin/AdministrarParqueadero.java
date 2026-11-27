package com.uam.parqueadero.admin;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uam.parqueadero.admin.ConfiguracionParqueadero.Tarifas;
import com.uam.parqueadero.comun.SedeActual;

/** Valida y aplica la configuración administrativa del parqueadero (HU-08). */
@Service
public class AdministrarParqueadero {

    private final ConfiguracionAdministrable configuracion;
    private final SedeActual sede;
    private final Clock reloj;

    public AdministrarParqueadero(ConfiguracionAdministrable configuracion, SedeActual sede, Clock reloj) {
        this.configuracion = configuracion;
        this.sede = sede;
        this.reloj = reloj;
    }

    /** Consulta todos los valores editables de administración. */
    @Transactional(readOnly = true)
    public ConfiguracionParqueadero consultar() {
        UUID sedeId = sede.id();
        return new ConfiguracionParqueadero(configuracion.tarifas(sedeId));
    }

    /** Consulta las tarifas vigentes de la sede. */
    public List<Tarifas> tarifas() {
        return configuracion.tarifas(sede.id());
    }

    /** Guarda las dos tarifas de un tipo juntas y conserva las versiones usadas por cobros previos. */
    @Transactional
    public void cambiarTarifas(UUID tipo, BigDecimal diurna, BigDecimal nocturna) {
        UUID sedeId = sede.id();
        String codigo = exigirTipo(tipo);
        boolean motoGratis = "MOTO".equals(codigo) && diurna.signum() == 0 && nocturna.signum() == 0;
        if (diurna.signum() < 0 || nocturna.signum() < 0) {
            throw new ConfiguracionInvalidaException("Las tarifas no pueden ser negativas");
        }
        if (!motoGratis && nocturna.compareTo(diurna) >= 0) {
            throw new ConfiguracionInvalidaException("RN-02: la tarifa nocturna debe ser menor que la diurna");
        }
        OffsetDateTime ahora = OffsetDateTime.now(reloj).truncatedTo(ChronoUnit.MICROS);
        configuracion.reemplazarTarifa(sedeId, tipo, "DIURNA", diurna, ahora);
        configuracion.reemplazarTarifa(sedeId, tipo, "NOCTURNA", nocturna, ahora);
    }

    private String exigirTipo(UUID tipo) {
        String codigo = configuracion.bloquearTipo(tipo);
        if (codigo == null) {
            throw new ConfiguracionInvalidaException("El tipo de vehículo no existe");
        }
        return codigo;
    }

}
