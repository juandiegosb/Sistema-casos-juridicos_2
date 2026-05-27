# Backend - Perfiles

El módulo de perfiles administra perfiles internos asociados a usuarios del sistema.

Los perfiles representan actores operativos del consultorio jurídico y se integran con autenticación, permisos, consultas, seguimientos, procesos y conciliaciones.

## Paquetes principales

```text
business/controller/perfil
business/dto/perfil
business/model/perfil
business/repository/perfil
business/service/perfil
business/service/acceso/perfil
```

## Perfiles administrados

| Perfil | Entidad | Controller | Endpoint base |
|---|---|---|---|
| Administrativo | `Administrativo` | `AdministrativoController` | `/api/administrativos` |
| Asesor | `Asesor` | `AsesorController` | `/api/asesores` |
| Monitor | `Monitor` | `MonitorController` | `/api/monitores` |
| Estudiante | `Estudiante` | `EstudianteController` | `/api/estudiantes` |
| Conciliador | `Conciliador` | `ConciliadorController` | `/api/conciliadores` |

## Relación con usuarios del sistema

Cada perfil puede asociarse a un `UsuarioSistema`.

Relación general:

```text
Perfil interno -> UsuarioSistema -> Rol -> Permisos
```

Las entidades de perfil tienen relación `@OneToOne` con `UsuarioSistema`.

El flujo de creación guarda primero el perfil y luego crea el usuario del sistema correspondiente mediante `UsuarioSistemaRegistroService`.

## Campos comunes

Los perfiles internos comparten campos principales:

| Campo | Uso |
|---|---|
| `id` | Identificador del perfil. |
| `usuarioSistema` | Usuario del sistema asociado. |
| `nombre` | Nombre del perfil. |
| `tipoDocumento` | Tipo de documento. |
| `documento` | Número de documento único. |
| `email` | Correo único. |
| `telefono` | Teléfono único. |
| `usuario` | Nombre de usuario único. |
| `codigo` | Código institucional único. |
| `sede` | Sede asociada. |
| `activo` | Estado activo/inactivo. |

## Campos específicos por perfil

| Perfil | Campos específicos |
|---|---|
| Administrativo | `directora`. |
| Asesor | `area`. |
| Estudiante | `asesor`, `conciliacion`. |
| Conciliador | `tipoConciliador`. |
| Monitor | No agrega campos específicos adicionales a los comunes. |

## DTOs

| DTO | Campos específicos | Validaciones principales |
|---|---|---|
| `AdministrativoDTO` | `directora` | nombre, tipoDocumentoId, documento, email, teléfono, usuario, código, sede. |
| `AsesorDTO` | `areaId` | nombre, tipoDocumentoId, documento, email, teléfono, usuario, código, sede, área. |
| `MonitorDTO` | - | nombre, tipoDocumentoId, documento, email, teléfono, usuario, código, sede. |
| `EstudianteDTO` | `asesorId`, `conciliacion` | nombre, tipoDocumentoId, documento, email, teléfono, usuario, código, sede, asesor. |
| `ConciliadorDTO` | `tipoConciliador` | nombre, tipoDocumentoId, documento, email, teléfono, usuario, código, sede, tipo de conciliador. |

## Servicios por perfil

Cada perfil tiene una estructura similar:

| Componente | Responsabilidad |
|---|---|
| Service fachada | Expone métodos del módulo y delega. |
| CommandService | Crea, actualiza, cambia estado y elimina lógicamente. |
| QueryService | Lista, lista activos y consulta por id. |
| Validator | Valida campos, duplicados, cambios y estados. |
| Mapper | Convierte entidad a DTO y aplica datos. |
| AccessService | Valida permisos y reglas de acceso. |
| Repository | Consulta y persiste datos. |

## Normalización

Los CommandService normalizan:

- nombre;
- documento;
- email;
- teléfono;
- usuario;
- código.

La normalización evita diferencias superficiales y mejora validaciones de duplicados.

## Reglas comunes de creación

Al crear un perfil:

- no se permite enviar `id`;
- se normalizan datos principales;
- se validan campos obligatorios;
- se validan relaciones activas;
- se validan duplicados;
- se guarda el perfil;
- se crea el `UsuarioSistema` asociado;
- se vincula el perfil guardado con el usuario del sistema.

## Reglas comunes de actualización

Al actualizar:

- se valida que el `id` del DTO no cambie;
- se cargan relaciones activas;
- se normalizan datos;
- se validan duplicados excluyendo el mismo registro;
- se valida que existan cambios efectivos;
- se actualiza el perfil sin modificar campos de control que tienen endpoints específicos.

## Reglas comunes de estado

La desactivación se realiza como eliminación lógica mediante `activo=false`.

El backend conserva los perfiles porque pueden estar relacionados con:

- usuarios del sistema;
- consultas;
- seguimientos;
- procesos;
- conciliaciones.

