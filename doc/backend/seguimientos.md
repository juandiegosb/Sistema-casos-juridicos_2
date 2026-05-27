# Backend - Seguimientos

El módulo de seguimientos administra actuaciones, tareas, alertas, notificaciones y respuestas asociadas a consultas jurídicas.

El seguimiento permite registrar una actividad relacionada con una consulta, definir fecha de entrega, configurar notificaciones, permitir respuesta del estudiante cuando aplique y controlar el estado operativo del seguimiento.

## Paquetes principales

```text
business/controller/seguimiento
business/dto/seguimiento
business/dto/seguimiento/notificacion
business/dto/seguimiento/respuesta
business/model/seguimiento
business/model/seguimiento/notificacion
business/model/seguimiento/respuesta
business/repository/seguimiento
business/repository/seguimiento/notificacion
business/repository/seguimiento/respuesta
business/scheduler/seguimiento
business/service/acceso/seguimiento
business/service/seguimiento
business/service/seguimiento/catalogo
business/service/seguimiento/notificacion
business/service/seguimiento/respuesta
business/service/seguimiento/seguimiento
```

## Componentes principales

| Componente | Responsabilidad |
|---|---|
| `SeguimientoController` | Expone endpoints para crear, consultar, actualizar, cambiar estado y eliminar seguimientos. |
| `SeguimientoRespuestaController` | Expone endpoints para respuestas de estudiantes y revisión de respuestas. |
| `CategoriaSeguimientoController` | Expone endpoints para administrar categorías de seguimiento. |
| `SeguimientoService` | Fachada del módulo de seguimientos. |
| `SeguimientoCommandService` | Orquesta creación, actualización, cambio de estado y eliminación de seguimientos. |
| `SeguimientoQueryService` | Orquesta consultas y listados de seguimientos. |
| `SeguimientoEstadoService` | Controla transiciones de estado y efectos sobre notificaciones. |
| `SeguimientoValidator` | Centraliza reglas de negocio de seguimiento. |
| `SeguimientoMapper` | Convierte entidades de seguimiento a DTOs de respuesta. |
| `SeguimientoAccessService` | Valida permisos y alcance sobre seguimientos. |
| `SeguimientoRespuestaService` | Fachada del submódulo de respuestas. |
| `SeguimientoRespuestaCommandService` | Orquesta creación, edición y decisión de respuestas. |
| `SeguimientoRespuestaQueryService` | Orquesta consultas de respuestas. |
| `SeguimientoRespuestaValidator` | Centraliza reglas de negocio de respuestas. |
| `SeguimientoRespuestaAccessService` | Valida permisos y alcance sobre respuestas. |
| `SeguimientoNotificacionService` | Coordina sincronización, cancelación y procesamiento de notificaciones. |
| `SeguimientoDestinatarioService` | Calcula destinatarios de notificaciones. |
| `SeguimientoEnvioNotificacionService` | Envía notificaciones pendientes y registra resultado. |
| `SeguimientoNotificacionScheduler` | Ejecuta procesamiento programado de notificaciones pendientes. |
| `CategoriaSeguimientoService` | Administra categorías de seguimiento. |

## Permisos usados

| Permiso | Uso |
|---|---|
| `Ver seguimientos` | Permite consultar seguimientos y respuestas según alcance. |
| `Crear seguimientos` | Permite crear seguimientos. |
| `Editar seguimientos` | Permite actualizar seguimientos y cambiar su estado. |
| `Eliminar seguimientos` | Permite desactivar seguimientos. |
| `Responder seguimientos` | Permite al estudiante responder seguimientos visibles. |
| `Aprobar respuestas de seguimiento` | Permite revisar respuestas pendientes. |
| `Ver alertas disciplinarias` | Permite consultar seguimientos marcados como alerta disciplinaria. |
| `Gestionar categorías de seguimiento` | Permite administrar categorías de seguimiento. |

## Entidades principales

### `Seguimiento`

Tabla:

```text
seguimiento
```

Campos principales:

