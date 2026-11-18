package com.uam.parqueadero.config;

import java.io.IOException;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.uam.parqueadero.usuario.Rol;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Seguridad de la API (HU-05): sesión con cookie y acceso por rol.
 * <ul>
 *   <li>Sin sesión válida → 401.</li>
 *   <li>Con sesión pero sin el rol necesario → 403.</li>
 * </ul>
 * CSRF queda desactivado porque la API es consumida por nuestro propio frontend
 * y el MVP no expone formularios del servidor.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/health").permitAll()
                // Documentación de la API (Swagger)
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                // Solo el administrador gestiona la configuración (tarifas, franjas, cupos)
                .requestMatchers("/api/admin/**").hasRole(Rol.ADMINISTRADOR.name())
                .anyRequest().authenticated())
            .exceptionHandling(e -> e
                .authenticationEntryPoint(sinSesion())
                .accessDeniedHandler(sinPermiso()));
        return http.build();
    }

    /** Responde 401 en JSON cuando la petición no trae una sesión válida. */
    private AuthenticationEntryPoint sinSesion() {
        return (request, response, excepcion) ->
                escribirJson(response, HttpStatus.UNAUTHORIZED, "Debe iniciar sesión");
    }

    /** Responde 403 en JSON cuando el rol del usuario no alcanza. */
    private AccessDeniedHandler sinPermiso() {
        return (request, response, excepcion) ->
                escribirJson(response, HttpStatus.FORBIDDEN, "No tiene permiso para esta operación");
    }

    private static void escribirJson(HttpServletResponse response,
                                     HttpStatus estado, String mensaje) throws IOException {
        response.setStatus(estado.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"mensaje\":\"" + mensaje + "\"}");
    }

    /** Autentica contra la abstracción {@link UserDetailsService}, comparando con el hash BCrypt. */
    @Bean
    AuthenticationManager authenticationManager(UserDetailsService usuarios, PasswordEncoder encoder) {
        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider(usuarios);
        proveedor.setPasswordEncoder(encoder);
        return new ProviderManager(proveedor);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        // Necesario para que el navegador envíe la cookie de sesión en desarrollo
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
