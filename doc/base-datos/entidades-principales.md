# Entidades principales

Este documento describe las entidades persistentes principales del sistema y sus relaciones.

La estructura está organizada por dominios funcionales.

## 1. Seguridad y acceso

### `usuario_sistema`

Entidad:

```text
UsuarioSistema
```

Propósito:

Representa al usuario autenticable del sistema.

Campos principales:

| Columna | Uso |
|---|---|
| `id` | Identificador. |
| `username` | Usuario de acceso. Corresponde al correo usado para login. |
| `password_hash` | Contraseña cifrada. |
| `activo` | Estado lógico del usuario. |
| `tipo_perfil_actual` | Tipo de perfil real activo. |
| `rol_id` | Rol asignado. |

Relaciones:

```text
usuario_sistema -> rol
usuario_sistema -> perfil real activo según tipo_perfil_actual
```

Tipos de perfil:

```text
ASESOR
ESTUDIANTE
MONITOR
ADMINISTRATIVO
CONCILIADOR
```

### `rol`

Entidad:

```text
Rol
```

Propósito:

Agrupa permisos funcionales y se asocia a un tipo de perfil.

Campos:

| Columna | Uso |
|---|---|
| `id` | Identificador. |
| `nombre` | Nombre único del rol. |
| `descripcion` | Descripción funcional. |
| `activo` | Estado lógico. |
| `tipo_perfil` | Tipo de perfil al que pertenece el rol. |

Relaciones:

```text
rol -> rol_permiso -> permiso
usuario_sistema -> rol
```

### `permiso`

Entidad:

```text
Permiso
```

Propósito:

Representa acciones, accesos o capacidades funcionales.

Campos:

| Columna | Uso |
|---|---|
| `id` | Identificador. |
| `nombre` | Nombre único del permiso. |
| `descripcion` | Descripción funcional. |
| `activo` | Estado lógico. |

### `rol_permiso`

Tabla de relación:

```text
rol_permiso
```

Propósito:

Relaciona roles con permisos.

Relaciones:

```text
rol_permiso.rol_id -> rol.id
rol_permiso.permiso_id -> permiso.id
```

### `usuario_cambio_perfil_historial`

Entidad:

```text
UsuarioCambioPerfilHistorial
```

Propósito:

Conserva historial de cambios de perfil real de un usuario del sistema.

Uso funcional:

- trazabilidad de cambio de perfil;
- registro de perfil anterior y nuevo;
- registro de motivo;
- auditoría operativa de cambios de identidad funcional.

### `password_reset_token`

Entidad:

```text
PasswordResetToken
```

Propósito:

Administra tokens de recuperación de contraseña.

Reglas generales:

- no almacena el token plano;
- guarda hash del token;
- controla expiración;
- controla si fue usado.

## 2. Catálogos generales

### `area`

Entidad:

```text
Area
```

Propósito:

Agrupa temas jurídicos.

Campos:

| Columna | Uso |
|---|---|
| `id` | Identificador. |
| `nombre` | Nombre único del área. |
| `activo` | Estado lógico. |

Relaciones:

```text
area -> tema
area -> asesor
area -> consulta
```

### `tema`

Entidad:

```text
Tema
```

Propósito:

Clasifica la consulta dentro de un área.

Campos:

| Columna | Uso |
|---|---|
| `id` | Identificador. |
| `nombre` | Nombre del tema. |
| `activo` | Estado lógico. |
| `area_id` | Área asociada. |

Relaciones:

```text
tema -> area
tema -> tipo
tema -> consulta
```

### `tipo`

Entidad:

```text
Tipo
```

Propósito:

Clasifica la consulta dentro de un tema.

Campos:

| Columna | Uso |
|---|---|
| `id` | Identificador. |
| `nombre` | Nombre del tipo. |
| `activo` | Estado lógico. |
| `tema_id` | Tema asociado. |

Relaciones:

```text
tipo -> tema
tipo -> consulta
```

### `departamento`

Entidad:

```text
Departamento
```

Propósito:

Catálogo territorial para municipios y procesos.

Relaciones:

```text
departamento -> municipio
departamento -> proceso
```

### `municipio`

Entidad:

```text
Municipio
```

Propósito:

Catálogo territorial asociado a departamento.

Relaciones:

```text
municipio -> departamento
municipio -> barrio
municipio -> persona
```

