# Decisión técnica - Estado funcional vs activo lógico

## Contexto

El sistema maneja recursos con ciclos de vida operativos y también necesita conservar información histórica.

Algunos recursos tienen estados funcionales, como consulta, seguimiento, proceso y conciliación. Otros recursos usan `activo` para indicar si están disponibles o desactivados.

## Decisión

El sistema separa:

```text
estado funcional
activo lógico
```

El estado funcional representa el ciclo de vida del recurso.

El activo lógico representa disponibilidad o desactivación sin eliminación física.

## Definiciones

### Estado funcional

Representa el estado del proceso de negocio.

Ejemplos:

- `Consulta.estado`;
- `Proceso.estado`;
- `Seguimiento.estado`;
- `SeguimientoRespuesta.estado`;
- `Conciliacion.estado`.

### Activo lógico

Representa si un registro está disponible para operación o fue desactivado.

Ejemplos:

- `activo` en perfiles;
- `activo` en catálogos;
- `activo` en procesos;
- `activo` en seguimientos;
- `activo` en conciliaciones.

## Justificación

Separar estos conceptos evita mezclar decisiones funcionales con borrado lógico.

Ejemplo incorrecto:

```text
Desactivar una conciliación para indicar que terminó.
```

Ejemplo correcto:

```text
La conciliación termina con estado final.
activo=false solo representa desactivación lógica.
```

## Consulta

Consulta usa estados:

```text
PENDIENTE
ACTIVO
EN_PROCESO
URGENTE
CERRADO
ARCHIVADO
```

Reglas:

- `CERRADO` representa cierre operativo;
- `ARCHIVADO` representa archivo histórico;
- no se usa `activo=false` para archivar consulta;
- los listados operativos filtran consultas archivadas;
- desarchivar devuelve a `CERRADO`.

## Proceso

Proceso usa:

```text
estado
activo
```

Reglas:

- `estado` representa resultado o etapa del proceso;
- `activo` representa disponibilidad lógica;
- un proceso `PENDIENTE` y activo bloquea cierre de consulta;
- desactivar proceso no equivale a finalizarlo.

## Seguimiento

Seguimiento usa:

```text
estado
activo
```

Reglas:

- `estado=PENDIENTE` representa actividad operativa pendiente;
- `estado=COMPLETADO` o `CANCELADO` representa cierre del seguimiento;
- `activo=false` representa desactivación lógica;
- seguimientos pendientes y activos bloquean cierre de consulta.

## Respuesta de seguimiento

Respuesta usa:

```text
estado
activo
```

Reglas:

- `PENDIENTE` representa respuesta pendiente de revisión;
- `APROBADA` y `RECHAZADA` representan decisión de revisión;
- `activo=false` representa desactivación lógica;
- respuestas pendientes y activas bloquean cierre de consulta.

## Conciliación

Conciliación usa:

```text
estado_id
activo
```

Reglas:

- `estado_id` apunta al catálogo `estado_conciliacion`;
- el estado representa flujo de conciliación;
- `activo=false` representa desactivación lógica;
- estados no finalizados bloquean cierre de consulta;
- estados finales no bloquean cierre.

## Catálogos

Catálogos usan `activo`.

Ejemplos:

- área;
- tema;
- tipo;
- sede;
- municipio;
- barrio;
- nacionalidad;
- tipo de documento;
- órgano de control;
- especialidad;
- categoría de seguimiento;
- estado de conciliación.

Regla:

```text
activo=false oculta el registro de flujos operativos, pero conserva historia.
```

## Perfiles

Perfiles usan `activo`.

Ejemplos:

- asesor;
- estudiante;
- monitor;
- administrativo;
- conciliador.

Regla:

```text
activo=false evita uso operativo del perfil.
```

## Impacto en endpoints

### PUT

Actualiza datos generales.

No debe cambiar estados funcionales ni activos lógicos cuando el módulo tiene endpoints específicos.

### PATCH

Se usa para:

- cambiar estado funcional;
- cambiar activo lógico;
- archivar/desarchivar;
- acciones específicas de ciclo de vida.

### DELETE

Se usa como desactivación lógica o archivo lógico cuando el dominio lo define.

## Ventajas

- conserva historial;
- evita pérdida de relaciones;
- mejora trazabilidad;
- facilita reglas de cierre;
- evita mezclar operación con eliminación;
- mantiene consistencia en módulos.

## Criterios de mantenimiento

Cuando se agregue un nuevo recurso:

1. Definir si necesita estado funcional.
2. Definir si necesita activo lógico.
3. Evitar usar `activo=false` como reemplazo de estado funcional.
4. Crear endpoints específicos para cambios de ciclo de vida.
5. Documentar qué estados bloquean operaciones.
6. Documentar si el recurso participa en cierre de consulta.

## Regla final

```text
estado explica qué está pasando funcionalmente.
activo explica si el registro está disponible para operación.
```