| Campo | Uso |
|---|---|
| `id` | Identificador del seguimiento. |
| `descripcion` | Descripción corta del seguimiento. |
| `fechaEntrega` | Fecha límite del seguimiento. |
| `diasNotificacion` | Días antes de la fecha de entrega para programar recordatorio. |
| `notificarPartes` | Indica si se notifica a persona principal, partes y contrapartes. |
| `notificarEstudiante` | Indica si se notifica y muestra al estudiante. |
| `alertaDisciplinaria` | Marca el seguimiento como alerta disciplinaria. |
| `estado` | Estado operativo del seguimiento. |
| `activo` | Borrado lógico del seguimiento. |
| `categoriaSeguimiento` | Categoría asociada. |
| `consulta` | Consulta jurídica asociada. |
| `autor` | Usuario del sistema que creó el seguimiento. |
| `fechaCreacion` | Fecha de creación. |
| `fechaActualizacion` | Fecha de última actualización. |

### `CategoriaSeguimiento`

Tabla:

```text
categoria_seguimiento
```

Campos:

| Campo | Uso |
|---|---|
| `id` | Identificador de la categoría. |
| `nombre` | Nombre único de la categoría. |
| `activo` | Estado activo/inactivo. |

### `SeguimientoRespuesta`

Tabla:

```text
seguimiento_respuesta
```

Campos principales:

| Campo | Uso |
|---|---|
| `id` | Identificador de la respuesta. |
| `seguimiento` | Seguimiento respondido. |
| `estudiante` | Estudiante que responde. |
| `contenido` | Contenido de la respuesta. |
| `estado` | Estado de revisión de la respuesta. |
| `fueraPlazo` | Indica si la respuesta fue enviada o editada después de la fecha de entrega. |
| `observacionRevision` | Observación del revisor. |
| `revisadoPor` | Usuario del sistema que revisa. |
| `fechaCreacion` | Fecha de creación. |
| `fechaActualizacion` | Fecha de actualización. |
| `fechaDecision` | Fecha de aprobación o rechazo. |
| `activo` | Borrado lógico. |

### `SeguimientoNotificacion`

Tabla:

```text
seguimiento_notificacion
```

Campos principales:

| Campo | Uso |
|---|---|
| `id` | Identificador de la notificación. |
| `seguimiento` | Seguimiento asociado. |
| `tipoNotificacion` | Destinatario lógico de la notificación. |
| `momentoNotificacion` | Momento de envío. |
| `fechaProgramada` | Fecha en la que debe enviarse. |
| `fechaEnvio` | Fecha real de envío. |
| `enviada` | Indica si fue enviada. |
| `intentos` | Cantidad de intentos de envío. |
| `error` | Error registrado en el último intento. |
| `fechaCreacion` | Fecha de creación. |
| `fechaActualizacion` | Fecha de actualización. |
| `activo` | Estado activo de la notificación. |
| `fechaCancelacion` | Fecha de cancelación cuando aplica. |

La tabla tiene una restricción única para evitar duplicar notificaciones del mismo tipo y momento sobre el mismo seguimiento.

## Estados

### Estado de seguimiento

Enum:

```text
EstadoSeguimiento
```

Valores:

| Estado | Uso |
|---|---|
| `PENDIENTE` | Seguimiento activo pendiente de gestión o respuesta. |
| `COMPLETADO` | Seguimiento finalizado correctamente. |
| `CANCELADO` | Seguimiento cancelado operativamente. |

### Estado de respuesta

Enum:

```text
EstadoRespuestaSeguimiento
```

Valores:

| Estado | Uso |
|---|---|
| `PENDIENTE` | Respuesta enviada por el estudiante y pendiente de revisión. |
| `APROBADA` | Respuesta aprobada por un usuario con permiso de revisión. |
| `RECHAZADA` | Respuesta rechazada por un usuario con permiso de revisión. |

### Tipo de notificación

Enum:

```text
TipoNotificacionSeguimiento
```

Valores:

| Tipo | Destinatarios |
|---|---|
| `PARTES` | Persona principal, partes y contrapartes de la consulta. |
| `ESTUDIANTE` | Estudiante asignado a la consulta. |
| `ALERTA_DISCIPLINARIA` | Administrativos activos. |
| `AUTOR` | Usuario que creó el seguimiento. |

### Momento de notificación

Enum:

