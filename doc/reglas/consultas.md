# Reglas de negocio - Consultas jurídicas

El módulo de consultas jurídicas administra el ciclo principal de atención del consultorio jurídico.

Una consulta se relaciona con una persona principal, partes, contrapartes, catálogos jurídicos, sede y responsables internos.

## Principios del módulo

- Toda consulta nueva inicia en estado `PENDIENTE`.
- El estado de consulta se cambia mediante endpoint específico.
- La edición general no cambia el estado.
- Una consulta cerrada o archivada no permite operaciones operativas.
- El archivado conserva la consulta como historial.
- El cierre de consulta valida que no existan pendientes operativos asociados.
- El acceso a consultas depende de permisos y alcance por perfil.

## Estados de consulta

| Estado | Uso |
|---|---|
| `PENDIENTE` | Estado inicial de revisión o asignación. |
| `ACTIVO` | Consulta en atención activa. |
| `EN_PROCESO` | Consulta con gestión en proceso. |
| `URGENTE` | Consulta priorizada. |
| `CERRADO` | Consulta cerrada operativamente. |
| `ARCHIVADO` | Consulta archivada para consulta histórica. |

## Creación de consulta

Reglas:

- el usuario debe tener permiso para crear consulta;
- no se permite enviar `id`;
- la fecha es obligatoria;
- descripción, hechos, pretensiones, concepto jurídico y trámite son obligatorios;
- persona principal, sede, área y tema son obligatorios;
- si se envía estado, debe ser `PENDIENTE`;
- el backend asigna estado `PENDIENTE` a toda consulta nueva;
- las relaciones deben existir y estar activas;
- se valida coherencia de área, tema y tipo;
- se valida coherencia de responsables cuando se asignan;
- se valida que persona principal, partes y contrapartes no se repitan indebidamente.

## Actualización de consulta

Reglas:

- requiere permiso de edición;
- el usuario debe tener alcance sobre la consulta;
- la consulta no puede estar archivada;
- la consulta no puede estar cerrada;
- si el DTO trae `id`, debe coincidir con la ruta;
- el estado no puede cambiarse desde edición general;
- si se cambian responsables, se requiere permiso de asignación;
- se vuelven a validar relaciones activas;
- se valida coherencia completa de dominio después de aplicar cambios.

## Cambio de estado

Reglas:

- el estado destino es obligatorio;
- no se permite cambiar al mismo estado actual;
- una consulta archivada no cambia estado operativo;
- una consulta cerrada solo puede archivarse;
- el estudiante no puede cambiar estado;
- se valida permiso funcional;
- se valida alcance;
- los estados operativos de atención requieren responsables mínimos;
- cerrar o archivar valida pendientes operativos.

## Estados que requieren responsables

Para pasar a:

```text
ACTIVO
EN_PROCESO
URGENTE
```

la consulta debe tener:

- asesor asignado;
- estudiante asignado.

También se valida:

- asesor pertenece al área de la consulta;
- estudiante pertenece al asesor asignado cuando ambos existen;
- el asesor del estudiante pertenece al área de la consulta.

## Responsables

Responsables posibles:

- asesor;
- monitor;
- estudiante.

Reglas:

- asignar o cambiar responsables requiere permiso `Asignar responsables consulta`;
- el estudiante no puede asignar responsables;
- si se asigna estudiante sin asesor explícito, el backend toma el asesor activo asociado al estudiante;
- si el asesor asociado al estudiante no existe o está inactivo, la operación se rechaza;
- el asesor asignado debe pertenecer al área de la consulta;
- el estudiante asignado debe pertenecer al asesor cuando ambos están definidos.

## Persona principal, partes y contrapartes

Reglas:

- la persona principal es obligatoria;
- la persona principal no puede repetirse como parte;
- la persona principal no puede repetirse como contraparte;
- una persona no puede estar al mismo tiempo como parte y contraparte;
- no se permiten duplicados dentro de partes;
- no se permiten duplicados dentro de contrapartes;
- todas las personas relacionadas deben existir y estar activas.

## Catálogos jurídicos

La consulta se relaciona con:

- área;
- tema;
- tipo.

Reglas:

- el área es obligatoria;
- el tema es obligatorio;
- el tema debe pertenecer al área;
- el tipo, si se informa, debe pertenecer al tema;
- sede, área, tema y tipo deben estar activos cuando se asignan.

## Cierre de consulta

Una consulta puede pasar a `CERRADO` cuando cumple las reglas de estado y no tiene pendientes operativos asociados.

Pendientes que bloquean cierre:

| Módulo | Condición |
|---|---|
| Procesos | Procesos activos en estado `PENDIENTE`. |
| Seguimientos | Seguimientos activos en estado `PENDIENTE`. |
| Respuestas de seguimiento | Respuestas activas en estado `PENDIENTE`. |
| Notificaciones de seguimiento | Notificaciones activas no enviadas. |
| Conciliaciones | Conciliaciones activas en estados no finalizados. |

## Archivo de consulta

Reglas:

- una consulta solo se archiva si está `CERRADO`;
- el archivado requiere permiso administrativo correspondiente;
- el archivado vuelve a validar pendientes operativos;
- el endpoint `DELETE` funciona como archivado lógico;
- el estado `ARCHIVADO` excluye la consulta de listados operativos;
- los datos asociados se conservan para historial.

## Desarchivo de consulta

Reglas:

- solo se pueden desarchivar consultas en estado `ARCHIVADO`;
- desarchivar devuelve la consulta a `CERRADO`;
- desarchivar no reabre operación activa;
- se validan pendientes operativos como defensa de consistencia.

## Alcance de consulta

| Perfil | Alcance |
|---|---|
| Administrador | Accede a todas las consultas según permisos. |
| Estudiante | Accede a consultas donde es estudiante asignado. |
| Asesor | Accede a consultas donde es asesor asignado o donde el estudiante pertenece a su asesoría. |
| Monitor | Accede a consultas donde es monitor asignado. |
| Conciliador | No accede desde el buscador general de consultas. |

## Reglas para frontend

- usar el buscador general; el backend filtra por alcance;
- no cambiar estado desde edición general;
- usar endpoint específico para cambio de estado;
- manejar errores de pendientes al cerrar;
- manejar errores de responsables mínimos al activar;
- no permitir duplicar personas en partes y contrapartes;
- usar endpoints de archivo/desarchivo para ciclo histórico.
