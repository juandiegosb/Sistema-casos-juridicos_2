# Backend - Conciliaciones

El módulo de conciliaciones administra conciliaciones asociadas a consultas jurídicas.

Permite crear una conciliación desde una consulta, registrar solicitud PDF, asignar estudiante y conciliador, consultar listados y detalle, cambiar estados operativos, finalizar con acta PDF y desactivar conciliaciones de forma lógica.

## Paquetes principales

```text
business/controller/conciliacion
business/dto/conciliacion
business/model/conciliacion
business/repository/conciliacion
business/service/acceso/conciliacion
business/service/conciliacion
business/service/conciliacion/conciliacion
```

## Componentes principales

| Componente | Responsabilidad |
|---|---|
| `ConciliacionController` | Expone endpoints HTTP del módulo de conciliaciones. |
| `ConciliacionService` | Fachada del módulo. El controller usa este service y no conoce command/query services. |
| `ConciliacionCommandService` | Orquesta operaciones de escritura: creación, asignaciones, cambio de estado, finalización, reemplazo de solicitud y desactivación. |
| `ConciliacionQueryService` | Orquesta consultas, listados y detalle según permisos y alcance. |
| `ConciliacionAsignacionService` | Selecciona estudiante y conciliador inicial según reglas de habilitación y carga. |
| `ConciliacionDocumentoService` | Centraliza validación y almacenamiento de solicitud y acta PDF. |
| `ConciliacionRelacionService` | Resuelve consulta, conciliación, estudiante, conciliador y estado. |
| `ConciliacionValidator` | Centraliza reglas de negocio propias de conciliación. |
| `ConciliacionMapper` | Convierte entidad a DTOs de salida. |
| `ConciliacionAccessService` | Valida permisos funcionales y delega alcance. |
| `ConciliacionAlcanceService` | Valida relación del usuario actual con la conciliación o consulta. |
| `ConciliacionRepository` | Acceso a datos de conciliaciones. |
| `EstadoConciliacionRepository` | Acceso a datos del catálogo de estados de conciliación. |

## Permisos usados

| Permiso | Uso |
|---|---|
| `Ver conciliaciones` | Permite listar y consultar conciliaciones visibles según alcance. |
| `Gestionar conciliaciones` | Permite crear conciliaciones, asignar conciliador, reemplazar solicitud y desactivar según alcance. |
| `Concluir conciliaciones` | Permite operar acciones de cierre y flujo para conciliador asignado según alcance. |
| `Programar reuniones de conciliación` | Permiso funcional relacionado con el flujo de reuniones de conciliación. |
| `Reprogramar reuniones de conciliación` | Permiso funcional relacionado con reprogramación de reuniones. |
| `Acceder conciliaciones` | Permiso de navegación hacia la sección de conciliaciones en frontend. |

## Entidades principales

### `Conciliacion`

Tabla:

```text
conciliacion
```

Campos principales:

| Campo | Uso |
|---|---|
| `id` | Identificador de la conciliación. |
| `consulta` | Consulta desde la que nace la conciliación. |
| `estudiante` | Estudiante encargado de la conciliación. Puede ser nulo si no hay estudiante habilitado asignado. |
| `conciliador` | Conciliador encargado. Puede ser nulo si aún no hay conciliador asignado o disponible. |
| `estado` | Estado funcional de la conciliación. |
| `fechaConciliacion` | Fecha principal programada para la conciliación. |
| `documentoSolicitudPath` | Ruta de la solicitud PDF. |
| `actaPath` | Ruta del acta PDF. |
| `solicitadoPor` | Usuario del sistema que generó la conciliación. |
| `fechaCreacion` | Fecha de creación. |
| `fechaActualizacion` | Fecha de última actualización. |
| `fechaFinalizacion` | Fecha en que se finaliza la conciliación. |
| `activo` | Borrado lógico propio de conciliación. |

La entidad normaliza valores antes de persistir o actualizar:

- si `fechaCreacion` es nula antes de persistir, se asigna la fecha actual;
- si `activo` es nulo, se asigna `true`;
- en actualización se asigna `fechaActualizacion`.

### `EstadoConciliacion`

Tabla:

```text
estado_conciliacion
```

Campos:

| Campo | Uso |
|---|---|
| `id` | Identificador del estado. |
| `codigo` | Código técnico usado por backend para reglas de negocio. |
| `nombre` | Nombre visible para frontend y usuarios. |
| `activo` | Indica si el estado está disponible. |
| `orden` | Orden de presentación. |

