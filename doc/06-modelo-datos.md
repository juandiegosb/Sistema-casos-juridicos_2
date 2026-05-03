# Modelo de datos

## 1. Propósito

Este documento describe el modelo de datos implementado en el backend del sistema de gestión de casos jurídicos. Incluye las entidades persistentes, sus atributos, relaciones y decisiones de modelado adoptadas, basadas en el código fuente real.

## 2. Separación entre entidades business y security

El modelo se divide en dos módulos principales:

- **Business**: Entidades relacionadas con la lógica de negocio del sistema (personas, perfiles, catálogos).
- **Security**: Entidades para gestión de autenticación, autorización y recuperación de contraseñas.

## 3. Entidades business

### Persona

#### Descripción
Entidad que representa a una persona en el sistema, con información completa de identificación, contacto, caracterización socioeconómica y académica.

#### Atributos principales
- `id`: Identificador único (autogenerado).
- `tipoUsuario`: Tipo de usuario (Demandante, Demandado, etc.).
- `tipoDocumento`: Tipo de documento.
- `numeroDocumento`: Número de documento (único).
- `fechaExpedicion`: Fecha de expedición del documento.
- `ciudadExpedicion`: Ciudad de expedición.
- `nombres`: Nombres de la persona.
- `apellidos`: Apellidos de la persona.
- `nombreIdentitario`: Nombre identitario.
- `pronombre`: Pronombre preferido.
- `sexo`: Sexo biológico.
- `genero`: Identidad de género.
- `orientacionSexual`: Orientación sexual.
- `fechaNacimiento`: Fecha de nacimiento.
- `telefono`: Teléfono de contacto.
- `correo`: Correo electrónico.
- `nacionalidad`: Nacionalidad.
- `estadoCivil`: Estado civil.
- `escolaridad`: Nivel de escolaridad.
- `grupoEtnico`: Grupo étnico.
- `condicionActual`: Condición actual.
- `sabeLeerEscribir`: Indicador de alfabetización.
- `discapacidad`: Tipo de discapacidad.
- `caracterizacionPcd`: Caracterización PCD.
- `necesitaAjustePcd`: Necesidad de ajustes PCD.
- `departamento`: Departamento de residencia.
- `municipio`: Municipio de residencia.
- `barrio`: Barrio de residencia.
- `direccion`: Dirección completa.
- `comuna`: Comuna.
- `localidad`: Localidad.
- `estrato`: Estrato socioeconómico.
- `tipoVivienda`: Tipo de vivienda.
- `zona`: Zona urbana/rural.
- `tenencia`: Tenencia de vivienda.
- `numeroPersonasACargo`: Número de personas a cargo.
- `ingresosAdicionales`: Indicador de ingresos adicionales.
- `energiaElectrica`: Acceso a energía eléctrica.
- `acueducto`: Acceso a acueducto.
- `alcantarillado`: Acceso a alcantarillado.
- `ocupacion`: Ocupación actual.
- `empresa`: Empresa donde trabaja.
- `salario`: Salario mensual.
- `cargo`: Cargo en la empresa.
- `direccionEmpresa`: Dirección de la empresa.
- `telefonoEmpresa`: Teléfono de la empresa.
- `comoSeEntero`: Cómo se enteró del servicio.
- `relacionConUniversidad`: Relación con la universidad.

#### Relaciones principales
- Ninguna (entidad independiente).

#### Decisiones de modelado
- Entidad completa con todos los campos requeridos para caracterización socioeconómica.
- No genera UsuarioSistema automáticamente.

### TipoDocumento

#### Descripción
Catálogo de tipos de documento de identificación.

#### Atributos principales
- `id`: Identificador único (autogenerado).
- `displayName`: Nombre para mostrar.
- `activo`: Estado activo/inactivo (por defecto true).

#### Relaciones principales
- 1:N con Asesor, Estudiante, Administrativo (tipoDocumento).

#### Decisiones de modelado
- Campo `activo` para control de estado sin eliminación física.

### Sede

#### Descripción
Entidad que representa sedes físicas del sistema.

#### Atributos principales
- `id`: Identificador único (autogenerado).
- `nombre`: Nombre de la sede.

#### Relaciones principales
- 1:N con Asesor, Estudiante, Administrativo (sede).

#### Decisiones de modelado
- Entidad básica, sin campos adicionales en el modelo actual.

### Area

#### Descripción
Primer nivel de clasificación jurídica (área).

#### Atributos principales
- `id`: Identificador único (autogenerado).
- `nombre`: Nombre del área (único).

