package com.casaaurora.panel;

import java.security.Principal;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/** Panel privado de los dueños. Solo accesible con sesión (ver SecurityConfig). */
@Controller
public class AdminController {

    /** Muestra el panel. */
    @GetMapping({"/admin", "/admin/"})
    public String panel() {
        return "forward:/admin/index.html";
    }

    /** Devuelve el usuario que inició sesión. */
    @GetMapping("/api/yo")
    @ResponseBody
    public Map<String, String> yo(Principal principal) {
        return Map.of("usuario", principal.getName());
    }
}