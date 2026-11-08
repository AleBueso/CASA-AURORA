package com.casaaurora.seguridad;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Seguridad de Casa Aurora.
 * - Público: inicio, login, PWA, robots, 404, /api/health y /api/csrf.
 * - Privado: /admin (panel de los dueños) y el resto de /api/.
 * - Sin sesión, /admin redirige al login sin mostrar contenido y la API responde 401.
 * - Agrega headers de seguridad a todas las respuestas.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String CSP = String.join("; ",
            "default-src 'self'",
            "script-src 'self' 'unsafe-inline'",
            "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com",
            "font-src 'self' https://fonts.gstatic.com",
            "img-src 'self' data:",
            "connect-src 'self'",
            "manifest-src 'self'",
            "worker-src 'self'",
            "object-src 'none'",
            "base-uri 'self'",
            "form-action 'self'",
            "frame-ancestors 'none'");

    @Bean
    public SecurityFilterChain filtroDeSeguridad(HttpSecurity http) throws Exception {
        RequestMatcher esApi = request -> request.getRequestURI().startsWith("/api/");

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/health", "/api/csrf").permitAll()
                .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/**").hasRole("ADMIN")
                .anyRequest().permitAll())
            // Login y logout responden JSON (sin redirecciones), para funcionar detrás de Vercel
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler((request, response, auth) -> responderJson(response, 200, "{\"ok\":true}"))
                .failureHandler((request, response, error) -> responderJson(response, 401, "{\"ok\":false}"))
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler((request, response, auth) -> responderJson(response, 200, "{\"ok\":true}"))
                .deleteCookies("JSESSIONID"))
            .exceptionHandling(e -> e
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), esApi))
            .headers(h -> h
                .contentSecurityPolicy(csp -> csp.policyDirectives(CSP))
                .frameOptions(f -> f.deny())
                .referrerPolicy(r -> r.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy",
                        "camera=(), microphone=(), geolocation=(), payment=()"))
                .addHeaderWriter(new StaticHeadersWriter("Cross-Origin-Opener-Policy", "same-origin")));

        return http.build();
    }

    private static void responderJson(HttpServletResponse response, int estado, String json) throws IOException {
        response.setStatus(estado);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    /** Usuario de los dueños. El usuario y la contraseña vienen de variables de entorno. */
    @Bean
    public UserDetailsService usuarios(@Value("${app.admin.usuario}") String usuario,
                                       @Value("${app.admin.password}") String password,
                                       PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername(usuario)
                    .password(encoder.encode(password))
                    .roles("ADMIN")
                    .build());
    }

    /** Las contraseñas se guardan cifradas con BCrypt, nunca en texto plano. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}