#### Relaciones principales
- 1:N con Tema (area).
- 1:N con Asesor (area).

#### Decisiones de modelado
- `nombre` único para evitar duplicados.

### Tema

#### Descripción
Segundo nivel de clasificación jurídica (tema), pertenece a un área.

#### Atributos principales
- `id`: Identificador único (autogenerado).
- `nombre`: Nombre del tema.
- `area`: Relación con Area (obligatoria).

#### Relaciones principales
- N:1 con Area.
- 1:N con Tipo (tema).

#### Decisiones de modelado
- Requiere área obligatoria.

### Tipo

#### Descripción
Tercer nivel de clasificación jurídica (tipo), pertenece a un tema.

#### Atributos principales
- `id`: Identificador único (autogenerado).
- `nombre`: Nombre del tipo.
- `tema`: Relación con Tema (obligatoria).

#### Relaciones principales
- N:1 con Tema.

#### Decisiones de modelado
- Requiere tema obligatorio.

### Asesor

#### Descripción
Perfil de asesor del sistema, genera UsuarioSistema automáticamente.

#### Atributos principales
- `id`: Identificador único (autogenerado).
- `nombre`: Nombre completo.
- `tipoDocumento`: Relación con TipoDocumento (obligatoria).
- `documento`: Número de documento (único).
- `email`: Correo electrónico (único).
- `telefono`: Teléfono (único).
- `usuario`: Nombre de usuario (único).
- `sede`: Relación con Sede (obligatoria).
- `codigo`: Código único del asesor.
- `area`: Relación con Area (obligatoria).
- `activo`: Estado activo/inactivo (por defecto true).

#### Relaciones principales
- N:1 con TipoDocumento.
- N:1 con Sede.
- N:1 con Area.
- 1:1 con UsuarioSistema (asesor).
- 1:N con Estudiante (asesor).

#### Decisiones de modelado
- Campos únicos para email, telefono, usuario, documento, codigo.
- Genera UsuarioSistema al crear.

### Estudiante

#### Descripción
Perfil de estudiante del sistema, genera UsuarioSistema automáticamente.

#### Atributos principales
- `id`: Identificador único (autogenerado).
- `nombre`: Nombre completo.
- `tipoDocumento`: Relación con TipoDocumento (obligatoria).
- `documento`: Número de documento (único).
- `email`: Correo electrónico (único).
- `telefono`: Teléfono (único).
- `usuario`: Nombre de usuario (único).
- `sede`: Relación con Sede (obligatoria).
- `codigo`: Código único del estudiante.
- `asesor`: Relación con Asesor (obligatoria).
- `activo`: Estado activo/inactivo (por defecto true).
- `conciliacion`: Indicador de participación en conciliación (por defecto false).

#### Relaciones principales
- N:1 con TipoDocumento.
- N:1 con Sede.
- N:1 con Asesor.
- 1:1 con UsuarioSistema (estudiante).

#### Decisiones de modelado
- Campos únicos para email, telefono, usuario, documento, codigo.
- Asesor asignado obligatorio.
- Campo `conciliacion` para control de participación.
- Genera UsuarioSistema al crear.

### Monitor

#### Descripción
Perfil de monitor del sistema, genera UsuarioSistema automáticamente.

#### Atributos principales
- `id`: Identificador único (autogenerado).
- `nombre`: Nombre completo.
- `tipoDocumento`: Relación con TipoDocumento (obligatoria).
- `documento`: Número de documento (único).
- `email`: Correo electrónico (único).
- `telefono`: Teléfono (único).
- `usuario`: Nombre de usuario (único).
- `sede`: Relación con Sede (obligatoria).
- `codigo`: Código único del monitor.
- `activo`: Estado activo/inactivo (por defecto true).

#### Relaciones principales
- N:1 con TipoDocumento.
- N:1 con Sede.
- 1:1 con UsuarioSistema (monitor).

#### Decisiones de modelado
- Campos únicos para email, telefono, usuario, documento, codigo.
- Genera UsuarioSistema al crear.

### Administrativo

#### Descripción
Perfil administrativo del sistema, genera UsuarioSistema automáticamente.

#### Atributos principales
- `id`: Identificador único (autogenerado).
- `nombre`: Nombre completo.
- `tipoDocumento`: Relación con TipoDocumento (opcional).
- `documento`: Número de documento (único, opcional).
- `email`: Correo electrónico (único, obligatorio).
- `telefono`: Teléfono (único, obligatorio).
- `usuario`: Nombre de usuario (único, obligatorio).
- `codigo`: Código único del administrativo (obligatorio).
- `sede`: Relación con Sede (opcional).
- `activo`: Estado activo/inactivo (por defecto true).
- `directora`: Indicador de rol directora (por defecto false).

