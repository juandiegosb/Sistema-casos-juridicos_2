# Reglas de negocio - Conciliaciones

El módulo de conciliaciones administra conciliaciones asociadas a consultas jurídicas.

La conciliación se crea desde una consulta, registra solicitud PDF, asigna estudiante y conciliador, controla estado funcional y permite finalizar con acta PDF.

## Principios del módulo

- Toda conciliación pertenece a una consulta.
- La consulta es fuente de consultante, partes y contrapartes.
- No se duplica información de partes y contrapartes en conciliación.
- El estado se administra mediante catálogo `estado_conciliacion`.
- Las reglas de estado se validan por código técnico.
- Los documentos obligatorios se reciben como PDF.
- La conciliación usa `activo` para desactivación lógica.
- Una conciliación pendiente bloquea el cierre de consulta.

## Estados de conciliación

| Código | Uso |
|---|---|
| `EN_ESPERA` | Estado automático cuando falta estudiante o conciliador. |
| `ESPERANDO_REUNION` | Estado operativo con estudiante y conciliador asignados. |
| `REUNION_PROGRAMADA` | Estado operativo con responsables y fecha programada. |
| `COMPLETO_CONCILIADO` | Estado final conciliado. |
| `COMPLETO_NO_CONCILIADO` | Estado final no conciliado. |

## Estados no finalizados

```text
EN_ESPERA
ESPERANDO_REUNION
REUNION_PROGRAMADA
```

## Estados finalizados

```text
COMPLETO_CONCILIADO
COMPLETO_NO_CONCILIADO
```

## Creación de conciliación

Reglas:

- requiere permiso para gestionar conciliaciones;
- valida alcance sobre la consulta;
- la consulta no puede estar cerrada;
- la consulta no puede estar archivada;
- no puede existir otra conciliación activa no finalizada para la misma consulta;
- la solicitud PDF es obligatoria;
- el archivo debe tener extensión `.pdf`;
- si se informa content type, debe ser `application/pdf`;
- el usuario autenticado se registra como solicitante;
- se autoasigna estudiante cuando aplica;
- se autoasigna conciliador cuando aplica;
- se calcula estado inicial;
- se guarda la solicitud en ruta estándar.

## Autoasignación de estudiante

Reglas:

1. Si la consulta tiene estudiante activo y habilitado para conciliación, se usa.
2. Si no, se selecciona estudiante activo habilitado para conciliación con menor carga.
3. En empate, se ordena por nombre.
4. Si persiste empate, se ordena por id.
5. Si no hay estudiante disponible, queda sin estudiante asignado.

La carga se calcula sobre conciliaciones activas en estados no finalizados.

## Autoasignación de conciliador

Reglas:

1. Se buscan conciliadores activos.
2. Se elige el conciliador con menor carga de conciliaciones activas no finalizadas.
3. En empate, se ordena por nombre.
4. Si persiste empate, se ordena por id.
5. Si no hay conciliador disponible, queda sin conciliador asignado.

## Estado inicial

| Condición | Estado |
|---|---|
| Tiene estudiante y conciliador | `ESPERANDO_REUNION` |
| Falta estudiante o conciliador | `EN_ESPERA` |

`EN_ESPERA` se calcula automáticamente y no se cambia manualmente por endpoint.

## Asignación de estudiante

Reglas:

- requiere permiso `Gestionar conciliaciones` o `Concluir conciliaciones`;
- la conciliación debe estar activa;
- la conciliación no puede estar finalizada;
- la consulta asociada no puede estar cerrada ni archivada;
- el estudiante debe existir;
- el estudiante debe estar activo;
- el estudiante debe estar habilitado para conciliación;
- después de asignar, se recalcula estado.

Alcance:

- administrador puede asignar estudiante;
- conciliador asignado puede asignar estudiante.

## Asignación de conciliador

Reglas:

