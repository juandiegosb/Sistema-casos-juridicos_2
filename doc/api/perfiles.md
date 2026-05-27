# API - Perfiles

Este documento describe los endpoints de perfiles internos del sistema.

Los perfiles representan actores operativos del consultorio jurídico y se relacionan con usuarios del sistema, roles, permisos y reglas de alcance.

## Autenticación

Todos los endpoints requieren sesión válida.

El frontend debe enviar:

```javascript
credentials: "include"
```

## Perfiles documentados

| Perfil | Base path |
|---|---|
| Administrativos | `/api/administrativos` |
| Asesores | `/api/asesores` |
| Monitores | `/api/monitores` |
| Estudiantes | `/api/estudiantes` |
| Conciliadores | `/api/conciliadores` |

## Campos comunes

Los DTOs de perfiles comparten una estructura base.

```json
{
  "id": 1,
  "nombre": "Nombre del perfil",
  "tipoDocumentoId": 1,
  "documento": "Documento",
  "email": "correo@dominio",
  "telefono": "Teléfono",
  "usuario": "usuario",
  "codigo": "Código interno",
  "sedeId": 1,
  "activo": true
}
```

Los valores anteriores son ilustrativos y no representan datos reales.

## Validaciones comunes

| Campo | Regla |
|---|---|
| `nombre` | Obligatorio. Máximo 150 caracteres. |
| `tipoDocumentoId` | Obligatorio. Debe referenciar un tipo de documento válido. |
| `documento` | Obligatorio. Máximo 30 caracteres. |
| `email` | Obligatorio. Debe tener formato de correo. Máximo 120 caracteres. |
| `telefono` | Obligatorio. Máximo 30 caracteres. |
| `usuario` | Obligatorio. Máximo 50 caracteres. |
| `codigo` | Obligatorio. Máximo 30 caracteres. |
| `sedeId` | Obligatorio. Debe referenciar una sede válida. |
| `activo` | Estado lógico. Se modifica por endpoint específico. |

## Reglas generales

- No se debe enviar `id` en creación.
- Si se envía `id` en actualización, debe coincidir con el id de la ruta.
- Documento, email, teléfono, usuario y código se validan como datos únicos dentro del perfil correspondiente.
- Las relaciones enviadas deben existir y estar activas.
- La creación de un perfil crea también su usuario del sistema asociado.
- La eliminación se maneja como desactivación lógica.
- El estado `activo` se modifica mediante endpoints específicos.

---

# Administrativos

Base path:

```text
/api/administrativos
```

## Permisos

| Permiso | Uso |
|---|---|
| `Ver administradores` | Consulta de administrativos. |
| `Gestionar administradores` | Creación, edición, cambio de estado, directora y eliminación lógica. |
| `Gestionar usuarios` | Permiso amplio de administración de usuarios. |
| `Ver perfiles auxiliares` | Consulta de administrativos activos para relaciones auxiliares. |

## DTO `AdministrativoDTO`

```json
{
  "id": 1,
  "nombre": "Nombre administrativo",
  "tipoDocumentoId": 1,
  "documento": "Documento",
  "email": "correo@dominio",
  "telefono": "Teléfono",
  "usuario": "usuario",
  "codigo": "Código interno",
  "sedeId": 1,
  "activo": true,
  "directora": false
}
```

Campo específico:

| Campo | Tipo | Uso |
|---|---|---|
| `directora` | Boolean | Indica si el administrativo tiene marca de directora. |

## Endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/administrativos` | `Ver administradores`, `Gestionar administradores` o `Gestionar usuarios` | Lista administrativos. |
| GET | `/api/administrativos/activos` | `Ver perfiles auxiliares`, `Ver administradores`, `Gestionar administradores` o `Gestionar usuarios` | Lista administrativos activos. |
| GET | `/api/administrativos/directoras` | `Ver administradores`, `Gestionar administradores` o `Gestionar usuarios` | Lista administrativos marcados como directoras. |
| GET | `/api/administrativos/{id}` | `Ver administradores`, `Gestionar administradores` o `Gestionar usuarios` | Consulta administrativo por id. |
| POST | `/api/administrativos` | `Gestionar administradores` o `Gestionar usuarios` | Crea administrativo. |
| PUT | `/api/administrativos/{id}` | `Gestionar administradores` o `Gestionar usuarios` | Actualiza administrativo. |
| PATCH | `/api/administrativos/{id}/activo?activo=` | `Gestionar administradores` o `Gestionar usuarios` | Cambia estado activo. |
| PATCH | `/api/administrativos/{id}/directora?directora=` | `Gestionar administradores` o `Gestionar usuarios` | Cambia marca de directora. |
| DELETE | `/api/administrativos/{id}` | `Gestionar administradores` o `Gestionar usuarios` | Desactiva administrativo. |

## POST `/api/administrativos`

### Request

```json
{
  "nombre": "Nombre administrativo",
  "tipoDocumentoId": 1,
  "documento": "Documento",
  "email": "correo@dominio",
  "telefono": "Teléfono",
  "usuario": "usuario",
  "codigo": "Código interno",
  "sedeId": 1,
  "directora": false
}
```

### Response `201 Created`

Retorna `AdministrativoDTO`.

## PATCH `/api/administrativos/{id}/directora`

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |
| `directora` | Boolean | Query |

### Response `200 OK`

Retorna `AdministrativoDTO`.

---

# Asesores

Base path:

```text
/api/asesores
```

## Permisos

| Permiso | Uso |
|---|---|
| `Ver asesores y monitores` | Consulta de asesores. |
| `Gestionar asesores y monitores` | Creación, edición, cambio de estado y eliminación lógica. |
| `Gestionar usuarios` | Permiso amplio de administración de usuarios. |
| `Ver perfiles auxiliares` | Consulta de asesores activos para relaciones auxiliares. |

## DTO `AsesorDTO`

```json
{
  "id": 1,
  "nombre": "Nombre asesor",
  "tipoDocumentoId": 1,
  "documento": "Documento",
  "email": "correo@dominio",
  "telefono": "Teléfono",
  "usuario": "usuario",
  "sedeId": 1,
  "codigo": "Código interno",
  "areaId": 1,
  "activo": true
}
```

Campo específico:

| Campo | Tipo | Regla |
|---|---|---|
| `areaId` | Long | Obligatorio. Área asociada al asesor. |

## Endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/asesores` | `Ver asesores y monitores`, `Gestionar asesores y monitores` o `Gestionar usuarios` | Lista asesores. |
| GET | `/api/asesores/activos` | `Ver perfiles auxiliares`, `Ver asesores y monitores`, `Gestionar asesores y monitores` o `Gestionar usuarios` | Lista asesores activos. |
| GET | `/api/asesores/{id}` | `Ver asesores y monitores`, `Gestionar asesores y monitores` o `Gestionar usuarios` | Consulta asesor por id. |
| POST | `/api/asesores` | `Gestionar asesores y monitores` o `Gestionar usuarios` | Crea asesor. |
| PUT | `/api/asesores/{id}` | `Gestionar asesores y monitores` o `Gestionar usuarios` | Actualiza asesor. |
| PATCH | `/api/asesores/{id}/activo?activo=` | `Gestionar asesores y monitores` o `Gestionar usuarios` | Cambia estado activo. |
| DELETE | `/api/asesores/{id}` | `Gestionar asesores y monitores` o `Gestionar usuarios` | Desactiva asesor. |

## POST `/api/asesores`

### Request

```json
{
  "nombre": "Nombre asesor",
  "tipoDocumentoId": 1,
  "documento": "Documento",
  "email": "correo@dominio",
  "telefono": "Teléfono",
  "usuario": "usuario",
  "codigo": "Código interno",
  "sedeId": 1,
  "areaId": 1
}
```

### Response `201 Created`

Retorna `AsesorDTO`.

---

# Monitores

Base path:

```text
/api/monitores
```

## Permisos

