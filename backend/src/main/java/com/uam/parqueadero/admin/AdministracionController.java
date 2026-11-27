package com.uam.parqueadero.admin;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.uam.parqueadero.admin.ConfiguracionParqueadero.Tarifas;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

/** API de configuración del parqueadero, accesible únicamente para administradores. */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administración", description = "Solo rol ADMINISTRADOR")
public class AdministracionController {

    private final AdministrarParqueadero parqueadero;

    public AdministracionController(AdministrarParqueadero parqueadero) {
        this.parqueadero = parqueadero;
    }

    /** Devuelve tarifas. */
    @Operation(summary = "Consultar la configuración del parqueadero")
    @GetMapping("/configuracion")
    public ConfiguracionParqueadero consultar() {
        return parqueadero.consultar();
    }

    /** Devuelve las tarifas vigentes por tipo de vehículo. */
    @Operation(summary = "Listar las tarifas vigentes")
    @GetMapping("/tarifas")
    public List<Tarifas> tarifas() {
        return parqueadero.tarifas();
    }

    /** Modifica las tarifas diurna y nocturna del tipo indicado. */
    @Operation(summary = "Cambiar las tarifas de un tipo de vehículo")
    @PostMapping("/tarifas/{tipo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void tarifas(@PathVariable UUID tipo, @Valid @RequestBody TarifasSolicitud solicitud) {
        parqueadero.cambiarTarifas(tipo, solicitud.diurna(), solicitud.nocturna());
    }

    /** Devuelve el motivo del rechazo de una regla de configuración. */
    @ExceptionHandler(ConfiguracionInvalidaException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public Map<String, String> configuracionInvalida(ConfiguracionInvalidaException error) {
        return Map.of("mensaje", error.getMessage());
    }

    /** Devuelve el motivo cuando falta un valor o tiene precisión no admitida. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> solicitudInvalida(MethodArgumentNotValidException error) {
        return Map.of("mensaje", error.getBindingResult().getFieldErrors().getFirst().getDefaultMessage());
    }

    /** Las dos tarifas en pesos, con hasta dos decimales. */
    public record TarifasSolicitud(
            @NotNull(message = "Ingresa la tarifa diurna")
            @Digits(integer = 10, fraction = 2, message = "La tarifa diurna admite 10 enteros y 2 decimales")
            BigDecimal diurna,
            @NotNull(message = "Ingresa la tarifa nocturna")
            @Digits(integer = 10, fraction = 2, message = "La tarifa nocturna admite 10 enteros y 2 decimales")
            BigDecimal nocturna) {
    }

}