```text
MomentoNotificacionSeguimiento
```

Valores:

| Momento | Uso |
|---|---|
| `INMEDIATA` | Se envía cuando el seguimiento se crea o actualiza. |
| `RECORDATORIO` | Se envía según `fechaEntrega - diasNotificacion`. |

## DTOs

### `SeguimientoRequestDTO`

DTO de entrada para crear o actualizar seguimientos.

Campos:

| Campo | Validación |
|---|---|
| `id` | No debe enviarse en creación y debe coincidir con la ruta en actualización. |
| `descripcion` | Obligatoria, máximo 200 caracteres. |
| `fechaEntrega` | Debe ser actual o futura cuando se informa. |
| `diasNotificacion` | Debe ser mayor o igual a cero. |
| `notificarPartes` | Booleano opcional. |
| `notificarEstudiante` | Booleano opcional. |
| `alertaDisciplinaria` | Booleano opcional. |
| `categoriaSeguimientoId` | Obligatorio. |
| `consultaId` | Obligatorio. |

Regla condicional:

- no se pueden definir días de notificación si no existe fecha de entrega.

### `SeguimientoResponseDTO`

DTO de salida del seguimiento.

Incluye:

- identificador;
- descripción;
- fecha de entrega;
- días de notificación;
- flags de notificación;
- estado;
- categoría;
- consulta;
- autor;
- fechas de creación y actualización.

### `CategoriaSeguimientoDTO`

DTO para categoría.

Campos:

| Campo | Validación |
|---|---|
| `id` | No debe enviarse en creación. |
| `nombre` | Obligatorio, máximo 50 caracteres. |
| `activo` | Estado del catálogo. |

### `SeguimientoRespuestaRequestDTO`

DTO para crear o editar respuesta.

| Campo | Validación |
|---|---|
| `id` | No debe enviarse en creación y debe coincidir con la ruta en actualización. |
| `contenido` | Obligatorio, máximo 1000 caracteres. |

### `SeguimientoRespuestaDecisionDTO`

DTO para aprobar o rechazar respuesta.

| Campo | Validación |
|---|---|
| `estado` | Obligatorio; solo `APROBADA` o `RECHAZADA`. |
| `observacionRevision` | Opcional, máximo 500 caracteres. |

### `SeguimientoRespuestaResponseDTO`

DTO de salida de respuesta.

Incluye:

- identificador;
- seguimiento;
- consulta;
- estudiante;
- contenido;
- estado;
- marca fuera de plazo;
- observación de revisión;
- usuario revisor;
- fechas de creación, actualización y decisión.

## Endpoints de seguimiento

Base path:

