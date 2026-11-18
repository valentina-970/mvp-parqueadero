package com.uam.parqueadero.salida;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final UltimaSalida ultimaSalida;

    public SalidaController(OperacionCarrilSalida operacion, UltimaSalida ultimaSalida) {
        this.operacion = operacion;
        this.ultimaSalida = ultimaSalida;
    }

    /** Procesa la lectura de una placa en el carril de salida. */
    @Operation(summary = "Autorizar o negar la salida",
            description = "Recibe la placa y su confianza (0 a 1). Responde si la barrera abre y el motivo.")
    @PostMapping
    public ResultadoSalida procesar(@Valid @RequestBody SolicitudSalida solicitud) {
        ResultadoSalida resultado = operacion.procesar(solicitud);
        ultimaSalida.guardar(resultado);
        return resultado;
    }

    /** Último intento de salida, para la pantalla del operador. */
    @Operation(summary = "Último intento de salida",
            description = "Responde 204 si todavía no hubo ninguna lectura.")
    @GetMapping("/ultima")
    public ResponseEntity<ResultadoSalida> ultima() {
        return ultimaSalida.ultima()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
