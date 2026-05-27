# API - Usuarios, roles y permisos

Este documento describe los endpoints de administración de usuarios del sistema, roles y permisos.

La administración de usuarios del sistema se relaciona con perfiles reales del negocio:

- Administrativo.
- Asesor.
- Estudiante.
- Monitor.
- Conciliador.

Los usuarios del sistema almacenan la información de acceso, rol asignado, estado activo y tipo de perfil actual.

## Base paths

| Recurso | Base path |
|---|---|
| Usuarios del sistema | `/api/usuarios-sistema` |
| Roles | `/api/roles` |
| Permisos | `/api/permisos` |

## Autenticación

Todos los endpoints documentados requieren sesión válida.

El frontend debe enviar:

```javascript
credentials: "include"
```

## DTOs principales

### `UsuarioSistemaDTO`

Respuesta usada por endpoints de usuarios del sistema.

```json
{
  "id": 1,
  "username": "<correo-del-usuario>",
  "activo": true,
  "rolId": 1,
  "rolNombre": "Nombre del rol",
  "perfilId": 1,
  "tipoPerfil": "TIPO_PERFIL",
  "permisos": [
    "Nombre del permiso"
  ]
}
```

Campos:

| Campo | Uso |
|---|---|
| `id` | Identificador del usuario del sistema. |
| `username` | Usuario de acceso. En el sistema corresponde al correo. |
| `activo` | Estado activo/inactivo del usuario. |
| `rolId` | Identificador del rol asignado. |
| `rolNombre` | Nombre del rol asignado. |
| `perfilId` | Identificador del perfil real activo. |
| `tipoPerfil` | Tipo de perfil real actual. |
| `permisos` | Lista de permisos activos del rol. |

### `RolDTO`

DTO usado para administración de roles.

```json
{
  "id": 1,
  "nombre": "Nombre del rol",
  "descripcion": "Descripción del rol",
  "activo": true,
  "permisoIds": [1, 2, 3],
  "permisos": [
    {
      "id": 1,
      "nombre": "Nombre del permiso",
      "descripcion": "Descripción del permiso",
      "activo": true
    }
  ]
}
```

Campos:

| Campo | Uso |
|---|---|
| `id` | Identificador del rol. |
| `nombre` | Nombre del rol. |
| `descripcion` | Descripción funcional. |
| `activo` | Estado activo/inactivo. |
| `permisoIds` | Lista de ids de permisos para crear o actualizar asignaciones. |
| `permisos` | Lista completa de permisos en respuestas. |

Validaciones:

| Campo | Regla |
|---|---|
| `nombre` | Obligatorio. Máximo 50 caracteres. |
| `descripcion` | Opcional. Máximo 255 caracteres. |

### `PermisoDTO`

DTO usado para administración de permisos.

```json
{
  "id": 1,
  "nombre": "Nombre del permiso",
  "descripcion": "Descripción del permiso",
  "activo": true
}
```

Validaciones:

| Campo | Regla |
|---|---|
| `nombre` | Obligatorio. Máximo 100 caracteres. |
| `descripcion` | Opcional. Máximo 255 caracteres. |

## Usuarios del sistema

Base path:

```text
/api/usuarios-sistema
```

### Resumen de endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/usuarios-sistema` | `Ver usuarios` o `Gestionar usuarios` | Lista usuarios del sistema. |
| GET | `/api/usuarios-sistema/activos` | `Ver usuarios` o `Gestionar usuarios` | Lista usuarios activos. |
| GET | `/api/usuarios-sistema/{id}` | `Ver usuarios` o `Gestionar usuarios` | Consulta usuario por id. |
| PATCH | `/api/usuarios-sistema/{id}/activo?activo=` | `Cambiar estado usuarios` o `Gestionar usuarios` | Cambia estado activo del usuario. |
| PATCH | `/api/usuarios-sistema/{id}/perfil/administrativo` | `Asignar rol usuarios` o `Gestionar usuarios` | Cambia perfil real a administrativo. |
| PATCH | `/api/usuarios-sistema/{id}/perfil/estudiante` | `Asignar rol usuarios` o `Gestionar usuarios` | Cambia perfil real a estudiante. |
| PATCH | `/api/usuarios-sistema/{id}/perfil/asesor` | `Asignar rol usuarios` o `Gestionar usuarios` | Cambia perfil real a asesor. |
| PATCH | `/api/usuarios-sistema/{id}/perfil/monitor` | `Asignar rol usuarios` o `Gestionar usuarios` | Cambia perfil real a monitor. |
| PATCH | `/api/usuarios-sistema/{id}/perfil/conciliador` | `Asignar rol usuarios` o `Gestionar usuarios` | Cambia perfil real a conciliador. |

