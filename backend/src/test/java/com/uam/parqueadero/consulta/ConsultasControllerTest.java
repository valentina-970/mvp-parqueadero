package com.uam.parqueadero.consulta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.uam.parqueadero.config.SecurityConfig;

/** Pruebas del contrato HTTP de las consultas (HU-09). */
@WebMvcTest(ConsultasController.class)
@Import(SecurityConfig.class)
class ConsultasControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ConsultaEventos eventos;

    @MockitoBean
    private ConsultaCobros cobros;

    @MockitoBean
    private UserDetailsService usuarios;

    /** CP-09-01 (éxito): el filtro por placa llega normalizado y la respuesta trae la página. */
    @Test
    @WithMockUser(roles = "OPERADOR")
    void consultaLosEventosDeUnaPlaca() throws Exception {
        EventoConsultado evento = new EventoConsultado("ABC123", "Carro", "ENTRADA", Instant.now(),
                "Principal", "AUTOMATICA", "PERMITIDO", "Ingreso registrado");
        when(eventos.buscar(any())).thenReturn(Pagina.de(List.of(evento), 0, 1));

        mvc.perform(get("/api/consultas/eventos").param("placa", "abc-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].placa").value("ABC123"))
                .andExpect(jsonPath("$.totalPaginas").value(1));

        ArgumentCaptor<FiltroConsulta> filtro = ArgumentCaptor.forClass(FiltroConsulta.class);
        verify(eventos).buscar(filtro.capture());
        assertEquals("ABC123", filtro.getValue().placa());
    }

    /** CP-09-02 y CA-8: el administrador filtra los cobros por estado. */
    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void consultaLosCobrosPorEstado() throws Exception {
        CobroConsultado cobro = new CobroConsultado("ABC123", LocalDate.of(2026, 11, 12), "DIURNA",
                new BigDecimal("5000"), new BigDecimal("5000"), "PENDIENTE");
        when(cobros.buscar(any())).thenReturn(Pagina.de(List.of(cobro), 0, 1));

        mvc.perform(get("/api/consultas/cobros").param("estado", "PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].estado").value("PENDIENTE"));

        ArgumentCaptor<FiltroConsulta> filtro = ArgumentCaptor.forClass(FiltroConsulta.class);
        verify(cobros).buscar(filtro.capture());
        assertEquals("PENDIENTE", filtro.getValue().estado());
    }

    /** CP-09-06 (error): rango invertido → 400 con el motivo y sin consultar la base. */
    @Test
    @WithMockUser(roles = "OPERADOR")
    void unRangoInvertidoResponde400SinConsultar() throws Exception {
        mvc.perform(get("/api/consultas/cobros").param("desde", "2026-11-12").param("hasta", "2026-11-10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(FiltroConsulta.RANGO_INVALIDO));

        verify(cobros, never()).buscar(any());
    }

    /** CP-09-05 (éxito): sin resultados la página llega vacía; la pantalla muestra "No hay registros". */
    @Test
    @WithMockUser(roles = "OPERADOR")
    void sinResultadosLaPaginaLlegaVacia() throws Exception {
        when(eventos.buscar(any())).thenReturn(Pagina.de(List.of(), 0, 0));

        mvc.perform(get("/api/consultas/eventos").param("placa", "ZZZ999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isEmpty())
                .andExpect(jsonPath("$.totalElementos").value(0));
    }

    @Test
    void sinSesionResponde401() throws Exception {
        mvc.perform(get("/api/consultas/eventos")).andExpect(status().isUnauthorized());
    }
}