```text
/api/seguimientos
```

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/seguimientos/consulta/{consultaId}` | `Ver seguimientos` | Lista seguimientos de una consulta según alcance. |
| GET | `/api/seguimientos/consulta/{consultaId}/visibles-estudiante` | `Ver seguimientos` | Lista seguimientos visibles para estudiante en una consulta. |
| GET | `/api/seguimientos/autor/{autorId}` | `Ver seguimientos` | Lista seguimientos creados por un autor. |
| GET | `/api/seguimientos/alertas-disciplinarias` | `Ver alertas disciplinarias` | Lista alertas disciplinarias. |
| GET | `/api/seguimientos/fecha-entrega?fechaEntrega=` | `Ver seguimientos` | Lista seguimientos por fecha de entrega. |
| GET | `/api/seguimientos/{id}` | `Ver seguimientos` | Consulta seguimiento por id. |
| POST | `/api/seguimientos` | `Crear seguimientos` | Crea seguimiento. |
| PUT | `/api/seguimientos/{id}` | `Editar seguimientos` | Actualiza seguimiento. |
| PATCH | `/api/seguimientos/{id}/estado?estado=` | `Editar seguimientos` | Cambia estado de seguimiento. |
| DELETE | `/api/seguimientos/{id}` | `Eliminar seguimientos` | Desactiva seguimiento. |

## Endpoints de respuestas

Base path:

```text
/api/seguimientos
```

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| POST | `/api/seguimientos/{seguimientoId}/respuestas` | `Responder seguimientos` | Crea respuesta del estudiante. |
| PUT | `/api/seguimientos/respuestas/{id}` | `Responder seguimientos` | Edita respuesta propia pendiente. |
| GET | `/api/seguimientos/respuestas/{id}` | `Ver seguimientos` | Consulta respuesta por id. |
| GET | `/api/seguimientos/{seguimientoId}/respuestas` | `Ver seguimientos` | Lista respuestas de un seguimiento. |
| GET | `/api/seguimientos/respuestas/pendientes` | `Aprobar respuestas de seguimiento` | Lista respuestas pendientes de revisión. |
| PATCH | `/api/seguimientos/respuestas/{id}/decision` | `Aprobar respuestas de seguimiento` | Aprueba o rechaza una respuesta. |

## Endpoints de categorías

Base path:

```text
/api/seguimientos/categorias
```

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/seguimientos/categorias` | `Gestionar categorías de seguimiento` | Lista todas las categorías. |
| GET | `/api/seguimientos/categorias/activas` | `Ver seguimientos`, `Crear seguimientos` o `Gestionar categorías de seguimiento` | Lista categorías activas. |
| GET | `/api/seguimientos/categorias/{id}` | `Gestionar categorías de seguimiento` | Consulta categoría por id. |
| POST | `/api/seguimientos/categorias` | `Gestionar categorías de seguimiento` | Crea categoría. |
| PUT | `/api/seguimientos/categorias/{id}` | `Gestionar categorías de seguimiento` | Actualiza categoría. |
| PATCH | `/api/seguimientos/categorias/{id}/activo?activo=` | `Gestionar categorías de seguimiento` | Cambia estado activo. |
| DELETE | `/api/seguimientos/categorias/{id}` | `Gestionar categorías de seguimiento` | Desactiva categoría. |

## Reglas de creación de seguimiento

Al crear un seguimiento:

- el DTO es obligatorio;
- no se permite enviar `id`;
- la descripción se normaliza;
- la descripción es obligatoria y máximo 200 caracteres;
- la fecha de entrega no puede ser anterior a la fecha actual;
- los días de notificación no pueden ser negativos;
- no se permiten días de notificación sin fecha de entrega;
- la categoría debe existir y estar activa;
- la consulta debe existir;
- la consulta debe permitir operación operativa;
- el usuario actual debe tener permiso y alcance sobre la consulta;
- el estudiante no puede crear seguimientos;
- el conciliador no puede crear seguimientos;
- el autor se toma del usuario autenticado;
- el seguimiento nace en estado `PENDIENTE`;
- las banderas booleanas nulas se asumen como `false`;
- se sincronizan notificaciones según configuración del seguimiento.

## Reglas de actualización de seguimiento

Al actualizar:

- el DTO es obligatorio;
- si se envía `id`, debe coincidir con la ruta;
- el usuario debe tener permiso de edición y alcance;
- solo se pueden editar seguimientos activos;
- solo se pueden editar seguimientos pendientes;
- no se permite cambiar la consulta de un seguimiento existente;
- la consulta asociada debe permitir operación operativa;
- se validan datos de fecha y notificación;
- se valida que existan cambios efectivos;
- al guardar se sincronizan o cancelan notificaciones según estado y flags.

## Reglas de cambio de estado

Estados permitidos:

- `PENDIENTE`;
- `COMPLETADO`;
- `CANCELADO`.

Reglas principales:

- el estado nuevo es obligatorio;
- no se permite cambiar al mismo estado;
- la consulta asociada debe permitir operación operativa;
- si se completa el seguimiento, no puede tener respuestas pendientes;
- si el seguimiento notifica al estudiante, para completarse debe tener una respuesta aprobada;
- no se puede reabrir como pendiente un seguimiento que ya tiene respuesta aprobada;
- al pasar a `PENDIENTE`, se sincronizan notificaciones;
- al pasar a `COMPLETADO` o `CANCELADO`, se cancelan notificaciones pendientes.

## Reglas de eliminación

La eliminación se implementa como desactivación lógica.

Al eliminar:

- se valida permiso de eliminación;
- se valida alcance;
- se valida que la consulta permita operación operativa;
- se cancelan notificaciones pendientes;
- el seguimiento queda con `activo=false`;
- las notificaciones enviadas permanecen como historial.

