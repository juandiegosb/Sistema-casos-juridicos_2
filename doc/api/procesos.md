# API - Procesos

Este documento describe los endpoints del módulo de procesos y de los catálogos asociados a procesos.

El módulo de procesos permite registrar actuaciones asociadas a consultas jurídicas, controlar número de radicado, departamento, órgano de control, especialidad, estado del proceso y estado lógico activo/inactivo.

## Base paths

| Recurso | Base path |
|---|---|
| Procesos | `/api/procesos` |
| Órganos de control | `/api/organos-control` |
| Especialidades | `/api/especialidades` |

## Autenticación

Todos los endpoints requieren sesión válida.

El frontend debe enviar:

```javascript
credentials: "include"
```

## Permisos

### Procesos

| Permiso | Uso |
|---|---|
| `Ver procesos` | Permite listar y consultar procesos dentro del alcance del usuario. |
| `Gestionar procesos` | Permite crear, actualizar, cambiar estado y desactivar procesos dentro del alcance del usuario. |

### Catálogos de proceso

| Permiso | Uso |
|---|---|
| `Ver catálogos` | Permite consultar órganos de control y especialidades activas. |
| `Gestionar catálogos` | Permite administrar órganos de control y especialidades. |

## Estados de proceso

Valores aceptados:

```text
PENDIENTE
SENTENCIA_FAVORABLE
SENTENCIA_DESFAVORABLE
DESISTIMIENTO
RECHAZO
PRESCRIPCION
```

| Estado | Uso |
|---|---|
| `PENDIENTE` | Proceso pendiente de resultado. |
| `SENTENCIA_FAVORABLE` | Proceso con sentencia favorable. |
| `SENTENCIA_DESFAVORABLE` | Proceso con sentencia desfavorable. |
| `DESISTIMIENTO` | Proceso terminado por desistimiento. |
| `RECHAZO` | Proceso terminado por rechazo. |
| `PRESCRIPCION` | Proceso terminado por prescripción. |

## DTO `ProcesoDTO`

DTO principal del módulo de procesos.

```json
{
  "id": 1,
  "numeroRadicado": "12345678901234567890123",
  "departamentoId": 1,
  "consultaId": 1,
  "especialidadId": 1,
  "organoControlId": 1,
  "estado": "PENDIENTE",
  "activo": true
}
```

Los valores son ilustrativos y no representan datos reales.

### Campos

| Campo | Tipo | Regla |
|---|---|---|
| `id` | Long | No se envía en creación. En actualización debe coincidir con la ruta si se informa. |
| `numeroRadicado` | String | Obligatorio. Debe tener exactamente 23 caracteres. |
| `departamentoId` | Long | Obligatorio. Debe referenciar departamento activo. |
| `consultaId` | Long | Obligatorio. Define el alcance del proceso. |
| `especialidadId` | Long | Opcional. Si se envía, debe referenciar especialidad activa. |
| `organoControlId` | Long | Opcional. Si se envía, debe referenciar órgano de control activo. |
| `estado` | EstadoProceso | Estado jurídico/operativo del proceso. Si no se define en creación, backend usa `PENDIENTE`. |
| `activo` | Boolean | Estado lógico del registro. Se modifica por endpoint específico. |

## DTO `OrganoControlDTO`

```json
{
  "id": 1,
  "nombre": "Nombre del órgano de control",
  "activo": true
}
```

### Campos

| Campo | Tipo | Regla |
|---|---|---|
| `id` | Long | No se envía en creación. En actualización debe coincidir con la ruta si se informa. |
| `nombre` | String | Obligatorio. Máximo 80 caracteres. |
| `activo` | Boolean | Estado lógico del catálogo. |

## DTO `EspecialidadDTO`

```json
{
  "id": 1,
  "nombre": "Nombre de la especialidad",
  "organoControlId": 1,
  "activo": true
}
```

### Campos

| Campo | Tipo | Regla |
|---|---|---|
| `id` | Long | No se envía en creación. En actualización debe coincidir con la ruta si se informa. |
| `nombre` | String | Obligatorio. Máximo 80 caracteres. |
| `organoControlId` | Long | Obligatorio. Debe referenciar órgano de control activo. |
| `activo` | Boolean | Estado lógico del catálogo. |

---

# Procesos

