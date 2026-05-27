# API - Autenticación

Este documento describe los endpoints de autenticación y gestión de contraseña.

Base path:

```text
/api/auth
```

## Resumen de endpoints

| Método | Ruta | Autenticación | Uso |
|---|---|---|---|
| POST | `/api/auth/login` | Pública | Inicia sesión y crea cookie de autenticación. |
| GET | `/api/auth/me` | Requiere sesión | Consulta usuario autenticado. |
| POST | `/api/auth/logout` | Pública | Cierra sesión eliminando la cookie. |
| PATCH | `/api/auth/cambiar-password` | Requiere sesión | Cambia contraseña del usuario autenticado. |
| POST | `/api/auth/solicitar-recuperacion` | Pública | Solicita recuperación de contraseña. |
| POST | `/api/auth/restablecer-password` | Pública | Restablece contraseña mediante token válido. |

## Cookie de autenticación

Nombre de cookie:

```text
access_token
```

Características:

| Atributo | Descripción |
|---|---|
| `HttpOnly` | La cookie no se lee desde JavaScript. |
| `Secure` | Configurable desde backend. |
| `SameSite` | Configurable desde backend. |
| `Path` | `/`. |
| `Max-Age` | Definido por backend. |

El frontend no debe manejar manualmente el token. Debe usar:

```javascript
credentials: "include"
```

## POST `/api/auth/login`

Inicia sesión con credenciales de usuario.

### Request

Content-Type:

```text
application/json
```

Body:

```json
{
  "username": "<correo-del-usuario>",
  "password": "<contraseña-del-usuario>"
}
```

### Validaciones

| Campo | Regla |
|---|---|
| `username` | Obligatorio. Se normaliza como correo. |
| `password` | Obligatorio. |

### Reglas de negocio

El backend valida:

- usuario existente;
- usuario activo;
- rol activo;
- perfil real asociado activo;
- contraseña correcta.

Si la autenticación es exitosa:

- genera token JWT;
- devuelve cookie `access_token`;
- retorna datos del usuario y permisos.

### Response `200 OK`

Headers:

```text
Set-Cookie: access_token=<token>; HttpOnly; Path=/; ...
```

Body:

```json
{
  "usuarioId": 1,
  "username": "<correo-del-usuario>",
  "rolId": 1,
  "rolNombre": "Nombre del rol",
  "perfilId": 1,
  "tipoPerfil": "TIPO_PERFIL",
  "permisos": [
    "Permiso ejemplo"
  ]
}
```

### Errores esperados

| Estado | Causa |
|---|---|
| `400 Bad Request` | Campos obligatorios ausentes o credenciales inválidas. |
| `400 Bad Request` | Usuario, rol o perfil inactivo. |

## GET `/api/auth/me`

Consulta el usuario autenticado a partir de la cookie de sesión.

### Request

No requiere body.

El frontend debe enviar:

```javascript
credentials: "include"
```

