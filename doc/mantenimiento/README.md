# Mantenimiento documental

Esta carpeta define las reglas para mantener actualizada la documentación del sistema de gestión de casos jurídicos.

Su objetivo es evitar que la documentación vuelva a quedar desalineada con el código fuente, los contratos de API, las reglas de negocio, los permisos o la estructura de base de datos.

## Propósito

El mantenimiento documental permite saber qué archivos revisar cuando cambia alguna parte del sistema.

La documentación del proyecto está organizada por propósito:

| Carpeta | Propósito |
|---|---|
| `doc/backend` | Explica implementación backend por módulo. |
| `doc/api` | Documenta contratos REST consumidos por frontend y pruebas. |
| `doc/reglas` | Describe reglas de negocio vigentes. |
| `doc/base-datos` | Describe entidades, estados, catálogos y relaciones. |
| `doc/decisiones` | Justifica decisiones técnicas relevantes. |
| `doc/mantenimiento` | Define cómo actualizar documentación ante cambios. |

## Principio central

```text
Si cambia el código, se revisa la documentación afectada.
Si cambia un contrato de API, se actualiza la documentación de API.
Si cambia una regla de negocio, se actualizan reglas, backend y API relacionados.
Si cambia un permiso, se actualizan permisos, seguridad y módulos afectados.
```

## Fuente de verdad

La fuente principal de verdad es el código fuente vigente.

Orden recomendado de revisión:

1. Código fuente.
2. Configuración del proyecto.
3. Entidades y migraciones o estructura real de base de datos cuando aplique.
4. Permisos actuales.
5. Documentación vigente.
6. Decisiones técnicas documentadas.

## Documentación segura

La documentación no debe incluir valores reales de:

- contraseñas;
- tokens;
- secretos JWT;
- firmas;
- llaves privadas;
- API keys;
- cadenas de conexión con credenciales;
- usuarios reales de prueba;
- datos personales reales;
- rutas privadas del equipo.

Se documentan nombres de variables y su propósito, no valores reales.

## Flujo recomendado para cambios

Cuando se haga un cambio funcional:

1. Identificar el tipo de cambio.
2. Consultar `matriz-actualizacion-documental.md`.
3. Actualizar los documentos impactados.
4. Revisar `checklist-cambios.md`.
5. Confirmar que no se expongan datos sensibles.
6. Hacer commit de código y documentación relacionada.

## Commits recomendados

Para documentación general:

```bash
git add doc
git commit -m "docs: actualizar documentacion del sistema"
```

Para documentación por área:

```bash
git add doc/api
git commit -m "docs(api): actualizar contratos"
```

```bash
git add doc/backend
git commit -m "docs(backend): actualizar documentacion tecnica"
```

```bash
git add doc/reglas
git commit -m "docs(reglas): actualizar reglas de negocio"
```

## Criterio de calidad

Un documento está actualizado cuando:

- no contradice el código;
- no documenta endpoints inexistentes;
- no omite cambios contractuales relevantes;
- no expone información sensible;
- usa nombres reales de clases, endpoints, permisos y estados;
- mantiene lenguaje formal y técnico;
- puede ser usado por backend, frontend y revisión técnica.
