# API - Archivos

Este documento describe los endpoints de carga, descarga y listado de archivos.

El módulo de archivos expone operaciones genéricas de almacenamiento. Los módulos funcionales que usan documentos aplican sus propias reglas de negocio antes de guardar o asociar archivos.

## Base path

```text
/api/files
```

## Autenticación

Todos los endpoints requieren sesión válida.

El frontend debe enviar:

```javascript
credentials: "include"
```

## Seguridad

El controller está protegido con:

```text
isAuthenticated()
```

Las reglas funcionales específicas se validan en el módulo que usa el archivo.

Ejemplo:

- conciliaciones valida permisos, alcance, estado y formato PDF antes de guardar solicitud o acta;
- otros módulos pueden aplicar sus propias validaciones antes de delegar almacenamiento.

## Configuración

El directorio base de almacenamiento se configura en backend mediante:

```text
file.upload-dir
```

La documentación no debe incluir rutas privadas reales del servidor ni rutas locales del equipo.

## Resumen de endpoints

| Método | Ruta | Content-Type | Uso |
|---|---|---|---|
| POST | `/api/files/upload` | `multipart/form-data` | Carga un archivo. |
| POST | `/api/files/upload-multiple` | `multipart/form-data` | Carga múltiples archivos. |
| GET | `/api/files/download/**` | - | Descarga archivo por ruta relativa. |
| GET | `/api/files/list` | - | Lista archivos del directorio base. |
| GET | `/api/files/list/{subDir}` | - | Lista archivos de un subdirectorio. |
| GET | `/api/files/directories` | - | Lista subdirectorios del almacenamiento base. |

---

# POST `/api/files/upload`

Carga un archivo individual.

## Request

Content-Type:

```text
multipart/form-data
```

Campos:

| Campo | Tipo | Obligatorio | Uso |
|---|---|---|---|
| `file` | File | Sí | Archivo a cargar. |
| `path` | String | No | Subdirectorio relativo dentro del almacenamiento base. |

Ejemplo conceptual de form-data:

```text
file = archivo
path = carpeta/subcarpeta
```

## Reglas

- el archivo se guarda con su nombre original;
- si `path` se informa, se usa como subdirectorio relativo;
- si el subdirectorio no existe, el backend lo crea;
- el backend rechaza nombres de archivo o subdirectorios con secuencia inválida `..`;
- si ya existe un archivo con la misma ruta, se reemplaza.

## Response `200 OK`

```json
{
  "fileName": "ruta-relativa/archivo.pdf",
  "fileSize": 12345,
  "message": "¡Archivo cargado exitosamente!"
}
```

## Response de error `500 Internal Server Error`

```json
{
  "error": "No se pudo cargar el archivo: detalle del error"
}
```

## Notas para frontend

- Enviar `FormData`.
- No establecer manualmente `Content-Type`; el navegador lo define con boundary.
- Usar rutas relativas controladas.
- No enviar rutas absolutas del equipo o del servidor.
- Usar la ruta retornada en `fileName` para referencia posterior.

---

# POST `/api/files/upload-multiple`

Carga múltiples archivos.

## Request

Content-Type:

```text
multipart/form-data
```

Campos:

| Campo | Tipo | Obligatorio | Uso |
|---|---|---|---|
| `files` | File[] | Sí | Archivos a cargar. |
| `path` | String | No | Subdirectorio relativo dentro del almacenamiento base. |

Ejemplo conceptual:

```text
files = archivo1
files = archivo2
path = carpeta/subcarpeta
```

## Reglas

- procesa cada archivo de forma independiente;
- para cada archivo exitoso retorna nombre, tamaño y mensaje;
- si un archivo falla, retorna el error en el elemento correspondiente;
- si existe un archivo con la misma ruta, se reemplaza.

## Response `200 OK`

```json
[
  {
    "fileName": "ruta-relativa/archivo1.pdf",
    "fileSize": 12345,
    "message": "Cargado exitosamente"
  },
  {
    "fileName": "ruta-relativa/archivo2.pdf",
    "fileSize": 67890,
    "message": "Cargado exitosamente"
  }
]
```

## Response con error parcial

```json
[
  {
    "fileName": "ruta-relativa/archivo1.pdf",
    "fileSize": 12345,
    "message": "Cargado exitosamente"
  },
  {
    "error": "Error al cargar: detalle del error"
  }
]
```

## Notas para frontend

