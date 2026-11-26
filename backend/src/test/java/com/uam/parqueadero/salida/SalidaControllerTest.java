package com.uam.parqueadero.salida;

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

/** Pruebas del contrato HTTP del carril de salida (CA-1). */
@WebMvcTest(SalidaController.class)
@Import(SecurityConfig.class)
class SalidaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private OperacionCarrilSalida operacion;

    @MockitoBean
    private UserDetailsService usuarios;

    @MockitoBean
    private UltimaSalida ultimaSalida;

    private static final String CUERPO_VALIDO = "{\"placa\":\"ABC123\",\"confianza\":0.95}";

    @Test
    @WithMockUser(roles = "OPERADOR")
    void recibeLaPlacaYLaConfianzaYResponde() throws Exception {
        when(operacion.procesar(any())).thenReturn(new ResultadoSalida(
                true, "Salida autorizada", "ABC123", "Carro", null, EstadoBarrera.ABIERTA, new BigDecimal("0.95"), Instant.now(), false, false));

        mvc.perform(post("/api/salida").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.abre").value(true))
                .andExpect(jsonPath("$.placa").value("ABC123"));
    }

    /** CP-01-06 aplicado al carril: una confianza fuera de rango se rechaza. */
    @Test
    @WithMockUser(roles = "OPERADOR")
    void unaConfianzaFueraDeRangoSeRechaza() throws Exception {
        mvc.perform(post("/api/salida").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"ABC123\",\"confianza\":1.5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    void sinConfianzaSeRechaza() throws Exception {
        mvc.perform(post("/api/salida").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"ABC123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    void laUltimaSalidaSeConsultaParaLaPantalla() throws Exception {
        when(ultimaSalida.ultima()).thenReturn(Optional.of(new ResultadoSalida(
                false, "Cobro pendiente por $5.000", "ABC123", "Carro", new BigDecimal("5000"),
                EstadoBarrera.CERRADA, new BigDecimal("0.97"), Instant.now(), false, false)));

        mvc.perform(get("/api/salida/ultima"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.abre").value(false))
                .andExpect(jsonPath("$.motivo").value("Cobro pendiente por $5.000"))
                .andExpect(jsonPath("$.barrera").value("CERRADA"));
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    void sinLecturasLaUltimaSalidaResponde204() throws Exception {
        when(ultimaSalida.ultima()).thenReturn(Optional.empty());

        mvc.perform(get("/api/salida/ultima")).andExpect(status().isNoContent());
    }

    @Test
    void sinSesionResponde401() throws Exception {
        mvc.perform(post("/api/salida").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isUnauthorized());
    }
}