### GET `/api/usuarios-sistema`

Lista usuarios del sistema.

#### Response `200 OK`

```json
[
  {
    "id": 1,
    "username": "<correo-del-usuario>",
    "activo": true,
    "rolId": 1,
    "rolNombre": "Nombre del rol",
    "perfilId": 1,
    "tipoPerfil": "TIPO_PERFIL",
    "permisos": [
      "Nombre del permiso"
    ]
  }
]
```

### GET `/api/usuarios-sistema/activos`

Lista usuarios activos.

#### Response `200 OK`

```json
[
  {
    "id": 1,
    "username": "<correo-del-usuario>",
    "activo": true,
    "rolId": 1,
    "rolNombre": "Nombre del rol",
    "perfilId": 1,
    "tipoPerfil": "TIPO_PERFIL",
    "permisos": [
      "Nombre del permiso"
    ]
  }
]
```

### GET `/api/usuarios-sistema/{id}`

Consulta un usuario por id.

#### Parámetros de ruta

| Parámetro | Tipo | Uso |
|---|---|---|
| `id` | Long | Identificador del usuario del sistema. |

#### Response `200 OK`

```json
{
  "id": 1,
  "username": "<correo-del-usuario>",
  "activo": true,
  "rolId": 1,
  "rolNombre": "Nombre del rol",
  "perfilId": 1,
  "tipoPerfil": "TIPO_PERFIL",
  "permisos": [
    "Nombre del permiso"
  ]
}
```

#### Errores esperados

| Estado | Causa |
|---|---|
| `400 Bad Request` | Id obligatorio o usuario no encontrado. |
| `403 Forbidden` | Usuario sin permiso. |

### PATCH `/api/usuarios-sistema/{id}/activo`

Cambia el estado activo del usuario del sistema.

#### Parámetros

| Parámetro | Tipo | Ubicación | Uso |
|---|---|---|---|
| `id` | Long | Path | Identificador del usuario. |
| `activo` | Boolean | Query | Nuevo estado activo. |

Ejemplo de ruta:

```text
PATCH /api/usuarios-sistema/1/activo?activo=false
```

#### Reglas

- `activo` es obligatorio.
- No se permite cambiar al mismo estado actual.
- El usuario debe existir.

#### Response `200 OK`

```json
{
  "id": 1,
  "username": "<correo-del-usuario>",
  "activo": false,
  "rolId": 1,
  "rolNombre": "Nombre del rol",
  "perfilId": 1,
  "tipoPerfil": "TIPO_PERFIL",
  "permisos": [
    "Nombre del permiso"
  ]
}
```

## Cambio de perfil real

Los endpoints de cambio de perfil actualizan el perfil real activo del usuario y registran historial del cambio.

Base común:

```text
PATCH /api/usuarios-sistema/{id}/perfil/{tipo}
```

Permiso:

```text
Asignar rol usuarios
```

o

```text
Gestionar usuarios
```

### Campos comunes

Todos los cambios de perfil usan campos base.

```json
{
  "rolId": 1,
  "motivo": "Motivo del cambio",
  "nombre": "Nombre del perfil",
  "tipoDocumentoId": 1,
  "documento": "<documento>",
  "telefono": "<telefono>",
  "usuario": "<nombre-de-usuario>",
  "codigo": "<codigo-interno>",
  "sedeId": 1
}
```

Campos comunes:

| Campo | Validación/Uso |
|---|---|
| `rolId` | Obligatorio. Rol destino del usuario. |
| `motivo` | Obligatorio. Máximo 255 caracteres. Se guarda en historial. |
| `nombre` | Obligatorio. Máximo 120 caracteres. |
| `tipoDocumentoId` | Requerido por los handlers de perfil cuando aplica. |
| `documento` | Máximo 30 caracteres. Requerido por los handlers de perfil cuando aplica. |
| `telefono` | Obligatorio. Máximo 30 caracteres. |
| `usuario` | Obligatorio. Máximo 100 caracteres. |
| `codigo` | Obligatorio. Máximo 30 caracteres. |
| `sedeId` | Requerido por los handlers de perfil cuando aplica. |