## Resumen de endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/procesos` | `Ver procesos` o `Gestionar procesos` | Lista procesos activos dentro del alcance. |
| GET | `/api/procesos/{id}` | `Ver procesos` o `Gestionar procesos` | Consulta proceso activo por id dentro del alcance. |
| POST | `/api/procesos` | `Gestionar procesos` | Crea proceso. |
| PUT | `/api/procesos/{id}` | `Gestionar procesos` | Actualiza datos generales del proceso. |
| PATCH | `/api/procesos/{id}/estado?estado=` | `Gestionar procesos` | Cambia el estado del proceso. |
| PATCH | `/api/procesos/{id}/activo?activo=` | `Gestionar procesos` | Cambia estado lógico activo/inactivo. |
| DELETE | `/api/procesos/{id}` | `Gestionar procesos` | Desactiva lógicamente el proceso. |

---

## GET `/api/procesos`

Lista procesos activos visibles para el usuario autenticado.

### Reglas

- requiere `Ver procesos` o `Gestionar procesos`;
- lista procesos activos;
- excluye procesos asociados a consultas archivadas;
- aplica alcance heredado desde la consulta.

### Response `200 OK`

```json
[
  {
    "id": 1,
    "numeroRadicado": "12345678901234567890123",
    "departamentoId": 1,
    "consultaId": 1,
    "especialidadId": 1,
    "organoControlId": 1,
    "estado": "PENDIENTE",
    "activo": true
  }
]
```

## GET `/api/procesos/{id}`

Consulta un proceso activo por id.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |

### Reglas

- requiere `Ver procesos` o `Gestionar procesos`;
- el proceso debe existir y estar activo;
- la consulta asociada no debe estar archivada;
- el usuario debe tener alcance sobre la consulta asociada.

### Response `200 OK`

```json
{
  "id": 1,
  "numeroRadicado": "12345678901234567890123",
  "departamentoId": 1,
  "consultaId": 1,
  "especialidadId": 1,
  "organoControlId": 1,
  "estado": "PENDIENTE",
  "activo": true
}
```

## POST `/api/procesos`

Crea proceso asociado a una consulta jurídica.

### Request

Content-Type:

```text
application/json
```

Body:

```json
{
  "numeroRadicado": "12345678901234567890123",
  "departamentoId": 1,
  "consultaId": 1,
  "organoControlId": 1,
  "especialidadId": 1
}
```

### Reglas

- requiere `Gestionar procesos`;
- no se debe enviar `id`;
- `numeroRadicado` es obligatorio y debe tener exactamente 23 caracteres;
- `numeroRadicado` debe ser único;
- `departamentoId` es obligatorio y debe referenciar departamento activo;
- `consultaId` es obligatorio;
- la consulta debe permitir operación;
- el usuario debe tener alcance sobre la consulta asociada;
- estudiante y conciliador no gestionan procesos;
- `organoControlId` es opcional;
- `especialidadId` es opcional;
- si se informa especialidad, debe informarse órgano de control;
- si se informa especialidad, debe pertenecer al órgano de control seleccionado;
- el proceso nace con estado `PENDIENTE`;
- el proceso nace con `activo=true`.

### Response `201 Created`

Retorna `ProcesoDTO`.

## PUT `/api/procesos/{id}`

Actualiza datos generales de un proceso.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |

### Request

```json
{
  "id": 1,
  "numeroRadicado": "12345678901234567890123",
  "departamentoId": 1,
  "consultaId": 1,
  "organoControlId": 1,
  "especialidadId": 1
}
```

### Reglas

- requiere `Gestionar procesos`;
- el proceso debe existir y estar activo;
- si el body trae `id`, debe coincidir con el id de la ruta;
- no se permite cambiar `consultaId`;
- `numeroRadicado` debe conservar unicidad;
- departamento, órgano de control y especialidad deben estar activos cuando se informan;
- si existe especialidad, debe pertenecer al órgano de control seleccionado;
- debe existir al menos un cambio real;
- actualizar datos generales no cambia `activo`;
- el estado del proceso se cambia por endpoint específico.

### Response `200 OK`

Retorna `ProcesoDTO`.

## PATCH `/api/procesos/{id}/estado`

Cambia el estado jurídico/operativo del proceso.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |
| `estado` | EstadoProceso | Query |

Ejemplo:

```text
PATCH /api/procesos/1/estado?estado=SENTENCIA_FAVORABLE
```

