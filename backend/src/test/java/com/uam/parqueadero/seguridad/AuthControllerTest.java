package com.uam.parqueadero.seguridad;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.uam.parqueadero.usuario.Rol;
import com.uam.parqueadero.usuario.Usuario;
import com.uam.parqueadero.usuario.UsuarioRepository;

/**
 * Pruebas del inicio de sesión. Usan el autenticador real con el repositorio simulado,
 * de modo que verifican la comparación contra el hash sin necesitar base de datos.
 */
class AuthControllerTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        when(usuarios.findByNombreUsuario("admin"))
                .thenReturn(Optional.of(new Usuario("admin", encoder.encode("clave-admin"), Rol.ADMINISTRADOR)));
        when(usuarios.findByNombreUsuario("operador1"))
                .thenReturn(Optional.of(new Usuario("operador1", encoder.encode("clave-op"), Rol.OPERADOR)));

        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider(new UsuarioDetailsService(usuarios));
        proveedor.setPasswordEncoder(encoder);
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(new ProviderManager(proveedor))).build();
    }

    private String cuerpo(String usuario, String contrasena) {
        return "{\"usuario\":\"" + usuario + "\",\"contrasena\":\"" + contrasena + "\"}";
    }

    /** CP-05-01 (éxito): el administrador entra y recibe su rol para ver la administración. */
    @Test
    void elAdministradorIniciaSesion() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("admin", "clave-admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value("admin"))
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"))
                .andExpect(request().sessionAttribute("SPRING_SECURITY_CONTEXT",
                        org.hamcrest.Matchers.notNullValue()));
    }

    /** CP-05-02 (éxito): el operador entra y su rol es OPERADOR (sin administración). */
    @Test
    void elOperadorIniciaSesion() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("operador1", "clave-op")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("OPERADOR"));
    }

    /** CP-05-04 (error): contraseña incorrecta → mensaje genérico de credenciales inválidas. */
    @Test
    void conContrasenaIncorrectaMuestraMensajeGenerico() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("admin", "otra")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos"));
    }

    /** CA-2: el mensaje es idéntico si el usuario no existe, para no revelar cuál dato falló. */
    @Test
    void conUsuarioInexistenteMuestraElMismoMensaje() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("fantasma", "x")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos"));
    }

    /** CA-8: al cerrar sesión la sesión HTTP queda invalidada. */
    @Test
    void cerrarSesionInvalidaLaSesion() throws Exception {
        MockHttpSession sesion = new MockHttpSession();

        mvc.perform(post("/api/auth/logout").session(sesion))
                .andExpect(status().isNoContent());

        assertTrue(sesion.isInvalid());
    }

    @Test
    void cerrarSesionSinSesionTambienResponde204() throws Exception {
        mvc.perform(post("/api/auth/logout")).andExpect(status().isNoContent());
    }

    /** El frontend consulta quién está conectado para mostrar su nombre y rol. */
    @Test
    void yoDevuelveElUsuarioYSuRol() throws Exception {
        UsernamePasswordAuthenticationToken autenticacion =
                UsernamePasswordAuthenticationToken.authenticated(
                        "operador1", null,
                        List.of(new SimpleGrantedAuthority(
                                "ROLE_OPERADOR")));

        mvc.perform(get("/api/auth/yo").principal(autenticacion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value("operador1"))
                .andExpect(jsonPath("$.rol").value("OPERADOR"));
    }

    @Test
    void sinDatosResponde400() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("", "")))
                .andExpect(status().isBadRequest());
    }
}