- requiere permiso `Gestionar conciliaciones`;
- solo administrador puede asignar conciliador;
- la conciliación debe estar activa;
- la conciliación no puede estar finalizada;
- la consulta asociada no puede estar cerrada ni archivada;
- el conciliador debe existir y estar activo;
- después de asignar, se recalcula estado.

## Cambio de estado operativo

Reglas:

- requiere `Gestionar conciliaciones` o `Concluir conciliaciones`;
- el estado recibido es obligatorio;
- el estado debe existir y estar activo;
- la conciliación debe estar activa;
- la conciliación no puede estar finalizada;
- la consulta asociada no puede estar cerrada ni archivada;
- no se permite cambiar al mismo estado;
- no se permite cambiar manualmente a `EN_ESPERA`;
- no se permite usar estados finales por endpoint de cambio de estado;
- `ESPERANDO_REUNION` exige estudiante y conciliador;
- `REUNION_PROGRAMADA` exige estudiante, conciliador y fecha de conciliación.

Alcance:

- administrador puede cambiar estado;
- conciliador asignado puede cambiar estado excepto devolver a `EN_ESPERA`.

## Finalización

Reglas:

- requiere `Gestionar conciliaciones` o `Concluir conciliaciones`;
- la conciliación debe estar activa;
- la conciliación no puede estar finalizada;
- la consulta asociada no puede estar cerrada ni archivada;
- el estado debe ser final;
- debe existir estudiante asignado;
- debe existir conciliador asignado;
- el acta PDF es obligatoria;
- el acta se guarda antes de cambiar estado;
- se registra `actaPath`;
- se registra `fechaFinalizacion`.

Estados finales:

```text
COMPLETO_CONCILIADO
COMPLETO_NO_CONCILIADO
```

## Reemplazo de solicitud

Reglas:

- requiere permiso `Gestionar conciliaciones`;
- solo administrador puede reemplazar solicitud;
- la conciliación debe estar activa;
- la conciliación no puede estar finalizada;
- la consulta asociada no puede estar cerrada ni archivada;
- la nueva solicitud PDF es obligatoria;
- se reemplaza el archivo en la ruta estándar.

## Desactivación lógica

Reglas:

- requiere permiso `Gestionar conciliaciones`;
- solo administrador puede desactivar;
- la conciliación debe estar activa;
- la conciliación no puede estar finalizada;
- la consulta asociada no puede estar cerrada ni archivada;
- se marca `activo=false`;
- desactivar no equivale a finalizar.

## Alcance

| Perfil | Regla |
|---|---|
| Administrador | Ve y gestiona conciliaciones según permisos. |
| Asesor | Ve o crea conciliaciones de consultas donde es asesor directo. |
| Monitor | Ve o crea conciliaciones de consultas donde es monitor directo. |
| Conciliador | Ve y opera conciliaciones donde está asignado. |
| Estudiante | Ve conciliaciones donde está asignado o donde es estudiante de la consulta. |

## Documentos

Rutas estándar:

| Documento | Ruta |
|---|---|
| Solicitud | `conciliacion/{id}/solicitud.pdf` |
| Acta | `conciliacion/{id}/acta.pdf` |

Reglas:

- los documentos se validan como PDF;
- el backend define la ruta;
- el frontend no construye rutas físicas del servidor.

## Relación con cierre de consulta

Una consulta no puede cerrarse si tiene conciliaciones activas en estados:

```text
EN_ESPERA
ESPERANDO_REUNION
REUNION_PROGRAMADA
```

## Reglas para frontend

- mostrar estados con `estadoNombre`;
- enviar cambios usando `estadoCodigo`;
- no intentar cambiar a `EN_ESPERA`;
- no usar cambio de estado para estados finales;
- usar finalización con acta para cierre;
- enviar solicitud y acta como `multipart/form-data`;
- manejar `403` como falta de permiso o alcance;
- usar `credentials: "include"`.