### Estados aceptados

```text
PENDIENTE
SENTENCIA_FAVORABLE
SENTENCIA_DESFAVORABLE
DESISTIMIENTO
RECHAZO
PRESCRIPCION
```

### Reglas

- requiere `Gestionar procesos`;
- `estado` es obligatorio;
- no se permite cambiar al mismo estado actual;
- el proceso debe estar activo;
- la consulta asociada debe permitir operación;
- el usuario debe tener alcance sobre la consulta asociada.

### Response `200 OK`

Retorna `ProcesoDTO`.

## PATCH `/api/procesos/{id}/activo`

Cambia estado lógico activo/inactivo del proceso.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |
| `activo` | Boolean | Query |

Ejemplo:

```text
PATCH /api/procesos/1/activo?activo=false
```

### Reglas

- requiere `Gestionar procesos`;
- `activo` es obligatorio;
- no se permite cambiar al mismo estado lógico;
- el usuario debe tener alcance sobre la consulta asociada;
- la consulta asociada debe permitir operación.

### Response `200 OK`

Retorna `ProcesoDTO`.

## DELETE `/api/procesos/{id}`

Desactiva lógicamente un proceso.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |

### Reglas

- requiere `Gestionar procesos`;
- el proceso debe estar activo;
- el usuario debe tener alcance sobre el proceso;
- la consulta asociada debe permitir operación;
- no elimina físicamente el registro.

### Response `204 No Content`

No retorna cuerpo.

---

# Órganos de control

Base path:

```text
/api/organos-control
```

## Resumen de endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/organos-control` | `Ver catálogos` o `Gestionar catálogos` | Lista órganos activos. |
| GET | `/api/organos-control/todos` | `Gestionar catálogos` | Lista órganos activos e inactivos. |
| GET | `/api/organos-control/{id}` | `Ver catálogos` o `Gestionar catálogos` | Consulta órgano activo por id. |
| POST | `/api/organos-control` | `Gestionar catálogos` | Crea órgano de control. |
| PUT | `/api/organos-control/{id}` | `Gestionar catálogos` | Actualiza órgano de control. |
| PATCH | `/api/organos-control/{id}/activo?activo=` | `Gestionar catálogos` | Cambia estado activo. |
| DELETE | `/api/organos-control/{id}` | `Gestionar catálogos` | Desactiva órgano de control. |

## GET `/api/organos-control`

Lista órganos de control activos.

### Response `200 OK`

```json
[
  {
    "id": 1,
    "nombre": "Nombre del órgano de control",
    "activo": true
  }
]
```

## GET `/api/organos-control/todos`

Lista todos los órganos de control.

### Response `200 OK`

Retorna lista de `OrganoControlDTO`.

## GET `/api/organos-control/{id}`

Consulta órgano de control activo por id.

### Response `200 OK`

Retorna `OrganoControlDTO`.

## POST `/api/organos-control`

Crea órgano de control.

### Request

```json
{
  "nombre": "Nombre del órgano de control"
}
```

### Reglas

- no enviar `id`;
- `nombre` es obligatorio;
- máximo 80 caracteres;
- nombre único ignorando mayúsculas/minúsculas;
- nace con `activo=true`.

### Response `201 Created`

Retorna `OrganoControlDTO`.

## PUT `/api/organos-control/{id}`

Actualiza órgano de control.

### Request

```json
{
  "id": 1,
  "nombre": "Nombre actualizado"
}
```

### Reglas

- si se envía `id`, debe coincidir con la ruta;
- nombre obligatorio;
- máximo 80 caracteres;
- nombre único excluyendo el mismo registro;
- debe existir al menos un cambio real;
- no cambia estado activo.

### Response `200 OK`

Retorna `OrganoControlDTO`.

## PATCH `/api/organos-control/{id}/activo`

Cambia estado activo de órgano de control.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |
| `activo` | Boolean | Query |

### Reglas

- `activo` es obligatorio;
- no se permite cambiar al mismo estado;
- para desactivar, no debe tener especialidades activas asociadas.

### Response `200 OK`

Retorna `OrganoControlDTO`.

## DELETE `/api/organos-control/{id}`

Desactiva órgano de control.

### Reglas

- no debe tener especialidades activas asociadas;
- se realiza desactivación lógica.

### Response `204 No Content`

No retorna cuerpo.

