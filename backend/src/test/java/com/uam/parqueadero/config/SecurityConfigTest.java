package com.uam.parqueadero.config;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.uam.parqueadero.admin.AdministracionController;
import com.uam.parqueadero.admin.AdministrarParqueadero;
import com.uam.parqueadero.controller.HealthController;

/** Pruebas del acceso por rol con la cadena de seguridad real (CA-4, CA-5 y CA-7). */
@WebMvcTest(controllers = { AdministracionController.class, HealthController.class })
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserDetailsService usuarios;

    @MockitoBean
    private AdministrarParqueadero administracion;

    /** CP-05-05 (error): el operador llama al endpoint de tarifas → 403. */
    @Test
    @WithMockUser(roles = "OPERADOR")
    void elOperadorNoAccedeALaAdministracion() throws Exception {
        mvc.perform(get("/api/admin/tarifas"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    /** CP-05-06 (error): petición sin sesión → 401. */
    @Test
    void sinSesionResponde401() throws Exception {
        mvc.perform(get("/api/admin/tarifas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    /** CA-5: el administrador accede a la administración. */
    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void elAdministradorAccedeALaAdministracion() throws Exception {
        mvc.perform(get("/api/admin/tarifas")).andExpect(status().isOk());
    }

    /** La documentación Swagger se puede abrir sin iniciar sesión. */
    @Test
    void laDocumentacionSwaggerEsPublica() throws Exception {
        // Sin sesión no debe responder 401: la seguridad deja pasar la ruta
        assertNotEquals(401,
                mvc.perform(get("/v3/api-docs")).andReturn().getResponse().getStatus());
    }

    /** El estado de la API no exige sesión: lo usa el frontend para comprobar la conexión. */
    @Test
    void elEstadoDeLaApiEsPublico() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk());
    }
}