### `barrio`

Entidad:

```text
Barrio
```

Propósito:

Catálogo territorial asociado a municipio.

Relaciones:

```text
barrio -> municipio
barrio -> persona
```

### `sede`

Entidad:

```text
Sede
```

Propósito:

Representa sedes del sistema y se relaciona con perfiles y consultas.

Relaciones:

```text
sede -> consulta
sede -> administrativo
sede -> asesor
sede -> estudiante
sede -> monitor
sede -> conciliador
```

### `tipodoc`

Entidad:

```text
TipoDocumento
```

Propósito:

Catálogo de tipos de documento para perfiles y usuarios internos.

Relaciones:

```text
tipodoc -> administrativo
tipodoc -> asesor
tipodoc -> estudiante
tipodoc -> monitor
tipodoc -> conciliador
```

### `nacionalidades`

Entidad:

```text
Nacionalidad
```

Propósito:

Catálogo de nacionalidades usado por persona.

Relación:

```text
nacionalidades -> persona
```

## 3. Personas y catálogos de persona

### `persona`

Entidad:

```text
Persona
```

Propósito:

Almacena información de personas relacionadas con consultas jurídicas y otros flujos.

Bloques de información:

| Bloque | Campos principales |
|---|---|
| Identificación | tipo de persona, tipo de documento textual, número de documento, fecha y ciudad de expedición. |
| Datos personales | nombres, apellidos, nombre identitario, pronombre, sexo, género, orientación sexual, fecha de nacimiento. |
| Contacto | teléfono, correo. |
| Caracterización | nacionalidad, estado civil, escolaridad, grupo étnico, condición actual, lectura/escritura, discapacidad y PCD. |
| Vivienda | municipio, barrio, dirección, comuna, localidad, estrato, vivienda, zona, tenencia y servicios. |
| Economía | ocupación, empresa, salario, cargo y datos laborales. |
| Acudiente | nombre, relación, teléfono, correo y dirección. |
| Servicio | cómo se enteró y relación con universidad. |
| Control | activo. |

Relaciones principales:

```text
persona -> tipo_persona
persona -> nacionalidades
persona -> condicion
persona -> municipio
persona -> barrio
persona -> ocupacion
persona -> empresas
persona -> consulta como persona principal
persona -> consulta_parte
persona -> consulta_contraparte
```

### `tipo_persona`

Entidad:

```text
TipoPersona
```

Propósito:

Clasifica personas.

### `condicion`

Entidad:

```text
Condicion
```

Propósito:

Catálogo de condición actual de persona.

### `empresas`

Entidad:

```text
Empresa
```

Propósito:

Catálogo de empresas asociadas a datos laborales.

### `ocupacion`

Entidad:

```text
Ocupacion
```

Propósito:

Catálogo de ocupaciones asociadas a datos económicos.

## 4. Perfiles internos

Los perfiles internos representan actores operativos del consultorio jurídico.

Todos tienen relación opcional uno a uno con `usuario_sistema` mediante `usuario_sistema_id`.

La relación es nullable en las entidades para permitir migración de datos existentes sin romper el arranque.

### `administrativo`

Entidad:

```text
Administrativo
```

Campos principales:

| Columna | Uso |
|---|---|
| `usuario_sistema_id` | Usuario asociado. |
| `nombre` | Nombre. |
| `tipo_documento` | Tipo de documento. |
| `documento` | Documento único. |
| `email` | Correo único. |
| `telefono` | Teléfono único. |
| `usuario` | Usuario único. |
| `codigo` | Código único. |
| `sede` | Sede. |
| `activo` | Estado lógico. |
| `directora` | Marca de directora. |

### `asesor`

Entidad:

```text
Asesor
```

Campos específicos:

| Columna | Uso |
|---|---|
| `area_id` | Área jurídica asociada. |

Relaciones:

```text
asesor -> area
asesor -> estudiante
asesor -> consulta
```

### `estudiante`

Entidad:

```text
Estudiante
```

Campos específicos:

| Columna | Uso |
|---|---|
| `asesor_id` | Asesor asociado. |
| `conciliacion` | Habilitación para conciliaciones. |

Relaciones:

```text
estudiante -> asesor
estudiante -> consulta
estudiante -> seguimiento_respuesta
estudiante -> conciliacion
```

### `monitor`

