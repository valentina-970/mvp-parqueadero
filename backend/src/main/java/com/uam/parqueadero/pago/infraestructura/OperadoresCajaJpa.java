package com.uam.parqueadero.pago.infraestructura;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.uam.parqueadero.pago.OperadoresCaja;
import com.uam.parqueadero.usuario.UsuarioRepository;

/** Implementación del puerto {@link OperadoresCaja} con los usuarios de HU-05. */
@Component
public class OperadoresCajaJpa implements OperadoresCaja {

    private final UsuarioRepository usuarios;

    public OperadoresCajaJpa(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UUID idDe(String nombreUsuario) {
        return usuarios.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new IllegalStateException("El usuario de la sesión no existe: " + nombreUsuario))
                .getId();
    }
}
