# API - Conciliaciones

Este documento describe los endpoints del módulo de conciliaciones.

El módulo permite crear conciliaciones asociadas a consultas jurídicas, consultar listados y detalle, asignar responsables, cambiar estado operativo, finalizar con acta PDF, reemplazar solicitud PDF y desactivar conciliaciones de forma lógica.

## Base path

```text
/api/conciliaciones
```

## Autenticación

Todos los endpoints requieren sesión válida.

El frontend debe enviar:

```javascript
credentials: "include"
```

## Permisos

| Permiso | Uso |
|---|---|
| `Ver conciliaciones` | Permite listar y consultar conciliaciones visibles según alcance. |
| `Gestionar conciliaciones` | Permite crear conciliaciones, asignar conciliador, reemplazar solicitud y desactivar según alcance. |
| `Concluir conciliaciones` | Permite acciones de flujo y finalización para conciliador asignado según alcance. |

## Estados de conciliación

Los estados se administran desde la tabla `estado_conciliacion`.

El backend valida reglas por `estadoCodigo`, no por el nombre visible.

Valores base:

```text
EN_ESPERA
ESPERANDO_REUNION
REUNION_PROGRAMADA
COMPLETO_CONCILIADO
COMPLETO_NO_CONCILIADO
```

| Código | Uso |
|---|---|
| `EN_ESPERA` | Estado automático cuando falta estudiante o conciliador asignado. |
| `ESPERANDO_REUNION` | Estado operativo cuando existe estudiante y conciliador asignados. |
| `REUNION_PROGRAMADA` | Estado operativo cuando la conciliación tiene responsables y fecha programada. |
| `COMPLETO_CONCILIADO` | Estado final con resultado conciliado. |
| `COMPLETO_NO_CONCILIADO` | Estado final con resultado no conciliado. |

## Clasificación de estados

### Estados no finalizados

```text
EN_ESPERA
ESPERANDO_REUNION
REUNION_PROGRAMADA
```

### Estados finalizados

```text
COMPLETO_CONCILIADO
COMPLETO_NO_CONCILIADO
```

## Normalización del parámetro `estado`

Cuando se recibe un código de estado, el backend normaliza:

- espacios;
- mayúsculas/minúsculas;
- guiones o espacios reemplazados por guion bajo.

Ejemplos de entrada equivalentes:

```text
completo conciliado
COMPLETO-CONCILIADO
COMPLETO_CONCILIADO
```

El código técnico persistido y retornado por API se mantiene en formato normalizado.

---

# DTO `ConciliacionResponseDTO`

DTO de salida para listados y operaciones generales.

```json
{
  "id": 1,
  "consultaId": 1,
  "estudianteId": 1,
  "estudianteNombre": "Nombre estudiante",
  "conciliadorId": 1,
  "conciliadorNombre": "Nombre conciliador",
  "estadoId": 1,
  "estadoCodigo": "EN_ESPERA",
  "estadoNombre": "En espera",
  "fechaConciliacion": "fecha-hora",
  "documentoSolicitudPath": "conciliacion/1/solicitud.pdf",
  "actaPath": "conciliacion/1/acta.pdf",
  "solicitadoPorId": 1,
  "solicitadoPorUsername": "usuario",
  "activo": true,
  "fechaCreacion": "fecha-hora",
  "fechaActualizacion": "fecha-hora",
  "fechaFinalizacion": "fecha-hora"
}
```

Los valores son ilustrativos y no representan datos reales.

## Campos

| Campo | Tipo | Uso |
|---|---|---|
| `id` | Long | Identificador de conciliación. |
| `consultaId` | Long | Consulta asociada. |
| `estudianteId` | Long | Estudiante asignado. Puede ser nulo. |
| `estudianteNombre` | String | Nombre del estudiante asignado. |
| `conciliadorId` | Long | Conciliador asignado. Puede ser nulo. |
| `conciliadorNombre` | String | Nombre del conciliador asignado. |
| `estadoId` | Long | Identificador del estado. |
| `estadoCodigo` | String | Código técnico del estado. |
| `estadoNombre` | String | Nombre visible del estado. |
| `fechaConciliacion` | DateTime | Fecha programada de conciliación cuando aplica. |
| `documentoSolicitudPath` | String | Ruta relativa de la solicitud PDF. |
| `actaPath` | String | Ruta relativa del acta PDF. |
| `solicitadoPorId` | Long | Usuario que solicitó la conciliación. |
| `solicitadoPorUsername` | String | Username del usuario solicitante. |
| `activo` | Boolean | Estado lógico de la conciliación. |
| `fechaCreacion` | DateTime | Fecha de creación. |
| `fechaActualizacion` | DateTime | Fecha de última actualización. |
| `fechaFinalizacion` | DateTime | Fecha de finalización cuando aplica. |

