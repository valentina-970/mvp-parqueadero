package com.uam.parqueadero.admin;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uam.parqueadero.admin.ConfiguracionParqueadero.Horarios;
import com.uam.parqueadero.admin.ConfiguracionParqueadero.Tarifas;
import com.uam.parqueadero.comun.SedeActual;
import com.uam.parqueadero.comun.UmbralConfianza;
import com.uam.parqueadero.cupo.Cupo;
import com.uam.parqueadero.cupo.CuposDeLaSede;
import com.uam.parqueadero.entrada.BloqueoCupos;

/** Valida y aplica la configuración administrativa del parqueadero (HU-08). */
@Service
public class AdministrarParqueadero {

    private final ConfiguracionAdministrable configuracion;
    private final CuposDeLaSede cupos;
    private final BloqueoCupos bloqueoCupos;
    private final UmbralConfianza umbral;
    private final SedeActual sede;
    private final Clock reloj;

    public AdministrarParqueadero(ConfiguracionAdministrable configuracion, CuposDeLaSede cupos,
                                   BloqueoCupos bloqueoCupos, UmbralConfianza umbral, SedeActual sede, Clock reloj) {
        this.configuracion = configuracion;
        this.cupos = cupos;
        this.bloqueoCupos = bloqueoCupos;
        this.umbral = umbral;
        this.sede = sede;
        this.reloj = reloj;
    }

    /** Consulta todos los valores editables de administración. */
    @Transactional(readOnly = true)
    public ConfiguracionParqueadero consultar() {
        UUID sedeId = sede.id();
        return new ConfiguracionParqueadero(configuracion.tarifas(sedeId), configuracion.horarios(),
                cupos.todos(sedeId), umbral.valor());
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

    /** Guarda dos franjas que cubren todo el día sin huecos ni solapamientos. */
    @Transactional
    public void cambiarHorarios(Horarios horarios) {
        int inicioDiurno = minuto(horarios.inicioDiurno());
        int finDiurno = minuto(horarios.finDiurno());
        int inicioNocturno = minuto(horarios.inicioNocturno());
        int finNocturno = minuto(horarios.finNocturno());
        if ((finDiurno + 1) % 1440 != inicioNocturno || (finNocturno + 1) % 1440 != inicioDiurno
                || inicioDiurno == inicioNocturno) {
            throw new ConfiguracionInvalidaException("Las franjas deben cubrir las 24 horas sin solaparse ni dejar huecos");
        }
        configuracion.guardarHorarios(horarios);
    }

    /** Cambia la capacidad sin quitar lugares a vehículos que ya están adentro. */
    @Transactional
    public void cambiarCupo(UUID tipo, int capacidad) {
        UUID sedeId = sede.id();
        exigirTipo(tipo);
        bloqueoCupos.bloquear(sedeId, tipo);
        int ocupados = cupos.deTipo(sedeId, tipo).map(Cupo::ocupados).orElse(0);
        if (capacidad < ocupados) {
            throw new ConfiguracionInvalidaException("El cupo no puede ser menor que los vehículos adentro (" + ocupados + ")");
        }
        configuracion.guardarCupo(sedeId, tipo, capacidad);
    }

    /** Actualiza el umbral usado para las próximas lecturas en ambos carriles. */
    @Transactional
    public void cambiarUmbral(BigDecimal valor) {
        if (valor.signum() < 0 || valor.compareTo(BigDecimal.ONE) > 0) {
            throw new ConfiguracionInvalidaException("El umbral de confianza debe estar entre 0 y 1");
        }
        configuracion.guardarUmbral(valor);
    }

    private String exigirTipo(UUID tipo) {
        String codigo = configuracion.bloquearTipo(tipo);
        if (codigo == null) {
            throw new ConfiguracionInvalidaException("El tipo de vehículo no existe");
        }
        return codigo;
    }

    private static int minuto(LocalTime hora) {
        if (hora.getSecond() != 0 || hora.getNano() != 0) {
            throw new ConfiguracionInvalidaException("Los horarios deben indicarse en horas y minutos");
        }
        return hora.getHour() * 60 + hora.getMinute();
    }
}
