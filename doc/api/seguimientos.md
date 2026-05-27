# API - Seguimientos

Este documento describe los endpoints del módulo de seguimientos.

El módulo permite registrar seguimientos asociados a consultas jurídicas, consultar seguimientos por diferentes criterios, controlar estado, administrar categorías, responder seguimientos desde estudiante y revisar respuestas.

## Base paths

| Recurso | Base path |
|---|---|
| Seguimientos | `/api/seguimientos` |
| Respuestas de seguimiento | `/api/seguimientos` |
| Categorías de seguimiento | `/api/seguimientos/categorias` |

## Autenticación

Todos los endpoints requieren sesión válida.

El frontend debe enviar:

```javascript
credentials: "include"
```

## Permisos

| Permiso | Uso |
|---|---|
| `Ver seguimientos` | Consulta de seguimientos y respuestas según alcance. |
| `Crear seguimientos` | Creación de seguimientos. |
| `Editar seguimientos` | Actualización y cambio de estado de seguimientos. |
| `Eliminar seguimientos` | Desactivación lógica de seguimientos. |
| `Responder seguimientos` | Creación y edición de respuestas del estudiante. |
| `Aprobar respuestas de seguimiento` | Revisión de respuestas pendientes. |
| `Ver alertas disciplinarias` | Consulta de seguimientos marcados como alerta disciplinaria. |
| `Gestionar categorías de seguimiento` | Administración de categorías de seguimiento. |

## Estados

### Estados de seguimiento

```text
PENDIENTE
COMPLETADO
CANCELADO
```

| Estado | Uso |
|---|---|
| `PENDIENTE` | Seguimiento activo en gestión. |
| `COMPLETADO` | Seguimiento finalizado. |
| `CANCELADO` | Seguimiento cancelado. |

### Estados de respuesta

```text
PENDIENTE
APROBADA
RECHAZADA
```

| Estado | Uso |
|---|---|
| `PENDIENTE` | Respuesta enviada y pendiente de revisión. |
| `APROBADA` | Respuesta aprobada. |
| `RECHAZADA` | Respuesta rechazada. |

## DTO `SeguimientoRequestDTO`

DTO de entrada para crear o actualizar seguimientos.

```json
{
  "id": 1,
  "descripcion": "Descripción del seguimiento",
  "fechaEntrega": "YYYY-MM-DD",
  "diasNotificacion": 3,
  "notificarPartes": true,
  "notificarEstudiante": true,
  "alertaDisciplinaria": false,
  "categoriaSeguimientoId": 1,
  "consultaId": 1
}
```

### Campos

| Campo | Tipo | Regla |
|---|---|---|
| `id` | Long | No debe enviarse en creación. En actualización debe coincidir con la ruta si se envía. |
| `descripcion` | String | Obligatoria. Máximo 200 caracteres. |
| `fechaEntrega` | Date | Opcional. No puede ser anterior a la fecha actual. |
| `diasNotificacion` | Integer | Opcional. Debe ser mayor o igual a 0. |
| `notificarPartes` | Boolean | Opcional. Si es nulo, backend lo interpreta como `false`. |
| `notificarEstudiante` | Boolean | Opcional. Si es nulo, backend lo interpreta como `false`. |
| `alertaDisciplinaria` | Boolean | Opcional. Si es nulo, backend lo interpreta como `false`. |
| `categoriaSeguimientoId` | Long | Obligatorio. Debe referenciar categoría activa. |
| `consultaId` | Long | Obligatorio. Consulta asociada al seguimiento. |

Regla condicional:

- no se pueden definir `diasNotificacion` si `fechaEntrega` es nula.

## DTO `SeguimientoResponseDTO`

DTO de salida para seguimientos.

```json
{
  "id": 1,
  "descripcion": "Descripción del seguimiento",
  "fechaEntrega": "YYYY-MM-DD",
  "diasNotificacion": 3,
  "notificarPartes": true,
  "notificarEstudiante": true,
  "alertaDisciplinaria": false,
  "estado": "PENDIENTE",
  "categoriaSeguimientoId": 1,
  "categoriaSeguimientoNombre": "Categoría",
  "consultaId": 1,
  "autorId": 1,
  "autorUsername": "usuario",
  "fechaCreacion": "fecha-hora",
  "fechaActualizacion": "fecha-hora"
}
```

Los valores son ilustrativos y no corresponden a datos reales.

## DTO `CategoriaSeguimientoDTO`

```json
{
  "id": 1,
  "nombre": "Nombre de la categoría",
  "activo": true
}
```

