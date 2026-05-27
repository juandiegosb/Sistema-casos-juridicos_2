# Decisión técnica - Documentación vigente

## Contexto

El proyecto requiere documentación técnica, funcional y de API que acompañe el código.

La documentación debe ayudar a:

- comprender arquitectura;
- integrar frontend y backend;
- explicar reglas de negocio;
- defender decisiones técnicas;
- evitar análisis basados en información desactualizada.

## Decisión

La documentación oficial del repositorio debe describir el estado vigente del código fuente.

No debe funcionar como histórico informal de cambios.

Cuando una regla, endpoint, DTO, permiso, entidad o estado cambie, se deben actualizar los documentos afectados.

## Fuente de verdad

Orden de prioridad:

1. Código fuente actual.
2. Configuración actual.
3. Base de datos actual cuando aplica.
4. Decisiones técnicas documentadas.
5. Documentación vigente.

La documentación no debe contradecir el código.

## Estructura documental

La documentación se organiza por propósito:

| Carpeta | Propósito |
|---|---|
| `doc/backend` | Implementación backend por módulo. |
| `doc/api` | Contratos de endpoints para frontend y pruebas. |
| `doc/reglas` | Reglas de negocio vigentes. |
| `doc/base-datos` | Entidades, relaciones, estados y catálogos. |
| `doc/decisiones` | Justificación de decisiones técnicas. |

## Qué se documenta

Se documenta:

- comportamiento vigente;
- endpoints reales;
- DTOs reales;
- permisos reales;
- reglas implementadas;
- relaciones principales;
- criterios de seguridad;
- decisiones técnicas relevantes.

## Qué no se documenta como estado vigente

No se documentan como si estuvieran implementados:

- reglas futuras;
- endpoints no implementados;
- pantallas en desarrollo;
- cambios no integrados;
- valores reales de configuración sensible.

Cuando se requiera hablar de evolución o mantenimiento, se hace en documentos específicos de decisiones o mantenimiento, sin presentarlo como contrato operativo actual.

## Documentación y cambios lógicos

Cuando se implemente una corrección lógica, se actualiza solo la documentación afectada.

Ejemplos:

| Cambio | Documentos a revisar |
|---|---|
| Cambio en regla de cierre de consulta | `backend/consultas.md`, `api/consultas.md`, `reglas/consultas.md`. |
| Cambio en radicado de proceso | `backend/procesos.md`, `api/procesos.md`, `reglas/procesos.md`, `base-datos/entidades-principales.md`. |
| Cambio en decisión de respuesta de seguimiento | `backend/seguimientos.md`, `api/seguimientos.md`, `reglas/seguimientos.md`. |
| Cambio en estados de conciliación | `backend/conciliaciones.md`, `api/conciliaciones.md`, `reglas/conciliaciones.md`, `base-datos/estados-y-catalogos.md`, `decisiones/conciliacion.md`. |
| Cambio en permisos | `04-permisos-roles-alcance.md`, `reglas/permisos.md`, `decisiones/permisos-y-alcance.md`, documentos API afectados. |

## Documentación y frontend

Si el frontend está en refactorización, no se documenta su estructura interna como definitiva.

Sí se documentan:

- contratos API;
- autenticación;
- uso de `credentials: "include"`;
- permisos;
- reglas que el frontend debe respetar;
- formatos de request y response.

La documentación interna del frontend se crea o actualiza cuando su estructura esté estable.

## Seguridad documental

Toda documentación debe respetar:

- no exponer secretos;
- no exponer contraseñas;
- no exponer tokens;
- no exponer usuarios reales de prueba;
- no exponer rutas privadas;
- no exponer datos personales reales.

## Criterios de revisión antes de commit

Antes de commitear documentación, verificar:

- no contradice el código;
- no documenta endpoints inexistentes;
- no usa datos reales sensibles;
- no presenta reglas futuras como vigentes;
- no duplica contenido de forma contradictoria;
- tiene nombres de clases, endpoints y permisos consistentes;
- usa lenguaje formal de proyecto real.

## Regla final

```text
Si cambia el código, cambia la documentación afectada.
Si la documentación contradice el código, se actualiza la documentación.
```
