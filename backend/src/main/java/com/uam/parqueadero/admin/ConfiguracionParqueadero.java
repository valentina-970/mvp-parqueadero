package com.uam.parqueadero.admin;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


/** Configuración editable de la sede y parámetros compartidos del MVP. */
public record ConfiguracionParqueadero(List<Tarifas> tarifas) {

    /** Tarifas vigentes de ambas franjas para un tipo de vehículo. */
    public record Tarifas(UUID tipoId, String codigo, String nombre, BigDecimal diurna, BigDecimal nocturna) {
    }

}