---

# DTO `ConciliacionDetalleResponseDTO`

DTO de salida para detalle de conciliación.

Incluye los campos de `ConciliacionResponseDTO` y agrega información de la consulta asociada:

```json
{
  "id": 1,
  "consultaId": 1,
  "estudianteId": 1,
  "estudianteNombre": "Nombre estudiante",
  "conciliadorId": 1,
  "conciliadorNombre": "Nombre conciliador",
  "estadoId": 1,
  "estadoCodigo": "EN_ESPERA",
  "estadoNombre": "En espera",
  "fechaConciliacion": "fecha-hora",
  "consultante": {
    "id": 1,
    "nombre": "Nombre consultante"
  },
  "partes": [
    {
      "id": 2,
      "nombre": "Nombre parte"
    }
  ],
  "contrapartes": [
    {
      "id": 3,
      "nombre": "Nombre contraparte"
    }
  ],
  "documentoSolicitudPath": "conciliacion/1/solicitud.pdf",
  "actaPath": "conciliacion/1/acta.pdf",
  "solicitadoPorId": 1,
  "solicitadoPorUsername": "usuario",
  "activo": true,
  "fechaCreacion": "fecha-hora",
  "fechaActualizacion": "fecha-hora",
  "fechaFinalizacion": "fecha-hora"
}
```

## Campos adicionales

| Campo | Tipo | Uso |
|---|---|---|
| `consultante` | `ConciliacionPersonaDTO` | Persona principal de la consulta. |
| `partes` | List<`ConciliacionPersonaDTO`> | Partes asociadas a la consulta. |
| `contrapartes` | List<`ConciliacionPersonaDTO`> | Contrapartes asociadas a la consulta. |

---

# DTO `ConciliacionPersonaDTO`

DTO liviano para personas en detalle.

```json
{
  "id": 1,
  "nombre": "Nombre completo"
}
```

| Campo | Tipo | Uso |
|---|---|---|
| `id` | Long | Identificador de persona. |
| `nombre` | String | Nombre completo construido desde nombres y apellidos. |

---

# Resumen de endpoints

| Método | Ruta | Content-Type | Permiso | Uso |
|---|---|---|---|---|
| GET | `/api/conciliaciones` | - | `Ver conciliaciones` | Lista conciliaciones visibles para el usuario. |
| GET | `/api/conciliaciones/consulta/{consultaId}` | - | `Ver conciliaciones` | Lista conciliaciones activas de una consulta. |
| GET | `/api/conciliaciones/{id}` | - | `Ver conciliaciones` | Consulta detalle de conciliación. |
| POST | `/api/conciliaciones/consulta/{consultaId}` | `multipart/form-data` | `Gestionar conciliaciones` | Crea conciliación desde consulta con solicitud PDF. |
| PATCH | `/api/conciliaciones/{id}/estudiante?estudianteId=` | - | `Gestionar conciliaciones` o `Concluir conciliaciones` | Asigna estudiante. |
| PATCH | `/api/conciliaciones/{id}/conciliador?conciliadorId=` | - | `Gestionar conciliaciones` | Asigna conciliador. |
| PATCH | `/api/conciliaciones/{id}/estado?estado=` | - | `Gestionar conciliaciones` o `Concluir conciliaciones` | Cambia estado operativo no final. |
| POST | `/api/conciliaciones/{id}/finalizar` | `multipart/form-data` | `Gestionar conciliaciones` o `Concluir conciliaciones` | Finaliza conciliación con acta PDF. |
| POST | `/api/conciliaciones/{id}/solicitud` | `multipart/form-data` | `Gestionar conciliaciones` | Reemplaza solicitud PDF. |
| DELETE | `/api/conciliaciones/{id}` | - | `Gestionar conciliaciones` | Desactiva conciliación. |

---

# GET `/api/conciliaciones`

Lista conciliaciones visibles para el usuario autenticado.

## Reglas

- requiere `Ver conciliaciones`;
- lista conciliaciones activas;
- excluye conciliaciones asociadas a consultas archivadas;
- aplica alcance según perfil;
- ordena por id descendente.

## Alcance

| Perfil | Alcance |
|---|---|
| Administrador | Ve conciliaciones de forma global. |
| Asesor | Ve conciliaciones de consultas donde es asesor directo. |
| Monitor | Ve conciliaciones de consultas donde es monitor directo. |
| Conciliador | Ve conciliaciones donde está asignado como conciliador. |
| Estudiante | Ve conciliaciones donde está asignado o donde es estudiante responsable de la consulta. |

