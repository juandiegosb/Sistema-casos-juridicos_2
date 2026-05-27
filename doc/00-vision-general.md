# Visión general del sistema

El sistema de gestión de casos jurídicos centraliza la operación del consultorio jurídico mediante una aplicación web compuesta por backend y frontend.

El sistema permite gestionar personas, usuarios, roles, permisos, consultas jurídicas, seguimientos, procesos, conciliaciones, auditoría y archivos relacionados con los casos.

## Propósito

El propósito del sistema es apoyar la atención y seguimiento de casos jurídicos, organizando la información operativa del consultorio y controlando el acceso de los usuarios según su rol, permisos y alcance sobre los recursos.

## Componentes principales

```text
backend/
  app/
    Backend principal del sistema.

frontend/
  Aplicación web del sistema.
```

## Backend

El backend expone una API REST protegida por autenticación y autorización.

Tecnologías principales:

- Java 21.
- Spring Boot.
- Spring Security.
- Spring Data JPA.
- PostgreSQL/Supabase.
- Jakarta Validation.
- Lombok.
- Maven.
- JWT.
- Cookies para sesión.
- Auditoría con AOP.
- Servicio de archivos.
- Servicio de correo.

## Frontend

El frontend consume la API del backend y presenta las pantallas operativas del sistema.

Tecnologías principales:

- Next.js.
- React.
- Tailwind CSS.
- Radix UI / shadcn.
- Fetch API.
- Playwright.

## Módulos funcionales

### Autenticación

Gestiona inicio de sesión, validación de sesión actual, cambio de contraseña, recuperación de contraseña y cierre de sesión.

### Usuarios, roles y permisos

Gestiona usuarios del sistema, roles, permisos funcionales y permisos de navegación.

### Personas

Gestiona datos de personas relacionadas con consultas y procesos del sistema.

### Catálogos

Gestiona datos auxiliares usados por formularios y reglas de negocio.

### Consultas jurídicas

Gestiona el registro y ciclo operativo principal de las consultas jurídicas.

### Seguimientos

Gestiona tareas, seguimientos, respuestas, revisión de respuestas y notificaciones asociadas a consultas.

### Procesos

Gestiona procesos asociados a consultas jurídicas, incluyendo datos judiciales y estado operativo.

### Conciliaciones

Gestiona conciliaciones asociadas a consultas, solicitud documental, asignación de responsables, estado de conciliación y finalización con acta.

### Archivos

Gestiona carga, consulta y descarga de documentos.

### Auditoría

Registra acciones relevantes mediante anotaciones y aspecto de auditoría.

## Flujo general de negocio

El flujo general del sistema parte del registro de personas y consultas jurídicas.

Desde una consulta pueden generarse operaciones relacionadas como:

- seguimientos;
- respuestas de seguimiento;
- procesos;
- conciliaciones;
- documentos asociados.

El backend mantiene reglas de negocio para controlar estados, alcance, permisos, cierre de consultas, archivo y operaciones permitidas.

## Principio de seguridad

El sistema usa una combinación de:

1. autenticación;
2. permisos funcionales;
3. roles;
4. perfiles;
5. alcance real sobre el recurso.

El frontend puede ocultar acciones según permisos, pero el backend conserva la responsabilidad final de validar autorización y reglas de negocio.