| Permiso | Uso |
|---|---|
| `Ver asesores y monitores` | Consulta de monitores. |
| `Gestionar asesores y monitores` | Creación, edición, cambio de estado y eliminación lógica. |
| `Gestionar usuarios` | Permiso amplio de administración de usuarios. |
| `Ver perfiles auxiliares` | Consulta de monitores activos para relaciones auxiliares. |

## DTO `MonitorDTO`

```json
{
  "id": 1,
  "nombre": "Nombre monitor",
  "tipoDocumentoId": 1,
  "documento": "Documento",
  "email": "correo@dominio",
  "telefono": "Teléfono",
  "usuario": "usuario",
  "codigo": "Código interno",
  "sedeId": 1,
  "activo": true
}
```

## Endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/monitores` | `Ver asesores y monitores`, `Gestionar asesores y monitores` o `Gestionar usuarios` | Lista monitores. |
| GET | `/api/monitores/activos` | `Ver perfiles auxiliares`, `Ver asesores y monitores`, `Gestionar asesores y monitores` o `Gestionar usuarios` | Lista monitores activos. |
| GET | `/api/monitores/{id}` | `Ver asesores y monitores`, `Gestionar asesores y monitores` o `Gestionar usuarios` | Consulta monitor por id. |
| POST | `/api/monitores` | `Gestionar asesores y monitores` o `Gestionar usuarios` | Crea monitor. |
| PUT | `/api/monitores/{id}` | `Gestionar asesores y monitores` o `Gestionar usuarios` | Actualiza monitor. |
| PATCH | `/api/monitores/{id}/activo?activo=` | `Gestionar asesores y monitores` o `Gestionar usuarios` | Cambia estado activo. |
| DELETE | `/api/monitores/{id}` | `Gestionar asesores y monitores` o `Gestionar usuarios` | Desactiva monitor. |

## POST `/api/monitores`

### Request

```json
{
  "nombre": "Nombre monitor",
  "tipoDocumentoId": 1,
  "documento": "Documento",
  "email": "correo@dominio",
  "telefono": "Teléfono",
  "usuario": "usuario",
  "codigo": "Código interno",
  "sedeId": 1
}
```

### Response `201 Created`

Retorna `MonitorDTO`.

---

# Estudiantes

Base path:

```text
/api/estudiantes
```

## Permisos

| Permiso | Uso |
|---|---|
| `Ver estudiantes` | Consulta de estudiantes según alcance. |
| `Ver perfiles auxiliares` | Consulta de estudiantes activos para relaciones auxiliares. |
| `Crear usuarios` | Creación de estudiante y usuario asociado. |
| `Editar usuarios` | Actualización de estudiante y cambio de habilitación para conciliación. |
| `Cambiar estado estudiantes` | Cambio de estado activo del estudiante. |
| `Gestionar usuarios` | Permiso amplio de administración de usuarios. |

## DTO `EstudianteDTO`

```json
{
  "id": 1,
  "nombre": "Nombre estudiante",
  "tipoDocumentoId": 1,
  "documento": "Documento",
  "email": "correo@dominio",
  "telefono": "Teléfono",
  "usuario": "usuario",
  "sedeId": 1,
  "codigo": "Código interno",
  "asesorId": 1,
  "activo": true,
  "conciliacion": true
}
```

Campos específicos:

| Campo | Tipo | Regla |
|---|---|---|
| `asesorId` | Long | Obligatorio. Asesor asociado al estudiante. |
| `conciliacion` | Boolean | Indica si el estudiante está habilitado para conciliaciones. |

## Endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/estudiantes` | `Ver estudiantes`, `Ver perfiles auxiliares` o `Gestionar usuarios` | Lista estudiantes según alcance. |
| GET | `/api/estudiantes/activos` | `Ver estudiantes`, `Ver perfiles auxiliares` o `Gestionar usuarios` | Lista estudiantes activos según alcance. |
| GET | `/api/estudiantes/conciliacion` | `Ver estudiantes`, `Ver perfiles auxiliares` o `Gestionar usuarios` | Lista estudiantes activos habilitados para conciliación. |
| GET | `/api/estudiantes/activos/asesor/{asesorId}` | `Ver estudiantes`, `Ver perfiles auxiliares` o `Gestionar usuarios` | Lista estudiantes activos de un asesor según alcance. |
| GET | `/api/estudiantes/{id}` | `Ver estudiantes`, `Ver perfiles auxiliares` o `Gestionar usuarios` | Consulta estudiante por id según alcance. |
| POST | `/api/estudiantes` | `Crear usuarios` o `Gestionar usuarios` | Crea estudiante. |
| PUT | `/api/estudiantes/{id}` | `Editar usuarios` o `Gestionar usuarios` | Actualiza estudiante. |
| PATCH | `/api/estudiantes/{id}/activo?activo=` | `Cambiar estado estudiantes` o `Gestionar usuarios` | Cambia estado activo. |
| PATCH | `/api/estudiantes/{id}/conciliacion?conciliacion=` | `Editar usuarios` o `Gestionar usuarios` | Cambia habilitación para conciliación. |
| DELETE | `/api/estudiantes/{id}` | `Gestionar usuarios` | Desactiva estudiante. |

## GET `/api/estudiantes/activos/asesor/{asesorId}`

Lista estudiantes activos asociados a un asesor.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `asesorId` | Long | Path |

### Reglas

- si el usuario autenticado es asesor, solo puede consultar su propio id de asesor;
- usuarios con alcance administrativo pueden consultar según permisos.

## POST `/api/estudiantes`

### Request

```json
{
  "nombre": "Nombre estudiante",
  "tipoDocumentoId": 1,
  "documento": "Documento",
  "email": "correo@dominio",
  "telefono": "Teléfono",
  "usuario": "usuario",
  "codigo": "Código interno",
  "sedeId": 1,
  "asesorId": 1,
  "conciliacion": true
}
```

### Response `201 Created`

Retorna `EstudianteDTO`.

## PATCH `/api/estudiantes/{id}/conciliacion`

Cambia si el estudiante está habilitado para asignaciones de conciliación.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |
| `conciliacion` | Boolean | Query |

### Response `200 OK`

Retorna `EstudianteDTO`.

---

# Conciliadores

Base path:

```text
/api/conciliadores
```

## Permisos

| Permiso | Uso |
|---|---|
| `Ver conciliadores` | Consulta de conciliadores. |
| `Gestionar conciliadores` | Creación, edición, cambio de estado y eliminación lógica. |
| `Gestionar usuarios` | Permiso amplio de administración de usuarios. |
| `Ver perfiles auxiliares` | Consulta de conciliadores activos para relaciones auxiliares. |

## DTO `ConciliadorDTO`

```json
{
  "id": 1,
  "nombre": "Nombre conciliador",
  "tipoDocumentoId": 1,
  "documento": "Documento",
  "email": "correo@dominio",
  "telefono": "Teléfono",
  "usuario": "usuario",
  "sedeId": 1,
  "codigo": "Código interno",
  "tipoConciliador": "INTERNO",
  "activo": true
}
```

Campo específico:

| Campo | Tipo | Regla |
|---|---|---|
| `tipoConciliador` | Enum | Obligatorio. Valores: `INTERNO`, `EXTERNO`. |

## Endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/conciliadores` | `Ver conciliadores`, `Gestionar conciliadores` o `Gestionar usuarios` | Lista conciliadores. |
| GET | `/api/conciliadores/activos` | `Ver perfiles auxiliares`, `Ver conciliadores`, `Gestionar conciliadores` o `Gestionar usuarios` | Lista conciliadores activos. |
| GET | `/api/conciliadores/{id}` | `Ver conciliadores`, `Gestionar conciliadores` o `Gestionar usuarios` | Consulta conciliador por id. |
| POST | `/api/conciliadores` | `Gestionar conciliadores` o `Gestionar usuarios` | Crea conciliador. |
| PUT | `/api/conciliadores/{id}` | `Gestionar conciliadores` o `Gestionar usuarios` | Actualiza conciliador. |
| PATCH | `/api/conciliadores/{id}/activo?activo=` | `Gestionar conciliadores` o `Gestionar usuarios` | Cambia estado activo. |
| DELETE | `/api/conciliadores/{id}` | `Gestionar conciliadores` o `Gestionar usuarios` | Desactiva conciliador. |

