## ¿Qué cambia?
<!-- Describí en 2 o 3 líneas qué hace este PR y por qué. -->

## ¿Qué necesidad de Casa Aurora resuelve?
<!-- Ej.: Ruth necesita registrar un lote sin usar el cuaderno. -->

## Tipo de cambio
- [ ] feat: funcionalidad nueva
- [ ] fix: corrección de un error
- [ ] refactor: mejora del código sin cambiar el comportamiento
- [ ] test: pruebas
- [ ] docs: documentación
- [ ] ci / chore: configuración o mantenimiento

## ¿Cómo lo probé?
- [ ] `./mvnw verify` pasa sin errores (BUILD SUCCESS)
- [ ] El pipeline de GitHub Actions está en verde
- [ ] Lo revisé en el panel (`/admin`) con datos de prueba

## Checklist antes de mergear
- [ ] Los commits usan formato convencional (`feat:`, `fix:`, `docs:`…)
- [ ] No hay contraseñas, tokens ni archivos `.env` en el cambio
- [ ] La cobertura sigue arriba del 60 % y SonarCloud sigue en verde
- [ ] Si cambia el esquema de la base de datos, regeneré `docs/db-export.json`