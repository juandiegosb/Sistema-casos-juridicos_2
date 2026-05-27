# Decisión técnica - Permisos y alcance

## Contexto

El sistema maneja distintos tipos de usuarios internos: administrador, asesor, estudiante, monitor y conciliador.

Cada perfil puede tener permisos funcionales, pero no todos los usuarios con un permiso deben poder operar todos los recursos del sistema.

Ejemplo:

- un asesor puede ver consultas, pero solo las relacionadas con su alcance;
- un estudiante puede ver ciertos recursos, pero no gestionarlos;
- un conciliador puede concluir conciliaciones, pero solo aquellas donde está asignado.

## Decisión

El sistema separa dos conceptos:

```text
permiso funcional
alcance real
```

Un permiso habilita la acción general.  
El alcance determina si el usuario puede ejecutar esa acción sobre un recurso específico.

Regla central:

```text
autenticación + permiso funcional + alcance real + regla de negocio
```

## Justificación

La separación evita que un permiso amplio otorgue acceso global indebido.

También permite implementar reglas cercanas al mundo real del consultorio jurídico:

- cada asesor opera sus consultas o las relacionadas con sus estudiantes;
- cada monitor opera sus consultas;
- cada estudiante solo accede a recursos relacionados;
- cada conciliador opera conciliaciones asignadas;
- el administrador opera globalmente según permisos.

## Permisos funcionales

Los permisos se centralizan en:

```text
PermisoNombre
```

Ejemplos:

- `Ver consultas`;
- `Crear consultas`;
- `Editar consultas`;
- `Cambiar estado consultas`;
- `Ver seguimientos`;
- `Responder seguimientos`;
- `Ver procesos`;
- `Gestionar procesos`;
- `Ver conciliaciones`;
- `Gestionar conciliaciones`;
- `Concluir conciliaciones`.

## Permisos de navegación

También existen permisos de navegación.

Ejemplos:

- `Acceder inicio`;
- `Acceder consultas jurídicas`;
- `Acceder personas`;
- `Acceder conciliaciones`;
- `Acceder procesos`.

Estos permisos ayudan al frontend a mostrar u ocultar secciones.

No reemplazan la seguridad del backend.

## Alcance

El alcance se valida en services especializados.

Ejemplos de services:

- `ConsultaAccessService`;
- `ConsultaAlcanceService`;
- `ConciliacionAccessService`;
- `ConciliacionAlcanceService`;
- services equivalentes de otros módulos.

## Regla por perfil

### Administrador

Puede acceder globalmente según permisos asignados.

### Asesor

Accede a recursos relacionados con:

- consultas donde es asesor asignado;
- consultas donde el estudiante asignado pertenece a su asesoría;
- recursos derivados de esas consultas según módulo.

### Monitor

Accede a recursos relacionados con consultas donde es monitor asignado.

### Estudiante

Accede a recursos donde participa directamente o donde el módulo lo relaciona.

Ejemplos:

- consultas donde es estudiante asignado;
- seguimientos visibles para estudiante;
- conciliaciones donde es estudiante de consulta o estudiante asignado.

### Conciliador

Accede a conciliaciones donde está asignado como conciliador.

## Impacto en backend

El backend aplica seguridad en dos niveles:

### 1. Anotaciones de autorización

Ejemplo conceptual:

```text
@PreAuthorize("hasAuthority('Permiso')")
```

### 2. Services de acceso y alcance

Estos validan:

- permiso funcional;
- relación del usuario con el recurso;
- reglas especiales del perfil;
- restricciones del caso de uso.

## Impacto en frontend

El frontend puede:

- ocultar menús;
- ocultar botones;
- filtrar acciones visibles;
- guiar la experiencia del usuario.

Pero siempre debe manejar:

- `401 Unauthorized`;
- `403 Forbidden`;
- errores de negocio.

La UI no debe asumir que mostrar un botón garantiza autorización final.

## Decisión en conciliación

El rol conciliador no requiere permiso administrativo amplio para operar el flujo asignado.

La decisión es:

- `Gestionar conciliaciones`: acciones administrativas o amplias del módulo;
- `Concluir conciliaciones`: acciones operativas del conciliador asignado;
- alcance: solo conciliaciones donde el conciliador está asignado.

Esto evita que el conciliador gestione globalmente conciliaciones ajenas.

## Decisión en estudiante

El estudiante puede consultar recursos relacionados, pero no debe gestionar acciones críticas como:

- crear conciliaciones;
- asignarse a conciliaciones;
- cambiar estado de conciliación;
- finalizar conciliación;
- asignar responsables de consulta;
- cambiar estado de consulta.

## Ventajas

- reduce riesgo de acceso indebido;
- permite frontend flexible sin sacrificar seguridad;
- facilita pruebas por rol;
- mantiene reglas de negocio centralizadas en backend;
- evita depender únicamente de la interfaz.

## Criterios de mantenimiento

Cuando se agregue un permiso nuevo:

1. Agregarlo en `PermisoNombre`.
2. Verificar creación por inicializador de seguridad.
3. Asignarlo a roles correspondientes en base de datos.
4. Actualizar documentación de permisos.
5. Revisar frontend si afecta navegación.
6. Revisar endpoints si afecta `@PreAuthorize`.
7. Revisar access services si requiere alcance.

Cuando se agregue un nuevo perfil o regla de alcance:

1. Actualizar services de alcance.
2. Actualizar DTOs de usuario si cambia perfil.
3. Actualizar documentación de permisos y reglas.
4. Validar frontend de navegación.
