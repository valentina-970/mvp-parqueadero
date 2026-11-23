package com.uam.parqueadero.pago;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

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

    /** Registra el pago en efectivo; el operador es el usuario de la sesión. */
    @Operation(summary = "Registrar el pago en efectivo",
            description = "201 con el pago y el cambio. Si el cobro ya estaba pagado responde 200 con el "
                    + "pago existente, sin crear otro. 422 si el efectivo no alcanza, 409 si el cobro "
                    + "no se puede pagar y 404 si la placa no tiene cobro.")
    @PostMapping("/pagos")
    public ResponseEntity<PagoRegistrado> pagar(@Valid @RequestBody SolicitudPago solicitud,
                                                Authentication operador) {
        PagoDeCaja pago = caja.pagar(solicitud.placa(), solicitud.valorRecibido(), operador.getName());
        // 201 si el pago se acaba de crear; 200 si el cobro ya estaba pagado y se devuelve el existente
        HttpStatus estado = pago.resultado().nuevo() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(estado).body(PagoRegistrado.de(pago));
    }
}