## Response `200 OK`

```json
[
  {
    "id": 1,
    "consultaId": 1,
    "estudianteId": 1,
    "estudianteNombre": "Nombre estudiante",
    "conciliadorId": 1,
    "conciliadorNombre": "Nombre conciliador",
    "estadoId": 1,
    "estadoCodigo": "EN_ESPERA",
    "estadoNombre": "En espera",
    "fechaConciliacion": "fecha-hora",
    "documentoSolicitudPath": "conciliacion/1/solicitud.pdf",
    "actaPath": null,
    "solicitadoPorId": 1,
    "solicitadoPorUsername": "usuario",
    "activo": true,
    "fechaCreacion": "fecha-hora",
    "fechaActualizacion": "fecha-hora",
    "fechaFinalizacion": null
  }
]
```

---

# GET `/api/conciliaciones/consulta/{consultaId}`

Lista conciliaciones activas de una consulta específica.

## Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `consultaId` | Long | Path |

## Reglas

- requiere `Ver conciliaciones`;
- `consultaId` es obligatorio;
- valida alcance sobre la consulta;
- devuelve conciliaciones activas asociadas a la consulta;
- excluye conciliaciones asociadas a consultas archivadas.

## Response `200 OK`

Retorna lista de `ConciliacionResponseDTO`.

---

# GET `/api/conciliaciones/{id}`

Consulta el detalle de una conciliación.

## Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |

## Reglas

- requiere `Ver conciliaciones`;
- la conciliación debe existir y estar activa;
- valida alcance sobre la conciliación;
- carga consultante, partes y contrapartes desde la consulta asociada.

## Response `200 OK`

Retorna `ConciliacionDetalleResponseDTO`.

---

# POST `/api/conciliaciones/consulta/{consultaId}`

Crea una conciliación desde una consulta.

## Content-Type

```text
multipart/form-data
```

## Parámetros

| Parámetro | Tipo | Ubicación | Obligatorio | Uso |
|---|---|---|---|---|
| `consultaId` | Long | Path | Sí | Consulta desde la que nace la conciliación. |
| `solicitud` | File | Form-data | Sí | Solicitud PDF de conciliación. |

## Request multipart

Campo requerido:

```text
solicitud = archivo PDF
```

## Reglas

- requiere `Gestionar conciliaciones`;
- valida alcance sobre la consulta;
- la consulta no puede estar cerrada;
- la consulta no puede estar archivada;
- no puede existir otra conciliación activa no finalizada para la misma consulta;
- la solicitud PDF es obligatoria;
- el archivo debe tener extensión `.pdf`;
- si se informa `Content-Type`, debe ser `application/pdf`;
- registra el usuario autenticado como solicitante;
- autoasigna estudiante si existe candidato habilitado;
- autoasigna conciliador si existe candidato activo;
- calcula estado inicial;
- guarda solicitud en ruta estándar de la conciliación.

## Estado inicial

| Condición | Estado |
|---|---|
| Tiene estudiante y conciliador asignados | `ESPERANDO_REUNION` |
| Falta estudiante o conciliador | `EN_ESPERA` |

## Response `201 Created`

Retorna `ConciliacionResponseDTO`.

---

# PATCH `/api/conciliaciones/{id}/estudiante`

Asigna estudiante a una conciliación.

## Parámetros

| Parámetro | Tipo | Ubicación | Obligatorio |
|---|---|---|---|
| `id` | Long | Path | Sí |
| `estudianteId` | Long | Query | Sí |

Ejemplo:

```text
PATCH /api/conciliaciones/1/estudiante?estudianteId=10
```

## Reglas

- requiere `Gestionar conciliaciones` o `Concluir conciliaciones`;
- valida alcance sobre la conciliación;
- la conciliación debe existir y estar activa;
- la conciliación no puede estar finalizada;
- la consulta asociada no puede estar cerrada ni archivada;
- el estudiante debe existir y estar activo;
- el estudiante debe estar habilitado para conciliación;
- después de asignar estudiante se recalcula estado según responsables.

## Alcance de operación

| Perfil | Regla |
|---|---|
| Administrador | Puede asignar estudiante. |
| Conciliador | Puede asignar estudiante si está asignado a la conciliación. |

## Response `200 OK`

Retorna `ConciliacionResponseDTO`.

---

# PATCH `/api/conciliaciones/{id}/conciliador`

Asigna conciliador a una conciliación.

## Parámetros

| Parámetro | Tipo | Ubicación | Obligatorio |
|---|---|---|---|
| `id` | Long | Path | Sí |
| `conciliadorId` | Long | Query | Sí |

