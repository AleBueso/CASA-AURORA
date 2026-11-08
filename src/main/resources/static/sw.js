/* Service worker de Casa Aurora
   - Precarga las páginas públicas y los recursos de la marca.
   - Las páginas privadas (/admin), el login y la API NUNCA se guardan en caché:
     así ningún dato privado queda disponible sin sesión.
   - Sin conexión, las páginas públicas muestran /offline.html. */

const CACHE = "casa-aurora-v1";
const PRECARGA = [
  "/",
  "/offline.html",
  "/manifest.json",
  "/favicon.ico",
  "/icons/icon-192.png",
  "/icons/icon-512.png",
  "/icons/icon-maskable-512.png",
  "/icons/apple-touch-icon.png",
  "/img/logo-insignia.png",
  "/img/logo-monograma.png"
];
const PRIVADO = ["/admin", "/login", "/logout", "/api/"];

self.addEventListener("install", (event) => {
  event.waitUntil(caches.open(CACHE).then((cache) => cache.addAll(PRECARGA)));
  self.skipWaiting();
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys().then((claves) =>
      Promise.all(claves.filter((c) => c !== CACHE).map((c) => caches.delete(c)))
    )
  );
  self.clients.claim();
});

self.addEventListener("fetch", (event) => {
  const req = event.request;
  const url = new URL(req.url);

  // Solo peticiones GET de este mismo sitio
  if (req.method !== "GET" || url.origin !== self.location.origin) return;

  // Privado y API: siempre a la red, nunca a la caché
  if (PRIVADO.some((ruta) => url.pathname.startsWith(ruta))) return;

  // Navegación (páginas): primero la red; sin conexión, la caché o la página offline
  if (req.mode === "navigate") {
    event.respondWith(
      fetch(req).catch(() =>
        caches.match(req).then((r) => r || caches.match("/offline.html"))
      )
    );
    return;
  }

  // Imágenes, íconos y demás recursos: primero la caché, si no la red
  event.respondWith(
    caches.match(req).then((guardado) =>
      guardado ||
      fetch(req).then((resp) => {
        if (resp.ok) {
          const copia = resp.clone();
          caches.open(CACHE).then((cache) => cache.put(req, copia));
        }
        return resp;
      })
    )
  );
});