# Reglas de negocio - Seguimientos

El módulo de seguimientos administra tareas, actuaciones, recordatorios, respuestas de estudiante y notificaciones asociadas a consultas jurídicas.

## Principios del módulo

- Todo seguimiento pertenece a una consulta.
- El seguimiento nace en estado `PENDIENTE`.
- La consulta asociada debe permitir operación.
- El estado se cambia por endpoint específico.
- Las respuestas del estudiante tienen ciclo de revisión propio.
- Las notificaciones se sincronizan según configuración del seguimiento.
- Las consultas con seguimientos o respuestas pendientes no se cierran.

## Estados de seguimiento

| Estado | Uso |
|---|---|
| `PENDIENTE` | Seguimiento activo en gestión. |
| `COMPLETADO` | Seguimiento finalizado. |
| `CANCELADO` | Seguimiento cancelado. |

## Estados de respuesta

| Estado | Uso |
|---|---|
| `PENDIENTE` | Respuesta enviada y pendiente de revisión. |
| `APROBADA` | Respuesta aprobada. |
| `RECHAZADA` | Respuesta rechazada. |

## Creación de seguimiento

Reglas:

- requiere permiso para crear seguimientos;
- no se permite enviar `id`;
- la descripción es obligatoria;
- la descripción se normaliza;
- la descripción no puede superar 200 caracteres;
- fecha de entrega, si se informa, no puede ser anterior a la fecha actual;
- días de notificación no puede ser negativo;
- no se permiten días de notificación sin fecha de entrega;
- la categoría debe existir y estar activa;
- la consulta debe existir y permitir operación;
- el usuario debe tener alcance sobre la consulta;
- estudiante y conciliador no crean seguimientos;
- el autor se toma del usuario autenticado;
- las banderas booleanas nulas se interpretan como `false`;
- al guardar se sincronizan notificaciones.

## Actualización de seguimiento

Reglas:

- requiere permiso para editar seguimientos;
- el usuario debe tener alcance sobre el seguimiento;
- el seguimiento debe estar activo;
- el seguimiento debe estar `PENDIENTE`;
- no se permite cambiar la consulta asociada;
- la consulta debe permitir operación;
- se validan fecha, días de notificación y categoría;
- debe existir al menos un cambio real;
- al actualizar se sincronizan o cancelan notificaciones según configuración.

## Cambio de estado

Reglas:

- el estado destino es obligatorio;
- no se permite cambiar al mismo estado actual;
- la consulta asociada debe permitir operación;
- para completar, no puede haber respuestas pendientes;
- si el seguimiento notifica estudiante, para completarse debe existir respuesta aprobada;
- si existe respuesta aprobada, no se permite volver a `PENDIENTE`;
- al quedar `PENDIENTE`, se sincronizan notificaciones;
- al quedar `COMPLETADO` o `CANCELADO`, se cancelan notificaciones pendientes.

## Eliminación lógica

Reglas:

- requiere permiso para eliminar seguimientos;
- valida alcance;
- la consulta asociada debe permitir operación;
- se cancelan notificaciones pendientes;
- el seguimiento queda inactivo;
- las notificaciones enviadas se conservan como historial.

## Seguimientos visibles para estudiante

Reglas:

- el endpoint de visibles para estudiante retorna seguimientos con `notificarEstudiante=true`;
- el estudiante debe tener alcance sobre la consulta;
- si el seguimiento no está marcado para notificar estudiante, no forma parte de su flujo de respuesta.

## Respuesta del estudiante

Reglas de creación:

- requiere permiso para responder seguimientos;
- solo estudiante puede responder;
- el seguimiento debe estar activo;
- el seguimiento debe estar `PENDIENTE`;
- el seguimiento debe tener `notificarEstudiante=true`;
- el estudiante debe tener alcance sobre la consulta del seguimiento;
- no puede existir respuesta pendiente previa del mismo estudiante para el mismo seguimiento;
- si la última respuesta fue aprobada, no se permite nuevo intento;
- si la última respuesta fue rechazada, se permite un nuevo intento;
- si se responde fuera de plazo, se marca `fueraPlazo=true`.

## Edición de respuesta

Reglas:

- requiere permiso para responder seguimientos;
- solo el estudiante propietario puede editar;
- la respuesta debe estar activa;
- la respuesta debe estar `PENDIENTE`;
- el seguimiento debe seguir en `PENDIENTE`;
- el seguimiento debe seguir visible para estudiante;
- el contenido se normaliza;
- debe existir cambio real;
- si se edita fuera de plazo, se marca `fueraPlazo=true`.

## Revisión de respuesta

Reglas:

- requiere permiso para aprobar respuestas de seguimiento;
- estudiantes y conciliadores no revisan respuestas;
- la respuesta debe estar activa;
- la respuesta debe estar `PENDIENTE`;
- la decisión debe ser `APROBADA` o `RECHAZADA`;
- la observación de revisión se normaliza si se informa;
- la observación de revisión no puede superar 500 caracteres;
- se registra usuario revisor y fecha de decisión;
- si se aprueba, el seguimiento se completa automáticamente;
- si se rechaza, el seguimiento permanece en flujo operativo.

## Categorías de seguimiento

Reglas:

- el nombre es obligatorio;
- el nombre se normaliza;
- el nombre no puede superar 50 caracteres;
- el nombre debe ser único ignorando mayúsculas/minúsculas;
- la categoría nace activa;
- actualizar requiere cambio real;
- cambiar estado activo requiere parámetro obligatorio;
- la eliminación es desactivación lógica.

## Notificaciones

Tipos:

| Tipo | Destinatarios |
|---|---|
| `PARTES` | Persona principal, partes y contrapartes. |
| `ESTUDIANTE` | Estudiante asignado a la consulta. |
| `ALERTA_DISCIPLINARIA` | Administrativos activos. |
| `AUTOR` | Usuario que creó el seguimiento. |

Momentos:

| Momento | Uso |
|---|---|
| `INMEDIATA` | Se genera cuando el seguimiento se crea o actualiza. |
| `RECORDATORIO` | Se programa con base en fecha de entrega y días de notificación. |

Reglas:

- las notificaciones inmediatas se generan según flags del seguimiento;
- los recordatorios requieren fecha de entrega y días de notificación;
- las notificaciones ya enviadas no se reenvían;
- las notificaciones pendientes dejan de estar activas cuando ya no aplican;
- las notificaciones enviadas se conservan como historial;
- los destinatarios se normalizan y deduplican por correo.

## Relación con cierre de consulta

Una consulta no puede cerrarse si tiene:

- seguimientos activos en estado `PENDIENTE`;
- respuestas activas en estado `PENDIENTE`;
- notificaciones activas no enviadas.

## Reglas para frontend

- usar categorías activas en formularios;
- usar endpoint de visibles para estudiante en la vista de estudiante;
- no permitir cambiar consulta del seguimiento desde edición;
- no mostrar respuesta si `notificarEstudiante=false`;
- manejar errores al completar seguimientos con respuestas pendientes;
- manejar errores al responder más de una vez;
- mostrar observaciones de revisión cuando existan;
- usar `credentials: "include"`.
