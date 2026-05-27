# Backend - Procesos

El módulo de procesos administra procesos asociados a consultas jurídicas.

Un proceso representa una actuación formal relacionada con una consulta, identificada por número de radicado, departamento, órgano de control, especialidad, estado del proceso y estado activo para control lógico.

## Paquetes principales

```text
business/controller/proceso
business/dto/proceso
business/model/proceso
business/repository/proceso
business/service/acceso/proceso
business/service/proceso
business/service/proceso/catalogo
business/service/proceso/proceso
```

## Componentes principales

| Componente | Responsabilidad |
|---|---|
| `ProcesoController` | Expone endpoints HTTP para procesos. |
| `ProcesoService` | Fachada del módulo de procesos. |
| `ProcesoCommandService` | Orquesta creación, actualización, cambio de estado, activación/desactivación y eliminación lógica. |
| `ProcesoQueryService` | Orquesta listados y consulta por id según permisos y alcance. |
| `ProcesoValidator` | Centraliza reglas de negocio del proceso. |
| `ProcesoMapper` | Convierte entidad a DTO y aplica datos validados a la entidad. |
| `ProcesoAccessService` | Valida permisos funcionales y alcance heredado desde consulta. |
| `ProcesoRepository` | Acceso a datos de procesos. |
| `OrganoControlService` | Administra catálogo de órganos de control. |
| `EspecialidadService` | Administra catálogo de especialidades asociadas a órgano de control. |
| `OrganoControlValidator` | Valida reglas del catálogo de órganos de control. |
| `EspecialidadValidator` | Valida reglas del catálogo de especialidades. |

## Permisos usados

### Procesos

| Permiso | Uso |
|---|---|
| `Ver procesos` | Permite listar y consultar procesos dentro del alcance del usuario. |
| `Gestionar procesos` | Permite crear, actualizar, cambiar estado y desactivar procesos dentro del alcance del usuario. |

### Catálogos relacionados

| Permiso | Uso |
|---|---|
| `Ver catálogos` | Permite consultar órganos de control y especialidades activas. |
| `Gestionar catálogos` | Permite administrar órganos de control y especialidades. |

## Entidades

### `Proceso`

Tabla:

```text
proceso
```

Campos principales:

| Campo | Uso |
|---|---|
| `id` | Identificador del proceso. |
| `numeroRadicado` | Número único de radicado del proceso. |
| `departamento` | Departamento asociado al proceso. |
| `consulta` | Consulta jurídica asociada. |
| `organoControl` | Órgano de control asociado. |
| `especialidad` | Especialidad asociada al órgano de control. |
| `estado` | Estado real del resultado del proceso. |
| `activo` | Control lógico de activación/desactivación. |

Regla importante:

```text
estado del proceso != activo
```

`estado` representa el resultado jurídico u operativo del proceso.  
`activo` representa borrado lógico o disponibilidad operativa del registro.

### `OrganoControl`

Tabla:

```text
organo_control
```

Campos:

| Campo | Uso |
|---|---|
| `id` | Identificador. |
| `nombre` | Nombre del órgano de control. |
| `activo` | Estado activo/inactivo. |
| `especialidades` | Especialidades relacionadas. |

### `Especialidad`

Tabla:

```text
especialidad
```

Campos:

| Campo | Uso |
|---|---|
| `id` | Identificador. |
| `nombre` | Nombre de la especialidad. |
| `activo` | Estado activo/inactivo. |
| `organoControl` | Órgano de control al que pertenece. |

## Estados de proceso

Enum:

```text
EstadoProceso
```

Valores:

| Estado | Uso |
|---|---|
| `PENDIENTE` | Proceso pendiente de resultado. |
| `SENTENCIA_FAVORABLE` | Proceso con sentencia favorable. |
| `SENTENCIA_DESFAVORABLE` | Proceso con sentencia desfavorable. |
| `DESISTIMIENTO` | Proceso terminado por desistimiento. |
| `RECHAZO` | Proceso terminado por rechazo. |
| `PRESCRIPCION` | Proceso terminado por prescripción. |

Estado por defecto:

```text
PENDIENTE
```

La entidad asegura valores por defecto antes de persistir o actualizar:

- si `estado` es nulo, se asigna `PENDIENTE`;
- si `activo` es nulo, se asigna `true`.

## DTOs