## Duplicados

Cada perfil valida duplicados por:

- documento;
- email;
- teléfono;
- usuario;
- código.

En actualización se excluye el registro actual.

## Perfiles y catálogos relacionados

| Perfil | Relaciones requeridas |
|---|---|
| Administrativo | Tipo de documento activo, sede activa. |
| Asesor | Tipo de documento activo, sede activa, área activa. |
| Monitor | Tipo de documento activo, sede activa. |
| Estudiante | Tipo de documento activo, sede activa, asesor activo. |
| Conciliador | Tipo de documento activo, sede activa, tipo de conciliador. |

## Administrativo

### Entidad

```text
Administrativo
```

Campos específicos:

- `directora`.

### Reglas específicas

La gestión de administrativos tiene una regla especial:

- el usuario debe tener rol administrador;
- además, el perfil administrativo actual debe estar marcado como directora.

Esta regla se valida en `AdministrativoAccessService`.

### Endpoints

Base path:

```text
/api/administrativos
```

| Método | Ruta | Permiso/Regla | Uso |
|---|---|---|---|
| GET | `/api/administrativos` | Administrador | Lista administrativos. |
| GET | `/api/administrativos/activos` | Administrador | Lista administrativos activos. |
| GET | `/api/administrativos/directoras` | Administrador | Lista administrativos directores activos. |
| GET | `/api/administrativos/{id}` | Administrador | Consulta administrativo por id. |
| POST | `/api/administrativos` | Gestión de administradores | Crea administrativo. |
| PUT | `/api/administrativos/{id}` | Gestión de administradores | Actualiza administrativo. |
| PATCH | `/api/administrativos/{id}/activo` | Gestión de administradores | Cambia estado activo. |
| PATCH | `/api/administrativos/{id}/directora` | Gestión de administradores | Cambia marca de directora. |
| DELETE | `/api/administrativos/{id}` | Gestión de administradores | Desactiva administrativo. |

## Asesor

### Entidad

```text
Asesor
```

Campos específicos:

- `area`.

### Reglas específicas

El asesor pertenece a un área activa.  
Puede relacionarse con estudiantes, consultas y reglas de alcance de otros módulos.

### Endpoints

Base path:

```text
/api/asesores
```

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/asesores` | `Ver asesores y monitores`, `Gestionar asesores y monitores` o `Gestionar usuarios` | Lista asesores. |
| GET | `/api/asesores/activos` | `Ver perfiles auxiliares`, `Ver asesores y monitores`, `Gestionar asesores y monitores` o `Gestionar usuarios` | Lista asesores activos. |
| GET | `/api/asesores/{id}` | `Ver asesores y monitores`, `Gestionar asesores y monitores` o `Gestionar usuarios` | Consulta asesor por id. |
| POST | `/api/asesores` | `Gestionar asesores y monitores` o `Gestionar usuarios` | Crea asesor. |
| PUT | `/api/asesores/{id}` | `Gestionar asesores y monitores` o `Gestionar usuarios` | Actualiza asesor. |
| PATCH | `/api/asesores/{id}/activo` | `Gestionar asesores y monitores` o `Gestionar usuarios` | Cambia estado activo. |
| DELETE | `/api/asesores/{id}` | `Gestionar asesores y monitores` o `Gestionar usuarios` | Desactiva asesor. |

## Monitor

### Entidad

```text
Monitor
```

El monitor participa en flujos de consulta y seguimiento según reglas de otros módulos.

### Endpoints

Base path:

```text
/api/monitores
```

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/monitores` | `Ver asesores y monitores`, `Gestionar asesores y monitores` o `Gestionar usuarios` | Lista monitores. |
| GET | `/api/monitores/activos` | `Ver perfiles auxiliares`, `Ver asesores y monitores`, `Gestionar asesores y monitores` o `Gestionar usuarios` | Lista monitores activos. |
| GET | `/api/monitores/{id}` | `Ver asesores y monitores`, `Gestionar asesores y monitores` o `Gestionar usuarios` | Consulta monitor por id. |
| POST | `/api/monitores` | `Gestionar asesores y monitores` o `Gestionar usuarios` | Crea monitor. |
| PUT | `/api/monitores/{id}` | `Gestionar asesores y monitores` o `Gestionar usuarios` | Actualiza monitor. |
| PATCH | `/api/monitores/{id}/activo` | `Gestionar asesores y monitores` o `Gestionar usuarios` | Cambia estado activo. |
| DELETE | `/api/monitores/{id}` | `Gestionar asesores y monitores` o `Gestionar usuarios` | Desactiva monitor. |

## Estudiante

### Entidad

```text
Estudiante
```

Campos específicos:

- `asesor`;
- `conciliacion`.

### Reglas específicas

El estudiante pertenece a un asesor activo.

El campo `conciliacion` indica si el estudiante está habilitado para asignaciones relacionadas con conciliación.