## POST `/api/conciliadores`

### Request

```json
{
  "nombre": "Nombre conciliador",
  "tipoDocumentoId": 1,
  "documento": "Documento",
  "email": "correo@dominio",
  "telefono": "Teléfono",
  "usuario": "usuario",
  "codigo": "Código interno",
  "sedeId": 1,
  "tipoConciliador": "INTERNO"
}
```

### Response `201 Created`

Retorna `ConciliadorDTO`.

---

# Operaciones comunes

## GET `/{base}`

Lista registros del perfil correspondiente.

### Response `200 OK`

```json
[
  {
    "id": 1,
    "nombre": "Nombre del perfil",
    "tipoDocumentoId": 1,
    "documento": "Documento",
    "email": "correo@dominio",
    "telefono": "Teléfono",
    "usuario": "usuario",
    "codigo": "Código interno",
    "sedeId": 1,
    "activo": true
  }
]
```

## GET `/{base}/activos`

Lista registros activos del perfil correspondiente.

### Response `200 OK`

Retorna lista del DTO correspondiente.

## GET `/{base}/{id}`

Consulta registro por id.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |

### Response `200 OK`

Retorna el DTO correspondiente.

## PUT `/{base}/{id}`

Actualiza datos generales del perfil.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |

### Reglas

- si el body trae `id`, debe coincidir con la ruta;
- no se debe modificar estado activo desde este endpoint;
- las relaciones deben existir y estar activas;
- debe existir al menos un cambio real.

### Response `200 OK`

Retorna el DTO actualizado.

## PATCH `/{base}/{id}/activo`

Cambia estado activo.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |
| `activo` | Boolean | Query |

### Reglas

- `activo` es obligatorio;
- no se permite cambiar al mismo estado.

### Response `200 OK`

Retorna el DTO actualizado.

## DELETE `/{base}/{id}`

Desactiva lógicamente el perfil.

### Response

| Estado | Uso |
|---|---|
| `204 No Content` | Desactivación exitosa. |

## Errores comunes

| Estado | Causa |
|---|---|
| `400 Bad Request` | DTO obligatorio ausente. |
| `400 Bad Request` | Id enviado en creación. |
| `400 Bad Request` | Id del body diferente al id de la ruta. |
| `400 Bad Request` | Registro no encontrado. |
| `400 Bad Request` | Campo obligatorio ausente. |
| `400 Bad Request` | Documento, email, teléfono, usuario o código duplicado. |
| `400 Bad Request` | Relación inexistente o inactiva. |
| `400 Bad Request` | Cambio al mismo estado. |
| `400 Bad Request` | Sin cambios para actualizar. |
| `401 Unauthorized` | Sesión no válida. |
| `403 Forbidden` | Usuario sin permiso o sin alcance suficiente. |

## Notas para frontend

- Usar endpoints `/activos` para combos y relaciones.
- Usar `GET /api/estudiantes/conciliacion` cuando se necesiten estudiantes habilitados para conciliación.
- Usar `GET /api/estudiantes/activos/asesor/{asesorId}` para cargar estudiantes de un asesor.
- En creación, no enviar `id`.
- En actualización, enviar `id` solo si coincide con la ruta.
- Para cambiar estado activo, usar endpoint `PATCH /activo`.
- Para estudiante, usar `PATCH /conciliacion` para habilitar o deshabilitar participación en conciliaciones.
- Para conciliador, enviar `tipoConciliador` con valores `INTERNO` o `EXTERNO`.
- Usar `credentials: "include"` en todas las peticiones protegidas.