### Response `200 OK`

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
    "Permiso ejemplo"
  ]
}
```

### Errores esperados

| Estado | Causa |
|---|---|
| `401 Unauthorized` | No existe sesión válida. |
| `400 Bad Request` | Sesión inválida para un usuario no disponible. |

## POST `/api/auth/logout`

Cierra sesión eliminando la cookie de autenticación.

### Request

No requiere body.

### Response `200 OK`

No requiere body.

Headers:

```text
Set-Cookie: access_token=; Max-Age=0; Path=/; ...
```

### Notas para frontend

Después de logout, el frontend debe limpiar estado local de usuario y redirigir a login o pantalla pública según el flujo de la aplicación.

## PATCH `/api/auth/cambiar-password`

Cambia la contraseña del usuario autenticado.

### Autenticación

Requiere sesión válida.

### Request

Content-Type:

```text
application/json
```

Body:

```json
{
  "passwordActual": "<contraseña-actual>",
  "passwordNueva": "<contraseña-nueva>"
}
```

### Validaciones

| Campo | Regla |
|---|---|
| `passwordActual` | Obligatoria. |
| `passwordNueva` | Obligatoria. Debe tener entre 8 y 100 caracteres. |

### Reglas de negocio

El backend valida:

- sesión válida;
- contraseña actual correcta;
- nueva contraseña con longitud válida;
- nueva contraseña diferente a la actual.

La nueva contraseña se almacena cifrada.

### Response `204 No Content`

No retorna cuerpo.

### Errores esperados

| Estado | Causa |
|---|---|
| `401 Unauthorized` | No existe sesión válida. |
| `400 Bad Request` | Contraseña actual incorrecta. |
| `400 Bad Request` | Nueva contraseña inválida o igual a la actual. |

## POST `/api/auth/solicitar-recuperacion`

Solicita el proceso de recuperación de contraseña.

### Request

Content-Type:

```text
application/json
```

Body:

```json
{
  "username": "<correo-del-usuario>"
}
```

También se acepta alias:

```json
{
  "email": "<correo-del-usuario>"
}
```

### Validaciones

| Campo | Regla |
|---|---|
| `username` / `email` | Obligatorio. Debe tener formato de correo. |

### Reglas de seguridad

El endpoint devuelve un mensaje genérico para no revelar si el correo existe.

Si el usuario existe y puede recuperar contraseña:

- se invalidan tokens anteriores;
- se genera token seguro;
- se guarda únicamente el hash del token;
- se genera enlace de recuperación;
- se envía correo con instrucciones.

Si el usuario no existe o no puede recuperar contraseña, el endpoint conserva la respuesta genérica.

### Response `200 OK`

```json
{
  "mensaje": "Si el correo existe, se enviarán instrucciones para recuperar la contraseña"
}
```

### Errores esperados

| Estado | Causa |
|---|---|
| `400 Bad Request` | Correo ausente o con formato inválido. |

## POST `/api/auth/restablecer-password`

Restablece contraseña mediante token de recuperación válido.

### Request

Content-Type:

```text
application/json
```

Body:

```json
{
  "token": "<token-de-recuperacion>",
  "passwordNueva": "<nueva-contraseña>",
  "confirmarPassword": "<confirmacion-de-contraseña>"
}
```

### Validaciones

| Campo | Regla |
|---|---|
| `token` | Obligatorio. |
| `passwordNueva` | Obligatoria. Debe tener entre 8 y 100 caracteres. |
| `confirmarPassword` | Obligatoria. Debe coincidir con `passwordNueva`. |

### Reglas de negocio

El backend valida:

- token existente y activo;
- token no expirado;
- usuario activo;
- rol activo;
- perfil activo;
- nueva contraseña diferente a la actual;
- confirmación igual a la nueva contraseña.

Después de restablecer:

- cifra la nueva contraseña;
- marca el token como usado;
- guarda el usuario actualizado.

### Response `200 OK`

```json
{
  "mensaje": "La contraseña se restableció correctamente"
}
```

### Errores esperados

| Estado | Causa |
|---|---|
| `400 Bad Request` | Token inválido, usado o expirado. |
| `400 Bad Request` | Nueva contraseña inválida. |
| `400 Bad Request` | Confirmación no coincide. |
| `400 Bad Request` | Nueva contraseña igual a la actual. |

## Manejo de seguridad desde frontend

### Peticiones autenticadas

Para rutas protegidas:

```javascript
await fetch(`${API_URL_BASE}/auth/me`, {
  method: "GET",
  credentials: "include",
});
```

### Login

Para login:

```javascript
await fetch(`${API_URL_BASE}/auth/login`, {
  method: "POST",
  headers: {
    "Content-Type": "application/json"
  },
  credentials: "include",
  body: JSON.stringify({
    username: "<correo-del-usuario>",
    password: "<contraseña-del-usuario>"
  })
});
```

### Logout

Para logout:

```javascript
await fetch(`${API_URL_BASE}/auth/logout`, {
  method: "POST",
  credentials: "include"
});
```

## Notas importantes

- El frontend no debe almacenar manualmente el token.
- El token viaja mediante cookie `HttpOnly`.
- Las rutas protegidas deben usar `credentials: "include"`.
- La recuperación de contraseña no revela si el correo existe.
- Los tokens de recuperación se almacenan como hash, no como valor real.
- No se deben documentar tokens reales ni enlaces reales de recuperación.
