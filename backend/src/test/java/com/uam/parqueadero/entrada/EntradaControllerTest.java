package com.uam.parqueadero.entrada;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

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

/** Pruebas del contrato HTTP del carril de entrada (CA-1 y CA-8). */
@WebMvcTest(EntradaController.class)
@Import(SecurityConfig.class)
class EntradaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private OperacionCarrilEntrada operacion;

    @MockitoBean
    private UserDetailsService usuarios;

    @MockitoBean
    private UltimoIngreso ultimoIngreso;

    private static final String CUERPO_VALIDO = "{\"placa\":\"ABC123\",\"confianza\":0.95}";

    private ResultadoIngreso registrado() {
        return new ResultadoIngreso(EstadoIngreso.PERMITIDO, "Ingreso registrado", "ABC123", "Carro",
                new BigDecimal("0.95"), Instant.now());
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    void recibeLaPlacaYLaConfianzaYResponde() throws Exception {
        when(operacion.procesar(any())).thenReturn(registrado());

        mvc.perform(post("/api/entrada").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("PERMITIDO"))
                .andExpect(jsonPath("$.tipoVehiculo").value("Carro"));
    }

    /** CP-01-06 (error): una confianza fuera de rango se rechaza. */
    @Test
    @WithMockUser(roles = "OPERADOR")
    void unaConfianzaFueraDeRangoSeRechaza() throws Exception {
        mvc.perform(post("/api/entrada").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"ABC123\",\"confianza\":1.5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    void elUltimoIngresoSeConsultaParaLaPantalla() throws Exception {
        when(ultimoIngreso.ultimo()).thenReturn(Optional.of(registrado()));

        mvc.perform(get("/api/entrada/ultimo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placa").value("ABC123"));
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    void sinLecturasElUltimoIngresoResponde204() throws Exception {
        when(ultimoIngreso.ultimo()).thenReturn(Optional.empty());

        mvc.perform(get("/api/entrada/ultimo")).andExpect(status().isNoContent());
    }

    @Test
    void sinSesionResponde401() throws Exception {
        mvc.perform(post("/api/entrada").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isUnauthorized());
    }
}