### `ProcesoDTO`

DTO principal del módulo.

Campos:

| Campo | Validación/Uso |
|---|---|
| `id` | Identificador. No debe enviarse en creación y no puede cambiar en actualización. |
| `numeroRadicado` | Obligatorio. Debe tener exactamente 23 caracteres. |
| `departamentoId` | Obligatorio. Debe referenciar un departamento activo. |
| `consultaId` | Obligatorio. Define el alcance del proceso. |
| `especialidadId` | Opcional. Debe referenciar una especialidad activa si se envía. |
| `organoControlId` | Opcional. Debe referenciar un órgano de control activo si se envía. |
| `estado` | Estado del proceso. |
| `activo` | Estado lógico del registro. |

### `OrganoControlDTO`

DTO para órgano de control.

Campos:

| Campo | Validación/Uso |
|---|---|
| `id` | Identificador. No debe enviarse en creación. |
| `nombre` | Obligatorio. Máximo 80 caracteres. |
| `activo` | Estado activo/inactivo. |

### `EspecialidadDTO`

DTO para especialidad.

Campos:

| Campo | Validación/Uso |
|---|---|
| `id` | Identificador. No debe enviarse en creación. |
| `nombre` | Obligatorio. Máximo 80 caracteres. |
| `organoControlId` | Obligatorio. Debe referenciar un órgano de control activo. |
| `activo` | Estado activo/inactivo. |

## Endpoints de procesos

Base path:

```text
/api/procesos
```

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/procesos` | `Ver procesos` o `Gestionar procesos` | Lista procesos activos dentro del alcance. |
| GET | `/api/procesos/{id}` | `Ver procesos` o `Gestionar procesos` | Consulta proceso activo por id dentro del alcance. |
| POST | `/api/procesos` | `Gestionar procesos` | Crea proceso. |
| PUT | `/api/procesos/{id}` | `Gestionar procesos` | Actualiza datos generales del proceso. |
| PATCH | `/api/procesos/{id}/estado?estado=` | `Gestionar procesos` | Cambia el estado jurídico/operativo del proceso. |
| PATCH | `/api/procesos/{id}/activo?activo=` | `Gestionar procesos` | Cambia el estado lógico activo/inactivo. |
| DELETE | `/api/procesos/{id}` | `Gestionar procesos` | Desactiva lógicamente el proceso. |

## Endpoints de órganos de control

Base path:

```text
/api/organos-control
```

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/organos-control` | `Ver catálogos` o `Gestionar catálogos` | Lista órganos activos. |
| GET | `/api/organos-control/todos` | `Gestionar catálogos` | Lista órganos activos e inactivos. |
| GET | `/api/organos-control/{id}` | `Ver catálogos` o `Gestionar catálogos` | Consulta órgano activo por id. |
| POST | `/api/organos-control` | `Gestionar catálogos` | Crea órgano de control. |
| PUT | `/api/organos-control/{id}` | `Gestionar catálogos` | Actualiza órgano de control. |
| PATCH | `/api/organos-control/{id}/activo?activo=` | `Gestionar catálogos` | Cambia estado activo. |
| DELETE | `/api/organos-control/{id}` | `Gestionar catálogos` | Desactiva órgano de control. |

## Endpoints de especialidades

Base path:

```text
/api/especialidades
```

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/especialidades` | `Ver catálogos` o `Gestionar catálogos` | Lista especialidades activas. |
| GET | `/api/especialidades/todos` | `Gestionar catálogos` | Lista especialidades activas e inactivas. |
| GET | `/api/especialidades/organo-control/{organoControlId}` | `Ver catálogos` o `Gestionar catálogos` | Lista especialidades activas de un órgano activo. |
| GET | `/api/especialidades/{id}` | `Ver catálogos` o `Gestionar catálogos` | Consulta especialidad activa por id. |
| POST | `/api/especialidades` | `Gestionar catálogos` | Crea especialidad. |
| PUT | `/api/especialidades/{id}` | `Gestionar catálogos` | Actualiza especialidad. |
| PATCH | `/api/especialidades/{id}/activo?activo=` | `Gestionar catálogos` | Cambia estado activo. |
| DELETE | `/api/especialidades/{id}` | `Gestionar catálogos` | Desactiva especialidad. |

## Reglas de creación de proceso

Al crear un proceso:

- el DTO es obligatorio;
- no se permite enviar `id`;
- el número de radicado es obligatorio;
- el número de radicado se normaliza;
- el número de radicado debe tener exactamente 23 caracteres;
- el número de radicado debe ser único;
- el departamento es obligatorio y debe estar activo;
- la consulta es obligatoria;
- la consulta debe permitir operación operativa;
- el usuario debe tener permiso `Gestionar procesos`;
- el usuario debe tener alcance sobre la consulta asociada;
- el estudiante no puede gestionar procesos;
- el conciliador no gestiona procesos;
- el órgano de control es opcional;
- la especialidad es opcional;
- si se informa especialidad, debe pertenecer al órgano de control seleccionado;
- el proceso nace con estado `PENDIENTE`;
- el proceso nace con `activo=true`.

## Reglas de actualización de proceso

Al actualizar un proceso:

- el usuario debe tener permiso `Gestionar procesos`;
- el usuario debe tener alcance sobre el proceso;
- el proceso debe estar activo;
- si el DTO trae `id`, debe coincidir con el `id` de la ruta;
- no se permite cambiar la consulta asociada;
- el número de radicado se normaliza;
- el número de radicado debe conservar unicidad;
- las relaciones deben estar activas;
- si existe especialidad, debe pertenecer al órgano de control seleccionado;
- debe existir al menos un cambio real para actualizar;
- actualizar datos generales no cambia `activo`;
- el estado del proceso se cambia por endpoint específico.

## Regla de consulta asociada

La consulta define el alcance del proceso.

Por esa razón:

- no se permite cambiar `consultaId` en actualización;
- el acceso al proceso se hereda desde la consulta;
- crear proceso valida acceso sobre la consulta;
- listar procesos filtra por alcance de consulta;
- ver proceso valida alcance de consulta;
- gestionar proceso valida alcance de consulta.

## Reglas de estado del proceso

Endpoint:

```text
PATCH /api/procesos/{id}/estado?estado=
```

Reglas:

- el estado es obligatorio;
- no se permite cambiar al mismo estado;
- el usuario debe tener permiso `Gestionar procesos`;
- el usuario debe tener alcance sobre la consulta del proceso;
- la consulta asociada debe permitir operación operativa;
- el proceso debe estar activo.

Estados permitidos:

- `PENDIENTE`;
- `SENTENCIA_FAVORABLE`;
- `SENTENCIA_DESFAVORABLE`;
- `DESISTIMIENTO`;
- `RECHAZO`;
- `PRESCRIPCION`.

## Reglas de estado activo

Endpoint:

```text
PATCH /api/procesos/{id}/activo?activo=
```

Reglas:

- el parámetro `activo` es obligatorio;
- no se permite cambiar al mismo estado lógico;
- el usuario debe tener permiso `Gestionar procesos`;
- el usuario debe tener alcance sobre la consulta del proceso;
- la consulta asociada debe permitir operación operativa.

## Eliminación lógica

Endpoint:

```text
DELETE /api/procesos/{id}
```

El proceso se desactiva mediante:

```text
activo=false
```

Reglas:

- el proceso debe estar activo;
- el usuario debe tener permiso `Gestionar procesos`;
- el usuario debe tener alcance sobre el proceso;
- la consulta asociada debe permitir operación operativa;
- no se elimina físicamente para conservar historial asociado a la consulta.

## Listado de procesos

Endpoint:

```text
GET /api/procesos
```

Reglas:

- requiere permiso `Ver procesos`;
- lista procesos activos;
- excluye procesos asociados a consultas archivadas;
- aplica alcance registro por registro;
- el alcance se hereda de la consulta.

## Consulta por id

Endpoint:

```text
GET /api/procesos/{id}
```

Reglas:

- requiere permiso `Ver procesos`;
- el proceso debe estar activo;
- el proceso no debe pertenecer a una consulta archivada;
- el usuario debe tener alcance sobre la consulta asociada.

## Reglas de alcance

`ProcesoAccessService` centraliza acceso a procesos.

### Administrador, asesor y monitor

Pueden acceder o gestionar procesos según permisos y alcance heredado desde la consulta.

### Estudiante

Puede consultar procesos dentro de su alcance cuando tiene permiso de visualización, pero no puede gestionarlos.

### Conciliador

El alcance operativo de procesos no se asigna al perfil conciliador.

## Relación con cierre de consulta

El proceso participa en reglas de cierre de consulta.

Una consulta no puede cerrarse si tiene procesos activos en estado:

```text
PENDIENTE
```

Esta validación evita cerrar una consulta con procesos operativos pendientes.

## Reglas de órganos de control

### Creación

Al crear órgano de control:

- el DTO es obligatorio;
- no se permite enviar `id`;
- el nombre es obligatorio;
- el nombre se normaliza;
- el nombre no puede superar 80 caracteres;
- el nombre debe ser único ignorando mayúsculas/minúsculas;
- nace con `activo=true`.

### Actualización

Al actualizar órgano de control:

- si el DTO trae `id`, debe coincidir con la ruta;
- el nombre se normaliza;
- se valida unicidad excluyendo el registro actual;
- debe existir cambio real;
- actualizar datos no cambia `activo`.

### Cambio de estado

Al desactivar:

- si tiene especialidades activas asociadas, no se permite desactivar;
- esto protege la jerarquía de catálogo y evita dejar especialidades activas bajo un órgano inactivo.

### Eliminación

La eliminación se implementa como desactivación lógica.

Antes de desactivar se valida que no existan especialidades activas asociadas.

## Reglas de especialidades

### Creación

Al crear especialidad:

- el DTO es obligatorio;
- no se permite enviar `id`;
- el nombre es obligatorio;
- el nombre se normaliza;
- el nombre no puede superar 80 caracteres;
- el órgano de control es obligatorio;
- el órgano de control debe estar activo;
- el nombre debe ser único dentro del órgano de control;
- nace con `activo=true`.

### Actualización

Al actualizar especialidad:

- si el DTO trae `id`, debe coincidir con la ruta;
- el nombre se normaliza;
- el órgano de control debe estar activo;
- el nombre debe ser único dentro del órgano de control excluyendo la misma especialidad;
- debe existir cambio real;
- actualizar datos no cambia `activo`.

### Cambio de estado

Al cambiar estado:

- el parámetro `activo` es obligatorio;
- no se permite cambiar al mismo estado.

### Eliminación

La eliminación se implementa como desactivación lógica para conservar procesos históricos que ya usan la especialidad.

## Reglas de duplicado

| Recurso | Regla |
|---|---|
| Proceso | `numeroRadicado` único. |
| Órgano de control | `nombre` único ignorando mayúsculas/minúsculas. |
| Especialidad | `nombre` único dentro del órgano de control ignorando mayúsculas/minúsculas. |

## Repositories

### `ProcesoRepository`

Consultas principales:

- buscar proceso activo por id;
- buscar proceso activo excluyendo consultas archivadas;
- listar procesos activos;
- listar procesos activos excluyendo consultas archivadas;
- validar número de radicado único;
- validar número de radicado único excluyendo id actual;
- validar procesos activos pendientes por consulta.

### `OrganoControlRepository`

Consultas principales:

- buscar órgano activo por id;
- listar órganos activos ordenados por nombre;
- validar nombre único;
- validar nombre único excluyendo id actual;
- listar todos ordenados por nombre.

### `EspecialidadRepository`

Consultas principales:

- buscar especialidad activa por id;
- listar especialidades activas ordenadas por nombre;
- listar especialidades activas por órgano de control;
- validar duplicado por nombre y órgano;
- validar duplicado por nombre y órgano excluyendo id actual;
- validar existencia de especialidades activas asociadas a un órgano;
- listar todas ordenadas por nombre;
- listar por órgano de control.

## Consideraciones para frontend

- Usar `GET /api/organos-control` para combos de órganos activos.
- Usar `GET /api/especialidades/organo-control/{organoControlId}` para cargar especialidades según el órgano seleccionado.
- No permitir cambiar la consulta asociada desde el formulario de edición.
- En creación, no enviar `id`.
- El número de radicado debe tener exactamente 23 caracteres.
- Para cambiar estado jurídico del proceso, usar `PATCH /api/procesos/{id}/estado`.
- Para activar o desactivar registro, usar `PATCH /api/procesos/{id}/activo`.
- Manejar errores de negocio cuando el número de radicado esté duplicado.
- Manejar errores de negocio cuando la especialidad no pertenezca al órgano seleccionado.
- Usar `credentials: "include"` en peticiones protegidas.
