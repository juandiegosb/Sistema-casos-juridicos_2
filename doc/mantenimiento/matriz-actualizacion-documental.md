# Matriz de actualización documental

Esta matriz indica qué documentos revisar cuando cambia una parte del sistema.

No todos los cambios obligan a actualizar toda la documentación. La regla es actualizar únicamente los documentos afectados por el cambio.

## Cambios en endpoints

| Cambio | Documentos a revisar |
|---|---|
| Nuevo endpoint | `doc/api/<modulo>.md`, `doc/backend/<modulo>.md` si afecta implementación, `doc/reglas/<modulo>.md` si agrega regla. |
| Cambio de método HTTP | `doc/api/<modulo>.md`, notas para frontend en backend si aplica. |
| Cambio de ruta | `doc/api/<modulo>.md`, documentación frontend si consume esa ruta. |
| Cambio de query param | `doc/api/<modulo>.md`, ejemplos de consumo. |
| Cambio de path variable | `doc/api/<modulo>.md`. |
| Nuevo endpoint multipart | `doc/api/<modulo>.md`, `doc/reglas/archivos.md` si afecta reglas de archivo, `doc/backend/archivos.md` si cambia almacenamiento. |
| Eliminación de endpoint | `doc/api/<modulo>.md`, revisar documentos que lo referencien. |

## Cambios en DTOs

| Cambio | Documentos a revisar |
|---|---|
| Nuevo campo en request | `doc/api/<modulo>.md`, `doc/backend/<modulo>.md`, `doc/reglas/<modulo>.md` si tiene validación de negocio. |
| Nuevo campo en response | `doc/api/<modulo>.md`, documentos frontend si consumen el campo. |
| Campo eliminado | `doc/api/<modulo>.md`, frontend si lo usaba. |
| Campo ahora obligatorio | `doc/api/<modulo>.md`, `doc/reglas/<modulo>.md`, `doc/backend/<modulo>.md`. |
| Campo ahora opcional | `doc/api/<modulo>.md`, `doc/reglas/<modulo>.md`. |
| Cambio de tipo de dato | `doc/api/<modulo>.md`, `doc/base-datos/entidades-principales.md` si impacta entidad. |
| Cambio de nombre de campo | `doc/api/<modulo>.md`, frontend si consume JSON. |

## Cambios en reglas de negocio

| Cambio | Documentos a revisar |
|---|---|
| Regla de consulta | `doc/reglas/consultas.md`, `doc/backend/consultas.md`, `doc/api/consultas.md`. |
| Regla de seguimiento | `doc/reglas/seguimientos.md`, `doc/backend/seguimientos.md`, `doc/api/seguimientos.md`. |
| Regla de proceso | `doc/reglas/procesos.md`, `doc/backend/procesos.md`, `doc/api/procesos.md`. |
| Regla de conciliación | `doc/reglas/conciliaciones.md`, `doc/backend/conciliaciones.md`, `doc/api/conciliaciones.md`, `doc/decisiones/conciliacion.md` si afecta decisión. |
| Regla de permisos o alcance | `doc/reglas/permisos.md`, `doc/decisiones/permisos-y-alcance.md`, `doc/04-permisos-roles-alcance.md`, API afectada. |
| Regla de archivo | `doc/reglas/archivos.md`, `doc/backend/archivos.md`, `doc/api/archivos.md`. |

## Cambios en estados

| Cambio | Documentos a revisar |
|---|---|
| Nuevo estado de consulta | `doc/base-datos/estados-y-catalogos.md`, `doc/reglas/consultas.md`, `doc/api/consultas.md`, `doc/backend/consultas.md`. |
| Nuevo estado de proceso | `doc/base-datos/estados-y-catalogos.md`, `doc/reglas/procesos.md`, `doc/api/procesos.md`, `doc/backend/procesos.md`. |
| Nuevo estado de seguimiento | `doc/base-datos/estados-y-catalogos.md`, `doc/reglas/seguimientos.md`, `doc/api/seguimientos.md`, `doc/backend/seguimientos.md`. |
| Nuevo estado de respuesta | `doc/base-datos/estados-y-catalogos.md`, `doc/reglas/seguimientos.md`, `doc/api/seguimientos.md`. |
| Nuevo estado de conciliación | `doc/base-datos/estados-y-catalogos.md`, `doc/reglas/conciliaciones.md`, `doc/api/conciliaciones.md`, `doc/backend/conciliaciones.md`, `doc/decisiones/conciliacion.md`. |
| Cambio de estado final/no final | Reglas del módulo, API del módulo, base de datos y decisiones si aplica. |

## Cambios en permisos

| Cambio | Documentos a revisar |
|---|---|
| Nuevo permiso en `PermisoNombre` | `doc/04-permisos-roles-alcance.md`, `doc/reglas/permisos.md`, `doc/decisiones/permisos-y-alcance.md`, API del módulo afectado. |
| Cambio de permiso en controller | `doc/api/<modulo>.md`, `doc/reglas/permisos.md`. |
| Cambio de matriz rol-permiso | `doc/reglas/permisos.md`, `doc/decisiones/permisos-y-alcance.md`. |
| Nuevo permiso de navegación | `doc/04-permisos-roles-alcance.md`, documentación frontend cuando esté vigente. |
| Cambio de alcance | `doc/reglas/permisos.md`, documento backend del módulo, documento API del módulo. |