El backend valida reglas por `codigo`, no por `nombre`.

El código se normaliza con:

- trim;
- mayúsculas;
- reemplazo de espacios o guiones por guion bajo cuando se recibe como parámetro.

## Códigos de estado

Los códigos técnicos se centralizan en `EstadoConciliacionCodigo`.

| Código | Nombre esperado |
|---|---|
| `EN_ESPERA` | En espera |
| `ESPERANDO_REUNION` | Esperando reunión |
| `REUNION_PROGRAMADA` | Reunión programada |
| `COMPLETO_CONCILIADO` | Completo - conciliado |
| `COMPLETO_NO_CONCILIADO` | Completo - no conciliado |

## Clasificación de estados

### Estados no finalizados

```text
EN_ESPERA
ESPERANDO_REUNION
REUNION_PROGRAMADA
```

Estos estados representan conciliaciones activas en flujo operativo.

### Estados finalizados

```text
COMPLETO_CONCILIADO
COMPLETO_NO_CONCILIADO
```

Estos estados representan conciliaciones terminadas.

## DTOs

### `ConciliacionResponseDTO`

DTO de salida para listados y respuestas generales.

Campos:

| Campo | Uso |
|---|---|
| `id` | Identificador. |
| `consultaId` | Consulta asociada. |
| `estudianteId` | Estudiante asignado. |
| `estudianteNombre` | Nombre del estudiante. |
| `conciliadorId` | Conciliador asignado. |
| `conciliadorNombre` | Nombre del conciliador. |
| `estadoId` | Identificador del estado. |
| `estadoCodigo` | Código técnico del estado. |
| `estadoNombre` | Nombre visible del estado. |
| `fechaConciliacion` | Fecha programada de conciliación. |
| `documentoSolicitudPath` | Ruta de solicitud PDF. |
| `actaPath` | Ruta de acta PDF. |
| `solicitadoPorId` | Usuario solicitante. |
| `solicitadoPorUsername` | Username del solicitante. |
| `activo` | Estado lógico. |
| `fechaCreacion` | Fecha de creación. |
| `fechaActualizacion` | Fecha de actualización. |
| `fechaFinalizacion` | Fecha de finalización. |

### `ConciliacionDetalleResponseDTO`

DTO de salida para detalle.

Incluye los campos de `ConciliacionResponseDTO` y agrega:

| Campo | Uso |
|---|---|
| `consultante` | Persona principal de la consulta. |
| `partes` | Personas relacionadas como partes. |
| `contrapartes` | Personas relacionadas como contrapartes. |

### `ConciliacionPersonaDTO`

DTO liviano para personas en detalle.

Campos:

| Campo | Uso |
|---|---|
| `id` | Identificador de la persona. |
| `nombre` | Nombre completo para visualización. |

## Endpoints principales

Base path:

```text
/api/conciliaciones
```

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/conciliaciones` | `Ver conciliaciones` | Lista conciliaciones visibles para el usuario. |
| GET | `/api/conciliaciones/consulta/{consultaId}` | `Ver conciliaciones` | Lista conciliaciones activas de una consulta según alcance. |
| GET | `/api/conciliaciones/{id}` | `Ver conciliaciones` | Consulta detalle de conciliación. |
| POST | `/api/conciliaciones/consulta/{consultaId}` | `Gestionar conciliaciones` | Crea conciliación desde consulta con solicitud PDF. |
| PATCH | `/api/conciliaciones/{id}/estudiante?estudianteId=` | `Gestionar conciliaciones` o `Concluir conciliaciones` | Asigna estudiante. |
| PATCH | `/api/conciliaciones/{id}/conciliador?conciliadorId=` | `Gestionar conciliaciones` | Asigna conciliador. |
| PATCH | `/api/conciliaciones/{id}/estado?estado=` | `Gestionar conciliaciones` o `Concluir conciliaciones` | Cambia estado operativo no final. |
| POST | `/api/conciliaciones/{id}/finalizar` | `Gestionar conciliaciones` o `Concluir conciliaciones` | Finaliza conciliación con acta PDF. |
| POST | `/api/conciliaciones/{id}/solicitud` | `Gestionar conciliaciones` | Reemplaza solicitud PDF. |
| DELETE | `/api/conciliaciones/{id}` | `Gestionar conciliaciones` | Desactiva conciliación. |

## Content-Type por endpoint

### Crear conciliación

```text
POST /api/conciliaciones/consulta/{consultaId}
Content-Type: multipart/form-data
```

Campos:

| Campo | Tipo | Uso |
|---|---|---|
| `solicitud` | PDF | Documento de solicitud de conciliación. |

### Finalizar conciliación

```text
POST /api/conciliaciones/{id}/finalizar
Content-Type: multipart/form-data
```

Campos:

| Campo | Tipo | Uso |
|---|---|---|
| `estado` | String | Código de estado final. |
| `acta` | PDF | Acta de conciliación. |

### Reemplazar solicitud

```text
POST /api/conciliaciones/{id}/solicitud
Content-Type: multipart/form-data
```

Campos:

| Campo | Tipo | Uso |
|---|---|---|
| `solicitud` | PDF | Nueva solicitud de conciliación. |

## Listado de conciliaciones

Endpoint:

```text
GET /api/conciliaciones
```

Reglas:

- requiere `Ver conciliaciones`;
- lista conciliaciones activas;
- excluye conciliaciones asociadas a consultas archivadas;
- filtra resultados según alcance del usuario;
- ordena por id descendente.

## Listado por consulta

Endpoint:

```text
GET /api/conciliaciones/consulta/{consultaId}
```

Reglas:

- requiere `Ver conciliaciones`;
- `consultaId` es obligatorio;
- lista conciliaciones activas de la consulta;
- excluye consultas archivadas;
- filtra resultados según alcance del usuario.

## Detalle de conciliación

Endpoint:

```text
GET /api/conciliaciones/{id}
```

Reglas:

- requiere `Ver conciliaciones`;
- valida alcance sobre la conciliación;
- la conciliación debe estar activa;
- carga la consulta con partes y contrapartes;
- retorna consultante, partes y contrapartes desde la consulta.

El detalle no duplica partes ni contrapartes en conciliación; usa la consulta como fuente de contexto.

## Creación de conciliación

Endpoint:

```text
POST /api/conciliaciones/consulta/{consultaId}
```

Reglas principales:

- requiere `Gestionar conciliaciones`;
- valida alcance sobre la consulta;
- la consulta no puede estar cerrada;
- la consulta no puede estar archivada;
- no puede existir otra conciliación activa no finalizada para la misma consulta;
- recibe solicitud PDF obligatoria;
- registra el usuario solicitante;
- autoasigna estudiante;
- autoasigna conciliador;
- calcula estado inicial según asignación;
- guarda primero la entidad para obtener id;
- guarda la solicitud en ruta dependiente del id;
- actualiza la ruta de solicitud en la conciliación.

## Autoasignación de estudiante

`ConciliacionAsignacionService` define la selección inicial.

Reglas:

1. Si la consulta ya tiene estudiante asignado, activo y habilitado para conciliación, se usa ese estudiante.
2. Si no, se selecciona un estudiante activo habilitado para conciliación con menor cantidad de conciliaciones activas no finalizadas.
3. En empate, se ordena por nombre normalizado.
4. Si continúa el empate, se ordena por id.
5. Si no hay estudiante disponible, el estudiante queda nulo.

La carga se calcula contando conciliaciones activas en estados no finalizados.

## Autoasignación de conciliador

Reglas:

1. Se listan conciliadores activos.
2. Se selecciona el conciliador con menor cantidad de conciliaciones activas no finalizadas.
3. En empate, se ordena por nombre normalizado.
4. Si continúa el empate, se ordena por id.
5. Si no hay conciliador disponible, queda nulo.

## Estado inicial

Después de asignar estudiante y conciliador:

| Condición | Estado asignado |
|---|---|
| Tiene estudiante y conciliador | `ESPERANDO_REUNION` |
| Falta estudiante o conciliador | `EN_ESPERA` |

El estado `EN_ESPERA` se calcula automáticamente y no se asigna manualmente por endpoint de cambio de estado.

## Asignación de estudiante

Endpoint:

```text
PATCH /api/conciliaciones/{id}/estudiante?estudianteId=
```

Reglas:

- requiere `Gestionar conciliaciones` o `Concluir conciliaciones`;
- el usuario debe tener alcance sobre la conciliación;
- la conciliación debe estar activa;
- la conciliación no puede estar finalizada;
- la consulta asociada no puede estar cerrada ni archivada;
- el estudiante debe existir y estar activo;
- el estudiante debe estar habilitado para conciliación;
- después de asignar, se recalcula estado según responsables.

Alcance:

- administrador puede asignar estudiante;
- conciliador puede asignar estudiante si está asignado a la conciliación.

## Asignación de conciliador

Endpoint:

```text
PATCH /api/conciliaciones/{id}/conciliador?conciliadorId=
```

Reglas:

- requiere `Gestionar conciliaciones`;
- solo administrador puede asignar conciliador;
- la conciliación debe estar activa;
- la conciliación no puede estar finalizada;
- la consulta asociada no puede estar cerrada ni archivada;
- el conciliador debe existir y estar activo;
- después de asignar, se recalcula estado según responsables.

## Cambio de estado operativo

Endpoint:

```text
PATCH /api/conciliaciones/{id}/estado?estado=
```

Reglas:

- requiere `Gestionar conciliaciones` o `Concluir conciliaciones`;
- valida alcance;
- la conciliación debe estar activa;
- la conciliación no puede estar finalizada;
- la consulta asociada no puede estar cerrada ni archivada;
- el estado recibido debe existir y estar activo;
- no se permite cambiar al mismo estado;
- no se permite usar este endpoint para estados finales;
- no se permite cambiar manualmente a `EN_ESPERA`;
- `ESPERANDO_REUNION` exige estudiante y conciliador asignados;
- `REUNION_PROGRAMADA` exige estudiante, conciliador y fecha de conciliación.

Alcance:

- administrador puede cambiar estado;
- conciliador asignado puede cambiar estado, excepto devolver a `EN_ESPERA`.

## Finalización de conciliación

Endpoint:

```text
POST /api/conciliaciones/{id}/finalizar
```

Reglas:

- requiere `Gestionar conciliaciones` o `Concluir conciliaciones`;
- valida alcance;
- la conciliación debe estar activa;
- la conciliación no puede estar finalizada;
- la consulta asociada no puede estar cerrada ni archivada;
- el estado debe ser final;
- debe existir estudiante asignado;
- debe existir conciliador asignado;
- el acta PDF es obligatoria;
- el acta se guarda antes de cambiar estado;
- se registra `actaPath`;
- se registra `fechaFinalizacion`;
- se actualiza estado final.

Estados finales permitidos:

```text
COMPLETO_CONCILIADO
COMPLETO_NO_CONCILIADO
```

Alcance:

- administrador puede finalizar;
- conciliador asignado puede finalizar.

## Reemplazo de solicitud

Endpoint:

```text
POST /api/conciliaciones/{id}/solicitud
```

Reglas:

- requiere `Gestionar conciliaciones`;
- solo administrador puede reemplazar solicitud;
- la conciliación debe estar activa;
- la conciliación no puede estar finalizada;
- la consulta asociada no puede estar cerrada ni archivada;
- la nueva solicitud PDF es obligatoria;
- se guarda sobre la ruta estándar de solicitud de la conciliación.

## Desactivación de conciliación

Endpoint:

```text
DELETE /api/conciliaciones/{id}
```

Reglas:

- requiere `Gestionar conciliaciones`;
- solo administrador puede desactivar;
- la conciliación debe estar activa;
- la conciliación no puede estar finalizada;
- la consulta asociada no puede estar cerrada ni archivada;
- la desactivación es lógica;
- desactivar no representa finalización.

## Documentos

`ConciliacionDocumentoService` centraliza documentos del módulo.

Rutas estándar:

| Documento | Ruta |
|---|---|
| Solicitud | `conciliacion/{id}/solicitud.pdf` |
| Acta | `conciliacion/{id}/acta.pdf` |

Validaciones:

- el archivo es obligatorio;
- el nombre debe terminar en `.pdf`;
- si se informa content type, debe ser `application/pdf`;
- el id de conciliación es obligatorio para construir ruta.

## Alcance por perfil

`ConciliacionAlcanceService` responde preguntas de alcance.

### Administrador

Puede ver y operar conciliaciones de forma global según permisos.

### Asesor

Puede ver o crear conciliaciones asociadas a consultas donde es asesor directo.

### Monitor

Puede ver o crear conciliaciones asociadas a consultas donde es monitor directo.

### Conciliador

Puede ver y operar conciliaciones donde está asignado como conciliador.

### Estudiante

Puede ver conciliaciones cuando:

- está asignado directamente a la conciliación; o
- es el estudiante responsable de la consulta asociada.

El estudiante no gestiona ni finaliza conciliaciones.

## AccessService

`ConciliacionAccessService` valida permisos y alcance antes de ejecutar cada caso de uso.

Validaciones principales:

| Método | Validación |
|---|---|
| `validarPuedeListarConciliaciones` | Requiere `Ver conciliaciones`. |
| `validarPuedeVerConciliacion` | Requiere `Ver conciliaciones` y alcance. |
| `validarPuedeCrearConciliacion` | Requiere `Gestionar conciliaciones` y alcance sobre consulta. |
| `validarPuedeAsignarConciliador` | Requiere `Gestionar conciliaciones` y alcance administrativo. |
| `validarPuedeAsignarEstudiante` | Requiere `Gestionar conciliaciones` o `Concluir conciliaciones` y alcance. |
| `validarPuedeCambiarEstado` | Requiere `Gestionar conciliaciones` o `Concluir conciliaciones` y alcance. |
| `validarPuedeFinalizar` | Requiere `Gestionar conciliaciones` o `Concluir conciliaciones` y alcance. |
| `validarPuedeReemplazarSolicitud` | Requiere `Gestionar conciliaciones` y alcance administrativo. |
| `validarPuedeDesactivarConciliacion` | Requiere `Gestionar conciliaciones` y alcance administrativo. |

## Validaciones de negocio

`ConciliacionValidator` centraliza reglas funcionales:

| Validación | Regla |
|---|---|
| `validarConsultaPermiteConciliacion` | No se crea sobre consulta cerrada o archivada. |
| `validarConsultaPermiteOperacionConciliacion` | No se opera sobre conciliación de consulta cerrada o archivada. |
| `validarNoExisteConciliacionActivaNoFinalizada` | No permite más de una conciliación activa no finalizada por consulta. |
| `validarConciliacionNoFinalizada` | No permite modificar conciliación finalizada. |
| `validarEstudianteHabilitadoParaConciliacion` | Estudiante activo y con conciliación habilitada. |
| `validarConciliadorActivo` | Conciliador activo. |
| `validarCambioEstado` | Controla estados no finales y reglas de responsables/fecha. |
| `validarFinalizacion` | Controla estados finales y responsables mínimos. |
| `validarTieneResponsablesMinimos` | Exige estudiante y conciliador asignados. |

## Relación con cierre de consulta

La consulta no puede cerrarse si tiene conciliaciones activas no finalizadas.

Estados que bloquean cierre:

```text
EN_ESPERA
ESPERANDO_REUNION
REUNION_PROGRAMADA
```

Estados que no bloquean cierre:

```text
COMPLETO_CONCILIADO
COMPLETO_NO_CONCILIADO
```

## Repositories

### `ConciliacionRepository`

Consultas principales:

- buscar conciliación activa por id;
- buscar conciliación activa excluyendo consultas archivadas;
- listar conciliaciones activas;
- listar conciliaciones activas excluyendo consultas archivadas;
- listar conciliaciones activas por consulta;
- validar existencia de conciliación activa no finalizada por consulta;
- contar conciliaciones no finalizadas por estudiante;
- contar conciliaciones no finalizadas por conciliador.

### `EstadoConciliacionRepository`

Consultas principales:

- buscar estado activo por código;
- buscar estado por código;
- listar estados activos ordenados por `orden` y `nombre`.

## Mapper

`ConciliacionMapper` genera:

- `ConciliacionResponseDTO`;
- `ConciliacionDetalleResponseDTO`;
- `ConciliacionPersonaDTO`.

El detalle usa la consulta asociada como fuente de:

- consultante;
- partes;
- contrapartes.

## Consideraciones para frontend

- Usar `GET /api/conciliaciones` para listado visible del usuario.
- Usar `GET /api/conciliaciones/{id}` para detalle.
- Usar `GET /api/conciliaciones/consulta/{consultaId}` para ver conciliaciones de una consulta.
- Para crear, enviar `multipart/form-data` con campo `solicitud`.
- Para finalizar, enviar `multipart/form-data` con campo `acta` y parámetro `estado`.
- No usar `PATCH /estado` para estados finales.
- No intentar cambiar a `EN_ESPERA` manualmente.
- Mostrar estados usando `estadoNombre`, pero enviar estados por `estadoCodigo`.
- Manejar `403` como falta de permiso o alcance.
- Manejar errores de negocio para consulta cerrada, archivada, conciliación finalizada o documentos inválidos.
- Usar `credentials: "include"` en peticiones protegidas.
