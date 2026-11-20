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
## Flujo de trabajo con Git

El proyecto usa **GitHub Flow**: `main` siempre está estable y desplegado (Render + Vercel), y todo cambio entra por un pull request desde una rama de trabajo.

### Ramas
| Prefijo | Para qué | Ejemplo |
|---|---|---|
| `feat/` | Funcionalidad nueva | `feat/registrar-lotes` |
| `fix/` | Corrección de errores | `fix/login-proxy` |
| `docs/` | Documentación | `docs/plantilla-pr` |
| `test/` | Pruebas | `test/pedidos` |
| `ci/` | Pipeline y configuración | `ci/pruebas-en-pr` |

### Commits
Se usa el formato de **Conventional Commits**: `tipo: descripción en presente`.
Ejemplos: `feat: permite registrar un lote nuevo desde el panel`, `fix: corrige el estado de un lote embotellado`.

### Antes de mergear un pull request
1. `./mvnw verify` pasa sin errores y GitHub Actions está en verde.
2. La cobertura se mantiene arriba del 60 % y SonarCloud no reporta bugs ni vulnerabilidades.
3. El cambio no incluye contraseñas, tokens ni archivos `.env`.
4. Se probó en el panel con datos de prueba.
5. Se mergea con **Rebase and merge** para mantener un historial lineal.