#### Relaciones principales
- N:1 con TipoDocumento.
- N:1 con Sede.
- 1:1 con UsuarioSistema (administrativo).

#### Decisiones de modelado
- Algunos campos opcionales (tipoDocumento, documento, sede) para flexibilidad.
- Campo `directora` para roles jerárquicos.
- Genera UsuarioSistema al crear.

### Conciliador

#### Descripción
Perfil de conciliador del sistema, genera UsuarioSistema automáticamente.

#### Atributos principales
- `id`: Identificador único (autogenerado).
- `nombre`: Nombre completo.
- `tipoDocumento`: Relación con TipoDocumento (obligatoria).
- `documento`: Número de documento (único).
- `email`: Correo electrónico (único).
- `telefono`: Teléfono (único).
- `usuario`: Nombre de usuario (único).
- `sede`: Relación con Sede (obligatoria).
- `codigo`: Código único del conciliador.
- `activo`: Estado activo/inactivo (por defecto true).

#### Relaciones principales
- N:1 con TipoDocumento.
- N:1 con Sede.
- 1:1 con UsuarioSistema (conciliador).

#### Decisiones de modelado
- Campos únicos para email, telefono, usuario, documento, codigo.
- Genera UsuarioSistema al crear.

## 4. Entidades security

### Permiso

#### Descripción
Entidad que define permisos específicos del sistema.

#### Atributos principales
- `id`: Identificador único (autogenerado).
- `nombre`: Nombre del permiso (único).
- `descripcion`: Descripción del permiso.
- `activo`: Estado activo/inactivo (por defecto true).

#### Relaciones principales
- N:N con Rol (a través de tabla rol_permiso).

#### Decisiones de modelado
- `nombre` único.
- Campo `activo` para control de estado.

### Rol

#### Descripción
Entidad que agrupa permisos y se asigna a usuarios.

#### Atributos principales
- `id`: Identificador único (autogenerado).
- `nombre`: Nombre del rol (único).
- `descripcion`: Descripción del rol.
- `activo`: Estado activo/inactivo (por defecto true).
- `permisos`: Conjunto de permisos asociados.

#### Relaciones principales
- N:N con Permiso (permisos).
- 1:N con UsuarioSistema (rol).

#### Decisiones de modelado
- `nombre` único.
- Campo `activo` para control de estado.
- Relación bidireccional con Permiso.

### UsuarioSistema

#### Descripción
Entidad que representa usuarios del sistema para autenticación y autorización.

#### Atributos principales
- `id`: Identificador único (autogenerado).
- `username`: Nombre de usuario (email, único).
- `passwordHash`: Contraseña hasheada con BCrypt.
- `activo`: Estado activo/inactivo (por defecto true).
- `rol`: Relación con Rol (obligatoria).

#### Relaciones principales
- N:1 con Rol.
- 1:1 con Asesor (asesor).
- 1:1 con Estudiante (estudiante).
- 1:1 con Monitor (monitor).
- 1:1 con Administrativo (administrativo).
- 1:1 con Conciliador (conciliador).
- 1:N con PasswordResetToken (usuarioSistema).

#### Decisiones de modelado
- Username único (correo electrónico).
- Password hasheada para seguridad.
- Relaciones 1:1 opcionales con perfiles (solo uno informado por usuario).
- Rol obligatorio para autorización.

### PasswordResetToken

#### Descripción
Entidad para tokens de recuperación de contraseña.

#### Atributos principales
- `id`: Identificador único (autogenerado).
- `tokenHash`: Hash del token (único).
- `usuarioSistema`: Relación con UsuarioSistema (obligatoria).
- `fechaCreacion`: Fecha de creación.
- `fechaExpiracion`: Fecha de expiración.
- `usado`: Indicador de uso (por defecto false).
- `fechaUso`: Fecha de uso (opcional).

#### Relaciones principales
- N:1 con UsuarioSistema.

#### Decisiones de modelado
- Token guardado como hash para seguridad.
- Expiración y uso único para prevenir reutilización.

## 5. Relaciones importantes

