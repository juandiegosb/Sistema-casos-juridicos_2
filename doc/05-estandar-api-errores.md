# Estándar de API y manejo de errores

El backend expone una API REST bajo el prefijo `/api`.

Los endpoints protegidos requieren autenticación y permisos según el módulo.

## Convenciones HTTP

| Método | Uso general |
|---|---|
| GET | Consultar recursos. |
| POST | Crear recursos o ejecutar acciones con cuerpo complejo. |
| PUT | Actualizar datos generales. |
| PATCH | Cambiar estados, flags o acciones específicas. |
| DELETE | Desactivar o eliminar según regla del módulo. |

## Autenticación en peticiones

El frontend debe enviar peticiones protegidas con:

```javascript
credentials: "include"
```

La sesión se valida mediante cookie.

## Respuestas exitosas

Respuestas habituales:

| Estado | Uso |
|---|---|
| 200 OK | Consulta o acción exitosa con respuesta. |
| 201 Created | Recurso creado. |
| 204 No Content | Acción exitosa sin cuerpo. |

## Respuesta estándar de error

La API usa un DTO estándar de error:

```json
{
  "fecha": "fecha-hora-del-error",
  "estado": 400,
  "error": "Tipo de error",
  "mensaje": "Mensaje descriptivo",
  "ruta": "/ruta/del/endpoint"
}
```

Cuando existen errores por campo, puede incluir `detalles`:

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

## Errores controlados

### Error de negocio

Se usa cuando una solicitud es técnicamente válida, pero incumple una regla funcional del sistema.

Ejemplo de casos:

- intentar operar sobre una consulta cerrada;
- intentar cerrar una consulta con pendientes;
- intentar asignar un responsable inválido;
- intentar finalizar un flujo sin cumplir requisitos.

### Error de validación

Se usa cuando el cuerpo o parámetros no cumplen restricciones declaradas en DTOs o validaciones de Jakarta Validation.

### Solicitud inválida

Se usa para parámetros con tipo inválido, parámetros faltantes o cuerpo JSON no legible.

### No autorizado

Se usa cuando un usuario autenticado no tiene permisos o alcance suficiente.

### No autenticado

Se usa cuando la petición requiere sesión y el usuario no está autenticado.

## Manejadores

El backend usa:

- `GlobalExceptionHandler` para excepciones generales, negocio y validación;
- `SecurityExceptionHandler` para errores generados por Spring Security.

## Archivos

El módulo de archivos usa endpoints bajo:

```text
/api/files
```

Operaciones principales:

- carga de archivo;
- carga múltiple;
- descarga;
- listado de archivos;
- listado de directorios.

Las cargas usan `multipart/form-data`.

La descarga devuelve recurso binario con headers de archivo.

## Multipart

Los módulos que requieren documentos usan `multipart/form-data`.

Ejemplos de campos usados por módulos:

- `file`;
- `files`;
- `solicitud`;
- `acta`.

## Reglas para frontend

El frontend debe:

- leer cuerpo de respuesta cuando exista;
- soportar respuestas vacías en `204`;
- mostrar `mensaje` cuando venga disponible;
- leer `detalles` para errores de validación;
- manejar `401` como sesión no válida;
- manejar `403` como falta de permiso o alcance;
- no asumir que todos los errores de archivos retornan JSON.
