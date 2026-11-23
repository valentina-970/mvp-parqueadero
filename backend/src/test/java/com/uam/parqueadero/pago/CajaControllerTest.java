package com.uam.parqueadero.pago;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.uam.parqueadero.config.SecurityConfig;

/** Pruebas del contrato HTTP de la caja. */
@WebMvcTest(CajaController.class)
@Import(SecurityConfig.class)
class CajaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CajaServicio caja;

    @MockitoBean
    private UserDetailsService usuarios;

    @Test
    @WithMockUser(roles = "OPERADOR")
    void devuelveElCobroDeLaPlaca() throws Exception {
        when(caja.consultar("ABC123")).thenReturn(new CobroPorPagar(
                UUID.randomUUID(), "ABC123", "Carro", LocalDate.of(2026, 11, 14), "DIURNA",
                new BigDecimal("5000.00"), "PENDIENTE"));

        mvc.perform(get("/api/caja/cobros/ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placa").value("ABC123"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.tarifaAplicada").value("Carro diurna · $5.000"))
                .andExpect(jsonPath("$.valor").value(5000.00));
    }

    /** CP-03-06 (error): placa sin cobro pendiente → 404 con el mensaje para el operador. */
    @Test
    @WithMockUser(roles = "OPERADOR")
    void sinCobroResponde404ConElMensaje() throws Exception {
        when(caja.consultar("ZZZ999")).thenThrow(new CobroNoEncontradoException());

        mvc.perform(get("/api/caja/cobros/ZZZ999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No hay cobro pendiente para esta placa"));
    }

    @Test
    void sinSesionResponde401() throws Exception {
        mvc.perform(get("/api/caja/cobros/ABC123")).andExpect(status().isUnauthorized());
    }

    private static final String PAGO_VALIDO = "{\"placa\":\"ABC123\",\"valorRecibido\":10000}";

    /** CP-03-01 (éxito): el pago responde 201 con el cambio y el pase vigente. */
    @Test
    @WithMockUser(username = "operador1", roles = "OPERADOR")
    void registrarElPagoResponde201ConElCambio() throws Exception {
        Pago pago = new Pago(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("5000.00"),
                new BigDecimal("10000"), new BigDecimal("5000.00"), UUID.randomUUID(), "operador1",
                Instant.parse("2026-11-14T15:15:00Z"), Instant.parse("2026-11-15T10:59:59Z"));
        when(caja.pagar(eq("ABC123"), any(), eq("operador1")))
                .thenReturn(new PagoDeCaja("ABC123", new ResultadoPago(pago, true)));

        mvc.perform(post("/api/caja/pagos").contentType(MediaType.APPLICATION_JSON).content(PAGO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.placa").value("ABC123"))
                .andExpect(jsonPath("$.cambio").value(5000.00))
                .andExpect(jsonPath("$.estadoCobro").value("PAGADO"))
                .andExpect(jsonPath("$.recibidoPor").value("operador1"))
                .andExpect(jsonPath("$.paseVigenteHasta").exists());
    }

    /** CP-03-04 (error): efectivo insuficiente → 422 con el mensaje. */
    @Test
    @WithMockUser(roles = "OPERADOR")
    void conEfectivoInsuficienteResponde422() throws Exception {
        when(caja.pagar(any(), any(), any()))
                .thenThrow(new ValorInsuficienteException(new BigDecimal("2000")));

        mvc.perform(post("/api/caja/pagos").contentType(MediaType.APPLICATION_JSON).content(PAGO_VALIDO))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensaje").value("Valor recibido insuficiente: faltan $2.000"));
    }

    /** CP-03-06 (error): placa sin cobro → 404 con el mensaje. */
    @Test
    @WithMockUser(roles = "OPERADOR")
    void pagarSinCobroResponde404() throws Exception {
        when(caja.pagar(any(), any(), any())).thenThrow(new CobroNoEncontradoException());

        mvc.perform(post("/api/caja/pagos").contentType(MediaType.APPLICATION_JSON).content(PAGO_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No hay cobro pendiente para esta placa"));
    }

    /** CP-03-05 (error): cobro anulado → 409 con el motivo. */
    @Test
    @WithMockUser(roles = "OPERADOR")
    void pagarUnCobroAnuladoResponde409ConElMotivo() throws Exception {
        when(caja.pagar(any(), any(), any()))
                .thenThrow(new CobroNoPagableException("El cobro está anulado y no se puede pagar"));

        mvc.perform(post("/api/caja/pagos").contentType(MediaType.APPLICATION_JSON).content(PAGO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El cobro está anulado y no se puede pagar"));
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    void sinValorRecibidoResponde400() throws Exception {
        mvc.perform(post("/api/caja/pagos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"ABC123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pagarSinSesionResponde401() throws Exception {
        mvc.perform(post("/api/caja/pagos").contentType(MediaType.APPLICATION_JSON).content(PAGO_VALIDO))
                .andExpect(status().isUnauthorized());
    }

    /** CP-03-02 (éxito): si el cobro ya estaba pagado se responde 200 con el pago existente. */
    @Test
    @WithMockUser(username = "operador2", roles = "OPERADOR")
    void repetirElPagoResponde200ConElPagoExistente() throws Exception {
        Pago pago = new Pago(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("5000.00"),
                new BigDecimal("10000"), new BigDecimal("5000.00"), UUID.randomUUID(), "operador1",
                Instant.parse("2026-11-14T15:15:00Z"), Instant.parse("2026-11-15T10:59:59Z"));
        when(caja.pagar(any(), any(), any()))
                .thenReturn(new PagoDeCaja("ABC123", new ResultadoPago(pago, false)));

        mvc.perform(post("/api/caja/pagos").contentType(MediaType.APPLICATION_JSON).content(PAGO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recibidoPor").value("operador1"))
                .andExpect(jsonPath("$.estadoCobro").value("PAGADO"));
    }
}