La gestión real de estudiantes queda restringida a administrador mediante `EstudianteAccessService`.

### Alcance de consulta

Los listados de estudiantes aplican alcance:

- administrador consulta todos;
- asesor consulta estudiantes asociados a su perfil;
- consulta por asesor valida que el asesor actual corresponda cuando no es administrador.

### Endpoints

Base path:

```text
/api/estudiantes
```

| Método | Ruta | Permiso/Regla | Uso |
|---|---|---|---|
| GET | `/api/estudiantes` | `Ver estudiantes`, `Ver perfiles auxiliares` o `Gestionar usuarios` | Lista estudiantes según alcance. |
| GET | `/api/estudiantes/activos` | `Ver estudiantes`, `Ver perfiles auxiliares` o `Gestionar usuarios` | Lista estudiantes activos según alcance. |
| GET | `/api/estudiantes/conciliacion` | `Ver estudiantes`, `Ver perfiles auxiliares` o `Gestionar usuarios` | Lista estudiantes activos habilitados para conciliación. |
| GET | `/api/estudiantes/activos/asesor/{asesorId}` | Permiso de consulta y alcance por asesor | Lista estudiantes activos de un asesor. |
| GET | `/api/estudiantes/{id}` | Permiso de consulta y alcance | Consulta estudiante por id. |
| POST | `/api/estudiantes` | `Crear usuarios` o `Gestionar usuarios`, con gestión restringida | Crea estudiante. |
| PUT | `/api/estudiantes/{id}` | `Editar usuarios` o `Gestionar usuarios`, con gestión restringida | Actualiza estudiante. |
| PATCH | `/api/estudiantes/{id}/activo` | `Cambiar estado estudiantes` o `Gestionar usuarios`, con gestión restringida | Cambia estado activo. |
| PATCH | `/api/estudiantes/{id}/conciliacion` | `Editar usuarios` o `Gestionar usuarios`, con gestión restringida | Cambia habilitación para conciliación. |
| DELETE | `/api/estudiantes/{id}` | `Gestionar usuarios` | Desactiva estudiante. |

## Conciliador

### Entidad

```text
Conciliador
```

Campos específicos:

- `tipoConciliador`.

El tipo de conciliador se maneja como enum:

```text
TipoConciliador
```

### Reglas específicas

El conciliador puede quedar asociado a conciliaciones.  
Por esto, la eliminación se implementa como desactivación lógica.

### Endpoints

Base path:

```text
/api/conciliadores
```

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/conciliadores` | `Ver conciliadores`, `Gestionar conciliadores` o `Gestionar usuarios` | Lista conciliadores. |
| GET | `/api/conciliadores/activos` | `Ver perfiles auxiliares`, `Ver conciliadores`, `Gestionar conciliadores` o `Gestionar usuarios` | Lista conciliadores activos. |
| GET | `/api/conciliadores/{id}` | `Ver conciliadores`, `Gestionar conciliadores` o `Gestionar usuarios` | Consulta conciliador por id. |
| POST | `/api/conciliadores` | `Gestionar conciliadores` o `Gestionar usuarios` | Crea conciliador. |
| PUT | `/api/conciliadores/{id}` | `Gestionar conciliadores` o `Gestionar usuarios` | Actualiza conciliador. |
| PATCH | `/api/conciliadores/{id}/activo` | `Gestionar conciliadores` o `Gestionar usuarios` | Cambia estado activo. |
| DELETE | `/api/conciliadores/{id}` | `Gestionar conciliadores` o `Gestionar usuarios` | Desactiva conciliador. |

## Usuario del sistema asociado

La creación de perfiles crea también un usuario del sistema mediante `UsuarioSistemaRegistroService`.

Métodos usados según perfil:

| Perfil | Método |
|---|---|
| Administrativo | `crearParaAdministrativo` |
| Asesor | `crearParaAsesor` |
| Monitor | `crearParaMonitor` |
| Estudiante | `crearParaEstudiante` |
| Conciliador | `crearParaConciliador` |

## Repositories

Los repositories de perfiles permiten:

- validar duplicados;
- listar activos;
- buscar por id activo;
- buscar por `usuarioSistema`;
- consultar destinatarios para notificaciones cuando aplica.

Campos usados para duplicados:

- documento;
- email;
- teléfono;
- usuario;
- código.

## Consideraciones para frontend

- Usar listados activos para combos.
- Usar endpoints generales para administración.
- No enviar `id` en creación.
- No modificar campos de estado desde endpoints de actualización general.
- Para estudiantes, usar `/conciliacion` cuando se requieran estudiantes habilitados para conciliación.
- Para estudiantes asociados a asesor, usar los endpoints por asesor.
- Manejar errores de negocio cuando no existan cambios, existan duplicados o el usuario no tenga alcance.
- Usar `credentials: "include"` en peticiones protegidas.