Entidad:

```text
Monitor
```

Propósito:

Perfil operativo asociado a consultas y seguimientos según reglas de alcance.

Relaciones:

```text
monitor -> consulta
```

### `conciliador`

Entidad:

```text
Conciliador
```

Campos específicos:

| Columna | Uso |
|---|---|
| `tipo_conciliador` | Enum `INTERNO` o `EXTERNO`. |

Relaciones:

```text
conciliador -> conciliacion
```

## 5. Consultas jurídicas

### `consulta`

Entidad:

```text
Consulta
```

Propósito:

Representa el caso o consulta jurídica principal.

Campos principales:

| Columna | Uso |
|---|---|
| `id` | Identificador. |
| `fecha` | Fecha de consulta. |
| `descripcion` | Descripción breve. |
| `hechos` | Relato de hechos. |
| `pretensiones` | Pretensiones. |
| `concepto_juridico` | Concepto jurídico. |
| `tramite` | Trámite. |
| `observaciones` | Observaciones. |
| `tipo_violencia` | Clasificación adicional. |
| `estado` | Estado funcional de consulta. |
| `resultado` | Resultado o conclusión. |
| `persona_id` | Persona principal. |
| `sede_id` | Sede. |
| `area_id` | Área. |
| `tema_id` | Tema. |
| `tipo_id` | Tipo jurídico. |
| `asesor_id` | Asesor responsable. |
| `monitor_id` | Monitor responsable. |
| `estudiante_id` | Estudiante responsable. |

Relaciones:

```text
consulta -> persona
consulta -> sede
consulta -> area
consulta -> tema
consulta -> tipo
consulta -> asesor
consulta -> monitor
consulta -> estudiante
consulta -> seguimiento
consulta -> proceso
consulta -> conciliacion
```

### `consulta_parte`

Tabla de relación:

```text
consulta_parte
```

Relaciona consulta con personas que actúan como partes adicionales.

```text
consulta_parte.consulta_id -> consulta.id
consulta_parte.persona_id -> persona.id
```

### `consulta_contraparte`

Tabla de relación:

```text
consulta_contraparte
```

Relaciona consulta con personas que actúan como contrapartes.

```text
consulta_contraparte.consulta_id -> consulta.id
consulta_contraparte.persona_id -> persona.id
```

## 6. Seguimientos

### `categoria_seguimiento`

Entidad:

```text
CategoriaSeguimiento
```

Propósito:

Catálogo de categorías para seguimientos.

### `seguimiento`

Entidad:

```text
Seguimiento
```

Propósito:

Representa tarea o actuación asociada a una consulta.

Campos principales:

| Columna | Uso |
|---|---|
| `id` | Identificador. |
| `descripcion` | Descripción. |
| `fecha_entrega` | Fecha límite. |
| `dias_notificacion` | Días previos para recordatorio. |
| `notificar_partes` | Notifica partes. |
| `notificar_estudiante` | Notifica y muestra al estudiante. |
| `alerta_disciplinaria` | Marca alerta disciplinaria. |
| `estado` | Estado funcional. |
| `activo` | Estado lógico. |
| `categoria_seguimiento` | Categoría. |
| `consulta` | Consulta asociada. |
| `autor` | Usuario que creó el seguimiento. |
| `fecha_creacion` | Fecha de creación. |
| `fecha_actualizacion` | Fecha de actualización. |

Relaciones:

```text
seguimiento -> consulta
seguimiento -> categoria_seguimiento
seguimiento -> usuario_sistema como autor
seguimiento -> seguimiento_respuesta
seguimiento -> seguimiento_notificacion
```

### `seguimiento_respuesta`

Entidad:

```text
SeguimientoRespuesta
```

Propósito:

Respuesta del estudiante a un seguimiento visible.

Relaciones:

```text
seguimiento_respuesta -> seguimiento
seguimiento_respuesta -> estudiante
seguimiento_respuesta -> usuario_sistema como revisor
```

### `seguimiento_notificacion`

Entidad:

```text
SeguimientoNotificacion
```

Propósito:

Controla notificaciones inmediatas y recordatorios de seguimiento.

Relación:

```text
seguimiento_notificacion -> seguimiento
```

Restricción única:

```text
seguimiento_id + tipo_notificacion + momento_notificacion
```

