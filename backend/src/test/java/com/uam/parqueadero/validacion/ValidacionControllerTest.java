package com.uam.parqueadero.validacion;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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

import com.uam.parqueadero.comun.LecturaPendiente;
import com.uam.parqueadero.config.SecurityConfig;

/** Contrato HTTP de la validación manual (HU-06). */
@WebMvcTest(ValidacionController.class)
@Import(SecurityConfig.class)
class ValidacionControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ValidacionManual validacion;

    @MockitoBean
    private UserDetailsService usuarios;

    private final UUID eventoId = UUID.randomUUID();

    /** CA-3: la pantalla recibe la placa leída y su confianza. */
    @Test
    @WithMockUser(roles = "OPERADOR")
    void listaLasLecturasPendientesDeUnCarril() throws Exception {
        when(validacion.pendientes("ENTRADA")).thenReturn(List.of(new LecturaPendiente(
                eventoId, "ENTRADA", "abc 128", "ABC128", "Carro", new BigDecimal("0.60"), Instant.now())));

        mvc.perform(get("/api/validaciones").param("carril", "ENTRADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].placa").value("ABC128"))
                .andExpect(jsonPath("$[0].confianza").value(0.60));
    }

    /** CA-6: la validación lleva el usuario de la sesión. */
    @Test
    @WithMockUser(username = "operador1", roles = "OPERADOR")
    void validaConElUsuarioDeLaSesion() throws Exception {
        when(validacion.validar(eventoId, "ABC123", "operador1"))
                .thenReturn(new ResultadoValidacion("ENTRADA", "ABC123", true, "Ingreso registrado"));

        mvc.perform(post("/api/validaciones/" + eventoId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"ABC123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permitido").value(true));

        verify(validacion).validar(eventoId, "ABC123", "operador1");
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    void unaLecturaYaValidadaResponde404() throws Exception {
        when(validacion.validar(eq(eventoId), any(), any())).thenThrow(new LecturaNoPendienteException());

        mvc.perform(post("/api/validaciones/" + eventoId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"ABC123\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value(LecturaNoPendienteException.MENSAJE));
    }

    /** CP-06-06 (error): AB12 → 422 con el motivo; la lectura sigue pendiente. */
    @Test
    @WithMockUser(roles = "OPERADOR")
    void unaPlacaSinFormatoResponde422() throws Exception {
        when(validacion.validar(eq(eventoId), eq("AB12"), any()))
                .thenThrow(new PlacaNoValidaException("Formato de placa no válido"));

        mvc.perform(post("/api/validaciones/" + eventoId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"AB12\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensaje").value("Formato de placa no válido"));
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    void unaPlacaVaciaResponde400() throws Exception {
        mvc.perform(post("/api/validaciones/" + eventoId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"  \"}"))
                .andExpect(status().isBadRequest());

        verify(validacion, never()).validar(any(), any(), any());
    }

    @Test
    void sinSesionResponde401() throws Exception {
        mvc.perform(get("/api/validaciones")).andExpect(status().isUnauthorized());
    }
}
