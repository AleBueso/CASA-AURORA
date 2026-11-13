# ADR-001: Usar SQL Server en vez de un archivo o SQLite embebido

- Estado: Aceptada
- Fecha: 2026-11-12

## Contexto

Casa Aurora hoy anota pedidos en un cuaderno o en el celular. Se pierden encargos y no se respeta el orden de llegada. Se producen hasta 6 lotes a la vez, se venden unas 90 botellas al mes (más de 150 en noviembre) y cada pedido debe poder asignarse a botellas de un lote concreto, sin duplicarse ni perderse.

Se necesita guardar lotes, pedidos y asignaciones con integridad: un pedido no puede quedar asignado a botellas que ya se comprometieron. El curso y el equipo trabajan con SQL Server, y la clienta puede crecer en ventas y en usuarios.

## Decisión

Usar **SQL Server** como base de datos, accedida desde Spring Boot por JDBC/JPA, con transacciones para asignar pedidos a lotes.

## Consecuencias

- Positivas: transacciones e integridad referencial para respetar el orden de llegada; consultas claras de botellas disponibles y comprometidas; el modelo escala si el negocio crece.
- Negativas: el servidor es más pesado que SQLite y su hosting gratuito es limitado, lo que complica el despliegue en Render (la app corre con `-Xmx350m`).
- Negativas: hay que configurar conexión, usuario y contraseña por variables de entorno, y el desarrollo local necesita una instancia de SQL Server.
- Se sacrifica simplicidad y costo cero de operación a cambio de integridad de datos y crecimiento. Para una clienta cuyo problema central son pedidos perdidos y entregas fuera de orden, ese trato vale la pena.
