# Configuración y seguridad

Este documento describe la configuración general del sistema y las reglas para manejar información sensible.

## Principio de seguridad documental

La documentación del repositorio no debe exponer valores reales de configuración sensible.

No se deben documentar valores reales de:

- contraseñas;
- tokens;
- secretos JWT;
- API keys;
- firmas;
- llaves privadas;
- cadenas de conexión con credenciales;
- usuarios reales de prueba;
- datos personales reales.

La documentación puede explicar que una variable existe y cuál es su propósito, pero no debe publicar su valor.

## Configuración backend

Archivo principal:

```text
backend/app/src/main/resources/application.properties
```

El backend usa variables de entorno para configuración sensible.

Variables principales:

| Variable | Propósito |
|---|---|
| `PORT` | Puerto HTTP de la aplicación. |
| `DB_URL` | URL de conexión a la base de datos. |
| `DB_USERNAME` | Usuario de conexión a base de datos. |
| `DB_PASSWORD` | Contraseña de conexión a base de datos. |
| `DB_DDL_AUTO` | Estrategia de actualización de esquema JPA/Hibernate. |
| `DB_SHOW_SQL` | Controla visualización de SQL generado por Hibernate. |
| `JWT_SECRET` | Secreto usado por el mecanismo de tokens. |
| `AUTH_COOKIE_SECURE` | Define si la cookie se marca como segura. |
| `AUTH_COOKIE_SAME_SITE` | Define la política SameSite de la cookie. |
| `BREVO_API_KEY` | Llave del proveedor de correo. |
| `MAIL_FROM_EMAIL` | Correo remitente configurado. |
| `FRONTEND_URL` | URL del frontend usada en flujos de recuperación. |
| `UPLOAD_DIR` | Directorio físico de almacenamiento de archivos. |

## Base de datos

El backend usa PostgreSQL/Supabase.

El esquema por defecto configurado para Hibernate es:

```text
DB_consultorioJuridico
```

La propiedad `spring.jpa.hibernate.ddl-auto` se controla mediante variable de entorno.

## JWT y cookie de sesión

El backend genera tokens JWT y los entrega al cliente mediante cookie HTTP.

Propiedades relevantes:

| Propiedad | Uso |
|---|---|
| `app.jwt.secret` | Secreto usado para firmar y validar tokens. |
| `app.jwt.expiration-ms` | Tiempo de expiración del token. |
| `app.auth.cookie.secure` | Define el atributo Secure de la cookie. |
| `app.auth.cookie.same-site` | Define política SameSite de la cookie. |

## CORS

La configuración de CORS se encuentra en:

```text
config/cors
```

Componentes:

- `CorsConfig`;
- `CorsProperties`.

La configuración permite ajustar orígenes, métodos, headers, credenciales y tiempo máximo sin modificar código Java.

Propiedades base:

| Propiedad | Uso |
|---|---|
| `app.cors.allowed-origin-patterns` | Orígenes permitidos. |
| `app.cors.allowed-methods` | Métodos HTTP permitidos. |
| `app.cors.allowed-headers` | Headers permitidos. |
| `app.cors.allow-credentials` | Permite envío de credenciales. |
| `app.cors.max-age` | Tiempo de cache para preflight. |

## Configuración frontend

Archivo principal:

```text
frontend/src/lib/config.js
```

Constantes exportadas:

```javascript
API_URL_BASE
FILE_STORAGE_API_URL_BASE
```

Variables soportadas:

| Variable | Propósito |
|---|---|
| `NEXT_PUBLIC_API_URL` | URL pública base del backend. |
| `NEXT_PUBLIC_API_URL_BASE` | URL pública base de API. |
| `NEXT_PUBLIC_FILE_STORAGE_API_URL_BASE` | URL pública base para archivos. |

Las variables `NEXT_PUBLIC_*` son visibles para el navegador y no deben contener secretos.

## Docker Compose

El proyecto define servicios para backend y frontend.

El frontend usa URL pública accesible desde el navegador para conectarse al backend.

## Almacenamiento de archivos

La propiedad `file.upload-dir` define el directorio de almacenamiento.

Los módulos que guardan documentos deben delegar en el servicio de archivos para evitar rutas construidas de forma insegura.

## Reglas de seguridad operacional

- No versionar archivos `.env` con valores reales.
- No publicar valores reales de secretos ni API keys.
- No documentar credenciales de usuarios reales.
- No usar variables públicas de frontend para secretos.
- Mantener fuera del repositorio builds, reportes locales y configuraciones de IDE.
- Documentar propósito de variables, no sus valores.
