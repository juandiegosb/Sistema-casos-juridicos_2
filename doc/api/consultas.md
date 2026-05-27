# API - Consultas jurídicas

Este documento describe los endpoints del módulo de consultas jurídicas.

La consulta jurídica es el eje funcional del sistema y se relaciona con personas, catálogos, perfiles internos, seguimientos, procesos, conciliaciones y documentos asociados.

## Base path

```text
/api/consultas
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
| `Ver consultas` | Permite buscar y consultar consultas dentro del alcance del usuario. |
| `Crear consultas` | Permite crear consultas. |
| `Editar consultas` | Permite actualizar datos generales de consultas dentro del alcance. |
| `Cambiar estado consultas` | Permite cambiar estado de consultas según reglas del módulo. |
| `Archivar consultas` | Permite archivar, desarchivar y consultar consultas archivadas. |
| `Asignar responsables consulta` | Permite asignar o cambiar asesor, monitor y estudiante responsable. |
| `Gestionar consultas` | Permiso amplio para operaciones generales del módulo. |

## Estados de consulta

Valores posibles:

```text
PENDIENTE
ACTIVO
EN_PROCESO
URGENTE
CERRADO
ARCHIVADO
```

Uso funcional:

| Estado | Uso |
|---|---|
| `PENDIENTE` | Estado inicial de toda consulta nueva. |
| `ACTIVO` | Consulta en atención activa. |
| `EN_PROCESO` | Consulta en gestión operativa. |
| `URGENTE` | Consulta priorizada. |
| `CERRADO` | Consulta cerrada operativamente. |
| `ARCHIVADO` | Consulta archivada para consulta histórica. |

## DTO `ConsultaDTO`

DTO principal de entrada y salida del módulo.

```json
{
  "id": 1,
  "fecha": "YYYY-MM-DD",
  "descripcion": "Descripción de la consulta",
  "hechos": "Relato de hechos",
  "pretensiones": "Pretensiones",
  "conceptoJuridico": "Concepto jurídico",
  "tramite": "Trámite",
  "observaciones": "Observaciones",
  "tipoViolencia": "Tipo de violencia",
  "estado": "PENDIENTE",
  "resultado": "Resultado",
  "personaId": 1,
  "partesIds": [2, 3],
  "contrapartesIds": [4, 5],
  "sedeId": 1,
  "areaId": 1,
  "temaId": 1,
  "tipoId": 1,
  "asesorId": 1,
  "monitorId": 1,
  "estudianteId": 1
}
```

Los valores anteriores son ilustrativos y no corresponden a datos reales.

### Campos y validaciones

| Campo | Tipo | Regla |
|---|---|---|
| `id` | Long | No se envía en creación. Si se envía en actualización, debe coincidir con la ruta. |
| `fecha` | Date | Obligatoria. |
| `descripcion` | String | Obligatoria. Máximo 500 caracteres. |
| `hechos` | String | Obligatorios. |
| `pretensiones` | String | Obligatorias. |
| `conceptoJuridico` | String | Obligatorio. |
| `tramite` | String | Obligatorio. Máximo 100 caracteres. |
| `observaciones` | String | Opcional. |
| `tipoViolencia` | String | Opcional. Máximo 100 caracteres. |
| `estado` | Enum | En creación solo puede enviarse `PENDIENTE`; en actualización general no se cambia desde `PUT`. |
| `resultado` | String | Opcional. Máximo 100 caracteres. |
| `personaId` | Long | Obligatorio. Persona principal de la consulta. |
| `partesIds` | List<Long> | Opcional. Personas relacionadas como partes. |
| `contrapartesIds` | List<Long> | Opcional. Personas relacionadas como contrapartes. |
| `sedeId` | Long | Obligatorio. |
| `areaId` | Long | Obligatorio. |
| `temaId` | Long | Obligatorio. |
| `tipoId` | Long | Opcional. |
| `asesorId` | Long | Opcional. Requiere permiso específico cuando se asigna o cambia. |
| `monitorId` | Long | Opcional. Requiere permiso específico cuando se asigna o cambia. |
| `estudianteId` | Long | Opcional. Requiere permiso específico cuando se asigna o cambia. |

## DTO `ConsultaBusquedaDTO`

DTO usado en listados de búsqueda.

```json
{
  "id": 1,
  "consulta": "Descripción de la consulta",
  "fecha": "YYYY-MM-DD",
  "nombre": "Nombre persona",
  "apellido": "Apellido persona",
  "cedula": "Número de documento",
  "estado": "PENDIENTE"
}
```

Campos:

| Campo | Uso |
|---|---|
| `id` | Identificador de consulta. |
| `consulta` | Descripción de la consulta. |
| `fecha` | Fecha de consulta. |
| `nombre` | Nombre de la persona principal. |
| `apellido` | Apellido de la persona principal. |
| `cedula` | Número de documento de la persona principal. |
| `estado` | Estado actual de la consulta. |

## Resumen de endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/consultas?search=` | `Ver consultas` o `Gestionar consultas` | Busca consultas según alcance del usuario. |
| GET | `/api/consultas/{id}` | `Ver consultas` o `Gestionar consultas` | Consulta detalle de una consulta. |
| POST | `/api/consultas` | `Crear consultas` o `Gestionar consultas` | Crea consulta. |
| PUT | `/api/consultas/{id}` | `Editar consultas` o `Gestionar consultas` | Actualiza datos generales de consulta. |
| PATCH | `/api/consultas/{id}/estado?estado=` | `Cambiar estado consultas`, `Archivar consultas` o `Gestionar consultas` | Cambia estado de consulta. |
| DELETE | `/api/consultas/{id}` | `Archivar consultas` | Archiva lógicamente una consulta. |
| PATCH | `/api/consultas/{id}/archivar` | `Archivar consultas` | Archiva una consulta y retorna DTO. |
| GET | `/api/consultas/archivadas` | `Archivar consultas` | Lista consultas archivadas. |
| PATCH | `/api/consultas/{id}/desarchivar` | `Archivar consultas` | Desarchiva consulta y la devuelve a `CERRADO`. |

