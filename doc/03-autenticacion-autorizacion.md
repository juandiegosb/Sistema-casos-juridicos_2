# Autenticación y autorización

El sistema usa autenticación basada en JWT y cookie de sesión.

La autorización combina permisos funcionales con reglas de alcance sobre recursos específicos.

## Componentes principales

Backend:

```text
AuthController
AuthService
PasswordResetService
JwtService
JwtAuthenticationFilter
SecurityConfig
SecurityExceptionHandler
UsuarioActualService
SecurityDataInitializer
PermisoNombre
```

Frontend:

```text
LoginForm
src/lib/config.js
src/lib/authz.js
src/lib/permission.js
PermissionSidebar
```

## Endpoints de autenticación

Base path:

```text
/api/auth
```

Endpoints principales:

| Método | Ruta | Uso |
|---|---|---|
| POST | `/api/auth/login` | Inicia sesión. |
| GET | `/api/auth/me` | Consulta usuario autenticado. |
| POST | `/api/auth/logout` | Cierra sesión. |
| PATCH | `/api/auth/cambiar-password` | Cambia contraseña de usuario autenticado. |
| POST | `/api/auth/solicitar-recuperacion` | Solicita recuperación de contraseña. |
| POST | `/api/auth/restablecer-password` | Restablece contraseña con token válido. |

## Login

El login valida credenciales y estado del usuario.

El backend verifica:

- usuario existente;
- usuario activo;
- rol activo;
- perfil asociado activo;
- contraseña correcta.

Cuando el login es exitoso, el backend genera un token y lo entrega en cookie.

## Cookie de sesión

La cookie usada por el backend se llama:

```text
access_token
```

Características:

- `HttpOnly`;
- `path=/`;
- `Secure` configurable;
- `SameSite` configurable;
- tiempo máximo alineado con expiración configurada.

## Validación de sesión

El frontend consulta:

```text
GET /api/auth/me
```

para obtener el usuario actual y sus permisos.

Las peticiones protegidas deben usar:

```javascript
credentials: "include"
```

## Logout

El cierre de sesión reemplaza la cookie por una cookie vacía con expiración inmediata.

## Filtro JWT

`JwtAuthenticationFilter` lee el token desde la cookie.

Si el token es válido:

- obtiene username;
- carga usuario con rol, permisos y perfil;
- valida usuario activo;
- valida rol activo;
- valida perfil activo;
- registra permisos activos como authorities en Spring Security.

Si el token no es válido, el contexto de seguridad se limpia y la petición continúa sin autenticación.

## Configuración de seguridad

`SecurityConfig` define:

- CORS;
- CSRF deshabilitado para API stateless;
- sesión stateless;
- deshabilitación de form login y HTTP Basic;
- endpoints públicos de autenticación;
- endpoints autenticados;
- filtro JWT antes del filtro estándar de usuario/contraseña.

## Errores de seguridad

`SecurityExceptionHandler` devuelve errores JSON para:

| Estado | Significado |
|---|---|
| 401 | Usuario no autenticado o sesión no válida. |
| 403 | Usuario autenticado sin permisos suficientes. |

## Permisos

Los permisos se centralizan en:

```text
PermisoNombre
```

`SecurityDataInitializer` crea permisos declarados en código cuando no existen, y crea roles base si faltan.

El inicializador no sobrescribe la matriz `rol_permiso`.

## Alcance

Además del permiso funcional, algunos módulos validan alcance real del usuario sobre el recurso.

Ejemplos de alcance:

- asesor sobre consultas donde es asesor directo;
- monitor sobre consultas donde es monitor directo;
- estudiante sobre consultas asignadas o relacionadas;
- conciliador sobre conciliaciones donde está asignado.

## Frontend

El frontend usa helpers de autorización para evaluar permisos, roles y perfiles:

```text
src/lib/authz.js
```

Funciones principales:

- normalización de nombres;
- obtención de permisos;
- validación de un permiso;
- validación de alguno o todos los permisos;
- validación de perfil;
- validación de rol.

La navegación se filtra según permisos, pero la seguridad final se valida en backend.