Validaciones:

| Campo | Regla |
|---|---|
| `id` | No se envía en creación. En actualización debe coincidir con la ruta si se informa. |
| `nombre` | Obligatorio. Máximo 50 caracteres. |
| `activo` | Si es nulo en creación, backend lo asume como `true`. |

## DTO `SeguimientoRespuestaRequestDTO`

```json
{
  "id": 1,
  "contenido": "Contenido de la respuesta"
}
```

Validaciones:

| Campo | Regla |
|---|---|
| `id` | No se envía en creación. En actualización debe coincidir con la ruta si se informa. |
| `contenido` | Obligatorio. Máximo 1000 caracteres. |

## DTO `SeguimientoRespuestaDecisionDTO`

```json
{
  "estado": "APROBADA",
  "observacionRevision": "Observación de revisión"
}
```

Validaciones:

| Campo | Regla |
|---|---|
| `estado` | Obligatorio. Solo `APROBADA` o `RECHAZADA`. |
| `observacionRevision` | Opcional. Máximo 500 caracteres. |

## DTO `SeguimientoRespuestaResponseDTO`

```json
{
  "id": 1,
  "seguimientoId": 1,
  "consultaId": 1,
  "estudianteId": 1,
  "estudianteNombre": "Nombre estudiante",
  "contenido": "Contenido de la respuesta",
  "estado": "PENDIENTE",
  "fueraPlazo": false,
  "observacionRevision": "Observación",
  "revisadoPorId": 1,
  "revisadoPorUsername": "usuario",
  "activo": true,
  "fechaCreacion": "fecha-hora",
  "fechaActualizacion": "fecha-hora",
  "fechaDecision": "fecha-hora"
}
```

---

# Seguimientos

## Resumen de endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/seguimientos/consulta/{consultaId}` | `Ver seguimientos` | Lista seguimientos de una consulta. |
| GET | `/api/seguimientos/consulta/{consultaId}/visibles-estudiante` | `Ver seguimientos` | Lista seguimientos visibles para estudiante. |
| GET | `/api/seguimientos/autor/{autorId}` | `Ver seguimientos` | Lista seguimientos creados por un autor. |
| GET | `/api/seguimientos/alertas-disciplinarias` | `Ver alertas disciplinarias` | Lista alertas disciplinarias. |
| GET | `/api/seguimientos/fecha-entrega?fechaEntrega=` | `Ver seguimientos` | Lista seguimientos por fecha de entrega. |
| GET | `/api/seguimientos/{id}` | `Ver seguimientos` | Consulta seguimiento por id. |
| POST | `/api/seguimientos` | `Crear seguimientos` | Crea seguimiento. |
| PUT | `/api/seguimientos/{id}` | `Editar seguimientos` | Actualiza seguimiento. |
| PATCH | `/api/seguimientos/{id}/estado?estado=` | `Editar seguimientos` | Cambia estado de seguimiento. |
| DELETE | `/api/seguimientos/{id}` | `Eliminar seguimientos` | Desactiva seguimiento. |

---

## GET `/api/seguimientos/consulta/{consultaId}`

Lista seguimientos de una consulta.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `consultaId` | Long | Path |

### Reglas

- requiere `Ver seguimientos`;
- valida alcance sobre la consulta;
- el estudiante debe usar el endpoint de seguimientos visibles;
- retorna seguimientos activos de la consulta.

### Response `200 OK`

```json
[
  {
    "id": 1,
    "descripcion": "Descripción del seguimiento",
    "fechaEntrega": "YYYY-MM-DD",
    "diasNotificacion": 3,
    "notificarPartes": true,
    "notificarEstudiante": true,
    "alertaDisciplinaria": false,
    "estado": "PENDIENTE",
    "categoriaSeguimientoId": 1,
    "categoriaSeguimientoNombre": "Categoría",
    "consultaId": 1,
    "autorId": 1,
    "autorUsername": "usuario",
    "fechaCreacion": "fecha-hora",
    "fechaActualizacion": "fecha-hora"
  }
]
```

## GET `/api/seguimientos/consulta/{consultaId}/visibles-estudiante`

Lista seguimientos visibles para estudiante en una consulta.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `consultaId` | Long | Path |

### Reglas

- requiere `Ver seguimientos`;
- valida alcance sobre la consulta;
- retorna seguimientos con `notificarEstudiante=true`.

### Response `200 OK`

Retorna lista de `SeguimientoResponseDTO`.

## GET `/api/seguimientos/autor/{autorId}`

Lista seguimientos creados por un usuario autor.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `autorId` | Long | Path |

