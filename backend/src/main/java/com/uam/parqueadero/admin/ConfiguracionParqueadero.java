package com.uam.parqueadero.admin;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.uam.parqueadero.cupo.Cupo;

/** Configuración editable de la sede y parámetros compartidos del MVP. */
public record ConfiguracionParqueadero(List<Tarifas> tarifas, Horarios horarios, List<Cupo> cupos,
                                       BigDecimal umbral) {

    /** Tarifas vigentes de ambas franjas para un tipo de vehículo. */
    public record Tarifas(UUID tipoId, String codigo, String nombre, BigDecimal diurna, BigDecimal nocturna) {
    }

    /** Horarios con inicio y fin inclusivos, a precisión de minutos. */
    public record Horarios(LocalTime inicioDiurno, LocalTime finDiurno,
                           LocalTime inicioNocturno, LocalTime finNocturno) {
    }
}
