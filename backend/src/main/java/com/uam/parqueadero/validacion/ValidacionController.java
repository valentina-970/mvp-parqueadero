package com.uam.parqueadero.validacion;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uam.parqueadero.comun.LecturaPendiente;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** Validación manual de las lecturas con confianza baja, en la entrada y en la salida (HU-06). */
@RestController
@RequestMapping("/api/validaciones")
@Tag(name = "Validación manual", description = "Lecturas dudosas que confirma o corrige el operador")
public class ValidacionController {

    private final ValidacionManual validacion;

    public ValidacionController(ValidacionManual validacion) {
        this.validacion = validacion;
    }

    @Operation(summary = "Lecturas pendientes de validación",
            description = "Placa leída, confianza y carril de cada lectura que espera al operador. "
                    + "Filtro opcional: carril=ENTRADA o carril=SALIDA.")
    @GetMapping
    public List<LecturaPendiente> pendientes(@RequestParam(required = false) String carril) {
        return validacion.pendientes(carril);
    }

    @Operation(summary = "Confirmar o corregir una lectura",
            description = "Con la misma placa la confirma; con otra la corrige. El carril decide como si la "
                    + "lectura fuera automática y el evento queda como lectura manual con el usuario. "
                    + "404 si ya fue validada; 422 si la placa no tiene formato válido.")
    @PostMapping("/{eventoId}")
    public ResultadoValidacion validar(@PathVariable UUID eventoId,
                                       @Valid @RequestBody SolicitudValidacion solicitud,
                                       Authentication usuario) {
        return validacion.validar(eventoId, solicitud.placa(), usuario.getName());
    }
}