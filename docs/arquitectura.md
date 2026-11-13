# Arquitectura de Casa Aurora

Sistema web para controlar lotes de vino de flor de Jamaica y los pedidos de la clienta (Ruth Daniela Rodríguez, Danlí, Honduras), asignándolos por orden de llegada.

## Diagrama C4 (contexto)

```mermaid
C4Context
    title Casa Aurora - Diagrama de contexto

    Person(duena, "Ruth (dueña)", "Registra lotes y pedidos desde el celular")
    Person(cliente, "Cliente", "Pide botellas por WhatsApp, Instagram, Facebook o ferias")

    System(casaaurora, "Casa Aurora", "Control de lotes, pedidos y botellas disponibles (Spring Boot + PWA)")

    System_Ext(canales, "WhatsApp / Instagram / Facebook", "Canales por donde llegan los pedidos")
    System_Ext(hosting, "Vercel / Render", "Publican la app en internet")
    SystemDb_Ext(sqlserver, "SQL Server", "Guarda lotes y pedidos")

    Rel(cliente, canales, "Hace su pedido")
    Rel(duena, canales, "Recibe el pedido")
    Rel(duena, casaaurora, "Registra y consulta", "HTTPS")
    Rel(casaaurora, sqlserver, "Lee y escribe", "JDBC")
    Rel(hosting, casaaurora, "Ejecuta y expone")
```

## Contenedores

```mermaid
flowchart LR
    Ruth([Ruth, dueña]) -->|HTTPS| Web[Frontend HTML/CSS/JS + PWA<br/>static/]
    Web -->|/api/*, sesión + CSRF| API[Backend Spring Boot<br/>Java 17]
    API --> Seg[Spring Security<br/>login único ADMIN]
    API --> Lotes[Módulo lotes<br/>EstadoLote]
    API --> Pedidos[Módulo pedidos<br/>orden de llegada]
    Lotes --> DB[(SQL Server)]
    Pedidos --> DB
```

## Decisiones

- [ADR-001: Persistencia](adr/ADR-001-persistencia.md)
- [ADR-002: Autenticación](adr/ADR-002-auth.md)
