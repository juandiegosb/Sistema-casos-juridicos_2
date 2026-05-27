# Estado de cobertura documental

Este documento resume la cobertura documental actual del sistema.

No funciona como lista de faltantes, sino como guía de ubicación y alcance de la documentación vigente.

## Cobertura actual

| Área | Carpeta/documentos |
|---|---|
| Visión general | `doc/00-vision-general.md` |
| Arquitectura | `doc/01-arquitectura.md` |
| Configuración y seguridad | `doc/02-configuracion-seguridad.md` |
| Autenticación y autorización | `doc/03-autenticacion-autorizacion.md` |
| Permisos y alcance | `doc/04-permisos-roles-alcance.md` |
| Estándar de API y errores | `doc/05-estandar-api-errores.md` |
| Backend por módulos | `doc/backend/` |
| Inventario de API | `doc/api/` |
| Reglas de negocio | `doc/reglas/` |
| Base de datos y estados | `doc/base-datos/` |
| Decisiones técnicas | `doc/decisiones/` |
| Mantenimiento documental | `doc/mantenimiento/` |

## Backend

Documentos:

```text
doc/backend/README.md
doc/backend/catalogos.md
doc/backend/personas.md
doc/backend/perfiles.md
doc/backend/consultas.md
doc/backend/seguimientos.md
doc/backend/procesos.md
doc/backend/conciliaciones.md
doc/backend/archivos.md
doc/backend/auditoria.md
```

Cobertura:

- estructura backend;
- módulos funcionales;
- entidades principales;
- services;
- validators;
- reglas de negocio por módulo;
- permisos;
- relaciones;
- endpoints de referencia;
- consideraciones para frontend.

## API

Documentos:

```text
doc/api/README.md
doc/api/autenticacion.md
doc/api/usuarios-roles-permisos.md
doc/api/catalogos.md
doc/api/personas.md
doc/api/perfiles.md
doc/api/consultas.md
doc/api/seguimientos.md
doc/api/procesos.md
doc/api/conciliaciones.md
doc/api/archivos.md
doc/api/auditoria.md
```

Cobertura:

- rutas;
- métodos HTTP;
- permisos;
- requests;
- responses;
- errores esperados;
- notas para frontend;
- contratos multipart;
- autenticación.

## Reglas

Documentos:

```text
doc/reglas/README.md
doc/reglas/consultas.md
doc/reglas/seguimientos.md
doc/reglas/procesos.md
doc/reglas/conciliaciones.md
doc/reglas/permisos.md
doc/reglas/archivos.md
```

Cobertura:

- reglas de consulta;
- reglas de seguimiento;
- reglas de proceso;
- reglas de conciliación;
- reglas de permisos y alcance;
- reglas de archivos;
- relación de cierre de consulta con pendientes operativos.

## Base de datos

Documentos:

```text
doc/base-datos/README.md
doc/base-datos/entidades-principales.md
doc/base-datos/estados-y-catalogos.md
```

Cobertura:

- entidades principales;
- relaciones;
- estados funcionales;
- catálogos;
- diferencia entre estado funcional y activo lógico;
- estados que bloquean cierre de consulta.

## Decisiones

Documentos:

```text
doc/decisiones/README.md
doc/decisiones/seguridad-documental.md
doc/decisiones/permisos-y-alcance.md
doc/decisiones/estado-vs-activo.md
doc/decisiones/conciliacion.md
doc/decisiones/documentacion-vigente.md
```

Cobertura:

- seguridad documental;
- permisos y alcance;
- estado funcional vs activo lógico;
- decisiones de conciliación;
- documentación vigente.

## Frontend

La documentación de frontend se maneja con dos niveles:

### Documentación contractual vigente

Ya cubierta en:

```text
doc/api/
doc/reglas/
doc/03-autenticacion-autorizacion.md
doc/04-permisos-roles-alcance.md
```

Incluye:

- contratos de backend;
- uso de `credentials: "include"`;
- permisos;
- manejo de errores;
- notas de consumo para frontend.

### Documentación interna de frontend

La documentación interna detallada de estructura, componentes, pantallas y formularios se organiza cuando la estructura frontend esté estable.

Ubicación prevista:

```text
doc/frontend/
```

## Correcciones lógicas futuras

Cuando se implementen ajustes de lógica funcional, la documentación se actualiza por impacto usando:

```text
doc/mantenimiento/matriz-actualizacion-documental.md
```

Ejemplos:

| Ajuste | Documentos afectados |
|---|---|
| Radicado condicional en procesos | Backend, API, reglas y base de datos de procesos. |
| Resultado obligatorio al cerrar consulta | Backend, API y reglas de consultas. |
| Observación obligatoria al rechazar respuesta | Backend, API y reglas de seguimientos. |
| Validación de asesor activo en creación de consulta por estudiante | Backend, API y reglas de consultas/perfiles. |

## Criterio de cierre documental

La cobertura documental se mantiene correcta cuando:

- los endpoints documentados existen;
- los DTOs coinciden con contratos actuales;
- las reglas coinciden con validators y services;
- los permisos coinciden con controllers y access services;
- los estados coinciden con enums o catálogos;
- las decisiones técnicas siguen reflejando el diseño real;
- no hay valores sensibles publicados.
