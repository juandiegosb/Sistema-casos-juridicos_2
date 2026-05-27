# Reglas de negocio

Esta carpeta documenta las reglas de negocio vigentes del sistema de gestión de casos jurídicos.

Las reglas descritas corresponden al comportamiento implementado en backend y deben mantenerse alineadas con el código fuente.

## Índice

| Documento | Contenido |
|---|---|
| `consultas.md` | Reglas de creación, actualización, estados, responsables, archivo y cierre de consultas. |
| `seguimientos.md` | Reglas de seguimientos, respuestas, revisión y notificaciones. |
| `procesos.md` | Reglas de procesos, estados, radicado, catálogos y relación con consultas. |
| `conciliaciones.md` | Reglas de conciliación, responsables, estados, documentos y alcance. |
| `permisos.md` | Reglas de permisos, roles, alcance y autorización. |
| `archivos.md` | Reglas de carga, descarga, rutas y seguridad de archivos. |

## Principios generales

Las reglas de negocio del sistema siguen estos principios:

- las reglas críticas se validan en backend;
- el frontend puede ocultar acciones, pero no reemplaza la validación del backend;
- las operaciones protegidas requieren usuario autenticado;
- los permisos funcionales se complementan con validaciones de alcance;
- el estado funcional de un recurso no debe confundirse con su estado lógico activo/inactivo;
- los cambios de ciclo de vida se hacen mediante endpoints específicos;
- los datos históricos se conservan mediante desactivación lógica o estados de archivo cuando corresponde.

## Estado funcional y activo lógico

El sistema diferencia:

| Concepto | Uso |
|---|---|
| Estado funcional | Representa el ciclo de vida del recurso. |
| Activo lógico | Representa disponibilidad operativa o desactivación lógica. |

Ejemplos:

- una consulta usa estado `PENDIENTE`, `ACTIVO`, `CERRADO` o `ARCHIVADO`;
- un proceso usa estado `PENDIENTE` o estados finales jurídicos;
- una conciliación usa estados del catálogo `estado_conciliacion`;
- perfiles, catálogos y algunos registros usan `activo` para desactivación lógica.

## Actualización de documentación ante cambios

Cuando una regla de negocio cambie en el código, deben revisarse los documentos relacionados.

Ejemplos de impacto:

| Cambio en código | Documentos a revisar |
|---|---|
| Cambio en reglas de consulta | `reglas/consultas.md`, `backend/consultas.md`, `api/consultas.md`. |
| Cambio en reglas de seguimiento | `reglas/seguimientos.md`, `backend/seguimientos.md`, `api/seguimientos.md`. |
| Cambio en reglas de proceso | `reglas/procesos.md`, `backend/procesos.md`, `api/procesos.md`. |
| Cambio en reglas de conciliación | `reglas/conciliaciones.md`, `backend/conciliaciones.md`, `api/conciliaciones.md`. |
| Cambio en permisos o alcance | `reglas/permisos.md`, `04-permisos-roles-alcance.md`, documentos API afectados. |
| Cambio en rutas o archivos | `reglas/archivos.md`, `backend/archivos.md`, `api/archivos.md`. |

## Seguridad documental

Estos documentos no deben incluir valores reales de secretos, contraseñas, tokens, llaves, firmas, credenciales, cadenas de conexión o usuarios reales de prueba.