## Cambios en entidades y base de datos

| Cambio | Documentos a revisar |
|---|---|
| Nueva entidad | `doc/base-datos/entidades-principales.md`, `doc/backend/<modulo>.md`, `doc/api/<modulo>.md` si expone endpoints. |
| Nueva relación | `doc/base-datos/entidades-principales.md`, backend del módulo. |
| Campo nuevo en entidad | `doc/base-datos/entidades-principales.md`, API si se expone. |
| Campo eliminado | `doc/base-datos/entidades-principales.md`, API si se exponía. |
| Cambio de nullable | `doc/base-datos/entidades-principales.md`, reglas y API si afecta validación. |
| Nueva restricción única | `doc/base-datos/entidades-principales.md`, reglas del módulo. |
| Nuevo catálogo | `doc/base-datos/estados-y-catalogos.md`, `doc/backend/catalogos.md`, `doc/api/catalogos.md`. |

## Cambios en archivos

| Cambio | Documentos a revisar |
|---|---|
| Nuevo flujo de carga de documento | `doc/reglas/archivos.md`, `doc/backend/archivos.md`, API del módulo. |
| Cambio de ruta estándar | `doc/reglas/archivos.md`, API del módulo, backend del módulo. |
| Cambio de tipo permitido | `doc/reglas/archivos.md`, API del módulo. |
| Cambio de endpoint de descarga | `doc/api/archivos.md`, frontend si consume descarga. |

## Cambios en autenticación

| Cambio | Documentos a revisar |
|---|---|
| Cambio de login | `doc/api/autenticacion.md`, `doc/03-autenticacion-autorizacion.md`. |
| Cambio de cookie | `doc/api/autenticacion.md`, `doc/02-configuracion-seguridad.md`, documentación frontend cuando aplique. |
| Cambio en JWT | `doc/02-configuracion-seguridad.md`, `doc/03-autenticacion-autorizacion.md`. |
| Cambio en recuperación de contraseña | `doc/api/autenticacion.md`, `doc/03-autenticacion-autorizacion.md`. |
| Cambio en CORS | `doc/02-configuracion-seguridad.md`, documentación frontend cuando aplique. |

## Cambios del frontend

| Cambio | Documentos a revisar |
|---|---|
| Cambio de configuración API | `doc/02-configuracion-seguridad.md`, `doc/api/README.md`, documentación frontend. |
| Cambio en manejo de sesión | `doc/03-autenticacion-autorizacion.md`, `doc/api/autenticacion.md`, documentación frontend. |
| Cambio en navegación por permisos | `doc/04-permisos-roles-alcance.md`, `doc/reglas/permisos.md`, documentación frontend. |
| Cambio de pantalla o estructura interna | Documentación frontend cuando esa fase esté vigente. |
| Cambio en consumo de endpoint | Documento API del módulo afectado. |

## Cambios en decisiones técnicas

| Cambio | Documentos a revisar |
|---|---|
| Nueva decisión de seguridad | `doc/decisiones/seguridad-documental.md`. |
| Cambio en permisos/alcance | `doc/decisiones/permisos-y-alcance.md`. |
| Cambio en estado vs activo | `doc/decisiones/estado-vs-activo.md`. |
| Cambio relevante en conciliación | `doc/decisiones/conciliacion.md`. |
| Cambio en metodología documental | `doc/decisiones/documentacion-vigente.md`, `doc/mantenimiento/*`. |

## Cambios P0/P1 de lógica funcional

Cuando se implementen mejoras lógicas priorizadas, revisar según módulo:

| Mejora | Documentos a actualizar |
|---|---|
| Proceso pendiente sin radicado y finalización con radicado obligatorio | `doc/backend/procesos.md`, `doc/api/procesos.md`, `doc/reglas/procesos.md`, `doc/base-datos/entidades-principales.md`. |
| Resultado obligatorio para cerrar consulta | `doc/backend/consultas.md`, `doc/api/consultas.md`, `doc/reglas/consultas.md`. |
| Observación obligatoria al rechazar respuesta | `doc/backend/seguimientos.md`, `doc/api/seguimientos.md`, `doc/reglas/seguimientos.md`. |
| Asesor activo al crear consulta como estudiante | `doc/backend/consultas.md`, `doc/backend/perfiles.md`, `doc/api/consultas.md`, `doc/reglas/consultas.md`. |
| Validar perfil activo con usuario de sistema | `doc/backend/perfiles.md`, `doc/api/usuarios-roles-permisos.md`, `doc/reglas/permisos.md`, `doc/03-autenticacion-autorizacion.md`. |
| Bloquear desactivación de responsables con consultas vivas | `doc/backend/perfiles.md`, `doc/api/perfiles.md`, `doc/reglas/permisos.md`, `doc/reglas/consultas.md`. |