### Reglas

- requiere `Ver seguimientos`;
- administrador puede consultar cualquier autor;
- usuarios no administradores solo pueden consultar sus propios seguimientos.

### Response `200 OK`

Retorna lista de `SeguimientoResponseDTO`.

## GET `/api/seguimientos/alertas-disciplinarias`

Lista seguimientos marcados como alerta disciplinaria.

### Reglas

- requiere `Ver alertas disciplinarias`;
- retorna seguimientos activos con `alertaDisciplinaria=true`.

### Response `200 OK`

Retorna lista de `SeguimientoResponseDTO`.

## GET `/api/seguimientos/fecha-entrega`

Lista seguimientos por fecha de entrega.

### Query params

| Parámetro | Tipo | Obligatorio | Formato |
|---|---|---|---|
| `fechaEntrega` | Date | Sí | `YYYY-MM-DD` |

Ejemplo:

```text
GET /api/seguimientos/fecha-entrega?fechaEntrega=2026-05-27
```

### Response `200 OK`

Retorna lista de `SeguimientoResponseDTO`.

## GET `/api/seguimientos/{id}`

Consulta un seguimiento por id.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |

### Reglas

- requiere `Ver seguimientos`;
- valida existencia del seguimiento activo;
- valida alcance sobre el seguimiento.

### Response `200 OK`

Retorna `SeguimientoResponseDTO`.

---

# POST `/api/seguimientos`

Crea seguimiento.

## Request

Content-Type:

```text
application/json
```

Body:

```json
{
  "descripcion": "Descripción del seguimiento",
  "fechaEntrega": "YYYY-MM-DD",
  "diasNotificacion": 3,
  "notificarPartes": true,
  "notificarEstudiante": true,
  "alertaDisciplinaria": false,
  "categoriaSeguimientoId": 1,
  "consultaId": 1
}
```

## Reglas

- requiere `Crear seguimientos`;
- no se debe enviar `id`;
- estudiante no puede crear seguimientos;
- conciliador no puede crear seguimientos;
- la categoría debe existir y estar activa;
- la consulta debe existir y permitir operación;
- el usuario debe tener alcance sobre la consulta;
- el seguimiento nace con estado `PENDIENTE`;
- el autor se toma del usuario autenticado;
- se sincronizan notificaciones según flags y fecha.

## Response `200 OK`

Retorna `SeguimientoResponseDTO`.

## PUT `/api/seguimientos/{id}`

Actualiza seguimiento.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |

### Request

Mismo formato de `SeguimientoRequestDTO`.

### Reglas

- requiere `Editar seguimientos`;
- el usuario debe tener alcance sobre el seguimiento;
- el seguimiento debe estar activo;
- el seguimiento debe estar en estado `PENDIENTE`;
- no se permite cambiar la consulta asociada;
- la consulta asociada debe permitir operación;
- debe existir al menos un cambio real;
- después de actualizar se sincronizan notificaciones.

### Response `200 OK`

Retorna `SeguimientoResponseDTO`.

## PATCH `/api/seguimientos/{id}/estado`

Cambia estado del seguimiento.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |
| `estado` | EstadoSeguimiento | Query |

Ejemplo:

```text
PATCH /api/seguimientos/1/estado?estado=COMPLETADO
```

### Estados aceptados

```text
PENDIENTE
COMPLETADO
CANCELADO
```

### Reglas

- requiere `Editar seguimientos`;
- el estado es obligatorio;
- no se permite cambiar al mismo estado;
- la consulta asociada debe permitir operación;
- para completar no puede tener respuestas pendientes;
- si `notificarEstudiante=true`, para completar debe existir respuesta aprobada;
- no se puede reabrir como `PENDIENTE` si ya tiene respuesta aprobada;
- al quedar `PENDIENTE`, se sincronizan notificaciones;
- al quedar `COMPLETADO` o `CANCELADO`, se cancelan notificaciones pendientes.

### Response `200 OK`

Retorna `SeguimientoResponseDTO`.

## DELETE `/api/seguimientos/{id}`

Desactiva seguimiento.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |

### Reglas

- requiere `Eliminar seguimientos`;
- valida alcance sobre el seguimiento;
- la consulta asociada debe permitir operación;
- cancela notificaciones pendientes;
- desactiva el seguimiento de forma lógica.

### Response `200 OK`

Sin cuerpo.

---

# Respuestas de seguimiento

