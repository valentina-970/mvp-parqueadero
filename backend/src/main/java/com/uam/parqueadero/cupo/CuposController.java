package com.uam.parqueadero.cupo;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uam.parqueadero.comun.SedeActual;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Cupos disponibles por tipo de vehículo (HU-07, CA-6). Lo consultan la entrada y el simulador. */
@RestController
@RequestMapping("/api/cupos")
@Tag(name = "Cupos", description = "Cupos disponibles por tipo de vehículo")
public class CuposController {

    private final CuposDeLaSede cupos;
    private final SedeActual sede;

    public CuposController(CuposDeLaSede cupos, SedeActual sede) {
        this.cupos = cupos;
        this.sede = sede;
    }

    /** Capacidad, ocupados y disponibles de cada tipo de vehículo en la sede. */
    @Operation(summary = "Cupos disponibles por tipo de vehículo",
            description = "Los ocupados son los vehículos que están adentro (estancias abiertas).")
    @GetMapping
    @Transactional(readOnly = true)
    public List<CupoDisponible> listar() {
        return cupos.todos(sede.id()).stream().map(CupoDisponible::de).toList();
    }
}