Evita duplicar notificaciones equivalentes para un seguimiento.

## 7. Procesos

### `proceso`

Entidad:

```text
Proceso
```

Propósito:

Representa proceso asociado a una consulta.

Campos:

| Columna | Uso |
|---|---|
| `id` | Identificador. |
| `numero_radicado` | Número único de radicado. |
| `departamento_id` | Departamento. |
| `consulta_id` | Consulta asociada. |
| `organo_control_id` | Órgano de control. |
| `especialidad_id` | Especialidad. |
| `estado` | Estado funcional del proceso. |
| `activo` | Estado lógico. |

Relaciones:

```text
proceso -> consulta
proceso -> departamento
proceso -> organo_control
proceso -> especialidad
```

### `organo_control`

Entidad:

```text
OrganoControl
```

Propósito:

Catálogo de órganos de control.

Relación:

```text
organo_control -> especialidad
```

### `especialidad`

Entidad:

```text
Especialidad
```

Propósito:

Catálogo de especialidades asociadas a órganos de control.

Relación:

```text
especialidad -> organo_control
especialidad -> proceso
```

## 8. Conciliaciones

### `conciliacion`

Entidad:

```text
Conciliacion
```

Propósito:

Representa conciliación asociada a una consulta jurídica.

Campos principales:

| Columna | Uso |
|---|---|
| `id` | Identificador. |
| `consulta_id` | Consulta origen. |
| `estudiante_id` | Estudiante asignado. |
| `conciliador_id` | Conciliador asignado. |
| `estado_id` | Estado funcional en catálogo. |
| `fecha_conciliacion` | Fecha principal programada. |
| `documento_solicitud_path` | Ruta de solicitud PDF. |
| `acta_path` | Ruta de acta PDF. |
| `solicitado_por_id` | Usuario solicitante. |
| `fecha_creacion` | Fecha de creación. |
| `fecha_actualizacion` | Fecha de actualización. |
| `fecha_finalizacion` | Fecha de finalización. |
| `activo` | Estado lógico. |

Relaciones:

```text
conciliacion -> consulta
conciliacion -> estudiante
conciliacion -> conciliador
conciliacion -> estado_conciliacion
conciliacion -> usuario_sistema como solicitante
```

### `estado_conciliacion`

Entidad:

```text
EstadoConciliacion
```

Propósito:

Catálogo administrable de estados de conciliación.

Campos:

| Columna | Uso |
|---|---|
| `id` | Identificador. |
| `codigo` | Código técnico. |
| `nombre` | Nombre visible. |
| `activo` | Estado activo. |
| `orden` | Orden de presentación. |

## 9. Auditoría

### `audit_logs`

Entidad:

```text
AuditLog
```

Propósito:

Registra eventos auditables del backend.

Campos:

| Columna | Uso |
|---|---|
| `id` | Identificador. |
| `username` | Usuario que ejecutó la acción. |
| `action` | Acción auditada. |
| `entity_name` | Entidad lógica afectada. |
| `entity_id` | Identificador de entidad afectada. |
| `timestamp` | Fecha y hora. |
| `details` | Detalles técnicos. |

## 10. Relaciones principales del dominio

Vista resumida:

```text
usuario_sistema -> rol -> permiso
usuario_sistema -> perfil real
perfil -> consulta
persona -> consulta
consulta -> seguimiento
consulta -> proceso
consulta -> conciliacion
seguimiento -> seguimiento_respuesta
seguimiento -> seguimiento_notificacion
proceso -> organo_control -> especialidad
conciliacion -> estado_conciliacion
```

## 11. Tablas con desactivación lógica

Usan `activo` o equivalente:

- catálogos;
- personas;
- perfiles;
- roles;
- permisos;
- procesos;
- seguimientos;
- respuestas de seguimiento;
- notificaciones;
- conciliaciones.

## 12. Tablas con estado funcional

Usan campo `estado` o relación a estado:

- `consulta.estado`;
- `proceso.estado`;
- `seguimiento.estado`;
- `seguimiento_respuesta.estado`;
- `conciliacion.estado_id`.

## 13. Consideraciones de integridad

La integridad se protege mediante:

- llaves foráneas JPA;
- validaciones de service;
- validaciones de negocio;
- estados funcionales;
- desactivación lógica;
- relaciones activas;
- restricciones únicas;
- permisos y alcance.