---

# GET `/api/consultas`

Busca consultas jurídicas filtradas por el usuario autenticado.

## Query params

| Parámetro | Tipo | Obligatorio | Valor por defecto | Uso |
|---|---|---|---|---|
| `search` | String | No | `""` | Término de búsqueda. |

Ejemplo:

```text
GET /api/consultas?search=contrato
```

## Alcance

El backend filtra resultados según el perfil actual.

| Perfil | Alcance |
|---|---|
| Administrador | Consulta todas las consultas no archivadas. |
| Estudiante | Consulta donde es estudiante asignado. |
| Asesor | Consulta donde es asesor asignado o donde el estudiante asignado pertenece a su asesoría. |
| Monitor | Consulta donde es monitor asignado. |
| Conciliador | No recibe consultas desde el buscador general de consultas. |

El frontend no debe filtrar manualmente consultas ajenas; el backend ya devuelve únicamente las permitidas.

## Response `200 OK`

```json
[
  {
    "id": 1,
    "consulta": "Descripción de la consulta",
    "fecha": "YYYY-MM-DD",
    "nombre": "Nombre persona",
    "apellido": "Apellido persona",
    "cedula": "Número de documento",
    "estado": "PENDIENTE"
  }
]
```

## Errores esperados

| Estado | Causa |
|---|---|
| `401 Unauthorized` | Sesión no válida. |
| `403 Forbidden` | Usuario sin permiso de consulta. |

---

# GET `/api/consultas/{id}`

Consulta el detalle de una consulta jurídica.

## Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |

## Reglas

- requiere permiso de consulta;
- valida alcance del usuario sobre la consulta;
- carga partes y contrapartes asociadas;
- retorna `ConsultaDTO`.

## Response `200 OK`

```json
{
  "id": 1,
  "fecha": "YYYY-MM-DD",
  "descripcion": "Descripción de la consulta",
  "hechos": "Relato de hechos",
  "pretensiones": "Pretensiones",
  "conceptoJuridico": "Concepto jurídico",
  "tramite": "Trámite",
  "observaciones": "Observaciones",
  "tipoViolencia": "Tipo de violencia",
  "estado": "PENDIENTE",
  "resultado": "Resultado",
  "personaId": 1,
  "partesIds": [2, 3],
  "contrapartesIds": [4, 5],
  "sedeId": 1,
  "areaId": 1,
  "temaId": 1,
  "tipoId": 1,
  "asesorId": 1,
  "monitorId": 1,
  "estudianteId": 1
}
```

