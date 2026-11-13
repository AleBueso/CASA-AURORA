# ADR-002: Usar un usuario administrador único con Spring Security en vez de cuentas de usuario en base de datos

- Estado: Aceptada
- Fecha: 2026-11-12

## Contexto

El panel (`/admin` y `/api/**`) solo lo usan los dueños del negocio, hoy una sola persona (Ruth). Los clientes no tienen cuenta: piden por WhatsApp, Instagram o Facebook. La app se publica detrás de Vercel y Render, por lo que el login no debe depender de redirecciones.

## Decisión

Usar **Spring Security** con un usuario administrador en memoria (`InMemoryUserDetailsManager`, rol `ADMIN`). Usuario y contraseña vienen de las variables de entorno `ADMIN_USER` y `ADMIN_PASSWORD`, y la contraseña se guarda con BCrypt. Se usa sesión por cookie con protección CSRF. Login y logout responden JSON. Solo `/api/health` y `/api/csrf` son públicos.

## Consecuencias

- Positivas: implementación pequeña y fácil de entender, sin tabla de usuarios ni registro, y las credenciales nunca viven en el repositorio.
- Positivas: toda la API y el panel quedan protegidos por defecto con headers de seguridad.
- Negativas: no hay múltiples cuentas, roles ni recuperación de contraseña. Cambiarla exige cambiar la variable de entorno y reiniciar.
- Negativas: la sesión vive en memoria del servidor, así que reiniciar o escalar a varias instancias cierra las sesiones.
- Si el negocio suma empleados, habrá que pasar a usuarios en SQL Server.
