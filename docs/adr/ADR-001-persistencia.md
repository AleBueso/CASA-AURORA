# ADR-001: Usar H2 en memoria con datos mock en vez de SQL Server gestionado

- **Estado:** Aceptado
- **Fecha:** 2026-11-07
- **Autora:** Alejandra Bueso

## Contexto

Casa Aurora es un emprendimiento pequeño de Danlí que hoy lleva sus lotes y pedidos en un cuaderno o de memoria. La dueña necesita **probar el sistema y validar que le resuelve el problema** antes de asumir cualquier costo mensual.

El stack de la clase es Spring Boot con SQL Server, y el sistema debe estar publicado en una URL pública con HTTPS. Las opciones de base de datos para producción fueron:

- **SQL Server gestionado (Azure SQL Database):** coincide con el stack, pero requiere cuenta de Azure, normalmente con tarjeta, configuración de red y credenciales, y puede generar costos.
- **Postgres gratuito de Render:** persiste los datos, pero la instancia gratuita se elimina a los 30 días y cambia el motor de base de datos.
- **H2 en memoria dentro de la misma aplicación:** cero costo y cero configuración, pero los datos se pierden al reiniciar el servidor.

Además, el plan gratuito de Render apaga el servicio tras 15 minutos sin tráfico, así que cualquier dato guardado en memoria se pierde con frecuencia.

## Decisión

Usar **H2 en memoria** como base de datos de la versión publicada, cargando **datos mock** (lotes y pedidos de ejemplo con fechas relativas al día actual) cada vez que la aplicación arranca.

El acceso a datos se hace con **Spring Data JPA**, sin SQL específico de un motor, para que pasar a SQL Server sea un cambio de configuración (URL, usuario, contraseña y driver) y no de código.

## Consecuencias

### Positivas

- **Costo cero** para la clienta: no hay que pagar ni mantener un servidor de base de datos.
- **Cero configuración:** no hay credenciales de base de datos que proteger en producción.
- **Demo siempre lista:** al arrancar, el panel muestra un escenario completo (lotes en cada estado, pedidos cubiertos, reservados y en riesgo) para presentar y validar con la clienta.
- **Pruebas y CI** usan la misma base H2, sin depender de servicios externos.

### Negativas

- **Los datos no persisten:** cada reinicio o redespliegue vuelve a los datos mock. Por ahora el sistema no sirve para el registro real de la clienta.
- **Diferencias de motor:** H2 no es idéntico a SQL Server; algún detalle de tipos o consultas podría cambiar al migrar.
- **Un solo proceso:** la base vive dentro de la aplicación y no se puede escalar a varias instancias.

### Plan de salida

Cuando la clienta confirme que usará el sistema a diario, se migra a **SQL Server** (o a otra base gestionada) cambiando solo las propiedades de conexión en las variables de entorno de Render. El modelo JPA y los servicios no cambian.