package com.uam.parqueadero.seguridad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.uam.parqueadero.usuario.Rol;
import com.uam.parqueadero.usuario.Usuario;
import com.uam.parqueadero.usuario.UsuarioRepository;

/** Pruebas de la carga de usuarios para Spring Security (CA-1 y CA-3). */
class UsuarioDetailsServiceTest {

    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final UsuarioDetailsService servicio = new UsuarioDetailsService(usuarios);

    @Test
    void cargaUnAdministradorConSuRol() {
        when(usuarios.findByNombreUsuario("admin"))
                .thenReturn(Optional.of(new Usuario("admin", "hash", Rol.ADMINISTRADOR)));

        UserDetails detalles = servicio.loadUserByUsername("admin");

        assertEquals("admin", detalles.getUsername());
        assertTrue(detalles.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR")));
        assertTrue(detalles.isEnabled());
    }

    @Test
    void cargaUnOperadorConSuRol() {
        when(usuarios.findByNombreUsuario("operador1"))
                .thenReturn(Optional.of(new Usuario("operador1", "hash", Rol.OPERADOR)));

        UserDetails detalles = servicio.loadUserByUsername("operador1");

        assertTrue(detalles.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OPERADOR")));
    }

    @Test
    void unUsuarioDesactivadoNoPuedeEntrar() {
        Usuario inactivo = new Usuario("viejo", "hash", Rol.OPERADOR);
        inactivo.setActivo(false);
        when(usuarios.findByNombreUsuario("viejo")).thenReturn(Optional.of(inactivo));

        assertFalse(servicio.loadUserByUsername("viejo").isEnabled());
    }

    @Test
    void unUsuarioInexistenteLanzaExcepcion() {
        when(usuarios.findByNombreUsuario("nadie")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> servicio.loadUserByUsername("nadie"));
    }
}
