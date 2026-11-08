package com.casaaurora.seguridad;

import java.util.Map;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/** Páginas de acceso: login y token CSRF para el formulario. */
@Controller
public class LoginController {

    /** Muestra la página de login. */
    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }

    /** Entrega el token CSRF que el formulario de login necesita para enviarse. */
    @GetMapping("/api/csrf")
    @ResponseBody
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("parametro", token.getParameterName(), "token", token.getToken());
    }
}