## Resumen de endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| POST | `/api/seguimientos/{seguimientoId}/respuestas` | `Responder seguimientos` | Crea respuesta del estudiante. |
| PUT | `/api/seguimientos/respuestas/{id}` | `Responder seguimientos` | Edita respuesta propia pendiente. |
| GET | `/api/seguimientos/respuestas/{id}` | `Ver seguimientos` | Consulta respuesta por id. |
| GET | `/api/seguimientos/{seguimientoId}/respuestas` | `Ver seguimientos` | Lista respuestas de un seguimiento. |
| GET | `/api/seguimientos/respuestas/pendientes` | `Aprobar respuestas de seguimiento` | Lista respuestas pendientes de revisión. |
| PATCH | `/api/seguimientos/respuestas/{id}/decision` | `Aprobar respuestas de seguimiento` | Aprueba o rechaza una respuesta. |

## POST `/api/seguimientos/{seguimientoId}/respuestas`

Crea respuesta de estudiante a seguimiento.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `seguimientoId` | Long | Path |

### Request

```json
{
  "contenido": "Contenido de la respuesta"
}
```

### Reglas

- requiere `Responder seguimientos`;
- solo estudiante puede responder;
- el seguimiento debe estar activo;
- el seguimiento debe tener `notificarEstudiante=true`;
- el estudiante debe tener alcance sobre la consulta;
- el seguimiento debe estar `PENDIENTE`;
- no puede existir respuesta pendiente previa del mismo estudiante para ese seguimiento;
- si la última respuesta fue aprobada, no se permite nuevo intento;
- si se responde fuera de plazo, se marca `fueraPlazo=true`.

### Response `200 OK`

Retorna `SeguimientoRespuestaResponseDTO`.

## PUT `/api/seguimientos/respuestas/{id}`

Edita respuesta propia pendiente.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |

### Request

```json
{
  "id": 1,
  "contenido": "Contenido actualizado"
}
```

### Reglas

- requiere `Responder seguimientos`;
- solo estudiante puede editar;
- solo puede editar su propia respuesta;
- la respuesta debe estar activa;
- la respuesta debe estar `PENDIENTE`;
- el seguimiento debe seguir visible para estudiante;
- el seguimiento debe estar `PENDIENTE`;
- debe existir cambio real;
- si se edita fuera de plazo, se actualiza `fueraPlazo=true`.

### Response `200 OK`

Retorna `SeguimientoRespuestaResponseDTO`.

## GET `/api/seguimientos/respuestas/{id}`

Consulta respuesta por id.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |

### Reglas

- requiere `Ver seguimientos`;
- valida que el usuario pueda ver la respuesta;
- estudiante solo puede ver su propia respuesta cuando el seguimiento es visible para estudiante.

### Response `200 OK`

Retorna `SeguimientoRespuestaResponseDTO`.

## GET `/api/seguimientos/{seguimientoId}/respuestas`

Lista respuestas de un seguimiento.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `seguimientoId` | Long | Path |

### Reglas

- requiere `Ver seguimientos`;
- valida alcance sobre el seguimiento;
- estudiante solo puede ver respuestas si el seguimiento está visible para estudiante.

### Response `200 OK`

```json
[
  {
    "id": 1,
    "seguimientoId": 1,
    "consultaId": 1,
    "estudianteId": 1,
    "estudianteNombre": "Nombre estudiante",
    "contenido": "Contenido de la respuesta",
    "estado": "PENDIENTE",
    "fueraPlazo": false,
    "observacionRevision": "Observación",
    "revisadoPorId": 1,
    "revisadoPorUsername": "usuario",
    "activo": true,
    "fechaCreacion": "fecha-hora",
    "fechaActualizacion": "fecha-hora",
    "fechaDecision": "fecha-hora"
  }
]
```

## GET `/api/seguimientos/respuestas/pendientes`

Lista respuestas pendientes de revisión.

### Reglas

- requiere `Aprobar respuestas de seguimiento`;
- estudiante y conciliador no pueden revisar respuestas;
- retorna respuestas activas con estado `PENDIENTE`.

### Response `200 OK`

Retorna lista de `SeguimientoRespuestaResponseDTO`.

## PATCH `/api/seguimientos/respuestas/{id}/decision`

Aprueba o rechaza respuesta.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |

### Request

```json
{
  "estado": "APROBADA",
  "observacionRevision": "Observación de revisión"
}
```

### Reglas

- requiere `Aprobar respuestas de seguimiento`;
- la respuesta debe estar activa;
- la respuesta debe estar `PENDIENTE`;
- el estado solo puede ser `APROBADA` o `RECHAZADA`;
- el usuario debe tener alcance sobre la consulta;
- si se aprueba, el seguimiento se completa automáticamente;
- se registra revisor y fecha de decisión.

