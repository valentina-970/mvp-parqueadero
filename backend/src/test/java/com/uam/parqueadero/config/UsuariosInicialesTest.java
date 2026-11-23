package com.uam.parqueadero.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uam.parqueadero.usuario.Rol;
import com.uam.parqueadero.usuario.Usuario;
import com.uam.parqueadero.usuario.UsuarioRepository;

/** Pruebas de los usuarios iniciales: roles (CA-3) y contraseñas con hash (CA-6). */
class UsuariosInicialesTest {

    private UsuarioRepository usuarios;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioRepository.class);
    }

    private UsuariosIniciales crear(String adminClave, String operadorClave) {
        return new UsuariosIniciales(usuarios, encoder, "admin", adminClave, "operador1", operadorClave);
    }

    /** CP-05-03 (éxito): las contraseñas se guardan como hash BCrypt, no en texto plano. */
    @Test
    void guardaLasContrasenasComoHash() {
        when(usuarios.existsByNombreUsuario(any())).thenReturn(false);

        crear("clave-admin", "clave-operador").run(null);

        ArgumentCaptor<Usuario> capturado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios, org.mockito.Mockito.times(2)).save(capturado.capture());
        List<Usuario> guardados = capturado.getAllValues();
        for (Usuario u : guardados) {
            assertNotEquals("clave-admin", u.getContrasenaHash());
            assertNotEquals("clave-operador", u.getContrasenaHash());
            assertTrue(u.getContrasenaHash().startsWith("$2"), "debe ser un hash BCrypt");
        }
        assertTrue(encoder.matches("clave-admin", guardados.get(0).getContrasenaHash()));
    }

    /** CA-3: existen exactamente los roles administrador y operador. */
    @Test
    void creaUnAdministradorYUnOperador() {
        when(usuarios.existsByNombreUsuario(any())).thenReturn(false);

        crear("a", "b").run(null);

        ArgumentCaptor<Usuario> capturado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios, org.mockito.Mockito.times(2)).save(capturado.capture());
        assertEquals(Rol.ADMINISTRADOR, capturado.getAllValues().get(0).getRol());
        assertEquals(Rol.OPERADOR, capturado.getAllValues().get(1).getRol());
        assertEquals(2, Rol.values().length);
    }

    @Test
    void noDuplicaUsuariosExistentes() {
        when(usuarios.existsByNombreUsuario(any())).thenReturn(true);

        crear("a", "b").run(null);

        verify(usuarios, never()).save(any());
    }

    @Test
    void noCreaUsuariosSinContrasena() {
        when(usuarios.existsByNombreUsuario(any())).thenReturn(false);

        crear("", null).run(null);

        verify(usuarios, never()).save(any());
    }
}
