package com.casaaurora.health;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Healthcheck público: confirma que la API está viva y responde en JSON. */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "casa-aurora",
                "timestamp", Instant.now().toString());
    }
}