- El estado HTTP puede ser `200 OK` aunque un archivo individual falle.
- Revisar cada elemento del arreglo.
- Mostrar errores por archivo cuando exista clave `error`.

---

# GET `/api/files/download/**`

Descarga un archivo por ruta relativa.

## Ruta

La parte posterior a `/download/` corresponde a la ruta relativa del archivo dentro del almacenamiento base.

Ejemplo conceptual:

```text
GET /api/files/download/conciliacion/1/solicitud.pdf
```

## Reglas

- el backend decodifica la ruta solicitada;
- busca el archivo dentro del almacenamiento base;
- si encuentra el archivo, responde como recurso binario;
- intenta determinar `Content-Type`;
- si no se puede determinar, usa `application/octet-stream`;
- retorna header `Content-Disposition` como adjunto.

## Response `200 OK`

Headers relevantes:

```text
Content-Type: tipo-detectado-o-application/octet-stream
Content-Disposition: attachment; filename="nombre-del-archivo"
```

Body:

```text
contenido binario del archivo
```

## Errores esperados

| Estado | Causa |
|---|---|
| `404 Not Found` | Archivo no encontrado. |
| `500 Internal Server Error` | Error interno al cargar o enviar archivo. |

## Notas para frontend

- No tratar esta respuesta como JSON.
- Usar `blob()` cuando se descargue desde JavaScript.
- Soportar `404` cuando el archivo no exista.
- Usar únicamente rutas retornadas por backend.

---

# GET `/api/files/list`

Lista archivos del directorio base.

## Request

No requiere parámetros.

## Response `200 OK`

```json
[
  "archivo1.pdf",
  "archivo2.pdf"
]
```

## Errores esperados

| Estado | Causa |
|---|---|
| `404 Not Found` | Directorio no encontrado. |
| `500 Internal Server Error` | Error interno al listar archivos. |

---

# GET `/api/files/list/{subDir}`

Lista archivos de un subdirectorio.

## Parámetros

| Parámetro | Tipo | Ubicación | Uso |
|---|---|---|---|
| `subDir` | String | Path | Subdirectorio relativo. |

Ejemplo conceptual:

```text
GET /api/files/list/conciliacion/1
```

## Reglas

- `subDir` debe ser una ruta relativa;
- no se permiten secuencias inválidas `..`;
- el subdirectorio debe existir.

## Response `200 OK`

```json
[
  "solicitud.pdf",
  "acta.pdf"
]
```

## Errores esperados

| Estado | Causa |
|---|---|
| `404 Not Found` | Subdirectorio no encontrado. |
| `500 Internal Server Error` | Error interno al listar archivos. |

---

# GET `/api/files/directories`

Lista subdirectorios dentro del almacenamiento base.

## Response `200 OK`

```json
[
  "conciliacion",
  "conciliacion/1",
  "consultas/documentos"
]
```

Los valores anteriores son ilustrativos.

## Reglas

- retorna rutas relativas;
- normaliza separadores de ruta a `/`;
- excluye el directorio base del listado.

## Errores esperados

| Estado | Causa |
|---|---|
| `500 Internal Server Error` | Error interno al listar directorios. |

---

# Reglas de almacenamiento

## Nombre del archivo

El backend:

- limpia el nombre con utilidades de Spring;
- rechaza secuencias `..`;
- conserva el nombre original cuando se usa carga genérica;
- reemplaza archivos existentes en la misma ruta.

## Subdirectorio

Cuando se informa `path` o `subDir`:

- debe ser relativo;
- no debe contener `..`;
- se resuelve contra el almacenamiento base;
- se crea si no existe durante carga.

## Rutas relativas

Los endpoints retornan rutas relativas como:

```text
subdirectorio/archivo.ext
```

El frontend debe guardar o usar esas rutas, no rutas absolutas del servidor.

---

# Errores comunes

| Estado | Causa |
|---|---|
| `401 Unauthorized` | Sesión no válida. |
| `403 Forbidden` | Usuario autenticado sin acceso al endpoint. |
| `404 Not Found` | Archivo o directorio no encontrado. |
| `500 Internal Server Error` | Error de almacenamiento, lectura o escritura. |

---

# Notas para frontend

- En cargas, usar `FormData`.
- En descargas, procesar como `Blob`.
- No enviar rutas absolutas.
- No enviar `..` en rutas.
- No asumir que los endpoints de descarga devuelven JSON.
- En carga múltiple, revisar errores por elemento.
- Usar `credentials: "include"` en todas las peticiones protegidas.