Ejemplo:

```text
PATCH /api/conciliaciones/1/conciliador?conciliadorId=5
```

## Reglas

- requiere `Gestionar conciliaciones`;
- solo administrador puede asignar conciliador según alcance del backend;
- la conciliación debe existir y estar activa;
- la conciliación no puede estar finalizada;
- la consulta asociada no puede estar cerrada ni archivada;
- el conciliador debe existir y estar activo;
- después de asignar conciliador se recalcula estado según responsables.

## Response `200 OK`

Retorna `ConciliacionResponseDTO`.

---

# PATCH `/api/conciliaciones/{id}/estado`

Cambia estado operativo no final de una conciliación.

## Parámetros

| Parámetro | Tipo | Ubicación | Obligatorio |
|---|---|---|---|
| `id` | Long | Path | Sí |
| `estado` | String | Query | Sí |

Ejemplo:

```text
PATCH /api/conciliaciones/1/estado?estado=ESPERANDO_REUNION
```

## Estados aceptados por este endpoint

```text
ESPERANDO_REUNION
REUNION_PROGRAMADA
```

El endpoint no permite estados finales.  
El estado `EN_ESPERA` se calcula automáticamente según asignaciones.

## Reglas

- requiere `Gestionar conciliaciones` o `Concluir conciliaciones`;
- `estado` es obligatorio;
- valida alcance;
- la conciliación debe existir y estar activa;
- la conciliación no puede estar finalizada;
- la consulta asociada no puede estar cerrada ni archivada;
- el estado recibido debe existir y estar activo;
- no se permite cambiar al mismo estado;
- no se permite cambiar manualmente a `EN_ESPERA`;
- no se permite usar estados finales;
- `ESPERANDO_REUNION` exige estudiante y conciliador asignados;
- `REUNION_PROGRAMADA` exige estudiante, conciliador y fecha de conciliación registrada.

## Alcance de operación

| Perfil | Regla |
|---|---|
| Administrador | Puede cambiar estado operativo. |
| Conciliador | Puede cambiar estado si está asignado a la conciliación y no intenta devolverla a `EN_ESPERA`. |

## Response `200 OK`

Retorna `ConciliacionResponseDTO`.

---

# POST `/api/conciliaciones/{id}/finalizar`

Finaliza una conciliación con acta PDF.

## Content-Type

```text
multipart/form-data
```

## Parámetros

| Parámetro | Tipo | Ubicación | Obligatorio |
|---|---|---|---|
| `id` | Long | Path | Sí |
| `estado` | String | Form-data o request param | Sí |
| `acta` | File | Form-data | Sí |

## Request multipart

Campos requeridos:

```text
estado = COMPLETO_CONCILIADO
acta = archivo PDF
```

o:

```text
estado = COMPLETO_NO_CONCILIADO
acta = archivo PDF
```

## Estados permitidos

```text
COMPLETO_CONCILIADO
COMPLETO_NO_CONCILIADO
```

## Reglas

- requiere `Gestionar conciliaciones` o `Concluir conciliaciones`;
- valida alcance;
- la conciliación debe existir y estar activa;
- la conciliación no puede estar finalizada;
- la consulta asociada no puede estar cerrada ni archivada;
- el estado debe ser final;
- debe existir estudiante asignado;
- debe existir conciliador asignado;
- el acta PDF es obligatoria;
- el archivo debe tener extensión `.pdf`;
- si se informa `Content-Type`, debe ser `application/pdf`;
- el acta se guarda antes de cambiar el estado;
- registra `actaPath`;
- registra `fechaFinalizacion`;
- actualiza el estado final.

## Alcance de operación

| Perfil | Regla |
|---|---|
| Administrador | Puede finalizar. |
| Conciliador | Puede finalizar si está asignado a la conciliación. |

## Response `200 OK`

Retorna `ConciliacionResponseDTO`.

---

# POST `/api/conciliaciones/{id}/solicitud`

Reemplaza la solicitud PDF de una conciliación.

## Content-Type

```text
multipart/form-data
```

## Parámetros

| Parámetro | Tipo | Ubicación | Obligatorio |
|---|---|---|---|
| `id` | Long | Path | Sí |
| `solicitud` | File | Form-data | Sí |

## Request multipart

Campo requerido:

```text
solicitud = archivo PDF
```

## Reglas

- requiere `Gestionar conciliaciones`;
- solo administrador puede reemplazar solicitud según alcance del backend;
- la conciliación debe existir y estar activa;
- la conciliación no puede estar finalizada;
- la consulta asociada no puede estar cerrada ni archivada;
- la nueva solicitud PDF es obligatoria;
- el archivo debe tener extensión `.pdf`;
- si se informa `Content-Type`, debe ser `application/pdf`;
- reemplaza el archivo en la ruta estándar de solicitud.

