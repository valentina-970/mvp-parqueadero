package com.uam.parqueadero.consulta;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Consultas de eventos y cobros (HU-09). Las pueden usar el operador y el administrador (CA-8):
 * basta con tener sesión. Todos los filtros son opcionales; las fechas van como {@code AAAA-MM-DD}.
 */
@RestController
@RequestMapping("/api/consultas")
@Tag(name = "Consultas", description = "Eventos de acceso y cobros con filtros")
public class ConsultasController {

    private final ConsultaEventos eventos;
    private final ConsultaCobros cobros;

    public ConsultasController(ConsultaEventos eventos, ConsultaCobros cobros) {
        this.eventos = eventos;
        this.cobros = cobros;
    }

    @Operation(summary = "Consultar eventos de acceso",
            description = "Filtra por placa y rango de fechas. Páginas de 20, del más reciente al más antiguo. "
                    + "400 si la fecha final es anterior a la inicial.")
    @GetMapping("/eventos")
    public ResponseEntity<?> eventos(
            @RequestParam(required = false) String placa,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer pagina) {
        return responder(FiltroConsulta.de(placa, desde, hasta, null, pagina), eventos::buscar);
    }

    @Operation(summary = "Consultar cobros",
            description = "Filtra por placa, rango de días operativos y estado (PENDIENTE, PAGADO, VENCIDO o ANULADO). "
                    + "Páginas de 20, del más reciente al más antiguo. 400 si el rango o el estado no son válidos.")
    @GetMapping("/cobros")
    public ResponseEntity<?> cobros(
            @RequestParam(required = false) String placa,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer pagina) {
        return responder(FiltroConsulta.de(placa, desde, hasta, estado, pagina), cobros::buscar);
    }

    /** Si el filtro no es válido responde 400 con el motivo y no consulta la base (CA-7). */
    private <T> ResponseEntity<?> responder(FiltroConsulta filtro, Function<FiltroConsulta, Pagina<T>> consulta) {
        Optional<String> error = filtro.error();
        if (error.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", error.get()));
        }
        return ResponseEntity.ok(consulta.apply(filtro));
    }
}