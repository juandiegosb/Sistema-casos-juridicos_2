# Estados y catálogos

Este documento describe estados funcionales, enumeraciones y catálogos principales del sistema.

## 1. Diferencia entre estado funcional y activo lógico

El sistema separa dos conceptos:

| Concepto | Propósito |
|---|---|
| Estado funcional | Representa ciclo operativo o resultado del recurso. |
| Activo lógico | Representa disponibilidad o desactivación lógica sin eliminar historial. |

Ejemplos:

- una consulta usa `estado` para su ciclo operativo;
- un proceso usa `estado` para resultado del proceso y `activo` para borrado lógico;
- un seguimiento usa `estado` y `activo`;
- una conciliación usa `estado_conciliacion` y `activo`;
- catálogos y perfiles usan `activo`.

## 2. Estados de consulta

Enum:

```text
EstadoConsulta
```

Valores:

| Estado | Descripción |
|---|---|
| `ACTIVO` | Consulta en atención activa. |
| `EN_PROCESO` | Consulta en gestión operativa. |
| `PENDIENTE` | Estado inicial o de espera. |
| `URGENTE` | Consulta priorizada. |
| `CERRADO` | Consulta cerrada operativamente. |
| `ARCHIVADO` | Consulta archivada para historial. |

Reglas principales:

- las consultas nuevas nacen `PENDIENTE`;
- `CERRADO` bloquea operaciones operativas;
- `ARCHIVADO` excluye de listados operativos;
- una consulta cerrada solo puede archivarse;
- desarchivar devuelve a `CERRADO`.

## 3. Estados de proceso

Enum:

```text
EstadoProceso
```

Valores:

| Estado | Descripción |
|---|---|
| `PENDIENTE` | Proceso pendiente de resultado. |
| `SENTENCIA_FAVORABLE` | Resultado favorable. |
| `SENTENCIA_DESFAVORABLE` | Resultado desfavorable. |
| `DESISTIMIENTO` | Terminado por desistimiento. |
| `RECHAZO` | Terminado por rechazo. |
| `PRESCRIPCION` | Terminado por prescripción. |

Reglas principales:

- el valor por defecto es `PENDIENTE`;
- un proceso pendiente bloquea el cierre de la consulta;
- `activo` no reemplaza el estado.

## 4. Estados de seguimiento

Enum:

```text
EstadoSeguimiento
```

Valores:

| Estado | Descripción |
|---|---|
| `PENDIENTE` | Seguimiento activo en gestión. |
| `COMPLETADO` | Seguimiento completado. |
| `CANCELADO` | Seguimiento cancelado. |

Reglas principales:

- todo seguimiento nace `PENDIENTE`;
- un seguimiento pendiente bloquea el cierre de consulta;
- completar puede depender de respuestas aprobadas cuando notifica estudiante;
- `activo` conserva borrado lógico.

## 5. Estados de respuesta de seguimiento

Enum:

```text
EstadoRespuestaSeguimiento
```

Valores:

| Estado | Descripción |
|---|---|
| `PENDIENTE` | Respuesta enviada y pendiente de revisión. |
| `APROBADA` | Respuesta aprobada. |
| `RECHAZADA` | Respuesta rechazada. |

Reglas principales:

- toda respuesta nace `PENDIENTE`;
- una respuesta pendiente bloquea cierre de consulta;
- una respuesta aprobada puede completar el seguimiento asociado;
- una respuesta rechazada permite nuevo intento según reglas del módulo.

## 6. Tipos de notificación de seguimiento

Enum:

```text
TipoNotificacionSeguimiento
```

Valores:

| Tipo | Destinatarios |
|---|---|
| `PARTES` | Persona principal, partes y contrapartes. |
| `ESTUDIANTE` | Estudiante asignado a la consulta. |
| `ALERTA_DISCIPLINARIA` | Administrativos activos. |
| `AUTOR` | Usuario que creó el seguimiento. |

## 7. Momentos de notificación de seguimiento

Enum:

```text
MomentoNotificacionSeguimiento
```

Valores:

| Momento | Descripción |
|---|---|
| `INMEDIATA` | Notificación generada al crear o actualizar seguimiento. |
| `RECORDATORIO` | Notificación programada según fecha de entrega y días de notificación. |

## 8. Estados de conciliación

Entidad/catálogo:

```text
EstadoConciliacion
```

Tabla:

```text
estado_conciliacion
```

Campos:

| Campo | Uso |
|---|---|
| `codigo` | Código técnico usado por backend. |
| `nombre` | Nombre visible. |
| `activo` | Estado activo del catálogo. |
| `orden` | Orden de presentación. |

Códigos base:

| Código | Nombre visible |
|---|---|
| `EN_ESPERA` | En espera |
| `ESPERANDO_REUNION` | Esperando reunión |
| `REUNION_PROGRAMADA` | Reunión programada |
| `COMPLETO_CONCILIADO` | Completo - conciliado |
| `COMPLETO_NO_CONCILIADO` | Completo - no conciliado |

Clasificación:

