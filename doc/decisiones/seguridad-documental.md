# Decisión técnica - Seguridad documental

## Contexto

El proyecto contiene configuración de seguridad, autenticación, conexión a base de datos, correo, almacenamiento de archivos y variables de entorno.

Parte de esa información puede involucrar secretos, tokens, credenciales, firmas, llaves o datos sensibles. La documentación debe explicar cómo se configura el sistema sin exponer valores reales.

## Decisión

La documentación del repositorio debe describir únicamente:

- nombres de variables;
- propósito de las variables;
- flujos de configuración;
- responsabilidades de cada componente;
- recomendaciones de uso seguro.

No debe incluir valores reales de configuración sensible.

## Información que no se documenta con valores reales

No se deben publicar valores reales de:

- contraseñas;
- secretos JWT;
- tokens;
- API keys;
- firmas;
- llaves privadas;
- cadenas de conexión con credenciales;
- usuarios reales de prueba;
- datos personales reales;
- rutas privadas del equipo;
- enlaces de recuperación reales;
- credenciales de correo.

## Ejemplo correcto

```text
JWT_SECRET: define el secreto usado por el mecanismo de tokens.
```

```text
DB_PASSWORD: define la contraseña de conexión a base de datos.
```

## Ejemplo incorrecto

```text
JWT_SECRET=valor-real
```

```text
DB_PASSWORD=valor-real
```

## Justificación

Esta decisión reduce riesgos de seguridad y evita que la documentación se convierta en una fuente de exposición de credenciales.

También permite que el repositorio pueda ser compartido o revisado sin comprometer ambientes reales.

## Impacto en backend

El backend puede seguir usando variables de entorno o configuración externa para valores sensibles.

La documentación debe explicar el propósito de propiedades como:

- `DB_URL`;
- `DB_USERNAME`;
- `DB_PASSWORD`;
- `JWT_SECRET`;
- `BREVO_API_KEY`;
- `MAIL_FROM_EMAIL`;
- `UPLOAD_DIR`;
- `FRONTEND_URL`.

Sin publicar valores reales.

## Impacto en frontend

Las variables públicas de Next.js tienen prefijo:

```text
NEXT_PUBLIC_
```

Estas variables son visibles para el navegador.

Por esa razón, nunca deben contener:

- secretos;
- tokens;
- API keys privadas;
- credenciales;
- firmas;
- información sensible.

Se documenta su propósito, no valores reales de producción.

## Ejemplos de variables públicas aceptables

| Variable | Uso |
|---|---|
| `NEXT_PUBLIC_API_URL` | URL pública base del backend. |
| `NEXT_PUBLIC_API_URL_BASE` | URL pública base de API. |
| `NEXT_PUBLIC_FILE_STORAGE_API_URL_BASE` | URL pública base para archivos. |

## Manejo de usuarios de prueba

No se deben documentar usuarios reales con contraseñas.

Cuando se requieran datos de prueba, se deben representar como variables o datos genéricos:

```text
E2E_USER_EMAIL
E2E_USER_PASSWORD
```

Los valores reales deben configurarse fuera del repositorio.

## Manejo de ejemplos JSON

Los ejemplos JSON deben usar marcadores genéricos.

Ejemplo correcto:

```json
{
  "username": "<correo-del-usuario>",
  "password": "<contraseña-del-usuario>"
}
```

Ejemplo incorrecto:

```json
{
  "username": "usuario.real@dominio",
  "password": "clave-real"
}
```

## Manejo de logs y auditoría

Los registros de auditoría pueden contener información técnica de operaciones internas.

No se deben copiar logs reales a documentación pública.

Cuando se documente auditoría, se deben usar ejemplos genéricos.

## Criterios de mantenimiento

Antes de agregar o modificar un documento, revisar que no incluya:

- secretos reales;
- credenciales reales;
- tokens reales;
- valores de firmas;
- datos personales reales;
- rutas privadas locales;
- capturas con información sensible.

## Regla final

La documentación puede explicar que existe una configuración sensible, pero no debe revelar el valor usado por ningún ambiente real.
