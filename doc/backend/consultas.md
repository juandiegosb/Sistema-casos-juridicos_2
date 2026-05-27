# Backend - Consultas jurídicas

El módulo de consultas jurídicas administra el registro, consulta, actualización, cambio de estado, archivo y desarchivo de las consultas atendidas por el consultorio jurídico.

La consulta jurídica es uno de los ejes centrales del sistema, porque se relaciona con personas, catálogos, perfiles internos, seguimientos, procesos, conciliaciones y documentos asociados.

## Paquetes principales

```text
business/controller/consulta
business/dto/consulta
business/model/consulta
business/repository/consulta
business/service/acceso/consulta
business/service/consulta
business/service/consulta/consulta
```

## Componentes principales

| Componente | Responsabilidad |
|---|---|
| `ConsultaController` | Expone endpoints HTTP del módulo de consultas. |
| `ConsultaService` | Fachada del módulo; separa lectura y escritura. |
| `ConsultaCommandService` | Orquesta creación, actualización, cambio de estado, archivo y desarchivo. |
| `ConsultaQueryService` | Orquesta búsquedas, listados y consulta por id según alcance del usuario. |
| `ConsultaConstruccionService` | Aplica datos del DTO sobre la entidad y resuelve relaciones. |
| `ConsultaRelacionService` | Centraliza búsqueda de entidades relacionadas activas. |
| `ConsultaEstadoService` | Valida cambios de estado y pendientes operativos. |
| `ConsultaValidator` | Centraliza reglas de negocio propias de consulta. |
| `ConsultaMapper` | Convierte entidades a DTOs de respuesta. |
| `ConsultaAccessService` | Valida permisos y alcance sobre consultas. |
| `ConsultaRepository` | Acceso a datos y consultas especializadas. |

## Permisos usados

| Permiso | Uso |
|---|---|
| `Ver consultas` | Permite buscar y consultar consultas dentro del alcance del usuario. |
| `Crear consultas` | Permite crear consultas. |
| `Editar consultas` | Permite actualizar datos generales de consultas dentro del alcance. |
| `Cambiar estado consultas` | Permite cambiar estado de consultas según reglas del módulo. |
| `Archivar consultas` | Permite archivar, desarchivar y consultar archivadas con política administrativa. |
| `Asignar responsables consulta` | Permite asignar o modificar asesor, monitor y estudiante responsable. |
| `Gestionar consultas` | Permiso amplio conservado para operaciones generales del módulo. |

## Entidad principal

### `Consulta`

Tabla:

```text
consulta
```

Campos principales:

| Campo | Uso |
|---|---|
| `id` | Identificador de la consulta. |
| `fecha` | Fecha de la consulta. |
| `descripcion` | Descripción breve. |
| `hechos` | Relato de hechos. |
| `pretensiones` | Pretensiones de la persona consultante. |
| `conceptoJuridico` | Concepto jurídico asociado. |
| `tramite` | Trámite o ruta de atención. |
| `observaciones` | Observaciones internas. |
| `tipoViolencia` | Clasificación adicional cuando aplica. |
| `estado` | Estado operativo de la consulta. |
| `resultado` | Resultado o clasificación de cierre cuando aplica. |
| `persona` | Persona principal de la consulta. |
| `partes` | Personas relacionadas como partes adicionales. |
| `contrapartes` | Personas relacionadas como contrapartes. |
| `sede` | Sede de recepción. |
| `area` | Área jurídica. |
| `tema` | Tema jurídico. |
| `tipo` | Tipo jurídico asociado al tema. |
| `asesor` | Asesor responsable. |
| `monitor` | Monitor responsable. |
| `estudiante` | Estudiante responsable. |

## Relaciones principales

```text
Consulta -> Persona principal
Consulta -> Partes
Consulta -> Contrapartes
Consulta -> Sede
Consulta -> Área
Consulta -> Tema
Consulta -> Tipo
Consulta -> Asesor
Consulta -> Monitor
Consulta -> Estudiante
```

