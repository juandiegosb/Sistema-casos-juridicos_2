# Reglas de negocio - Permisos y alcance

El sistema usa permisos funcionales, roles y reglas de alcance para proteger acciones y recursos.

## Principios

- La autenticación identifica al usuario.
- El rol agrupa permisos.
- El permiso habilita una acción funcional.
- El alcance define si el usuario puede ejecutar esa acción sobre un recurso específico.
- El backend valida permisos y alcance.
- El frontend puede ocultar acciones, pero no reemplaza la seguridad del backend.

## Roles base

| Rol | Tipo de perfil |
|---|---|
| Administrador | Administrativo. |
| Asesor | Asesor. |
| Estudiante | Estudiante. |
| Monitor | Monitor. |
| Conciliador | Conciliador. |

## Permisos de navegación

Controlan visibilidad de secciones del frontend.

Ejemplos:

- `Acceder inicio`;
- `Acceder recepción`;
- `Acceder nueva consulta`;
- `Acceder consultas jurídicas`;
- `Acceder administración`;
- `Acceder roles`;
- `Acceder estudiantes`;
- `Acceder asesores y monitores`;
- `Acceder personas`;
- `Acceder eliminación`;
- `Acceder conciliaciones`;
- `Acceder procesos`.

## Permisos funcionales

Controlan acciones concretas.

Ejemplos:

- `Ver personas`;
- `Crear personas`;
- `Editar personas`;
- `Cambiar estado personas`;
- `Ver consultas`;
- `Crear consultas`;
- `Editar consultas`;
- `Cambiar estado consultas`;
- `Archivar consultas`;
- `Asignar responsables consulta`;
- `Ver seguimientos`;
- `Crear seguimientos`;
- `Responder seguimientos`;
- `Aprobar respuestas de seguimiento`;
- `Ver procesos`;
- `Gestionar procesos`;
- `Ver conciliaciones`;
- `Gestionar conciliaciones`;
- `Concluir conciliaciones`.

## Inicialización de permisos

El backend centraliza nombres de permisos en `PermisoNombre`.

El inicializador de seguridad:

- crea permisos que existan en código y no estén en base de datos;
- crea roles base si no existen;
- no sobrescribe la matriz de asignación rol-permiso.

## Alcance por perfil

### Administrador

Accede de forma global según permisos asignados.

### Asesor

Accede a recursos relacionados con:

- consultas donde es asesor asignado;
- consultas donde el estudiante asignado pertenece a su asesoría;
- recursos derivados de esas consultas según módulo.

### Monitor

Accede a recursos relacionados con consultas donde es monitor asignado.

### Estudiante

Accede a recursos donde está asignado o relacionado, según reglas del módulo.

Ejemplos:

- consultas donde es estudiante asignado;
- seguimientos visibles para estudiante;
- conciliaciones donde es estudiante asignado o estudiante de la consulta.

### Conciliador

Accede a recursos de conciliación donde está asignado como conciliador.

## Permisos y reglas por módulo

### Consultas

Reglas:

- ver y buscar consultas requiere permiso de consulta;
- editar requiere permiso de edición;
- cambiar estado requiere permiso de cambio de estado;
- archivar requiere permiso de archivo y alcance administrativo;
- asignar responsables requiere permiso específico;
- estudiante no asigna responsables;
- estudiante no cambia estado.

### Seguimientos

Reglas:

- crear seguimiento requiere permiso de creación;
- estudiante y conciliador no crean seguimientos;
- responder seguimiento es acción propia de estudiante;
- revisar respuestas requiere permiso de aprobación;
- estudiante y conciliador no revisan respuestas.

### Procesos

Reglas:

- ver procesos requiere permiso de visualización;
- gestionar procesos requiere permiso de gestión;
- estudiante y conciliador no gestionan procesos;
- el alcance se hereda desde la consulta.

### Conciliaciones

Reglas:

- ver conciliaciones requiere permiso de visualización;
- crear requiere permiso de gestión y alcance sobre la consulta;
- asignar conciliador es acción administrativa;
- asignar estudiante puede realizarla administrador o conciliador asignado;
- concluir o finalizar requiere permiso y alcance;
- estudiante solo consulta conciliaciones relacionadas.

### Archivos

Reglas:

- endpoints genéricos requieren autenticación;
- reglas de negocio se validan en el módulo que usa el archivo;
- el backend controla rutas y almacenamiento.

## Frontend

Reglas:

- puede ocultar menús según permisos;
- puede ocultar botones según permisos;
- debe manejar `403` como falta de permiso o alcance;
- no debe asumir que ver un botón garantiza autorización;
- siempre debe respetar respuestas del backend.

## Regla central

```text
autenticación + permiso funcional + alcance real + regla de negocio
```

Una acción solo es válida cuando los cuatro elementos se cumplen según corresponda.
