# Casa Aurora · Control de lotes y pedidos

**Código de verificación:** `LEARN-CAP-1E72416E`

Sistema web para **Casa Aurora**, emprendimiento de vino artesanal de flor de Jamaica en Danlí, El Paraíso (Honduras), activo desde 2019. Proyecto de la clase **Ingeniería de Software 1**.

## Cliente

- **Clienta:** Ruth Daniela Rodríguez, dueña de Casa Aurora.
- **Producción:** micro lotes de 5 galones (19 litros, unas 24 botellas de 750 ml), con hasta 6 lotes al mismo tiempo. Cada lote fermenta 8 días y reposa 3.
- **Ventas:** unas 90 botellas al mes (más de 150 esperadas en noviembre) por WhatsApp, Instagram, Facebook y ferias de emprendedores. Precio: L.300 la unidad y L.280 por mayoreo (más de 6 botellas).

## Problema

Los pedidos y reservas se anotan en un cuaderno o en el celular solo cuando hay uno cerca, así que se olvidan encargos y fechas de entrega. Al no llevar el orden de llegada, a veces se entregan botellas a quien pidió después y quedan sin vino clientes que pidieron antes. Además, no hay un control claro de cuántas botellas hay disponibles, y revisarlo toma unos 20 minutos.

## Feature

**Registro de pedidos y reservas conectado a los lotes de producción.**

- Registro de lotes con cálculo automático de días y estado (fermentando, reposando, listo, embotellado).
- Ajuste de la fecha estimada cuando la temperatura alarga la fermentación.
- Registro de pedidos con cliente, cantidad, canal y fecha de entrega.
- Asignación de pedidos por orden de llegada a botellas disponibles o al próximo lote.
- Vista de botellas disponibles y comprometidas.

## Tecnologías

| Capa | Tecnología |
|---|---|
| Backend | Java, Spring Boot |
| Base de datos | SQL Server |
| Frontend | HTML, CSS y JavaScript |
| Tests | JUnit 5 |
| Integración continua | GitHub Actions |

## Estado del proyecto

- [x] Definición del cliente y del problema
- [ ] Proyecto base en Spring Boot
- [ ] Módulo de lotes
- [ ] Módulo de pedidos
- [ ] Interfaz web para celular