### PATCH `/api/usuarios-sistema/{id}/perfil/administrativo`

Cambia el perfil real a administrativo.

#### Body

```json
{
  "rolId": 1,
  "motivo": "Motivo del cambio",
  "nombre": "Nombre administrativo",
  "tipoDocumentoId": 1,
  "documento": "<documento>",
  "telefono": "<telefono>",
  "usuario": "<nombre-de-usuario>",
  "codigo": "<codigo-interno>",
  "sedeId": 1,
  "directora": false
}
```

Campo específico:

| Campo | Uso |
|---|---|
| `directora` | Indica si el administrativo queda marcado como directora. |

#### Response `200 OK`

Retorna `UsuarioSistemaDTO`.

### PATCH `/api/usuarios-sistema/{id}/perfil/estudiante`

Cambia el perfil real a estudiante.

#### Body

```json
{
  "rolId": 1,
  "motivo": "Motivo del cambio",
  "nombre": "Nombre estudiante",
  "tipoDocumentoId": 1,
  "documento": "<documento>",
  "telefono": "<telefono>",
  "usuario": "<nombre-de-usuario>",
  "codigo": "<codigo-interno>",
  "sedeId": 1,
  "asesorId": 1,
  "conciliacion": true
}
```

Campos específicos:

| Campo | Validación/Uso |
|---|---|
| `asesorId` | Obligatorio. Asesor activo asociado al estudiante. |
| `conciliacion` | Indica si el estudiante queda habilitado para conciliación. |

#### Response `200 OK`

Retorna `UsuarioSistemaDTO`.

### PATCH `/api/usuarios-sistema/{id}/perfil/asesor`

Cambia el perfil real a asesor.

#### Body

```json
{
  "rolId": 1,
  "motivo": "Motivo del cambio",
  "nombre": "Nombre asesor",
  "tipoDocumentoId": 1,
  "documento": "<documento>",
  "telefono": "<telefono>",
  "usuario": "<nombre-de-usuario>",
  "codigo": "<codigo-interno>",
  "sedeId": 1,
  "areaId": 1
}
```

Campo específico:

| Campo | Validación/Uso |
|---|---|
| `areaId` | Obligatorio. Área activa asociada al asesor. |

#### Response `200 OK`

Retorna `UsuarioSistemaDTO`.

### PATCH `/api/usuarios-sistema/{id}/perfil/monitor`

Cambia el perfil real a monitor.

#### Body

```json
{
  "rolId": 1,
  "motivo": "Motivo del cambio",
  "nombre": "Nombre monitor",
  "tipoDocumentoId": 1,
  "documento": "<documento>",
  "telefono": "<telefono>",
  "usuario": "<nombre-de-usuario>",
  "codigo": "<codigo-interno>",
  "sedeId": 1
}
```

#### Response `200 OK`

Retorna `UsuarioSistemaDTO`.

### PATCH `/api/usuarios-sistema/{id}/perfil/conciliador`

Cambia el perfil real a conciliador.

#### Body

```json
{
  "rolId": 1,
  "motivo": "Motivo del cambio",
  "nombre": "Nombre conciliador",
  "tipoDocumentoId": 1,
  "documento": "<documento>",
  "telefono": "<telefono>",
  "usuario": "<nombre-de-usuario>",
  "codigo": "<codigo-interno>",
  "sedeId": 1,
  "tipoConciliador": "TIPO_CONCILIADOR"
}
```

Campo específico:

| Campo | Validación/Uso |
|---|---|
| `tipoConciliador` | Obligatorio. Tipo de conciliador permitido por el dominio. |

#### Response `200 OK`

Retorna `UsuarioSistemaDTO`.

### Reglas generales de cambio de perfil

El backend:

- valida datos obligatorios;
- valida usuario del sistema existente;
- resuelve perfil actual;
- desactiva el perfil anterior;
- crea o activa el perfil destino;
- actualiza `tipoPerfilActual`;
- actualiza el rol del usuario;
- registra historial de cambio;
- retorna el usuario actualizado.

### Errores esperados en cambio de perfil