## Errores esperados

| Estado | Causa |
|---|---|
| `400 Bad Request` | Consulta no encontrada. |
| `401 Unauthorized` | Sesión no válida. |
| `403 Forbidden` | Usuario sin permiso o sin alcance sobre la consulta. |

---

# POST `/api/consultas`

Crea una consulta jurídica.

## Request

Content-Type:

```text
application/json
```

Body:

```json
{
  "fecha": "YYYY-MM-DD",
  "descripcion": "Descripción de la consulta",
  "hechos": "Relato de hechos",
  "pretensiones": "Pretensiones",
  "conceptoJuridico": "Concepto jurídico",
  "tramite": "Trámite",
  "observaciones": "Observaciones",
  "tipoViolencia": "Tipo de violencia",
  "estado": "PENDIENTE",
  "resultado": "Resultado",
  "personaId": 1,
  "partesIds": [2, 3],
  "contrapartesIds": [4, 5],
  "sedeId": 1,
  "areaId": 1,
  "temaId": 1,
  "tipoId": 1,
  "asesorId": 1,
  "monitorId": 1,
  "estudianteId": 1
}
```

## Reglas

- no se debe enviar `id`;
- si se envía `estado`, solo puede ser `PENDIENTE`;
- toda consulta nueva nace en estado `PENDIENTE`;
- si se envían responsables, el usuario debe tener permiso `Asignar responsables consulta`;
- se validan relaciones activas;
- se valida jerarquía área-tema-tipo;
- se valida coherencia de asesor y estudiante;
- se valida que la persona principal no se repita como parte o contraparte;
- se valida que una persona no esté al mismo tiempo en partes y contrapartes.

## Response `201 Created`

Retorna `ConsultaDTO`.

## Errores esperados

| Estado | Causa |
|---|---|
| `400 Bad Request` | Id enviado en creación. |
| `400 Bad Request` | Campo obligatorio ausente. |
| `400 Bad Request` | Estado inicial diferente de `PENDIENTE`. |
| `400 Bad Request` | Relación inexistente o inactiva. |
| `400 Bad Request` | Tema no pertenece al área. |
| `400 Bad Request` | Tipo no pertenece al tema. |
| `400 Bad Request` | Asesor no pertenece al área. |
| `400 Bad Request` | Estudiante no pertenece al asesor. |
| `400 Bad Request` | Personas duplicadas entre principal, partes y contrapartes. |
| `401 Unauthorized` | Sesión no válida. |
| `403 Forbidden` | Usuario sin permiso para crear o asignar responsables. |

---

# PUT `/api/consultas/{id}`

Actualiza datos generales de una consulta.

## Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |

## Request

Mismo formato de `ConsultaDTO`.

## Reglas

- requiere permiso de edición;
- valida alcance del usuario sobre la consulta;
- la consulta no puede estar archivada;
- no se puede operar sobre consulta cerrada;
- si el DTO trae `id`, debe coincidir con la ruta;
- el estado no se cambia desde `PUT`;
- si el DTO intenta cambiar estado, se rechaza;
- si se modifican responsables, se exige permiso `Asignar responsables consulta`;
- se validan relaciones activas;
- se validan reglas de dominio.

## Response `200 OK`

Retorna `ConsultaDTO`.

## Errores esperados

| Estado | Causa |
|---|---|
| `400 Bad Request` | Consulta no encontrada. |
| `400 Bad Request` | Consulta archivada. |
| `400 Bad Request` | Consulta cerrada. |
| `400 Bad Request` | Id del body diferente al id de ruta. |
| `400 Bad Request` | Intento de cambiar estado desde edición general. |
| `400 Bad Request` | Relación inválida o inactiva. |
| `400 Bad Request` | Inconsistencia de área, tema, tipo o responsables. |
| `401 Unauthorized` | Sesión no válida. |
| `403 Forbidden` | Usuario sin permiso o sin alcance. |