## Response `200 OK`

Retorna `ConciliacionResponseDTO`.

---

# DELETE `/api/conciliaciones/{id}`

Desactiva lógicamente una conciliación.

## Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |

## Reglas

- requiere `Gestionar conciliaciones`;
- solo administrador puede desactivar según alcance del backend;
- la conciliación debe existir y estar activa;
- la conciliación no puede estar finalizada;
- la consulta asociada no puede estar cerrada ni archivada;
- desactivar no representa finalizar la conciliación.

## Response `204 No Content`

No retorna cuerpo.

---

# Rutas documentales

El backend guarda documentos de conciliación con rutas relativas estándar.

| Documento | Ruta |
|---|---|
| Solicitud PDF | `conciliacion/{id}/solicitud.pdf` |
| Acta PDF | `conciliacion/{id}/acta.pdf` |

El frontend debe usar las rutas retornadas por backend y no construir rutas físicas del servidor.

---

# Reglas de autoasignación

## Estudiante

Al crear conciliación:

1. Si la consulta tiene estudiante asignado, activo y habilitado para conciliación, se usa ese estudiante.
2. Si no, se selecciona estudiante activo y habilitado con menor carga de conciliaciones activas no finalizadas.
3. En empate, se ordena por nombre normalizado.
4. Si continúa el empate, se ordena por id.
5. Si no hay estudiante disponible, queda sin estudiante asignado.

## Conciliador

Al crear conciliación:

1. Se buscan conciliadores activos.
2. Se selecciona el de menor carga de conciliaciones activas no finalizadas.
3. En empate, se ordena por nombre normalizado.
4. Si continúa el empate, se ordena por id.
5. Si no hay conciliador disponible, queda sin conciliador asignado.

---

# Errores comunes

| Estado | Causa |
|---|---|
| `400 Bad Request` | Id obligatorio ausente. |
| `400 Bad Request` | Consulta no encontrada. |
| `400 Bad Request` | Conciliación no encontrada o inactiva. |
| `400 Bad Request` | Consulta cerrada o archivada. |
| `400 Bad Request` | Ya existe conciliación activa no finalizada para la consulta. |
| `400 Bad Request` | Solicitud PDF obligatoria. |
| `400 Bad Request` | Acta PDF obligatoria. |
| `400 Bad Request` | Archivo sin extensión `.pdf`. |
| `400 Bad Request` | Archivo con `Content-Type` diferente a `application/pdf`. |
| `400 Bad Request` | Estudiante inexistente, inactivo o no habilitado para conciliación. |
| `400 Bad Request` | Conciliador inexistente o inactivo. |
| `400 Bad Request` | Estado obligatorio, inexistente o inactivo. |
| `400 Bad Request` | La conciliación ya tiene ese estado. |
| `400 Bad Request` | Intento de cambiar manualmente a `EN_ESPERA`. |
| `400 Bad Request` | Intento de finalizar usando endpoint de cambio de estado. |
| `400 Bad Request` | Estado final inválido para finalización. |
| `400 Bad Request` | Falta estudiante o conciliador para estado requerido o finalización. |
| `400 Bad Request` | Falta fecha registrada para `REUNION_PROGRAMADA`. |
| `400 Bad Request` | Intento de modificar conciliación finalizada. |
| `401 Unauthorized` | Sesión no válida. |
| `403 Forbidden` | Usuario sin permiso o sin alcance sobre la conciliación o consulta. |

---

# Notas para frontend

- Usar `GET /api/conciliaciones` para listado visible del usuario.
- Usar `GET /api/conciliaciones/{id}` para vista de detalle.
- Usar `GET /api/conciliaciones/consulta/{consultaId}` para consultar conciliaciones asociadas a una consulta.
- Para crear conciliación, enviar `multipart/form-data` con campo `solicitud`.
- Para finalizar conciliación, enviar `multipart/form-data` con campos `estado` y `acta`.
- Para reemplazar solicitud, enviar `multipart/form-data` con campo `solicitud`.
- Mostrar estados usando `estadoNombre`.
- Enviar estados usando `estadoCodigo`.
- No usar `PATCH /estado` para estados finales.
- No intentar enviar `EN_ESPERA` como cambio manual.
- Manejar `204` en eliminación lógica sin intentar leer JSON.
- Usar rutas retornadas en `documentoSolicitudPath` y `actaPath`.
- Usar `credentials: "include"` en todas las peticiones protegidas.