| Estado | Causa |
|---|---|
| `400 Bad Request` | Datos obligatorios ausentes. |
| `400 Bad Request` | Rol destino inexistente o incompatible. |
| `400 Bad Request` | Relaciones requeridas inexistentes o inactivas. |
| `400 Bad Request` | Duplicados de documento, email, teléfono, usuario o código. |
| `403 Forbidden` | Usuario sin permiso para asignar rol o gestionar usuarios. |

## Roles

Base path:

```text
/api/roles
```

### Resumen de endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/roles` | `Gestionar roles` o `Ver roles` | Lista roles. |
| GET | `/api/roles/activos` | `Gestionar roles` o `Ver roles` | Lista roles activos. |
| GET | `/api/roles/{id}` | `Gestionar roles` o `Ver roles` | Consulta rol por id. |
| POST | `/api/roles` | `Gestionar roles` o `Crear roles` | Crea rol. |
| PUT | `/api/roles/{id}` | `Gestionar roles` o `Editar roles` | Actualiza rol. |
| PATCH | `/api/roles/{id}/activo?activo=` | `Gestionar roles` o `Editar roles` | Cambia estado activo del rol. |
| PATCH | `/api/roles/{rolId}/permisos/{permisoId}` | `Gestionar roles` o `Asignar permisos a roles` | Asigna un permiso al rol. |
| DELETE | `/api/roles/{rolId}/permisos/{permisoId}` | `Gestionar roles` o `Asignar permisos a roles` | Quita un permiso del rol. |

### GET `/api/roles`

Lista roles.

#### Response `200 OK`

```json
[
  {
    "id": 1,
    "nombre": "Nombre del rol",
    "descripcion": "Descripción del rol",
    "activo": true,
    "permisoIds": [1, 2],
    "permisos": [
      {
        "id": 1,
        "nombre": "Nombre del permiso",
        "descripcion": "Descripción del permiso",
        "activo": true
      }
    ]
  }
]
```

### GET `/api/roles/activos`

Lista roles activos.

#### Response `200 OK`

Retorna lista de `RolDTO`.

### GET `/api/roles/{id}`

Consulta rol por id.

#### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |

#### Response `200 OK`

Retorna `RolDTO`.

### POST `/api/roles`

Crea rol.

#### Body

```json
{
  "nombre": "Nombre del rol",
  "descripcion": "Descripción del rol",
  "activo": true,
  "permisoIds": [1, 2, 3]
}
```

#### Reglas

- no se debe enviar `id`;
- `nombre` es obligatorio;
- el nombre se normaliza;
- el nombre debe ser único ignorando mayúsculas/minúsculas;
- si se envían permisos, deben existir y estar activos;
- si no se envía `activo`, se asume `true`.

#### Response `201 Created`

Retorna `RolDTO`.

### PUT `/api/roles/{id}`

Actualiza rol.

#### Body

```json
{
  "id": 1,
  "nombre": "Nombre actualizado",
  "descripcion": "Descripción actualizada",
  "activo": true,
  "permisoIds": [1, 2, 3]
}
```

#### Reglas

- si se envía `id`, debe coincidir con la ruta;
- el nombre debe ser único excluyendo el mismo rol;
- si se envían `permisoIds`, reemplazan la lista actual;
- si no se envían `permisoIds`, se conservan los permisos actuales;
- debe existir al menos un cambio real.

#### Response `200 OK`

Retorna `RolDTO`.

### PATCH `/api/roles/{id}/activo`

Cambia estado activo del rol.

#### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |
| `activo` | Boolean | Query |

#### Reglas

- `activo` es obligatorio;
- no se permite cambiar al mismo estado actual.

#### Response `200 OK`

Retorna `RolDTO`.

### PATCH `/api/roles/{rolId}/permisos/{permisoId}`

Asigna permiso activo a rol.

#### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `rolId` | Long | Path |
| `permisoId` | Long | Path |

#### Reglas

- el rol debe existir;
- el permiso debe existir y estar activo;
- el permiso no debe estar ya asignado al rol.

#### Response `200 OK`

Retorna `RolDTO`.

### DELETE `/api/roles/{rolId}/permisos/{permisoId}`

Quita permiso de rol.

#### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `rolId` | Long | Path |
| `permisoId` | Long | Path |

#### Reglas

- el rol debe existir;
- el permiso debe estar asignado al rol.

