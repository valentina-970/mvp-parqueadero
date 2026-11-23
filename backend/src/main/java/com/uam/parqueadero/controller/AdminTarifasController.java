package com.uam.parqueadero.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Marcador solo para administradores. Permite comprobar el 403 del operador (CP-05-05).
 * HU-08 lo reemplaza con la administración real de tarifas.
 */
@RestController
@RequestMapping("/api/admin/tarifas")
@Tag(name = "Administración", description = "Solo rol ADMINISTRADOR")
public class AdminTarifasController {

    @Operation(summary = "Listar tarifas (marcador hasta HU-08)")
    @GetMapping
    public List<Object> listar() {
        return List.of();
    }
}
