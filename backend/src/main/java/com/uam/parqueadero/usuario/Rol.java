package com.uam.parqueadero.usuario;

/**
 * Roles del sistema. Los nombres coinciden con la restricción
 * {@code ck_usuario_rol} de la tabla {@code usuario}.
 */
public enum Rol {
    OPERADOR,
    ADMINISTRADOR;

    /** Nombre del rol tal como lo usa Spring Security (con prefijo ROLE_). */
    public String autoridad() {
        return "ROLE_" + name();
    }
}
