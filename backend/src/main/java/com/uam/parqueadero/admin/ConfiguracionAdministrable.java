package com.uam.parqueadero.admin;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.uam.parqueadero.admin.ConfiguracionParqueadero.Horarios;
import com.uam.parqueadero.admin.ConfiguracionParqueadero.Tarifas;

/** Acceso a los datos de configuración que modifica exclusivamente administración. */
public interface ConfiguracionAdministrable {

    /** Consulta las tarifas vigentes por tipo, incluyendo los tipos aún sin tarifas. */
    List<Tarifas> tarifas(UUID sede);

    /** Lee los horarios de las franjas del MVP. */
    Horarios horarios();

    /** Bloquea un tipo para coordinar las ediciones administrativas y devuelve su código. */
    String bloquearTipo(UUID tipo);

    /** Cierra la versión vigente de una tarifa modificada y crea su reemplazo. */
    void reemplazarTarifa(UUID sede, UUID tipo, String franja, BigDecimal valor, OffsetDateTime instante);

    /** Guarda el par de horarios en la misma transacción. */
    void guardarHorarios(Horarios horarios);

}
