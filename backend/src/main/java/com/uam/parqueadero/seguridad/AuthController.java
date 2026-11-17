package com.uam.parqueadero.seguridad;

import java.util.Map;

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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

/** Inicio de sesión (HU-05). La sesión viaja en una cookie. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /** Mensaje único para credenciales inválidas: no revela si falló el usuario o la contraseña. */
    static final String MENSAJE_CREDENCIALES = "Usuario o contraseña incorrectos";

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contextoRepository = new HttpSessionSecurityContextRepository();

    /** Recibe la abstracción {@link AuthenticationManager}: el controlador no conoce la base de datos. */
    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /** Valida las credenciales y abre la sesión; responde 401 si no son válidas. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest peticion,
                                               HttpServletRequest request, HttpServletResponse response) {
        try {
            Authentication autenticacion = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            peticion.usuario().trim(), peticion.contrasena()));
            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(autenticacion);
            SecurityContextHolder.setContext(contexto);
            contextoRepository.saveContext(contexto, request, response);
            return ResponseEntity.ok(aSesion(autenticacion));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", MENSAJE_CREDENCIALES));
        }
    }

    /** Cierra la sesión actual: invalida la cookie y limpia el contexto de seguridad. */
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
    @GetMapping("/yo")
    public UsuarioSesion yo(Authentication autenticacion) {
        return aSesion(autenticacion);
    }

    private static UsuarioSesion aSesion(Authentication autenticacion) {
        String rol = autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("")
                .replaceFirst("^ROLE_", "");
        return new UsuarioSesion(autenticacion.getName(), rol);
    }
}
