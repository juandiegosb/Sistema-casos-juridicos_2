# Reglas de negocio - Archivos

El módulo de archivos administra almacenamiento, descarga y listado de documentos.

Los archivos se manejan con rutas relativas controladas por backend.

## Principios

- Los endpoints de archivos requieren autenticación.
- Las reglas funcionales las aplica el módulo que usa el archivo.
- El backend controla las rutas.
- El frontend no debe construir rutas físicas del servidor.
- Las rutas relativas no deben contener secuencias inválidas.
- Los archivos generados o subidos no se versionan en el repositorio.

## Almacenamiento base

El directorio base se configura mediante:

```text
file.upload-dir
```

La documentación no debe publicar rutas privadas reales del entorno.

## Carga de archivos

Reglas:

- el archivo se recibe por `multipart/form-data`;
- el nombre del archivo se limpia antes de guardar;
- se rechaza path traversal mediante `..`;
- si el subdirectorio no existe, se crea;
- si el archivo ya existe en la misma ruta, se reemplaza.

## Carga múltiple

Reglas:

- cada archivo se procesa de forma independiente;
- errores por archivo se informan en el resultado del elemento;
- un error parcial no implica que todos los archivos fallen.

## Descarga

Reglas:

- la ruta solicitada es relativa al almacenamiento base;
- se rechaza path traversal;
- se devuelve recurso binario;
- el backend determina `Content-Type` cuando puede;
- si no puede determinarlo, usa `application/octet-stream`;
- se devuelve como archivo adjunto.

## Listado

Reglas:

- se pueden listar archivos del directorio base;
- se pueden listar archivos de un subdirectorio;
- se pueden listar subdirectorios;
- las rutas retornadas son relativas;
- los separadores se normalizan a `/`.

## Rutas de módulo

Los módulos funcionales pueden definir rutas estables.

Ejemplo:

| Módulo | Ruta |
|---|---|
| Conciliación | `conciliacion/{id}/solicitud.pdf` |
| Conciliación | `conciliacion/{id}/acta.pdf` |

## Validaciones por módulo

El servicio genérico de archivos valida rutas y almacenamiento.

Cada módulo valida reglas funcionales adicionales.

Ejemplos:

- conciliación valida que solicitud y acta sean PDF;
- conciliación valida permisos y estado antes de guardar documentos;
- otros módulos validan su contexto antes de asociar documentos.

## Seguridad

Reglas:

- no exponer rutas absolutas;
- no aceptar `..` en nombre o subdirectorio;
- no documentar rutas privadas;
- no versionar archivos subidos;
- no almacenar secretos en archivos subidos como configuración del sistema;
- no usar nombres de archivo entregados por usuario para definir rutas sensibles sin control backend.

## Frontend

Reglas:

- usar `FormData` para carga;
- no establecer manualmente `Content-Type` en multipart;
- procesar descargas como `Blob`;
- no tratar descargas como JSON;
- usar rutas relativas retornadas por backend;
- usar `credentials: "include"` en peticiones protegidas;
- manejar `404` cuando un archivo no existe.
