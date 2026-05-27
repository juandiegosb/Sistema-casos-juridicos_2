# Decisiones técnicas

Esta carpeta documenta decisiones técnicas relevantes del sistema de gestión de casos jurídicos.

Las decisiones explican por qué ciertos patrones, reglas y criterios fueron adoptados en el proyecto. Su propósito es facilitar mantenimiento, revisión técnica, evaluación académica e integración entre backend y frontend.

## Índice

| Documento | Contenido |
|---|---|
| `seguridad-documental.md` | Criterios para documentar sin exponer secretos ni datos sensibles. |
| `permisos-y-alcance.md` | Decisión de separar permisos funcionales y alcance real sobre recursos. |
| `estado-vs-activo.md` | Decisión de separar estado funcional y activo lógico. |
| `conciliacion.md` | Decisiones técnicas y funcionales del módulo de conciliación. |
| `documentacion-vigente.md` | Criterios para mantener documentación alineada con el código actual. |

## Formato de decisión

Cada documento describe:

- contexto;
- decisión tomada;
- justificación;
- impacto en backend;
- impacto en frontend;
- criterios de mantenimiento.

## Principios generales

Las decisiones del proyecto siguen estos principios:

- el backend valida reglas críticas de negocio;
- el frontend mejora experiencia, pero no reemplaza seguridad;
- las reglas de negocio deben estar centralizadas y ser mantenibles;
- la documentación debe describir el estado vigente del sistema;
- la documentación no debe exponer valores sensibles;
- la persistencia debe conservar historial cuando el dominio lo requiere;
- los permisos deben diferenciar acción general y alcance real;
- los estados funcionales no deben mezclarse con desactivación lógica.

## Relación con otras carpetas

Las decisiones complementan:

| Carpeta | Relación |
|---|---|
| `doc/backend` | Describe implementación por módulo. |
| `doc/api` | Describe contratos de endpoints. |
| `doc/reglas` | Describe reglas funcionales vigentes. |
| `doc/base-datos` | Describe entidades, relaciones, estados y catálogos. |

## Uso recomendado

Cuando se realice un cambio funcional, se debe revisar si afecta una decisión documentada.

Ejemplos:

| Cambio | Documento a revisar |
|---|---|
| Cambio en permisos o navegación | `permisos-y-alcance.md`. |
| Cambio en estados o activo lógico | `estado-vs-activo.md`. |
| Cambio en conciliación | `conciliacion.md`. |
| Cambio en criterios de documentación | `documentacion-vigente.md`. |
| Cambio en manejo de secretos o variables | `seguridad-documental.md`. |