#### Response `200 OK`

Retorna `RolDTO`.

## Permisos

Base path:

```text
/api/permisos
```

### Resumen de endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/permisos` | `Gestionar permisos` o `Asignar permisos a roles` | Lista permisos. |
| GET | `/api/permisos/activos` | `Gestionar permisos` o `Asignar permisos a roles` | Lista permisos activos. |
| GET | `/api/permisos/{id}` | `Gestionar permisos` o `Asignar permisos a roles` | Consulta permiso por id. |
| POST | `/api/permisos` | `Gestionar permisos` | Crea permiso. |
| PUT | `/api/permisos/{id}` | `Gestionar permisos` | Actualiza permiso. |
| PATCH | `/api/permisos/{id}/activo?activo=` | `Gestionar permisos` | Cambia estado activo del permiso. |

### GET `/api/permisos`

Lista permisos.

#### Response `200 OK`

```json
[
  {
    "id": 1,
    "nombre": "Nombre del permiso",
    "descripcion": "Descripción del permiso",
    "activo": true
  }
]
```

### GET `/api/permisos/activos`

Lista permisos activos.

#### Response `200 OK`

Retorna lista de `PermisoDTO`.

### GET `/api/permisos/{id}`

Consulta permiso por id.

#### Response `200 OK`

Retorna `PermisoDTO`.

### POST `/api/permisos`

Crea permiso.

#### Body

```json
{
  "nombre": "Nombre del permiso",
  "descripcion": "Descripción del permiso",
  "activo": true
}
```

#### Reglas

- no se debe enviar `id`;
- `nombre` es obligatorio;
- el nombre se normaliza;
- el nombre debe ser único ignorando mayúsculas/minúsculas;
- si no se envía `activo`, se asume `true`.

#### Response `201 Created`

Retorna `PermisoDTO`.

### PUT `/api/permisos/{id}`

Actualiza permiso.

#### Body

```json
{
  "id": 1,
  "nombre": "Nombre actualizado",
  "descripcion": "Descripción actualizada",
  "activo": true
}
```

#### Reglas

- si se envía `id`, debe coincidir con la ruta;
- el nombre debe ser único excluyendo el mismo permiso;
- debe existir al menos un cambio real.

#### Response `200 OK`

Retorna `PermisoDTO`.

### PATCH `/api/permisos/{id}/activo`

Cambia estado activo del permiso.

#### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |
| `activo` | Boolean | Query |

#### Reglas

- `activo` es obligatorio;
- no se permite cambiar al mismo estado actual.

#### Response `200 OK`

Retorna `PermisoDTO`.

## Errores comunes

| Estado | Causa |
|---|---|
| `400 Bad Request` | Id obligatorio ausente. |
| `400 Bad Request` | DTO obligatorio ausente. |
| `400 Bad Request` | Nombre obligatorio ausente. |
| `400 Bad Request` | Nombre duplicado. |
| `400 Bad Request` | Intento de cambiar id. |
| `400 Bad Request` | Cambio al mismo estado. |
| `400 Bad Request` | Sin cambios para actualizar. |
| `400 Bad Request` | Permiso inexistente o inactivo al asignar a rol. |
| `400 Bad Request` | Permiso ya asignado al rol. |
| `400 Bad Request` | Permiso no asignado al rol al intentar quitarlo. |
| `401 Unauthorized` | Sesión no válida. |
| `403 Forbidden` | Usuario sin permiso suficiente. |

## Notas para frontend

- Para formularios de roles, usar `GET /api/permisos/activos` como catálogo de permisos disponibles.
- Para pantallas administrativas, `GET /api/permisos` y `GET /api/roles` permiten ver activos e inactivos.
- Para asignación masiva de permisos en un rol, enviar `permisoIds` en `POST` o `PUT /api/roles`.
- Para asignar o quitar un permiso específico, usar los endpoints `PATCH` y `DELETE` de permisos de rol.
- Para cambiar perfil real de usuario, usar el endpoint específico del tipo de perfil destino.
- Para cambiar estado de usuario, usar `PATCH /api/usuarios-sistema/{id}/activo`.
- Usar `credentials: "include"` en todas las peticiones protegidas.
- Manejar `403` como falta de permiso.
- Manejar mensajes de negocio para duplicados y cambios sin efecto.