Las partes y contrapartes se modelan mediante relaciones `ManyToMany`:

```text
consulta_parte
consulta_contraparte
```

## Estados de consulta

La consulta usa el enum `EstadoConsulta`.

Estados:

| Estado | Uso |
|---|---|
| `PENDIENTE` | Estado inicial de toda consulta nueva. |
| `ACTIVO` | Consulta en atención activa. |
| `EN_PROCESO` | Consulta con gestión en proceso. |
| `URGENTE` | Consulta priorizada por urgencia. |
| `CERRADO` | Consulta cerrada operativamente. |
| `ARCHIVADO` | Consulta archivada para consulta histórica. |

## DTOs

### `ConsultaDTO`

DTO principal de entrada y salida.

Campos principales:

| Campo | Uso |
|---|---|
| `id` | Identificador de consulta. |
| `fecha` | Fecha de la consulta. |
| `descripcion` | Descripción breve. |
| `hechos` | Hechos relatados. |
| `pretensiones` | Pretensiones. |
| `conceptoJuridico` | Concepto jurídico. |
| `tramite` | Trámite. |
| `observaciones` | Observaciones. |
| `tipoViolencia` | Tipo de violencia. |
| `estado` | Estado de consulta. |
| `resultado` | Resultado. |
| `personaId` | Persona principal. |
| `partesIds` | Partes adicionales. |
| `contrapartesIds` | Contrapartes. |
| `sedeId` | Sede. |
| `areaId` | Área. |
| `temaId` | Tema. |
| `tipoId` | Tipo. |
| `asesorId` | Asesor asignado. |
| `monitorId` | Monitor asignado. |
| `estudianteId` | Estudiante asignado. |

Validaciones declaradas:

- `fecha` obligatoria;
- `descripcion` obligatoria y máximo 500 caracteres;
- `hechos` obligatorios;
- `pretensiones` obligatorias;
- `conceptoJuridico` obligatorio;
- `tramite` obligatorio y máximo 100 caracteres;
- `tipoViolencia` máximo 100 caracteres;
- `resultado` máximo 100 caracteres;
- `personaId` obligatorio;
- `sedeId` obligatorio;
- `areaId` obligatorio;
- `temaId` obligatorio.

### `ConsultaBusquedaDTO`

DTO usado en búsquedas y listados resumidos.

Campos:

| Campo | Uso |
|---|---|
| `id` | Identificador de consulta. |
| `consulta` | Descripción de la consulta. |
| `fecha` | Fecha. |
| `nombre` | Nombre de la persona principal. |
| `apellido` | Apellido de la persona principal. |
| `cedula` | Número de documento de la persona principal. |
| `estado` | Estado de consulta. |

Este DTO está alineado con los datos que consume el frontend para la búsqueda de consultas.

## Endpoints principales

Base path:

```text
/api/consultas
```

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/consultas?search=` | `Ver consultas` o `Gestionar consultas` | Busca consultas según alcance del usuario. |
| GET | `/api/consultas/{id}` | `Ver consultas` o `Gestionar consultas` | Consulta detalle por id. |
| POST | `/api/consultas` | `Crear consultas` o `Gestionar consultas` | Crea consulta. |
| PUT | `/api/consultas/{id}` | `Editar consultas` o `Gestionar consultas` | Actualiza datos generales. |
| PATCH | `/api/consultas/{id}/estado?estado=` | `Cambiar estado consultas`, `Archivar consultas` o `Gestionar consultas` | Cambia estado. |
| DELETE | `/api/consultas/{id}` | `Archivar consultas` | Archiva lógicamente. |
| PATCH | `/api/consultas/{id}/archivar` | `Archivar consultas` | Archiva consulta. |
| GET | `/api/consultas/archivadas` | `Archivar consultas` | Lista consultas archivadas. |
| PATCH | `/api/consultas/{id}/desarchivar` | `Archivar consultas` | Devuelve consulta archivada a estado cerrado. |

## Búsqueda y alcance

La búsqueda principal se realiza con:

```text
GET /api/consultas?search=
```

El backend filtra por usuario autenticado.

Reglas de búsqueda:

| Perfil | Alcance |
|---|---|
| Administrador | Consulta todas las consultas no archivadas. |
| Estudiante | Consulta las consultas donde es estudiante asignado. |
| Asesor | Consulta las consultas donde es asesor asignado o donde el estudiante asignado pertenece a su asesoría. |
| Monitor | Consulta las consultas donde es monitor asignado. |
| Conciliador | No recibe consultas desde el buscador general de consultas. |

La búsqueda compara el término contra:

- descripción de consulta;
- nombres de la persona principal;
- apellidos de la persona principal;
- número de documento de la persona principal.

Las consultas archivadas se excluyen de la búsqueda general.

## Consulta por id

Para obtener una consulta por id:

```text
GET /api/consultas/{id}
```

El backend valida:

- permiso de consulta;
- existencia de la consulta;
- alcance real del usuario sobre la consulta.

El repository carga partes y contrapartes en consultas separadas porque Hibernate no permite hacer `JOIN FETCH` de dos colecciones al mismo tiempo.

## Creación de consulta

Endpoint:

```text
POST /api/consultas
```

Reglas principales:

- el usuario debe tener permiso para crear consulta;
- no se permite enviar `id`;
- los campos obligatorios deben estar presentes;
- si se envía estado, debe ser `PENDIENTE`;
- toda consulta nueva se guarda en estado `PENDIENTE`;
- si el DTO solicita asignar responsables, se valida permiso de asignación;
- se resuelven relaciones activas;
- se normalizan textos;
- se valida coherencia del dominio antes de guardar.

## Actualización de consulta

Endpoint:

```text
PUT /api/consultas/{id}
```

Reglas principales:

- el usuario debe tener permiso de edición;
- el usuario debe tener alcance sobre la consulta;
- la consulta no puede estar archivada;
- la consulta no puede estar cerrada;
- no se permite cambiar el `id`;
- la edición de datos generales no cambia el estado;
- si se intenta cambiar estado desde el DTO, se rechaza;
- si se modifican responsables, se valida permiso específico;
- se valida coherencia del dominio después de aplicar cambios.

## Asignación de responsables

Responsables posibles:

- asesor;
- monitor;
- estudiante.

Reglas:

- asignar o cambiar responsables requiere `Asignar responsables consulta`;
- el estudiante no puede asignar responsables;
- si se asigna estudiante sin asesor explícito, el backend toma el asesor activo asociado al estudiante;
- el asesor asignado debe pertenecer al área de la consulta;
- si hay estudiante y asesor, el estudiante debe pertenecer al asesor seleccionado;
- el asesor del estudiante debe pertenecer al área de la consulta.

## Cambio de estado

Endpoint:

```text
PATCH /api/consultas/{id}/estado?estado=
```

Reglas principales:

- el estado es obligatorio;
- no se permite cambiar al mismo estado actual;
- la consulta no puede estar archivada;
- una consulta cerrada solo puede archivarse;
- el estudiante no puede cambiar estado;
- se valida permiso funcional;
- se valida alcance;
- se validan reglas de cierre y archivo;
- para pasar a estados operativos de atención se exigen responsables mínimos.

## Estados operativos que exigen responsables

Para pasar a alguno de estos estados:

- `ACTIVO`;
- `EN_PROCESO`;
- `URGENTE`;

la consulta debe tener:

- asesor asignado;
- estudiante asignado.

Además, se valida coherencia completa del dominio.

## Cierre de consulta

Para cerrar una consulta:

```text
estado=CERRADO
```

El backend valida que no existan pendientes operativos asociados.

Bloquean el cierre:

| Módulo | Condición |
|---|---|
| Procesos | Procesos activos en estado `PENDIENTE`. |
| Seguimientos | Seguimientos activos en estado `PENDIENTE`. |
| Respuestas de seguimiento | Respuestas activas en estado `PENDIENTE`. |
| Notificaciones de seguimiento | Notificaciones activas no enviadas. |
| Conciliaciones | Conciliaciones activas en estados no finalizados. |

Estados de conciliación que bloquean cierre:

- `EN_ESPERA`;
- `ESPERANDO_REUNION`;
- `REUNION_PROGRAMADA`.

## Archivo de consulta

La consulta se archiva en estado `ARCHIVADO`.

Reglas:

- solo se pueden archivar consultas cerradas;
- solo administrador con permiso de archivo puede archivar;
- antes de archivar se vuelven a validar pendientes operativos;
- `DELETE /api/consultas/{id}` funciona como archivado lógico, no como eliminación física.

## Desarchivo de consulta

Endpoint:

```text
PATCH /api/consultas/{id}/desarchivar
```

Reglas:

- solo se pueden desarchivar consultas archivadas;
- usa la misma política de permisos que archivar;
- al desarchivar, la consulta vuelve a `CERRADO`;
- se validan pendientes operativos antes de desarchivar.

## Coherencia de dominio

`ConsultaValidator` valida reglas cruzadas:

### Jerarquía de catálogos

- el tema debe pertenecer al área;
- si existe tipo, el tipo debe pertenecer al tema.

### Responsables

- el asesor debe pertenecer al área de la consulta;
- si estudiante y asesor están asignados, el estudiante debe pertenecer al asesor;
- el asesor del estudiante debe pertenecer al área de la consulta.

### Personas

- la persona principal no puede repetirse como parte adicional;
- la persona principal no puede repetirse como contraparte;
- una persona no puede estar al mismo tiempo como parte y contraparte;
- no puede haber personas duplicadas en partes;
- no puede haber personas duplicadas en contrapartes.

## Relaciones activas

`ConsultaRelacionService` resuelve únicamente relaciones activas para:

- persona;
- sede;
- área;
- tema;
- tipo;
- asesor;
- monitor;
- estudiante.

Esto evita asociar consultas a entidades inactivas.

## Repositories y consultas especializadas

`ConsultaRepository` incluye consultas para:

- cargar consulta con partes;
- cargar consulta con contrapartes;
- buscar consultas para administrador;
- buscar consultas para estudiante;
- buscar consultas para asesor;
- buscar consultas para monitor;
- buscar consultas filtradas;
- obtener destinatario principal para notificaciones;
- obtener destinatarios de partes;
- obtener destinatarios de contrapartes;
- obtener destinatario estudiante;
- listar por estado.

## Destinatarios para seguimientos

El repository expone consultas para obtener destinatarios de notificaciones asociadas a una consulta:

- persona principal;
- partes;
- contrapartes;
- estudiante asignado.

Solo se devuelven destinatarios con correo informado.

## Mapper

`ConsultaMapper` convierte:

- `Consulta` a `ConsultaDTO`;
- `Consulta` a `ConsultaBusquedaDTO`.

En el DTO de búsqueda se usan datos de la persona principal para nombre, apellido y cédula.

## Consideraciones para frontend

- Usar el buscador general con `search`.
- No filtrar consultas ajenas en frontend; el backend filtra por alcance.
- En creación, no enviar `id`.
- En creación, no enviar estado diferente de `PENDIENTE`.
- Para cambiar estado, usar `PATCH /api/consultas/{id}/estado`.
- No intentar cambiar estado desde `PUT`.
- Para responsables, enviar ids solo cuando el usuario tenga permiso de asignación.
- Para partes y contrapartes, evitar duplicados desde UI, aunque backend también valida.
- Manejar errores de negocio al cerrar o archivar consultas con pendientes.
- Usar `credentials: "include"` en peticiones protegidas.