## Reglas de consulta

### Listar por consulta

Reglas:

- requiere `Ver seguimientos`;
- el estudiante debe usar el endpoint de visibles para estudiante;
- se valida alcance sobre la consulta;
- se excluyen consultas archivadas en listados operativos.

### Listar visibles para estudiante

Reglas:

- requiere `Ver seguimientos`;
- valida alcance sobre la consulta;
- devuelve seguimientos con `notificarEstudiante=true`.

### Listar por autor

Reglas:

- requiere `Ver seguimientos`;
- administrador puede consultar por cualquier autor;
- usuarios no administradores solo pueden consultar seguimientos creados por su propio usuario.

### Alertas disciplinarias

Reglas:

- requiere `Ver alertas disciplinarias`;
- devuelve seguimientos activos marcados con `alertaDisciplinaria=true`;
- excluye consultas archivadas en listados operativos.

## Reglas de respuestas

### Crear respuesta

Al responder:

- requiere `Responder seguimientos`;
- solo el estudiante puede responder;
- el seguimiento debe estar activo;
- el seguimiento debe tener `notificarEstudiante=true`;
- el estudiante debe tener alcance sobre la consulta del seguimiento;
- el seguimiento debe estar `PENDIENTE`;
- el estudiante actual debe existir y estar activo;
- no puede existir una respuesta pendiente previa del mismo estudiante para el mismo seguimiento;
- si la última respuesta fue aprobada, no se permiten nuevos intentos;
- si la última respuesta fue rechazada, se permite un nuevo intento;
- si se responde fuera de plazo, se marca `fueraPlazo=true`.

### Editar respuesta

Al editar:

- requiere `Responder seguimientos`;
- solo el estudiante propietario puede editar;
- la respuesta debe estar activa;
- la respuesta debe estar `PENDIENTE`;
- el seguimiento debe seguir estando `PENDIENTE`;
- el seguimiento debe seguir siendo visible para el estudiante;
- el contenido se normaliza;
- si no hay cambios, se rechaza la actualización;
- si se edita fuera de plazo, se marca `fueraPlazo=true`.

### Revisar respuesta

Al decidir:

- requiere `Aprobar respuestas de seguimiento`;
- estudiantes y conciliadores no pueden revisar respuestas;
- el usuario debe tener alcance sobre la consulta del seguimiento;
- solo se pueden revisar respuestas pendientes;
- la decisión debe ser `APROBADA` o `RECHAZADA`;
- la observación de revisión no puede superar 500 caracteres;
- se registra usuario revisor y fecha de decisión;
- si la respuesta se aprueba, el seguimiento se completa automáticamente.

## Reglas de categorías

Al crear categoría:

- no se permite enviar `id`;
- el nombre es obligatorio;
- el nombre se normaliza;
- el nombre no puede superar 50 caracteres;
- el nombre debe ser único ignorando mayúsculas/minúsculas;
- nace activa.

Al actualizar:

- el `id` del DTO no puede diferir del `id` de la ruta;
- el nombre se normaliza;
- se valida duplicado excluyendo el registro actual;
- debe existir cambio efectivo.

Al cambiar estado:

- el parámetro `activo` es obligatorio;
- no se permite cambiar al mismo estado.

Al eliminar:

- se valida que la categoría exista;
- se desactiva lógicamente mediante `activo=false`.

## Notificaciones

El módulo genera notificaciones según configuración del seguimiento.

### Notificaciones inmediatas

Se sincronizan cuando se crea o actualiza el seguimiento.

Tipos posibles:

- `PARTES`, si `notificarPartes=true`;
- `ESTUDIANTE`, si `notificarEstudiante=true`;
- `ALERTA_DISCIPLINARIA`, si `alertaDisciplinaria=true`.

Si una notificación inmediata ya fue enviada, no se reenvía para evitar duplicados.

### Recordatorios

Se programan cuando existen:

- `fechaEntrega`;
- `diasNotificacion`.

La fecha programada se calcula como:

```text
fechaEntrega - diasNotificacion
```

Tipos posibles:

- `AUTOR`, siempre que haya fecha y días configurados;
- `PARTES`, si `notificarPartes=true`;
- `ESTUDIANTE`, si `notificarEstudiante=true`;
- `ALERTA_DISCIPLINARIA`, si `alertaDisciplinaria=true`.

Si deja de aplicar, la notificación pendiente se desactiva.

Las notificaciones enviadas se conservan como historial.

### Scheduler

`SeguimientoNotificacionScheduler` ejecuta el procesamiento de notificaciones pendientes usando la expresión configurada en:

```text
app.seguimiento.notificaciones.cron
```

El scheduler procesa notificaciones pendientes cuya fecha programada sea menor o igual a la fecha actual.

## Destinatarios de notificación

`SeguimientoDestinatarioService` calcula destinatarios según tipo:

| Tipo | Destinatarios |
|---|---|
| `PARTES` | Persona principal, partes y contrapartes de la consulta. |
| `ESTUDIANTE` | Estudiante asignado a la consulta. |
| `ALERTA_DISCIPLINARIA` | Administrativos activos. |
| `AUTOR` | Usuario que creó el seguimiento, normalmente asesor o monitor. |

El servicio limpia destinatarios:

- elimina nulos;
- normaliza correos;
- descarta correos vacíos o inválidos;
- elimina duplicados por correo.

## Envío de correo

`SeguimientoEnvioNotificacionService` envía notificaciones pendientes.

Reglas:

- si no hay destinatarios, marca la notificación como enviada;
- si el correo se envía correctamente, marca `enviada=true`;
- registra `fechaEnvio`;
- limpia errores anteriores;
- si falla el envío, incrementa `intentos`;
- guarda el error recortado para no superar la longitud de columna.

## Relación con cierre de consulta

Las consultas no pueden cerrarse si existen seguimientos activos en estado `PENDIENTE`.

También bloquean cierre:

- respuestas activas pendientes;
- notificaciones activas no enviadas.

Esto evita cerrar una consulta con actividades operativas pendientes.

## Repositories

### `SeguimientoRepository`

Incluye consultas para:

- buscar seguimiento activo por id;
- buscar seguimiento activo excluyendo consulta archivada;
- listar por consulta;
- listar visibles para estudiante;
- listar por autor;
- listar alertas disciplinarias;
- listar por fecha de entrega;
- validar seguimientos asociados a categoría;
- validar seguimiento pendiente por consulta;
- obtener datos mínimos de notificación;
- obtener datos mínimos de correo.

### `SeguimientoRespuestaRepository`

Incluye consultas para:

- buscar respuesta activa por id;
- buscar respuesta activa excluyendo consulta archivada;
- buscar respuesta del estudiante por seguimiento;
- obtener último intento de respuesta;
- listar respuestas por seguimiento;
- listar respuestas por estado;
- validar respuestas pendientes o aprobadas por seguimiento;
- validar respuestas pendientes por consulta.

### `SeguimientoNotificacionRepository`

Incluye consultas para:

- buscar notificación por seguimiento, tipo y momento;
- listar notificaciones por seguimiento;
- listar pendientes por fecha programada;
- listar pendientes por seguimiento;
- validar notificaciones pendientes por consulta.

### `CategoriaSeguimientoRepository`

Incluye consultas para:

- validar nombre único;
- listar activas ordenadas por nombre;
- buscar activa por id.

## Consideraciones para frontend

- Usar `/api/seguimientos/categorias/activas` para combos al crear seguimiento.
- Usar `/api/seguimientos/consulta/{consultaId}` para perfiles con acceso general a la consulta.
- Usar `/api/seguimientos/consulta/{consultaId}/visibles-estudiante` para vista de estudiante.
- Usar `/api/seguimientos/{seguimientoId}/respuestas` para listar respuestas del seguimiento.
- Usar `/api/seguimientos/respuestas/pendientes` para bandeja de revisión.
- No permitir desde frontend cambiar consulta de un seguimiento.
- No mostrar edición de seguimientos completados o cancelados.
- No mostrar respuesta si `notificarEstudiante=false`.
- Manejar errores de negocio cuando existan respuestas pendientes o aprobadas.
- Usar `credentials: "include"` en peticiones protegidas.
