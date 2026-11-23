package com.uam.parqueadero.seguridad;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

/** Inicio de sesión (HU-05). La sesión viaja en una cookie. */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Inicio y cierre de sesión por rol")
public class AuthController {

    /** Mensaje único para credenciales inválidas: no revela si falló el usuario o la contraseña. */
    static final String MENSAJE_CREDENCIALES = "Usuario o contraseña incorrectos";

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contextoRepository = new HttpSessionSecurityContextRepository();

    /** Recibe la abstracción {@link AuthenticationManager}: el controlador no conoce la base de datos. */
    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /** Valida las credenciales y abre la sesión; responde 401 si no son válidas. */
    @Operation(summary = "Iniciar sesión",
            description = "Responde 200 con el usuario y su rol, o 401 con el mensaje "
                    + "\"Usuario o contraseña incorrectos\".")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest peticion,
                                               HttpServletRequest request, HttpServletResponse response) {
        try {
            Authentication autenticacion = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            peticion.usuario().trim(), peticion.contrasena()));
            // Si ya había una sesión, se cambia su identificador: evita la fijación de sesión
            if (request.getSession(false) != null) {
                request.changeSessionId();
            }
            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(autenticacion);
            SecurityContextHolder.setContext(contexto);
            contextoRepository.saveContext(contexto, request, response);
            log.info("Inicio de sesión de '{}'", autenticacion.getName());
            return ResponseEntity.ok(aSesion(autenticacion));
        } catch (AuthenticationException e) {
            log.warn("Inicio de sesión fallido para '{}'", limpiar(peticion.usuario()));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", MENSAJE_CREDENCIALES));
        }
    }

    /** Cierra la sesión actual: invalida la cookie y limpia el contexto de seguridad. */
    @Operation(summary = "Cerrar sesión")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    /** Devuelve el usuario de la sesión actual; sin sesión la seguridad responde 401. */
    @Operation(summary = "Usuario de la sesión actual")
    @GetMapping("/yo")
    public UsuarioSesion yo(Authentication autenticacion) {
        return aSesion(autenticacion);
    }

    /** Quita saltos de línea y otros caracteres de control para que un usuario no falsifique líneas del log. */
    private static String limpiar(String texto) {
        return texto == null ? "" : texto.replaceAll("\\p{Cntrl}", "_");
    }

    private static UsuarioSesion aSesion(Authentication autenticacion) {
        String rol = autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("")
                .replaceFirst("^ROLE_", "");
        return new UsuarioSesion(autenticacion.getName(), rol);
    }
}
