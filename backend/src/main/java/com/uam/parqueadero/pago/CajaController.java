package com.uam.parqueadero.pago;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Caja del operador (HU-03): buscar el cobro de una placa y registrar su pago. */
@RestController
@RequestMapping("/api/caja")
@Tag(name = "Caja", description = "Cobros y pagos en efectivo")
public class CajaController {

    private final CajaServicio caja;

    public CajaController(CajaServicio caja) {
        this.caja = caja;
    }

    /** Busca por placa el cobro del día operativo. */
    @Operation(summary = "Buscar el cobro de una placa",
            description = "Responde el cobro del día operativo o 404 con el mensaje "
                    + "\"No hay cobro pendiente para esta placa\".")
    @GetMapping("/cobros/{placa}")
    public CobroConsulta consultar(@PathVariable String placa) {
        return CobroConsulta.de(caja.consultar(placa));
    }
}