---

# PATCH `/api/consultas/{id}/estado`

Cambia el estado de una consulta.

## Parámetros

| Parámetro | Tipo | Ubicación | Uso |
|---|---|---|---|
| `id` | Long | Path | Identificador de consulta. |
| `estado` | EstadoConsulta | Query | Nuevo estado. |

Ejemplo:

```text
PATCH /api/consultas/1/estado?estado=ACTIVO
```

## Estados aceptados

```text
PENDIENTE
ACTIVO
EN_PROCESO
URGENTE
CERRADO
ARCHIVADO
```

## Reglas generales

- `estado` es obligatorio;
- no se permite cambiar al mismo estado;
- la consulta no puede estar archivada;
- una consulta cerrada solo puede archivarse;
- el estudiante no puede cambiar estado;
- se valida permiso funcional;
- se valida alcance;
- se validan pendientes operativos cuando el estado destino es `CERRADO` o `ARCHIVADO`;
- se validan responsables mínimos para estados operativos de atención.

## Estados operativos que exigen responsables

Para pasar a:

```text
ACTIVO
EN_PROCESO
URGENTE
```

la consulta debe tener:

- asesor asignado;
- estudiante asignado.

También se valida coherencia de dominio.

## Reglas para cerrar

Para cambiar a:

```text
CERRADO
```

la consulta no debe tener pendientes operativos.

Bloquean cierre:

- procesos activos en estado `PENDIENTE`;
- seguimientos activos en estado `PENDIENTE`;
- respuestas de seguimiento activas en estado `PENDIENTE`;
- notificaciones de seguimiento activas no enviadas;
- conciliaciones activas no finalizadas.

## Reglas para archivar desde cambio de estado

Para cambiar a:

```text
ARCHIVADO
```

la consulta debe estar `CERRADO`.

Además, solo aplica para usuario con permiso de archivo y rol administrador según política del backend.

## Response `200 OK`

Retorna `ConsultaDTO`.

## Errores esperados

| Estado | Causa |
|---|---|
| `400 Bad Request` | Estado obligatorio o inválido. |
| `400 Bad Request` | Consulta ya tiene ese estado. |
| `400 Bad Request` | Consulta archivada. |
| `400 Bad Request` | Consulta cerrada y estado destino no permitido. |
| `400 Bad Request` | Falta asesor o estudiante para activar la consulta. |
| `400 Bad Request` | Existen pendientes operativos para cerrar o archivar. |
| `401 Unauthorized` | Sesión no válida. |
| `403 Forbidden` | Usuario sin permiso, estudiante intentando cambiar estado o usuario sin alcance. |

---

# DELETE `/api/consultas/{id}`

Archiva lógicamente una consulta.

Este endpoint se conserva por compatibilidad. No elimina físicamente la consulta.

## Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |

## Reglas

- requiere permiso `Archivar consultas`;
- solo administrador puede archivar;
- la consulta debe existir;
- la consulta no debe estar archivada;
- solo se pueden archivar consultas cerradas;
- no debe tener pendientes operativos.

## Response `204 No Content`

No retorna cuerpo.

## Errores esperados

| Estado | Causa |
|---|---|
| `400 Bad Request` | Consulta no encontrada. |
| `400 Bad Request` | Consulta ya archivada. |
| `400 Bad Request` | Consulta no está cerrada. |
| `400 Bad Request` | Existen pendientes operativos. |
| `401 Unauthorized` | Sesión no válida. |
| `403 Forbidden` | Usuario sin permiso o no administrador. |

---

# PATCH `/api/consultas/{id}/archivar`

Archiva una consulta y retorna el DTO actualizado.

## Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |

## Reglas

Mismas reglas de archivado lógico:

- requiere permiso `Archivar consultas`;
- solo administrador puede archivar;
- la consulta debe estar `CERRADO`;
- la consulta no debe estar ya archivada;
- no debe tener pendientes operativos.

## Response `200 OK`

Retorna `ConsultaDTO` con:

```json
{
  "estado": "ARCHIVADO"
}
```

El cuerpo real incluye todos los campos del DTO.

---

# GET `/api/consultas/archivadas`

Lista consultas archivadas.

## Reglas