### Response `200 OK`

Retorna `SeguimientoRespuestaResponseDTO`.

---

# Categorías de seguimiento

## Resumen de endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/seguimientos/categorias` | `Gestionar categorías de seguimiento` | Lista categorías. |
| GET | `/api/seguimientos/categorias/activas` | `Ver seguimientos`, `Crear seguimientos` o `Gestionar categorías de seguimiento` | Lista categorías activas. |
| GET | `/api/seguimientos/categorias/{id}` | `Gestionar categorías de seguimiento` | Consulta categoría por id. |
| POST | `/api/seguimientos/categorias` | `Gestionar categorías de seguimiento` | Crea categoría. |
| PUT | `/api/seguimientos/categorias/{id}` | `Gestionar categorías de seguimiento` | Actualiza categoría. |
| PATCH | `/api/seguimientos/categorias/{id}/activo?activo=` | `Gestionar categorías de seguimiento` | Cambia estado activo. |
| DELETE | `/api/seguimientos/categorias/{id}` | `Gestionar categorías de seguimiento` | Desactiva categoría. |

## POST `/api/seguimientos/categorias`

Crea categoría.

### Request

```json
{
  "nombre": "Nombre de la categoría"
}
```

### Reglas

- no enviar `id`;
- nombre obligatorio;
- nombre máximo de 50 caracteres;
- nombre único ignorando mayúsculas/minúsculas;
- si `activo` es nulo, backend lo asume como `true`.

### Response `200 OK`

Retorna `CategoriaSeguimientoDTO`.

## PUT `/api/seguimientos/categorias/{id}`

Actualiza categoría.

### Request

```json
{
  "id": 1,
  "nombre": "Nombre actualizado",
  "activo": true
}
```

### Reglas

- si se envía `id`, debe coincidir con la ruta;
- nombre obligatorio;
- nombre único excluyendo el mismo registro;
- debe existir cambio real.

### Response `200 OK`

Retorna `CategoriaSeguimientoDTO`.

## PATCH `/api/seguimientos/categorias/{id}/activo`

Cambia estado activo.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |
| `activo` | Boolean | Query |

### Response `200 OK`

Retorna `CategoriaSeguimientoDTO`.

## DELETE `/api/seguimientos/categorias/{id}`

Desactiva categoría.

### Response `200 OK`

Sin cuerpo.

---

# Errores comunes

| Estado | Causa |
|---|---|
| `400 Bad Request` | DTO obligatorio ausente. |
| `400 Bad Request` | Id enviado en creación. |
| `400 Bad Request` | Id del body diferente al id de ruta. |
| `400 Bad Request` | Campo obligatorio ausente. |
| `400 Bad Request` | Fecha de entrega anterior a la fecha actual. |
| `400 Bad Request` | Días de notificación negativos. |
| `400 Bad Request` | Días de notificación sin fecha de entrega. |
| `400 Bad Request` | Consulta, seguimiento, categoría o respuesta no encontrada. |
| `400 Bad Request` | Intento de cambiar consulta de seguimiento existente. |
| `400 Bad Request` | Seguimiento no editable por estado. |
| `400 Bad Request` | Cambio al mismo estado. |
| `400 Bad Request` | Respuesta pendiente bloquea completar seguimiento. |
| `400 Bad Request` | Respuesta aprobada bloquea reapertura. |
| `400 Bad Request` | Respuesta duplicada o intento no permitido. |
| `400 Bad Request` | Decisión distinta de `APROBADA` o `RECHAZADA`. |
| `401 Unauthorized` | Sesión no válida. |
| `403 Forbidden` | Usuario sin permiso o sin alcance. |

# Notas para frontend

- Usar `/api/seguimientos/categorias/activas` para combos.
- Usar `/api/seguimientos/consulta/{consultaId}` para vista general de una consulta.
- Usar `/api/seguimientos/consulta/{consultaId}/visibles-estudiante` para vista de estudiante.
- Usar `/api/seguimientos/respuestas/pendientes` para bandeja de revisión.
- No permitir cambiar `consultaId` al editar seguimiento.
- Mostrar respuesta del estudiante solo si el seguimiento tiene `notificarEstudiante=true`.
- Para completar un seguimiento con respuesta de estudiante, debe existir respuesta aprobada.
- Manejar errores de negocio al completar, reabrir, responder o revisar.
- Usar `credentials: "include"` en todas las peticiones protegidas.