| Grupo | Estados |
|---|---|
| No finalizados | `EN_ESPERA`, `ESPERANDO_REUNION`, `REUNION_PROGRAMADA`. |
| Finalizados | `COMPLETO_CONCILIADO`, `COMPLETO_NO_CONCILIADO`. |

Reglas principales:

- el backend valida por `codigo`, no por `nombre`;
- el nombre puede ser usado por frontend para visualización;
- `EN_ESPERA` se calcula automáticamente según asignaciones;
- estados finalizados se usan en flujo de finalización con acta.

## 9. Tipo de conciliador

Enum:

```text
TipoConciliador
```

Valores:

| Tipo | Uso |
|---|---|
| `INTERNO` | Conciliador interno. |
| `EXTERNO` | Conciliador externo. |

## 10. Tipo de perfil de usuario

Enum:

```text
TipoPerfilUsuario
```

Valores:

| Tipo | Perfil real |
|---|---|
| `ASESOR` | Perfil asesor. |
| `ESTUDIANTE` | Perfil estudiante. |
| `MONITOR` | Perfil monitor. |
| `ADMINISTRATIVO` | Perfil administrativo. |
| `CONCILIADOR` | Perfil conciliador. |

Uso:

- se almacena en `usuario_sistema.tipo_perfil_actual`;
- permite resolver el perfil real activo del usuario;
- se usa para validaciones de alcance y compatibilidad de rol.

## 11. Catálogos generales

Catálogos con `activo`:

| Catálogo | Tabla | Uso |
|---|---|---|
| Área | `area` | Clasificación jurídica principal. |
| Tema | `tema` | Clasificación dependiente de área. |
| Tipo | `tipo` | Clasificación dependiente de tema. |
| Departamento | `departamento` | Catálogo territorial. |
| Municipio | `municipio` | Catálogo territorial dependiente de departamento. |
| Barrio | `barrio` | Catálogo territorial dependiente de municipio. |
| Sede | `sede` | Sedes del sistema. |
| Nacionalidad | `nacionalidades` | Nacionalidades de personas. |
| Tipo de documento | `tipodoc` | Tipos de documento para perfiles y usuarios. |

Reglas comunes:

- se listan activos para formularios;
- se listan todos para administración;
- se validan duplicados;
- se desactivan lógicamente.

## 12. Catálogos de persona

| Catálogo | Tabla | Uso |
|---|---|---|
| TipoPersona | `tipo_persona` | Clasifica personas. |
| Condicion | `condicion` | Condición actual de persona. |
| Empresa | `empresas` | Empresa asociada a datos laborales. |
| Ocupacion | `ocupacion` | Ocupación de persona. |

## 13. Catálogos de proceso

| Catálogo | Tabla | Uso |
|---|---|---|
| Órgano de control | `organo_control` | Clasifica entidad u órgano competente. |
| Especialidad | `especialidad` | Especialidad dependiente de órgano de control. |

Reglas:

- especialidad pertenece a órgano de control;
- órgano con especialidades activas no se desactiva;
- especialidad se desactiva lógicamente.

## 14. Catálogos de seguimiento

| Catálogo | Tabla | Uso |
|---|---|---|
| Categoría de seguimiento | `categoria_seguimiento` | Clasifica seguimientos. |

Reglas:

- nombre único;
- activo/inactivo;
- se usa para crear seguimientos.

## 15. Catálogo de estados de conciliación

A diferencia de otros estados funcionales implementados como enum, conciliación usa tabla:

```text
estado_conciliacion
```

Justificación funcional:

- permite nombre visible administrable;
- mantiene código técnico estable;
- evita validar reglas por texto visible;
- facilita orden de presentación.

## 16. Estados que bloquean cierre de consulta

Una consulta no puede cerrar si existen recursos pendientes.

| Recurso | Estado o condición bloqueante |
|---|---|
| Proceso | `PENDIENTE` y activo. |
| Seguimiento | `PENDIENTE` y activo. |
| Respuesta de seguimiento | `PENDIENTE` y activa. |
| Notificación de seguimiento | Activa y no enviada. |
| Conciliación | `EN_ESPERA`, `ESPERANDO_REUNION` o `REUNION_PROGRAMADA`, activa. |

## 17. Reglas de documentación

Cuando se agregue, elimine o cambie un estado:

- actualizar este documento;
- actualizar reglas del módulo correspondiente;
- actualizar API si cambia request o response;
- actualizar backend del módulo si cambia interpretación del estado;
- actualizar base de datos si cambia catálogo administrable.

Ejemplos:

| Cambio | Documentos a revisar |
|---|---|
| Nuevo estado de consulta | `estados-y-catalogos.md`, `reglas/consultas.md`, `api/consultas.md`. |
| Nuevo estado de proceso | `estados-y-catalogos.md`, `reglas/procesos.md`, `api/procesos.md`. |
| Cambio en estado de conciliación | `estados-y-catalogos.md`, `reglas/conciliaciones.md`, `api/conciliaciones.md`, `backend/conciliaciones.md`. |
| Nuevo catálogo | `estados-y-catalogos.md`, `backend/catalogos.md`, `api/catalogos.md`. |
