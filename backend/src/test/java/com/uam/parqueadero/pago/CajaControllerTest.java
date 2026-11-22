package com.uam.parqueadero.pago;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
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
}
