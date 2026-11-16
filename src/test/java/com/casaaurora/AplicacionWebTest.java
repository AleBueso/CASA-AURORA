package com.casaaurora;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Pruebas de la aplicación completa con un servidor real en un puerto aleatorio:
 * páginas públicas, seguridad, login, portal privado y cierre de sesión.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AplicacionWebTest {

    private static final String PASSWORD_LOCAL = "casaaurora-local";

    @Value("${local.server.port}")
    private int puerto;

    private HttpClient cliente;

    @BeforeEach
    void preparar() {
        cliente = HttpClient.newBuilder()
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    private HttpResponse<String> get(String ruta) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta)).GET().build();
        return cliente.send(req, HttpResponse.BodyHandlers.ofString());
    }

    /** Igual que un navegador: pide una página web (Accept: text/html). */
    private HttpResponse<String> getComoNavegador(String ruta) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
                .header("Accept", "text/html")
                .GET()
                .build();
        return cliente.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postFormulario(String ruta, String cuerpo) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                .build();
        return cliente.send(req, HttpResponse.BodyHandlers.ofString());
    }

    /** Pide el token CSRF y lo devuelve listo para agregar al formulario: "_csrf=valor". */
    private String campoCsrf() throws Exception {
        String json = get("/api/csrf").body();
        return valor(json, "parametro") + "=" + URLEncoder.encode(valor(json, "token"), StandardCharsets.UTF_8);
    }

    private static String valor(String json, String clave) {
        Matcher m = Pattern.compile("\"" + clave + "\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        assertTrue(m.find(), "No se encontró " + clave + " en " + json);
        return m.group(1);
    }

    private HttpResponse<String> iniciarSesion(String password) throws Exception {
        String cuerpo = "username=ruth&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8) + "&" + campoCsrf();
        return postFormulario("/login", cuerpo);
    }

    @Test
    void healthcheckRespondeUpEnJson() throws Exception {
        HttpResponse<String> r = get("/api/health");
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("\"status\":\"UP\""));
    }

    @Test
    void paginaDeInicioEsPublica() throws Exception {
        HttpResponse<String> r = get("/");
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("LEARN-CAP-1E72416E"));
    }

    @Test
    void archivosPublicosDeLaPwaYSeo() throws Exception {
        assertEquals(200, get("/manifest.json").statusCode());
        assertEquals(200, get("/robots.txt").statusCode());
        assertEquals(200, get("/sw.js").statusCode());
        assertEquals(200, get("/login").statusCode());
    }

    @Test
    void rutaInexistenteDevuelve404() throws Exception {
        assertEquals(404, get("/esto-no-existe").statusCode());
    }

    @Test
    void agregaHeadersDeSeguridad() throws Exception {
        HttpResponse<String> r = get("/");
        assertEquals("DENY", r.headers().firstValue("X-Frame-Options").orElse(""));
        assertEquals("nosniff", r.headers().firstValue("X-Content-Type-Options").orElse(""));
        assertTrue(r.headers().firstValue("Content-Security-Policy").orElse("").contains("default-src 'self'"));
    }

    @Test
    void portalPrivadoNoSeMuestraSinSesion() throws Exception {
        HttpResponse<String> r = getComoNavegador("/admin");
        assertEquals(302, r.statusCode());
        assertTrue(r.headers().firstValue("Location").orElse("").endsWith("/login"));
        assertTrue(!r.body().contains("Panel de gestión"));
    }

    @Test
    void apiPrivadaRespondeNoAutorizadoSinSesion() throws Exception {
        assertEquals(401, get("/api/yo").statusCode());
    }

    @Test
    void loginConPasswordIncorrectoEsRechazado() throws Exception {
        HttpResponse<String> r = iniciarSesion("incorrecta");
        assertEquals(401, r.statusCode());
        assertEquals(401, get("/api/yo").statusCode());
    }

    @Test
    void loginCorrectoDaAccesoAlPanel() throws Exception {
        HttpResponse<String> login = iniciarSesion(PASSWORD_LOCAL);
        assertEquals(200, login.statusCode());
        assertTrue(login.body().contains("\"ok\":true"));

        HttpResponse<String> yo = get("/api/yo");
        assertEquals(200, yo.statusCode());
        assertTrue(yo.body().contains("ruth"));

        HttpResponse<String> panel = getComoNavegador("/admin");
        assertEquals(200, panel.statusCode());
        assertTrue(panel.body().contains("Panel de gestión"));
    }

    @Test
    void cerrarSesionQuitaElAcceso() throws Exception {
        assertEquals(200, iniciarSesion(PASSWORD_LOCAL).statusCode());
        assertEquals(200, postFormulario("/logout", campoCsrf()).statusCode());
        assertEquals(401, get("/api/yo").statusCode());
    }

    @Test
    void loginSinTokenCsrfEsBloqueado() throws Exception {
        HttpResponse<String> r = postFormulario("/login", "username=ruth&password=" + PASSWORD_LOCAL);
        assertEquals(403, r.statusCode());
    }
}