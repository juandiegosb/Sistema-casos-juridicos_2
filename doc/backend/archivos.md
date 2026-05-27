# Backend - Archivos

El módulo de archivos administra carga, descarga y listado de documentos usados por los módulos del sistema.

El backend centraliza el almacenamiento físico mediante `FileStorageService` y expone endpoints autenticados desde `FileUploadController`.

## Paquetes principales

```text
file_storage/controller
file_storage/service
file_storage/exception
```

## Componentes principales

| Componente | Responsabilidad |
|---|---|
| `FileUploadController` | Expone endpoints HTTP para cargar, descargar y listar archivos. |
| `FileStorageService` | Centraliza almacenamiento, recuperación y listado de archivos en disco. |
| `FileStorageException` | Excepción para errores generales de almacenamiento. |
| `FileNotFoundException` | Excepción para archivos o directorios no encontrados. |

## Configuración

El directorio base de almacenamiento se define mediante la propiedad:

```text
file.upload-dir
```

Esta propiedad se configura desde el entorno de ejecución y no debe documentarse con rutas privadas del equipo.

Durante la inicialización, `FileStorageService`:

- resuelve el directorio base;
- normaliza la ruta;
- crea el directorio si no existe;
- mantiene la ruta base como `Path` para operaciones posteriores.

## Seguridad de acceso

Todos los endpoints del controller requieren usuario autenticado:

```text
@PreAuthorize("isAuthenticated()")
```

La validación de permisos específicos de negocio corresponde a los módulos que usan archivos.

Ejemplo:

- conciliaciones valida permisos y alcance antes de guardar solicitud o acta;
- seguimientos valida permisos antes de operar adjuntos cuando aplique;
- consultas o formularios validan su flujo antes de asociar documentos.

## Endpoints

Base path:

```text
/api/files
```

| Método | Ruta | Content-Type | Uso |
|---|---|---|---|
| POST | `/api/files/upload` | `multipart/form-data` | Carga un archivo. |
| POST | `/api/files/upload-multiple` | `multipart/form-data` | Carga múltiples archivos. |
| GET | `/api/files/download/**` | - | Descarga un archivo por ruta relativa. |
| GET | `/api/files/list` | - | Lista archivos del directorio base. |
| GET | `/api/files/list/{subDir}` | - | Lista archivos de un subdirectorio. |
| GET | `/api/files/directories` | - | Lista directorios dentro del almacenamiento base. |

## Carga de archivo único

Endpoint:

```text
POST /api/files/upload
```

Parámetros:

| Parámetro | Tipo | Obligatorio | Uso |
|---|---|---|---|
| `file` | Archivo | Sí | Archivo a cargar. |
| `path` | Texto | No | Subdirectorio relativo dentro del almacenamiento base. |

Respuesta exitosa:

```json
{
  "fileName": "ruta-relativa-del-archivo",
  "fileSize": 12345,
  "message": "Archivo cargado exitosamente"
}
```

El nombre retornado puede incluir el subdirectorio cuando se informa `path`.

## Carga múltiple

Endpoint:

```text
POST /api/files/upload-multiple
```

Parámetros:

| Parámetro | Tipo | Obligatorio | Uso |
|---|---|---|---|
| `files` | Lista de archivos | Sí | Archivos a cargar. |
| `path` | Texto | No | Subdirectorio relativo dentro del almacenamiento base. |

Respuesta:

```json
[
  {
    "fileName": "ruta-relativa-del-archivo",
    "fileSize": 12345,
    "message": "Cargado exitosamente"
  }
]
```

Si un archivo falla, la respuesta de ese elemento incluye `error`.

## Descarga

Endpoint:

```text
GET /api/files/download/**
```

La ruta después de `/download/` se interpreta como ruta relativa del archivo dentro del almacenamiento base.

El controller:

- decodifica la ruta solicitada;
- busca el archivo como `Resource`;
- intenta determinar `Content-Type`;
- usa `application/octet-stream` si no se puede determinar;
- retorna `Content-Disposition` como adjunto.

Respuestas:

| Estado | Uso |
|---|---|
| `200 OK` | Archivo encontrado y retornado. |
| `404 Not Found` | Archivo no encontrado. |
| `500 Internal Server Error` | Error interno al procesar la descarga. |

## Listado de archivos

Endpoints:

```text
GET /api/files/list
GET /api/files/list/{subDir}
```

Reglas:

- sin `subDir`, lista archivos del directorio base;
- con `subDir`, lista archivos del subdirectorio indicado;
- si el directorio no existe, retorna `404`;
- si ocurre un error de lectura, retorna error de almacenamiento.

## Listado de directorios

Endpoint:

```text
GET /api/files/directories
```

Reglas:

- recorre el almacenamiento base;
- retorna rutas relativas de subdirectorios;
- normaliza separadores de ruta para devolver `/`.

## Reglas de almacenamiento

`FileStorageService` aplica las siguientes reglas:

### Nombre de archivo

Antes de almacenar:

- toma el nombre original del archivo o el nombre objetivo;
- limpia la ruta con `StringUtils.cleanPath`;
- rechaza nombres que contengan `..`.

### Subdirectorio

Cuando se informa subdirectorio:

- se rechazan valores que contengan `..`;
- se resuelve la ruta relativa contra el directorio base;
- se normaliza la ruta;
- se crea el subdirectorio si no existe.

### Escritura

La escritura usa:

```text
StandardCopyOption.REPLACE_EXISTING
```

Por tanto, guardar un archivo con la misma ruta reemplaza el archivo anterior.

## Guardado con nombre objetivo

`FileStorageService` expone:

```text
storeFileAs(file, subDir, targetFileName)
```

Uso:

- permite guardar un archivo con un nombre definido por el módulo;
- se usa para flujos donde el backend define una ruta estable.

Ejemplo de uso funcional:

- solicitud de conciliación;
- acta de conciliación;
- documentos que deban guardarse en una ubicación determinística.

## Carga de archivo como recurso

`loadFileAsResource(fileName)`:

- rechaza rutas con `..`;
- resuelve el archivo contra el directorio base;
- retorna un `UrlResource`;
- lanza `FileNotFoundException` si no existe.

## Excepciones

### `FileStorageException`

Se usa para:

- errores creando directorio base;
- rutas inválidas;
- errores de escritura;
- errores de listado.

### `FileNotFoundException`

Se usa para:

- archivos inexistentes;
- directorios inexistentes.

## Relación con otros módulos

Los módulos de negocio no deben construir rutas sensibles expuestas al usuario.

Patrón recomendado:

1. el módulo valida permisos y reglas de negocio;
2. el módulo define subdirectorio y nombre objetivo;
3. `FileStorageService` guarda el archivo;
4. el módulo persiste la ruta relativa retornada.

Ejemplo en conciliaciones:

```text
conciliacion/{id}/solicitud.pdf
conciliacion/{id}/acta.pdf
```

## Consideraciones para frontend

- Las cargas se envían con `multipart/form-data`.
- Las peticiones protegidas deben usar `credentials: "include"`.
- El parámetro `path` debe ser una ruta relativa controlada.
- Para descargas, usar la ruta relativa retornada por backend.
- No construir rutas físicas del servidor en frontend.
- No enviar rutas privadas del sistema operativo.
- Manejar `404` cuando el archivo ya no exista o la ruta sea incorrecta.

## Reglas de seguridad

- No aceptar rutas con `..`.
- No exponer rutas absolutas internas del servidor.
- No documentar rutas privadas del equipo.
- No versionar archivos subidos por usuarios.
- No usar el almacenamiento de archivos como fuente de configuración sensible.
