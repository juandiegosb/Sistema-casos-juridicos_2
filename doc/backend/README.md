# Backend

El backend principal del sistema se encuentra en:

```text
backend/app
```

Este backend expone la API REST del sistema de gestión de casos jurídicos y concentra las reglas de negocio, seguridad, persistencia, auditoría y manejo de archivos.

## Tecnologías principales

- Java 21.
- Spring Boot.
- Spring Security.
- Spring Data JPA.
- PostgreSQL/Supabase.
- Jakarta Validation.
- Maven.
- Lombok.
- JWT.
- Cookies de sesión.
- AOP para auditoría.
- Servicio de correo.
- Servicio de almacenamiento de archivos.

## Estructura principal

```text
src/main/java/co/edu/ufps/legal_cases/
  App.java
  audit/
  business/
  common/
  config/
  file_storage/
  security/
```

## Paquetes transversales

### `audit`

Contiene soporte de auditoría del sistema.

Componentes principales:

- anotación `Auditable`;
- aspecto `AuditAspect`;
- entidad `AuditLog`;
- `AuditLogController`;
- `AuditLogService`;
- `AuditLogRepository`.

La auditoría registra acciones relevantes de backend según las anotaciones configuradas en los casos de uso.

### `common`

Contiene componentes reutilizables y transversales.

Componentes principales:

- `BusinessException`;
- `ErrorResponseDTO`;
- `GlobalExceptionHandler`;
- utilidades de normalización;
- servicios de correo y notificación cuando aplican.

Este paquete centraliza errores de negocio, validaciones globales y respuestas controladas.

### `config`

Contiene configuración general de la aplicación.

Componentes principales:

- configuración de seguridad;
- configuración CORS;
- inicialización de datos de seguridad;
- propiedades de CORS.

### `file_storage`

Contiene la lógica de almacenamiento de archivos.

Componentes principales:

- `FileUploadController`;
- `FileStorageService`;
- excepciones propias del módulo de archivos.

Este paquete gestiona carga, descarga y listado de archivos.

### `security`

Contiene autenticación, autorización, usuarios del sistema, roles y permisos.

Componentes principales:

- `AuthController`;
- `AuthService`;
- `JwtService`;
- `JwtAuthenticationFilter`;
- `UsuarioActualService`;
- `SecurityDataInitializer`;
- `PermisoNombre`;
- modelos, DTOs, repositories y services de seguridad.

## Paquete `business`

El paquete `business` contiene los módulos funcionales principales del sistema.

Estructura general:

```text
business/
  controller/
  dto/
  model/
  repository/
  scheduler/
  service/
  service/acceso/
```

## Módulos funcionales

### Catálogos

Administra datos auxiliares usados por formularios y reglas de negocio.

Incluye:

- áreas;
- temas;
- tipos;
- departamentos;
- municipios;
- barrios;
- sedes;
- nacionalidades;
- tipos de documento.

### Personas

Administra personas vinculadas a los casos, usuarios y flujos del consultorio jurídico.

### Perfiles

Administra perfiles internos del sistema.

Incluye:

- estudiantes;
- asesores;
- monitores;
- administrativos;
- conciliadores.

### Consultas jurídicas

Administra el ciclo principal de las consultas jurídicas.

Incluye registro, consulta, edición, asignación de responsables, cambio de estado, archivo y cierre según reglas de negocio.

### Seguimientos

Administra seguimientos, respuestas, categorías y notificaciones asociadas a consultas jurídicas.

### Procesos

Administra procesos asociados a consultas jurídicas.

### Conciliaciones

Administra conciliaciones asociadas a consultas jurídicas.

Incluye creación desde consulta, asignación de responsables, estados de conciliación, documentos y cierre con acta.

## Capas de backend

### Controller

Expone endpoints HTTP y delega en services.

Responsabilidades:

- recibir parámetros, cuerpos JSON y archivos;
- aplicar anotaciones de autorización;
- retornar DTOs o respuestas HTTP;
- mantener delgada la capa HTTP.

### DTO

Transporta datos de entrada y salida.

Convenciones:

- los DTOs de entrada pueden usar Jakarta Validation;
- los DTOs de salida evitan exponer entidades completas;
- los DTOs definen contratos claros para frontend.

### Model

Representa entidades persistentes del dominio.

Incluye relaciones JPA, columnas, estados y campos de control.

### Repository

Encapsula acceso a datos mediante Spring Data JPA.

Se usa para consultas por estado activo, relaciones, existencia de duplicados y búsqueda por identificadores.

### Service

Orquesta casos de uso y reglas de negocio.

En módulos complejos se separan responsabilidades en services específicos:

- command service;
- query service;
- validator;
- mapper;
- relation service;
- access service;
- alcance service;
- document service;
- assignment service.

### Validator

Centraliza validaciones de negocio.

Ejemplos:

- validar datos obligatorios;
- validar duplicados;
- validar estado activo;
- validar cambios efectivos;
- validar coherencia entre entidades relacionadas.

### Mapper

Convierte entidades a DTOs y aplica datos de DTOs a entidades cuando corresponde.

### AccessService

Valida permisos funcionales y acciones permitidas por perfil.

### AlcanceService

Valida relación del usuario actual con el recurso concreto.

## Seguridad

El backend usa Spring Security con JWT y cookie de sesión.

Los endpoints protegidos validan autenticación y permisos.  
Los módulos con reglas específicas complementan permisos con validaciones de alcance.

Regla general:

```text
permiso funcional + alcance real + regla de negocio
```

## Manejo de errores

Los errores controlados se manejan mediante `GlobalExceptionHandler`.

Tipos principales:

- errores de negocio;
- errores de validación;
- errores por parámetros inválidos;
- errores de autenticación;
- errores de autorización;
- recursos no encontrados;
- errores de archivos.

## Convenciones de diseño

- Las reglas críticas se validan en backend.
- Los controllers no contienen reglas de negocio complejas.
- Los repositories no deciden permisos.
- Los DTOs evitan exponer entidades completas.
- Los services encapsulan casos de uso.
- Los validators concentran reglas de negocio.
- Los mappers concentran conversión de datos.
- Los permisos se centralizan en `PermisoNombre`.
- La documentación no expone valores sensibles.
