package com.uam.parqueadero.entrada;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** Carril de entrada (HU-01). Recibe la lectura del simulador y registra el ingreso. */
@RestController
@RequestMapping("/api/entrada")
@Tag(name = "Entrada", description = "Carril de entrada y clasificación del vehículo")
public class EntradaController {

    private final OperacionCarrilEntrada operacion;
    private final UltimoIngreso ultimoIngreso;

    public EntradaController(OperacionCarrilEntrada operacion, UltimoIngreso ultimoIngreso) {
        this.operacion = operacion;
        this.ultimoIngreso = ultimoIngreso;
    }

    /** Procesa la lectura de una placa en el carril de entrada. */
    @Operation(summary = "Registrar un ingreso",
            description = "Recibe la placa y su confianza (0 a 1). Responde el resultado: "
                    + "PERMITIDO, PENDIENTE_VALIDACION o RECHAZADO, con su motivo.")
    @PostMapping
    public ResultadoIngreso procesar(@Valid @RequestBody SolicitudIngreso solicitud) {
        ResultadoIngreso resultado = operacion.procesar(solicitud);
        ultimoIngreso.guardar(resultado);
        return resultado;
    }

    /** Última lectura del carril de entrada, para la pantalla del operador. */
    @Operation(summary = "Última lectura de la entrada",
            description = "Responde 204 si todavía no hubo ninguna lectura.")
    @GetMapping("/ultimo")
    public ResponseEntity<ResultadoIngreso> ultimo() {
        return ultimoIngreso.ultimo()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}