---

# Especialidades

Base path:

```text
/api/especialidades
```

## Resumen de endpoints

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

## GET `/api/especialidades`

Lista especialidades activas.

### Response `200 OK`

```json
[
  {
    "id": 1,
    "nombre": "Nombre de la especialidad",
    "organoControlId": 1,
    "activo": true
  }
]
```

## GET `/api/especialidades/todos`

Lista todas las especialidades.

### Response `200 OK`

Retorna lista de `EspecialidadDTO`.

## GET `/api/especialidades/organo-control/{organoControlId}`

Lista especialidades activas de un órgano de control activo.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `organoControlId` | Long | Path |

### Response `200 OK`

Retorna lista de `EspecialidadDTO`.

## GET `/api/especialidades/{id}`

Consulta especialidad activa por id.

### Response `200 OK`

Retorna `EspecialidadDTO`.

## POST `/api/especialidades`

Crea especialidad.

### Request

```json
{
  "nombre": "Nombre de la especialidad",
  "organoControlId": 1
}
```

### Reglas

- no enviar `id`;
- `nombre` es obligatorio;
- máximo 80 caracteres;
- `organoControlId` es obligatorio;
- el órgano de control debe estar activo;
- el nombre debe ser único dentro del órgano de control;
- nace con `activo=true`.

### Response `201 Created`

Retorna `EspecialidadDTO`.

## PUT `/api/especialidades/{id}`

Actualiza especialidad.

### Request

```json
{
  "id": 1,
  "nombre": "Nombre actualizado",
  "organoControlId": 1
}
```

### Reglas

- si se envía `id`, debe coincidir con la ruta;
- nombre obligatorio;
- `organoControlId` obligatorio;
- el órgano de control debe estar activo;
- nombre único dentro del órgano de control excluyendo el registro actual;
- debe existir cambio real;
- no cambia estado activo.

### Response `200 OK`

Retorna `EspecialidadDTO`.

## PATCH `/api/especialidades/{id}/activo`

Cambia estado activo de especialidad.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |
| `activo` | Boolean | Query |

### Reglas

- `activo` es obligatorio;
- no se permite cambiar al mismo estado.

### Response `200 OK`

Retorna `EspecialidadDTO`.

## DELETE `/api/especialidades/{id}`

Desactiva especialidad.

### Response `204 No Content`

No retorna cuerpo.

---

# Errores comunes

| Estado | Causa |
|---|---|
| `400 Bad Request` | DTO obligatorio ausente. |
| `400 Bad Request` | Id enviado en creación. |
| `400 Bad Request` | Id del body diferente al id de ruta. |
| `400 Bad Request` | Campo obligatorio ausente. |
| `400 Bad Request` | Número de radicado con longitud diferente a 23 caracteres. |
| `400 Bad Request` | Número de radicado duplicado. |
| `400 Bad Request` | Departamento, consulta, órgano o especialidad inexistente o inactivo. |
| `400 Bad Request` | Intento de cambiar la consulta de un proceso existente. |
| `400 Bad Request` | Especialidad sin órgano de control. |
| `400 Bad Request` | Especialidad no pertenece al órgano seleccionado. |
| `400 Bad Request` | Cambio al mismo estado. |
| `400 Bad Request` | Sin cambios para actualizar. |
| `400 Bad Request` | Órgano de control con especialidades activas al desactivar. |
| `401 Unauthorized` | Sesión no válida. |
| `403 Forbidden` | Usuario sin permiso o sin alcance sobre la consulta asociada. |

# Notas para frontend

- Usar `GET /api/organos-control` para combos de órganos activos.
- Usar `GET /api/especialidades/organo-control/{organoControlId}` para cargar especialidades después de seleccionar órgano de control.
- Usar `GET /api/procesos` para listado visible del usuario.
- En creación de proceso, no enviar `id`.
- No permitir cambiar `consultaId` al editar proceso.
- Validar en UI que `numeroRadicado` tenga 23 caracteres, conservando validación final del backend.
- Para cambiar estado del proceso, usar `PATCH /api/procesos/{id}/estado`.
- Para activar o desactivar proceso, usar `PATCH /api/procesos/{id}/activo`.
- Para desactivar por eliminación lógica, usar `DELETE /api/procesos/{id}`.
- Usar `credentials: "include"` en todas las peticiones protegidas.
