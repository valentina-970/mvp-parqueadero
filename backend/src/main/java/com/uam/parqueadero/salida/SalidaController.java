package com.uam.parqueadero.salida;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** Carril de salida (HU-04). Recibe la lectura del simulador y responde si la barrera abre. */
@RestController
@RequestMapping("/api/salida")
@Tag(name = "Salida", description = "Carril de salida y barrera")
public class SalidaController {

    private final OperacionCarrilSalida operacion;

    public SalidaController(OperacionCarrilSalida operacion) {
        this.operacion = operacion;
    }

    /** Procesa la lectura de una placa en el carril de salida. */
    @Operation(summary = "Autorizar o negar la salida",
            description = "Recibe la placa y su confianza (0 a 1). Responde si la barrera abre y el motivo.")
    @PostMapping
    public ResultadoSalida procesar(@Valid @RequestBody SolicitudSalida solicitud) {
        return operacion.procesar(solicitud);
    }
}