- Area 1:N Tema
- Tema 1:N Tipo
- Rol N:N Permiso (tabla rol_permiso)
- UsuarioSistema N:1 Rol
- UsuarioSistema 1:1 Asesor
- UsuarioSistema 1:1 Monitor
- UsuarioSistema 1:1 Administrativo
- UsuarioSistema 1:1 Conciliador
- UsuarioSistema 1:1 Estudiante
- PasswordResetToken N:1 UsuarioSistema
- Asesor N:1 TipoDocumento
- Asesor N:1 Sede
- Asesor N:1 Area
- Estudiante N:1 TipoDocumento
- Estudiante N:1 Sede
- Estudiante N:1 Asesor
- Monitor N:1 TipoDocumento
- Monitor N:1 Sede
- Administrativo N:1 TipoDocumento
- Administrativo N:1 Sede
- Conciliador N:1 TipoDocumento
- Conciliador N:1 Sede

## 6. Decisiones clave

- Perfiles no tienen rol directamente; el rol pertenece a UsuarioSistema.
- Persona no genera UsuarioSistema; es entidad independiente.
- Asesor, Monitor, Administrativo, Conciliador y Estudiante sí generan UsuarioSistema automáticamente.
- Username en UsuarioSistema es el email del perfil.
- Campos únicos en perfiles: documento, email, telefono, usuario, codigo.
- Token de recuperación se guarda como hash, con expiración y de un solo uso.
- Relaciones opcionales en Administrativo (tipoDocumento, documento, sede) para flexibilidad.
- Campo `conciliacion` en Estudiante para control específico.
- Campo `directora` en Administrativo para roles jerárquicos.

## 7. Representación textual del modelo

```
Area
├── Tema
│   └── Tipo
└── Asesor

TipoDocumento
├── Asesor
├── Estudiante
├── Monitor
├── Administrativo
└── Conciliador

Sede
├── Asesor
├── Estudiante
├── Monitor
├── Administrativo
└── Conciliador

Persona (independiente)

Asesor
├── UsuarioSistema
└── Estudiante

Estudiante
└── UsuarioSistema

Monitor
└── UsuarioSistema

Administrativo
└── UsuarioSistema

Conciliador
└── UsuarioSistema

Permiso
└── Rol (N:N)

Rol
└── UsuarioSistema

UsuarioSistema
├── Rol
├── Asesor
├── Monitor
├── Administrativo
├── Conciliador
├── Estudiante
└── PasswordResetToken
```
- Campo `activo` para soft delete.

### Asesor

#### Descripción
Perfil de asesor, genera UsuarioSistema automáticamente.

#### Atributos principales
- `id`: Identificador único.
- `persona`: Relación con Persona.
- `email`: Correo electrónico (username).
- `telefono`: Teléfono.
- `usuarioSistema`: Relación con UsuarioSistema.
- `activo`: Estado activo/inactivo.

#### Relaciones principales
- 1:1 con Persona.
- 1:1 con UsuarioSistema.
- 1:N con Estudiante (como asesor asignado).

#### Decisiones de modelado
- Genera UsuarioSistema al crear.
- Email como username único.

### Monitor

#### Descripción
Perfil de monitor, genera UsuarioSistema automáticamente.

#### Atributos principales
- `id`: Identificador único.
- `persona`: Relación con Persona.
- `email`: Correo electrónico (username).
- `telefono`: Teléfono.
- `usuarioSistema`: Relación con UsuarioSistema.
- `activo`: Estado activo/inactivo.

#### Relaciones principales
- 1:1 con Persona.
- 1:1 con UsuarioSistema.

#### Decisiones de modelado
- Genera UsuarioSistema al crear.
- Email como username único.

### Administrativo

#### Descripción
Perfil administrativo, genera UsuarioSistema automáticamente.

#### Atributos principales
- `id`: Identificador único.
- `persona`: Relación con Persona.
- `email`: Correo electrónico (username).
- `telefono`: Teléfono.
- `usuarioSistema`: Relación con UsuarioSistema.
- `activo`: Estado activo/inactivo.

#### Relaciones principales
- 1:1 con Persona.
- 1:1 con UsuarioSistema.

#### Decisiones de modelado
- Genera UsuarioSistema al crear.
- Email como username único.

### Conciliador

#### Descripción
Perfil de conciliador, genera UsuarioSistema automáticamente.

#### Atributos principales
- `id`: Identificador único.
- `persona`: Relación con Persona.
- `email`: Correo electrónico (username).
- `telefono`: Teléfono.
- `usuarioSistema`: Relación con UsuarioSistema.
- `activo`: Estado activo/inactivo.

#### Relaciones principales
- 1:1 con Persona.
- 1:1 con UsuarioSistema.

#### Decisiones de modelado
- Genera UsuarioSistema al crear.
- Email como username único.

### Estudiante

#### Descripción
Perfil de estudiante, genera UsuarioSistema automáticamente.

