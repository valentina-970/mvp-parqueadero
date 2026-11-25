package com.uam.parqueadero.cupo;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.uam.parqueadero.comun.SedeActual;
import com.uam.parqueadero.config.SecurityConfig;

/** Pruebas del contrato HTTP de los cupos (HU-07, CA-6). */
@WebMvcTest(CuposController.class)
@Import(SecurityConfig.class)
class CuposControllerTest {

    private static final UUID SEDE = UUID.randomUUID();

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CuposDeLaSede cupos;

    @MockitoBean
    private SedeActual sede;

    @MockitoBean
    private UserDetailsService usuarios;

    @Test
    @WithMockUser(roles = "OPERADOR")
    void listaLosCuposDeCadaTipoConSusDisponibles() throws Exception {
        when(sede.id()).thenReturn(SEDE);
        when(cupos.todos(SEDE)).thenReturn(List.of(
                new Cupo(UUID.randomUUID(), "CARRO", "Carro", 40, 28),
                new Cupo(UUID.randomUUID(), "MOTO", "Moto", 20, 20)));

        mvc.perform(get("/api/cupos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("CARRO"))
                .andExpect(jsonPath("$[0].disponibles").value(12))
                .andExpect(jsonPath("$[0].lleno").value(false))
                .andExpect(jsonPath("$[1].codigo").value("MOTO"))
                .andExpect(jsonPath("$[1].disponibles").value(0))
                .andExpect(jsonPath("$[1].lleno").value(true));
    }

    @Test
    void sinSesionResponde401() throws Exception {
        mvc.perform(get("/api/cupos")).andExpect(status().isUnauthorized());
    }
}
