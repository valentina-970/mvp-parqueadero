package com.uam.parqueadero.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Crea los usuarios iniciales si no existen. Las contraseñas vienen del entorno.
@Component
public class UsuariosIniciales implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UsuariosIniciales.class);

    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    private final String adminNombre;
    private final String adminContrasena;
    private final String operadorNombre;
    private final String operadorContrasena;

    public UsuariosIniciales(JdbcTemplate jdbc,
                             PasswordEncoder encoder,
                             @Value("${app.usuarios.admin.nombre}") String adminNombre,
                             @Value("${app.usuarios.admin.contrasena}") String adminContrasena,
                             @Value("${app.usuarios.operador.nombre}") String operadorNombre,
                             @Value("${app.usuarios.operador.contrasena}") String operadorContrasena) {
        this.jdbc = jdbc;
        this.encoder = encoder;
        this.adminNombre = adminNombre;
        this.adminContrasena = adminContrasena;
        this.operadorNombre = operadorNombre;
        this.operadorContrasena = operadorContrasena;
    }

    @Override
    public void run(ApplicationArguments args) {
        crearSiNoExiste(adminNombre, adminContrasena, "ADMINISTRADOR");
        crearSiNoExiste(operadorNombre, operadorContrasena, "OPERADOR");
    }

    private void crearSiNoExiste(String nombre, String contrasena, String rol) {
        if (contrasena == null || contrasena.isBlank()) {
            log.warn("No se creó el usuario '{}': falta su contraseña en el entorno", nombre);
            return;
        }
        Integer existe = jdbc.queryForObject(
                "SELECT COUNT(*) FROM usuario WHERE nombre_usuario = ?", Integer.class, nombre);
        if (existe != null && existe > 0) {
            return;
        }
        jdbc.update(
                "INSERT INTO usuario (nombre_usuario, contrasena_hash, rol) VALUES (?, ?, ?)",
                nombre, encoder.encode(contrasena), rol);
        log.info("Usuario '{}' creado con rol {}", nombre, rol);
    }
}