#### Atributos principales
- `id`: Identificador único.
- `persona`: Relación con Persona.
- `email`: Correo electrónico (username).
- `telefono`: Teléfono.
- `asesor`: Relación con Asesor.
- `usuarioSistema`: Relación con UsuarioSistema.
- `activo`: Estado activo/inactivo.

#### Relaciones principales
- 1:1 con Persona.
- 1:1 con UsuarioSistema.
- N:1 con Asesor.

#### Decisiones de modelado
- Genera UsuarioSistema al crear.
- Email como username único.
- Asesor asignado opcional.

## 4. Entidades security

### Permiso

#### Descripción
Entidad que define permisos específicos del sistema.

#### Atributos principales
- `id`: Identificador único.
- `nombre`: Nombre del permiso (único).
- `descripcion`: Descripción del permiso.
- `activo`: Estado activo/inactivo.

#### Relaciones principales
- N:N con Rol (a través de tabla intermedia).

#### Decisiones de modelado
- `nombre` único.
- Campo `activo` para soft delete.

### Rol

#### Descripción
Entidad que agrupa permisos y se asigna a usuarios.

#### Atributos principales
- `id`: Identificador único.
- `nombre`: Nombre del rol (único).
- `descripcion`: Descripción del rol.
- `activo`: Estado activo/inactivo.

#### Relaciones principales
- N:N con Permiso.
- 1:N con UsuarioSistema.

#### Decisiones de modelado
- `nombre` único.
- Campo `activo` para soft delete.

### UsuarioSistema

#### Descripción
Entidad que representa usuarios del sistema para autenticación.

#### Atributos principales
- `id`: Identificador único.
- `username`: Nombre de usuario (email, único).
- `password`: Contraseña hasheada.
- `rol`: Relación con Rol.
- `activo`: Estado activo/inactivo.
- `fechaCreacion`: Fecha de creación.
- `ultimoAcceso`: Fecha de último acceso.

#### Relaciones principales
- N:1 con Rol.
- 1:1 con Asesor.
- 1:1 con Monitor.
- 1:1 con Administrativo.
- 1:1 con Conciliador.
- 1:1 con Estudiante.
- 1:N con PasswordResetToken.

#### Decisiones de modelado
- Username único.
- Password hasheada con BCrypt.
- Relaciones 1:1 con perfiles internos.

### PasswordResetToken

#### Descripción
Entidad para tokens de recuperación de contraseña.

#### Atributos principales
- `id`: Identificador único.
- `token`: Token hasheado (único).
- `usuarioSistema`: Relación con UsuarioSistema.
- `fechaExpiracion`: Fecha de expiración.
- `usado`: Indicador de uso.

#### Relaciones principales
- N:1 con UsuarioSistema.

#### Decisiones de modelado
- Token hasheado para seguridad.
- Expiración y uso único.

## 5. Relaciones importantes

- Area 1:N Tema
- Tema 1:N Tipo
- Rol N:N Permiso
- UsuarioSistema N:1 Rol
- UsuarioSistema 1:1 Asesor
- UsuarioSistema 1:1 Monitor
- UsuarioSistema 1:1 Administrativo
- UsuarioSistema 1:1 Conciliador
- UsuarioSistema 1:1 Estudiante
- PasswordResetToken N:1 UsuarioSistema
- Estudiante N:1 Asesor

## 6. Decisiones clave

- Perfiles no tienen rol directamente; el rol pertenece a UsuarioSistema.
- Frontend no envía rolId al crear perfiles internos.
- Persona no genera UsuarioSistema.
- Asesor, Monitor, Administrativo, Conciliador y Estudiante sí generan UsuarioSistema automáticamente.
- Username es el email del perfil.
- Contraseña inicial es el documento cifrado con BCrypt.
- Token de recuperación se guarda como hash, con expiración y de un solo uso.

## 7. Representación textual del modelo

```
Area
├── Tema
│   └── Tipo

Persona
├── TipoDocumento

Sede

Asesor
├── Persona
├── UsuarioSistema
└── Estudiante (como asesor)

Monitor
├── Persona
└── UsuarioSistema

Administrativo
├── Persona
└── UsuarioSistema

Conciliador
├── Persona
└── UsuarioSistema

Estudiante
├── Persona
├── UsuarioSistema
└── Asesor

Permiso
└── Rol (N:N)

Rol
└── UsuarioSistema

UsuarioSistema
├── Rol
├── Asesor
├── Monitor
├── Administrativo
├── Conciliador
├── Estudiante
└── PasswordResetToken
```