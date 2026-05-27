# Permisos, roles y alcance

El sistema usa permisos funcionales y reglas de alcance para controlar acciones y visibilidad.

## Conceptos

### Rol

Agrupa permisos funcionales y está asociado a un tipo de perfil.

Roles base:

- Administrador;
- Asesor;
- Estudiante;
- Monitor;
- Conciliador.

### Permiso

Representa una acción o acceso funcional.

Ejemplos:

- Ver consultas.
- Crear consultas.
- Cambiar estado consultas.
- Ver conciliaciones.
- Gestionar conciliaciones.
- Concluir conciliaciones.
- Acceder inicio.

### Alcance

Define si un usuario puede actuar sobre un recurso específico.

Ejemplo:

Un usuario puede tener permiso para ver conciliaciones, pero solo verá conciliaciones dentro de su alcance.

## Inicialización de permisos

`SecurityDataInitializer` crea permisos declarados en `PermisoNombre` cuando no existen.

También crea roles base cuando faltan.

La matriz de asignación rol-permiso se administra desde base de datos o desde el módulo correspondiente.

## Permisos de navegación

Controlan acceso visual a secciones del frontend.

Ejemplos:

- Acceder inicio.
- Acceder recepción.
- Acceder nueva consulta.
- Acceder consultas jurídicas.
- Acceder administración.
- Acceder roles.
- Acceder estudiantes.
- Acceder asesores y monitores.
- Acceder personas.
- Acceder eliminación.
- Acceder conciliaciones.
- Acceder procesos.

## Permisos funcionales

Controlan acciones sobre módulos.

Ejemplos:

- Ver personas.
- Crear personas.
- Editar personas.
- Cambiar estado personas.
- Ver consultas.
- Crear consultas.
- Editar consultas.
- Cambiar estado consultas.
- Archivar consultas.
- Ver seguimientos.
- Crear seguimientos.
- Responder seguimientos.
- Aprobar respuestas de seguimiento.
- Ver procesos.
- Gestionar procesos.
- Ver conciliaciones.
- Gestionar conciliaciones.
- Concluir conciliaciones.

## Frontend

El frontend declara permisos en:

```text
src/lib/permission.js
```

La navegación usa permisos en:

```text
PermissionSidebar
```

El frontend filtra menús por permisos, pero no reemplaza las validaciones del backend.

## Backend

El backend usa permisos en controllers y services mediante:

- `@PreAuthorize`;
- services de acceso;
- helpers de usuario actual;
- authorities cargadas desde el token.

## Reglas de alcance

### Administrador

Puede operar recursos globales según permisos asignados.

### Asesor

Opera recursos asociados a consultas donde participa como asesor según reglas del módulo.

### Monitor

Opera recursos asociados a consultas donde participa como monitor según reglas del módulo.

### Estudiante

Consulta y actúa sobre recursos asociados a su perfil según reglas de cada módulo.

### Conciliador

Opera conciliaciones donde está asignado como conciliador.

## Conciliaciones

Permisos relevantes:

| Permiso | Uso |
|---|---|
| Acceder conciliaciones | Navegación hacia la sección de conciliaciones. |
| Ver conciliaciones | Consulta de conciliaciones visibles para el usuario. |
| Gestionar conciliaciones | Acciones administrativas del módulo según reglas de acceso. |
| Concluir conciliaciones | Operación de cierre de conciliación según alcance. |
| Programar reuniones de conciliación | Acciones relacionadas con reuniones de conciliación. |
| Reprogramar reuniones de conciliación | Acciones relacionadas con reprogramación de reuniones. |

Reglas de alcance:

- administrador gestiona de forma global según permisos;
- asesor crea y consulta conciliaciones de consultas donde es asesor directo;
- monitor crea y consulta conciliaciones de consultas donde es monitor directo;
- conciliador consulta y opera conciliaciones donde está asignado;
- estudiante consulta conciliaciones donde está asignado o donde es estudiante responsable de la consulta.

## Regla central

El frontend puede ocultar acciones, pero el backend siempre valida:

```text
permiso funcional + alcance real + regla de negocio
```
