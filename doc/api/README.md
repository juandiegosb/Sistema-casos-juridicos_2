# Inventario de API

Esta carpeta documenta los endpoints REST expuestos por el backend del sistema de gestión de casos jurídicos.

La documentación está orientada a consumo desde frontend, pruebas funcionales e integración entre módulos.

## Convenciones generales

Base general de la API:

```text
/api
```

Las rutas documentadas asumen que el frontend usa la URL base configurada en:

```javascript
API_URL_BASE
```

## Autenticación

Los endpoints protegidos requieren cookie de sesión válida.

El frontend debe enviar las solicitudes protegidas con:

```javascript
credentials: "include"
```

Ejemplo general:

```javascript
const response = await fetch(`${API_URL_BASE}/recurso`, {
  method: "GET",
  credentials: "include",
});
```

## Seguridad

La autorización del backend combina:

```text
permiso funcional + alcance real + regla de negocio
```

Esto significa que un usuario puede tener permiso para una acción, pero el backend también valida si tiene relación con el recurso solicitado.

## Formatos de contenido

### JSON

Los endpoints que reciben o devuelven datos estructurados usan JSON.

Header habitual:

```text
Content-Type: application/json
```

### Multipart

Los endpoints que reciben documentos usan `multipart/form-data`.

Ejemplos de campos usados por módulos:

| Campo | Uso |
|---|---|
| `file` | Archivo genérico. |
| `files` | Lista de archivos. |
| `solicitud` | Solicitud PDF de conciliación. |
| `acta` | Acta PDF de conciliación. |

## Respuestas exitosas

Estados comunes:

| Estado | Uso |
|---|---|
| `200 OK` | Consulta o acción exitosa con cuerpo de respuesta. |
| `201 Created` | Recurso creado. |
| `204 No Content` | Acción exitosa sin cuerpo de respuesta. |

## Respuesta estándar de error

El backend responde errores controlados con una estructura común.

Forma general:

```json
{
  "fecha": "fecha-hora-del-error",
  "estado": 400,
  "error": "Tipo de error",
  "mensaje": "Mensaje descriptivo",
  "ruta": "/ruta/del/endpoint"
}
```

Cuando existen errores por campo, se incluye `detalles`:

```json
{
  "fecha": "fecha-hora-del-error",
  "estado": 400,
  "error": "Error de validación",
  "mensaje": "Uno o más campos no son válidos",
  "ruta": "/ruta/del/endpoint",
  "detalles": {
    "campo": "Mensaje de validación"
  }
}
```

## Errores comunes

| Estado | Causa habitual |
|---|---|
| `400 Bad Request` | Error de validación, regla de negocio, parámetro inválido o cuerpo JSON incorrecto. |
| `401 Unauthorized` | Sesión inexistente, expirada o no autenticada. |
| `403 Forbidden` | Usuario autenticado sin permiso o sin alcance suficiente. |
| `404 Not Found` | Recurso inexistente o no disponible. |
| `405 Method Not Allowed` | Método HTTP no soportado por la ruta. |
| `500 Internal Server Error` | Error inesperado no controlado. |

## Manejo recomendado en frontend

El frontend debe:

- enviar `credentials: "include"` en rutas protegidas;
- tratar `401` como sesión no válida;
- tratar `403` como falta de permiso o alcance;
- leer `mensaje` para mostrar errores de negocio;
- leer `detalles` para errores de validación por campo;
- soportar respuestas vacías cuando el estado sea `204`;
- no asumir que las descargas de archivos devuelven JSON.

Helper general sugerido:

```javascript
async function leerRespuesta(response) {
  if (response.status === 204) {
    return null;
  }

  const text = await response.text();

  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text);
  } catch {
    return { mensaje: text };
  }
}
```

## Documentos de API

| Documento | Descripción |
|---|---|
| `autenticacion.md` | Endpoints de login, sesión actual, logout, cambio y recuperación de contraseña. |
| `usuarios-roles-permisos.md` | Inventario de usuarios del sistema, roles y permisos. |
| `catalogos.md` | Inventario de catálogos generales y catálogos auxiliares. |
| `personas.md` | Inventario del módulo de personas. |
| `perfiles.md` | Inventario de administrativos, asesores, monitores, estudiantes y conciliadores. |
| `consultas.md` | Inventario del módulo de consultas jurídicas. |
| `seguimientos.md` | Inventario de seguimientos, respuestas, categorías y notificaciones. |
| `procesos.md` | Inventario de procesos, órganos de control y especialidades. |
| `conciliaciones.md` | Inventario del módulo de conciliaciones. |
| `archivos.md` | Inventario de endpoints de carga, descarga y listado de archivos. |
| `auditoria.md` | Inventario de consulta de auditoría. |

## Regla de seguridad documental

La documentación de API no debe incluir:

- contraseñas reales;
- tokens reales;
- secretos JWT;
- API keys;
- firmas;
- cadenas de conexión con credenciales;
- usuarios reales de prueba;
- datos personales reales.

Los ejemplos deben usar valores genéricos o marcadores de posición.
