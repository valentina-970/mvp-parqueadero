package com.uam.parqueadero.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.uam.parqueadero.usuario.Rol;
import com.uam.parqueadero.usuario.Usuario;
import com.uam.parqueadero.usuario.UsuarioRepository;

/**
 * Crea los usuarios iniciales si no existen. Las contraseñas vienen del entorno
 * y se guardan siempre con hash, nunca en texto plano (HU-05, CA-6).
 */
@Component
public class UsuariosIniciales implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UsuariosIniciales.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final String adminNombre;
    private final String adminContrasena;
    private final String operadorNombre;
    private final String operadorContrasena;

    public UsuariosIniciales(UsuarioRepository usuarios,
                             PasswordEncoder encoder,
                             @Value("${app.usuarios.admin.nombre}") String adminNombre,
                             @Value("${app.usuarios.admin.contrasena}") String adminContrasena,
                             @Value("${app.usuarios.operador.nombre}") String operadorNombre,
                             @Value("${app.usuarios.operador.contrasena}") String operadorContrasena) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.adminNombre = adminNombre;
        this.adminContrasena = adminContrasena;
        this.operadorNombre = operadorNombre;
        this.operadorContrasena = operadorContrasena;
    }

    @Override
    public void run(ApplicationArguments args) {
        crearSiNoExiste(adminNombre, adminContrasena, Rol.ADMINISTRADOR);
        crearSiNoExiste(operadorNombre, operadorContrasena, Rol.OPERADOR);
    }

    private void crearSiNoExiste(String nombre, String contrasena, Rol rol) {
        if (contrasena == null || contrasena.isBlank()) {
            log.warn("No se creó el usuario '{}': falta su contraseña en el entorno", nombre);
            return;
        }
        if (usuarios.existsByNombreUsuario(nombre)) {
            return;
        }
        usuarios.save(new Usuario(nombre, encoder.encode(contrasena), rol));
        log.info("Usuario '{}' creado con rol {}", nombre, rol);
    }
}