- requiere permiso `Archivar consultas`;
- solo administrador puede consultar archivadas;
- retorna DTOs de búsqueda.

## Response `200 OK`

```json
[
  {
    "id": 1,
    "consulta": "Descripción de la consulta",
    "fecha": "YYYY-MM-DD",
    "nombre": "Nombre persona",
    "apellido": "Apellido persona",
    "cedula": "Número de documento",
    "estado": "ARCHIVADO"
  }
]
```

## Errores esperados

| Estado | Causa |
|---|---|
| `401 Unauthorized` | Sesión no válida. |
| `403 Forbidden` | Usuario sin permiso o no administrador. |

---

# PATCH `/api/consultas/{id}/desarchivar`

Desarchiva una consulta.

## Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |

## Reglas

- requiere permiso `Archivar consultas`;
- solo administrador puede desarchivar;
- la consulta debe existir;
- solo se pueden desarchivar consultas archivadas;
- se validan pendientes operativos;
- al desarchivar, la consulta vuelve a estado `CERRADO`.

## Response `200 OK`

Retorna `ConsultaDTO` con:

```json
{
  "estado": "CERRADO"
}
```

El cuerpo real incluye todos los campos del DTO.

## Errores esperados

| Estado | Causa |
|---|---|
| `400 Bad Request` | Consulta no encontrada. |
| `400 Bad Request` | Consulta no está archivada. |
| `400 Bad Request` | Existen pendientes operativos. |
| `401 Unauthorized` | Sesión no válida. |
| `403 Forbidden` | Usuario sin permiso o no administrador. |

---

# Reglas de responsables

Responsables posibles:

- asesor;
- monitor;
- estudiante.

## Asignación desde creación o actualización

La asignación o cambio de responsables requiere:

```text
Asignar responsables consulta
```

Reglas:

- el estudiante no puede asignar responsables;
- si se asigna estudiante sin asesor explícito, el backend puede resolver el asesor activo del estudiante;
- el asesor asignado debe pertenecer al área de la consulta;
- si hay estudiante y asesor, el estudiante debe pertenecer al asesor;
- si hay estudiante, el asesor del estudiante debe pertenecer al área de la consulta.

## Estados que requieren responsables

Para activar la atención real:

```text
ACTIVO
EN_PROCESO
URGENTE
```

se requiere:

- asesor;
- estudiante.

---

# Reglas de personas relacionadas

La consulta puede relacionar:

- persona principal;
- partes;
- contrapartes.

Reglas:

- la persona principal no puede estar también en partes;
- la persona principal no puede estar también en contrapartes;
- una persona no puede estar al mismo tiempo en partes y contrapartes;
- no puede haber duplicados dentro de partes;
- no puede haber duplicados dentro de contrapartes.

---

# Reglas de catálogos jurídicos

La consulta relaciona:

- área;
- tema;
- tipo.

Reglas:

- tema debe pertenecer al área;
- si se informa tipo, debe pertenecer al tema.

---

# Pendientes operativos que bloquean cierre

Una consulta no puede cerrarse si existen:

| Módulo | Condición |
|---|---|
| Procesos | Procesos activos en estado `PENDIENTE`. |
| Seguimientos | Seguimientos activos en estado `PENDIENTE`. |
| Respuestas | Respuestas activas en estado `PENDIENTE`. |
| Notificaciones | Notificaciones activas no enviadas. |
| Conciliaciones | Conciliaciones activas no finalizadas. |

---

# Notas para frontend

- Usar `GET /api/consultas?search=` para búsqueda principal.
- No filtrar consultas ajenas en frontend; el backend filtra por alcance.
- En creación, no enviar `id`.
- En creación, no enviar estado distinto de `PENDIENTE`.
- Para cambiar estado, usar `PATCH /api/consultas/{id}/estado`.
- No intentar cambiar estado desde `PUT /api/consultas/{id}`.
- Para asignar responsables desde creación o edición, el usuario debe tener permiso `Asignar responsables consulta`.
- Para cerrar, manejar errores de pendientes operativos.
- Para archivar/desarchivar, tratar la acción como operación administrativa.
- Usar `credentials: "include"` en todas las peticiones protegidas.
