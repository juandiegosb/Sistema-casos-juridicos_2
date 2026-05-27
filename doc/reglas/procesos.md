# Reglas de negocio - Procesos

El módulo de procesos administra procesos asociados a consultas jurídicas.

Un proceso registra información judicial u operativa como número de radicado, departamento, órgano de control, especialidad, estado funcional y estado lógico activo/inactivo.

## Principios del módulo

- Todo proceso pertenece a una consulta.
- La consulta define el alcance del proceso.
- El estado funcional del proceso se diferencia del campo `activo`.
- El número de radicado identifica el proceso y debe ser único.
- Un proceso pendiente bloquea el cierre de la consulta.
- La eliminación de proceso es desactivación lógica.

## Estados de proceso

| Estado | Uso |
|---|---|
| `PENDIENTE` | Proceso pendiente de resultado. |
| `SENTENCIA_FAVORABLE` | Proceso con sentencia favorable. |
| `SENTENCIA_DESFAVORABLE` | Proceso con sentencia desfavorable. |
| `DESISTIMIENTO` | Proceso terminado por desistimiento. |
| `RECHAZO` | Proceso terminado por rechazo. |
| `PRESCRIPCION` | Proceso terminado por prescripción. |

## Creación de proceso

Reglas:

- requiere permiso para gestionar procesos;
- no se permite enviar `id`;
- el número de radicado es obligatorio;
- el número de radicado se normaliza;
- el número de radicado debe tener exactamente 23 caracteres;
- el número de radicado debe ser único;
- el departamento es obligatorio y debe estar activo;
- la consulta es obligatoria;
- la consulta debe permitir operación;
- el usuario debe tener alcance sobre la consulta;
- estudiante y conciliador no gestionan procesos;
- órgano de control y especialidad son opcionales;
- si se informa especialidad, debe existir órgano de control;
- la especialidad debe pertenecer al órgano de control seleccionado;
- el proceso nace con estado `PENDIENTE`;
- el proceso nace activo.

## Actualización de proceso

Reglas:

- requiere permiso para gestionar procesos;
- el proceso debe existir y estar activo;
- el usuario debe tener alcance sobre la consulta;
- si el DTO trae `id`, debe coincidir con la ruta;
- no se permite cambiar la consulta asociada;
- el número de radicado debe mantener unicidad;
- departamento, órgano de control y especialidad deben estar activos cuando se informan;
- la especialidad debe pertenecer al órgano de control seleccionado;
- debe existir cambio real;
- actualizar datos generales no cambia `activo`;
- el estado funcional se cambia por endpoint específico.

## Cambio de estado funcional

Reglas:

- requiere permiso para gestionar procesos;
- el estado destino es obligatorio;
- no se permite cambiar al mismo estado;
- el proceso debe estar activo;
- la consulta asociada debe permitir operación;
- el usuario debe tener alcance sobre la consulta.

## Cambio de activo lógico

Reglas:

- requiere permiso para gestionar procesos;
- el parámetro `activo` es obligatorio;
- no se permite cambiar al mismo estado lógico;
- el usuario debe tener alcance sobre la consulta;
- la consulta asociada debe permitir operación.

## Eliminación lógica

Reglas:

- requiere permiso para gestionar procesos;
- el proceso debe estar activo;
- se valida alcance;
- la consulta asociada debe permitir operación;
- se marca `activo=false`;
- no se elimina físicamente.

## Alcance

El alcance del proceso se hereda desde la consulta asociada.

Reglas:

- listar procesos aplica alcance por consulta;
- ver proceso valida alcance por consulta;
- crear proceso valida alcance sobre consulta;
- gestionar proceso valida alcance sobre consulta.

## Relación con cierre de consulta

Una consulta no puede cerrarse si tiene procesos activos en estado:

```text
PENDIENTE
```

Esto conserva la coherencia del cierre operativo del caso.

## Órganos de control

Reglas:

- el nombre es obligatorio;
- máximo 80 caracteres;
- nombre único ignorando mayúsculas/minúsculas;
- nace activo;
- actualizar requiere cambio real;
- no se permite desactivar órgano con especialidades activas asociadas;
- la eliminación es desactivación lógica.

## Especialidades

Reglas:

- el nombre es obligatorio;
- máximo 80 caracteres;
- debe pertenecer a un órgano de control activo;
- el nombre debe ser único dentro del órgano de control;
- nace activa;
- actualizar requiere cambio real;
- cambiar activo requiere parámetro obligatorio;
- la eliminación es desactivación lógica.

## Reglas para frontend

- validar que el radicado tenga 23 caracteres antes de enviar;
- cargar especialidades según órgano de control;
- no permitir cambiar consulta en edición;
- usar endpoint específico para cambio de estado;
- usar endpoint específico para activo/inactivo;
- manejar errores de radicado duplicado;
- manejar errores de especialidad que no pertenece al órgano;
- usar `credentials: "include"`.
