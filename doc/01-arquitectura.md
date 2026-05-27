# Arquitectura del sistema

El sistema está organizado como una aplicación web con backend en Spring Boot y frontend en Next.js.

La arquitectura prioriza separación de responsabilidades, control de permisos, reglas de negocio en backend y configuración segura mediante variables de entorno.

## Arquitectura del backend

El backend se encuentra en:

```text
backend/app
```

Paquetes principales:

```text
co.edu.ufps.legal_cases
  audit
  business
  common
  config
  file_storage
  security
```

## Paquete `business`

Contiene los módulos funcionales del sistema.

Estructura general:

```text
business/controller
business/dto
business/model
business/repository
business/scheduler
business/service
business/service/acceso
```

### Controller

Expone endpoints HTTP.

Responsabilidades:

- recibir parámetros, cuerpos JSON o archivos;
- aplicar anotaciones de autorización cuando corresponde;
- delegar en services;
- devolver DTOs o respuestas HTTP.

No debe contener reglas de negocio complejas ni consultas directas a repositorios.

### DTO

Representa datos de entrada y salida.

Convenciones:

- DTOs de entrada pueden usar validaciones con Jakarta Validation;
- DTOs de salida no requieren validaciones;
- los DTOs evitan exponer entidades completas.

### Model

Representa entidades persistentes del dominio.

Las entidades definen relaciones JPA, estados, campos de control y estructura de persistencia.

### Repository

Encapsula acceso a datos con Spring Data JPA.

No debe decidir permisos ni reglas de negocio de alto nivel.

### Service

Orquesta casos de uso.

En módulos complejos se separan responsabilidades en:

- service fachada;
- command service;
- query service;
- validator;
- mapper;
- relation service;
- access service;
- alcance service.

### Validator

Centraliza reglas de negocio propias del módulo.

Ejemplos:

- validar duplicados;
- validar estados;
- validar datos obligatorios;
- validar coherencia entre relaciones;
- validar que un recurso pueda operar.

No valida permisos ni alcance.

### Mapper

Convierte entidades a DTOs y DTOs a entidades cuando corresponde.

No debe consultar repositorios ni validar permisos.

### AccessService

Valida permisos funcionales y acceso a acciones específicas.

### AlcanceService

Valida si el usuario actual está relacionado con el recurso concreto.

Este patrón permite separar:

```text
permiso funcional
alcance real
regla de negocio
persistencia
mapeo
```

## Paquete `security`

Contiene autenticación, autorización, usuarios del sistema, roles y permisos.

Componentes principales:

- `AuthController`;
- `AuthService`;
- `JwtService`;
- `JwtAuthenticationFilter`;
- `UsuarioActualService`;
- `PermisoNombre`;
- `SecurityDataInitializer`;
- repositories de usuarios, roles y permisos.

## Paquete `common`

Contiene componentes transversales:

- excepciones de negocio;
- DTO estándar de error;
- manejador global de excepciones;
- utilidades de normalización;
- servicios de correo.

## Paquete `config`

Contiene configuración de seguridad, CORS y datos iniciales.

## Paquete `file_storage`

Contiene manejo de archivos:

- carga de archivos;
- carga múltiple;
- listado;
- descarga;
- rutas controladas por backend;
- validación básica de rutas.

## Paquete `audit`

Contiene auditoría mediante AOP.

Componentes principales:

- anotación `Auditable`;
- aspecto `AuditAspect`;
- entidad `AuditLog`;
- controller y service de consulta de auditoría.

## Arquitectura del frontend

El frontend se encuentra en:

```text
frontend
```

Estructura principal:

```text
src/app
src/components
src/components/forms
src/components/navigation
src/components/ui
src/hooks
src/lib
```

### `src/app`

Contiene rutas de Next.js.

Se organiza con rutas públicas y rutas bajo layout de dashboard.

### `src/components`

Contiene componentes reutilizables.

### `src/components/forms`

Contiene formularios por módulo.

### `src/components/navigation`

Contiene navegación del sistema y filtrado de opciones por permisos.

### `src/lib/config.js`

Centraliza URLs de API:

```javascript
API_URL_BASE
FILE_STORAGE_API_URL_BASE
```

### `src/lib/permission.js`

Centraliza nombres de permisos usados por frontend.

### `src/lib/authz.js`

Contiene helpers para evaluar permisos, roles y perfiles en frontend.

## Relación backend/frontend

El frontend consume endpoints del backend mediante `fetch`.

Las peticiones protegidas usan:

```javascript
credentials: "include"
```

El backend valida la cookie de sesión y carga permisos en el contexto de seguridad.

## Convenciones arquitectónicas

- El backend valida reglas críticas.
- El frontend mejora experiencia y visibilidad, pero no reemplaza seguridad.
- Las reglas por rol y alcance se validan en backend.
- Los permisos se centralizan por nombre.
- Los documentos y archivos se reciben mediante `multipart/form-data` cuando corresponde.
