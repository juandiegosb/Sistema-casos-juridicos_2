# Decisión técnica - Módulo de conciliación

## Contexto

El módulo de conciliación se implementó para permitir generar conciliaciones desde consultas jurídicas, asignar responsables, controlar estado funcional y finalizar con soporte documental.

El módulo debía integrarse con consulta, perfiles, permisos, archivos y cierre de casos.

## Decisiones principales

1. Conciliación nace desde una consulta.
2. La consulta es la fuente de consultante, partes y contrapartes.
3. El estado de conciliación se administra en tabla.
4. Las reglas se validan por código técnico de estado.
5. La solicitud PDF se guarda al crear conciliación.
6. La finalización exige acta PDF.
7. El estudiante puede consultar conciliaciones relacionadas, pero no gestionarlas.
8. El conciliador opera solo conciliaciones asignadas.
9. El cierre de consulta se bloquea si hay conciliación pendiente.
10. `activo` no reemplaza el estado funcional.

## Conciliación nace desde consulta

La conciliación no se crea como recurso aislado.

Se crea desde:

```text
/api/conciliaciones/consulta/{consultaId}
```

Justificación:

- permite heredar contexto jurídico;
- evita duplicar persona principal, partes y contrapartes;
- mantiene trazabilidad con la consulta origen;
- permite validar asesor, monitor, estudiante y alcance.

## Consulta como fuente de partes y contrapartes

La conciliación no almacena partes ni contrapartes propias.

El detalle toma estos datos desde la consulta.

Justificación:

- evita duplicidad;
- mantiene consistencia;
- refleja cambios de consulta en el detalle;
- reduce riesgo de datos divergentes.

## Tabla `estado_conciliacion`

Se decidió usar tabla para estados de conciliación.

Campos:

- `codigo`;
- `nombre`;
- `activo`;
- `orden`.

Justificación:

- el frontend puede mostrar `nombre`;
- el backend valida con `codigo`;
- se evita depender de textos visibles;
- se facilita orden de presentación;
- se permite administración controlada del catálogo.

## Validación por código técnico

El backend no valida por `nombre`.

Valida por:

```text
EN_ESPERA
ESPERANDO_REUNION
REUNION_PROGRAMADA
COMPLETO_CONCILIADO
COMPLETO_NO_CONCILIADO
```

Justificación:

- el nombre visible puede cambiar;
- el código técnico es estable;
- se evitan errores por mayúsculas, tildes o redacción;
- las reglas de negocio permanecen consistentes.

## Normalización de estado recibido

El backend normaliza el parámetro de estado.

Ejemplos equivalentes:

```text
completo conciliado
COMPLETO-CONCILIADO
COMPLETO_CONCILIADO
```

Justificación:

- mejora tolerancia del contrato;
- reduce errores por formato;
- conserva código técnico normalizado.

## Solicitud PDF al crear

La creación requiere solicitud PDF.

Ruta estándar:

```text
conciliacion/{id}/solicitud.pdf
```

Justificación:

- la conciliación nace con soporte documental;
- el backend controla ruta y nombre del archivo;
- se evita depender del nombre original del archivo;
- se facilita reemplazo controlado por administrador.

## Acta PDF al finalizar

La finalización exige acta PDF.

Ruta estándar:

```text
conciliacion/{id}/acta.pdf
```

Justificación:

- el cierre de conciliación queda soportado documentalmente;
- evita estados finales sin soporte;
- mejora trazabilidad jurídica;
- permite verificar resultado conciliado o no conciliado.

## Separación de cambio de estado y finalización

Estados no finales se cambian mediante:

```text
PATCH /api/conciliaciones/{id}/estado
```

Estados finales se aplican mediante:

```text
POST /api/conciliaciones/{id}/finalizar
```

Justificación:

- finalizar requiere acta;
- evita que un usuario cierre conciliación solo cambiando estado;
- mantiene regla documental asociada al cierre.

## `EN_ESPERA` automático

`EN_ESPERA` no se asigna manualmente.

El backend lo calcula cuando falta estudiante o conciliador.

Justificación:

- representa falta de responsables mínimos;
- evita manipulación manual del estado;
- se recalcula al asignar estudiante o conciliador.

## Autoasignación de estudiante

Regla:

1. usar estudiante de la consulta si está activo y habilitado para conciliación;
2. si no aplica, seleccionar estudiante habilitado con menor carga;
3. desempatar por nombre e id.

Justificación:

- respeta responsable de la consulta cuando es apto;
- distribuye carga cuando debe asignarse otro estudiante;
- evita selección aleatoria.

## Autoasignación de conciliador

Regla:

- seleccionar conciliador activo con menor carga de conciliaciones no finalizadas;
- desempatar por nombre e id.

Justificación:

- balancea carga;
- evita asignaciones arbitrarias;
- facilita defensa del criterio de selección.

## Estudiante solo consulta

El estudiante puede ver conciliaciones relacionadas.

No puede:

- crear conciliaciones;
- asignarse;
- cambiar estado;
- finalizar;
- subir acta;
- reemplazar solicitud;
- desactivar.

Justificación:

- protege el flujo frente a autogestión indebida;
- mantiene control en asesor, monitor, administrador y conciliador asignado;
- se ajusta al contexto universitario del consultorio jurídico.

## Conciliador asignado

El conciliador puede operar solo conciliaciones donde está asignado.

Puede según permisos y alcance:

- consultar;
- asignar estudiante;
- cambiar estado operativo;
- finalizar con acta.

No puede:

- crear conciliaciones globalmente;
- asignar conciliador;
- reemplazar solicitud;
- desactivar conciliación;
- operar conciliaciones ajenas.

Justificación:

- separa rol operativo de rol administrativo;
- evita acceso global indebido;
- mantiene responsabilidad sobre casos asignados.

## Integración con cierre de consulta

Una consulta no puede cerrar si tiene conciliaciones activas no finalizadas.

Estados bloqueantes:

```text
EN_ESPERA
ESPERANDO_REUNION
REUNION_PROGRAMADA
```

Justificación:

- evita cerrar consulta con conciliación pendiente;
- conserva coherencia entre módulos;
- mantiene trazabilidad de cierre.

## `activo` en conciliación

`activo=false` representa desactivación lógica.

No representa finalización.

Justificación:

- finalización tiene estados propios;
- la desactivación es una acción administrativa;
- se conserva diferencia entre ciclo funcional y disponibilidad operativa.

## Impacto en frontend

El frontend debe:

- mostrar `estadoNombre`;
- enviar `estadoCodigo`;
- usar multipart para solicitud y acta;
- usar endpoint de finalización para estados finales;
- manejar `403` como falta de permiso o alcance;
- no depender solo de botones ocultos para seguridad.

## Criterios de mantenimiento

Si cambia una regla de conciliación, revisar:

- `doc/backend/conciliaciones.md`;
- `doc/api/conciliaciones.md`;
- `doc/reglas/conciliaciones.md`;
- `doc/base-datos/estados-y-catalogos.md`;
- este documento.

Si cambia el catálogo de estados, revisar también:

- scripts de base de datos;
- datos iniciales;
- frontend que renderiza estados;
- validaciones del backend.
