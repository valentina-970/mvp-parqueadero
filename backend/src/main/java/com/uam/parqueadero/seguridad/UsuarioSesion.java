package com.uam.parqueadero.seguridad;

/** Usuario autenticado que se muestra en la interfaz ({@code rol}: OPERADOR o ADMINISTRADOR). */
public record UsuarioSesion(String usuario, String rol) {
}
