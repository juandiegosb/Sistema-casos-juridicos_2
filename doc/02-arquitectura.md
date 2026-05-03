# Arquitectura Backend

## 1. Propósito del documento

Este documento describe la arquitectura del backend del sistema de gestión de casos jurídicos, desarrollado con Spring Boot. Se enfoca en la organización modular, flujos de procesamiento y componentes clave para la implementación de la lógica de negocio, seguridad y manejo de datos.

---

## 2. Visión general de arquitectura

El backend sigue una arquitectura en capas organizada por módulos, utilizando Spring Boot como framework principal. La estructura promueve la separación de responsabilidades, facilitando el mantenimiento y la escalabilidad. Los módulos principales son: `business`, `security`, `util` y `exception`.

La arquitectura se basa en:
- **Capas**: Controller, Service, Repository.
- **Módulos**: Separación por dominio (negocio y seguridad).
- **Persistencia**: JPA con PostgreSQL.
- **Seguridad**: Spring Security con JWT y cookies HttpOnly.

---

## 3. Estructura de paquetes

El proyecto está organizado en los siguientes módulos:

- **business**: Contiene la lógica de negocio principal, incluyendo entidades del dominio y operaciones CRUD.
- **security**: Gestiona autenticación, autorización, roles y permisos.
- **util**: Proporciona utilidades para normalización y comparación de datos.
- **exception**: Centraliza el manejo de errores.

Cada módulo sigue una estructura interna consistente con paquetes para controller, dto, model, repository, service y config.

---

## 4. Explicación del módulo business

El módulo `business` implementa la lógica de negocio del sistema, incluyendo entidades como Persona, Asesor, Monitor, etc.

### Paquetes

- **controller**: Expone endpoints REST para operaciones CRUD (e.g., conciliador, administrativo).
- **dto**: Define objetos de transferencia para entrada y salida de la API, aplicando validaciones.
- **model**: Contiene entidades JPA que representan tablas en la base de datos.
- **repository**: Interfaces para acceso a datos, utilizando Spring Data JPA.
- **service**: Implementa reglas de negocio, validaciones funcionales y lógica de procesamiento.
- **config**: Configuraciones específicas del módulo, como inicializadores de datos.

---

## 5. Explicación del módulo security

El módulo `security` maneja la autenticación y autorización del sistema.

### Paquetes

- **config**: Configura Spring Security, CORS, filtros y beans de seguridad.
- **controller**: Endpoints para login, logout, cambio de contraseña y recuperación.
- **dto**: DTOs para credenciales, tokens y respuestas de autenticación.
- **filter**: Filtros personalizados, como el filtro JWT para validar tokens en cookies.
- **model**: Entidades como UsuarioSistema, Rol, Permiso y PasswordResetToken.
- **repository**: Repositorios para consultas de usuarios, roles y tokens.
- **service**: Lógica para generación de JWT, validación de permisos y manejo de contraseñas.

---

## 6. Flujo general de una petición

El flujo de procesamiento de una petición HTTP es el siguiente:

```
Cliente → Controller → Service → Repository → Base de datos
```

1. El cliente envía una petición HTTP a un endpoint.
2. El Controller recibe la petición, valida la entrada con DTOs y delega al Service.
3. El Service aplica reglas de negocio, valida lógica funcional y coordina con el Repository.
4. El Repository ejecuta operaciones de persistencia en la base de datos.
5. La respuesta se construye y retorna al cliente a través de las capas inversas.

---

## 7. Flujo de creación automática de UsuarioSistema al crear usuarios internos

Cuando se crea un perfil interno (Asesor, Monitor, Administrativo, Conciliador o Estudiante):

```
Creación de perfil → Service asigna rol base → Genera UsuarioSistema → Username = email, Password = documento cifrado → Persiste en BD
```

1. El Service del módulo business recibe la solicitud de creación.
2. Asigna automáticamente el rol base según el tipo de perfil (sin intervención del frontend).
3. Crea un UsuarioSistema con username como email del perfil y contraseña inicial como el documento cifrado con BCrypt.
4. Persiste el UsuarioSistema en la base de datos.

---

## 8. Flujo de autenticación

El proceso de autenticación utiliza JWT en cookies HttpOnly:

```
Login → Validación de credenciales → Generación de JWT → Cookie HttpOnly → Respuesta
```

1. Usuario envía credenciales (username, password) al endpoint de login.
2. El Service valida las credenciales contra la base de datos.
3. Si válidas, genera un JWT con el username y lo almacena en una cookie HttpOnly llamada `access_token`.
4. Retorna respuesta de éxito; el frontend debe usar `credentials: "include"`.

---

## 9. Flujo de autorización

La autorización verifica permisos en tiempo real:

```
Cookie → Filtro JWT → Extrae username → Consulta permisos en BD → @PreAuthorize evalúa permisos
```

1. El filtro JWT intercepta la petición y extrae el JWT de la cookie HttpOnly.
2. Valida el JWT y obtiene el username.
3. Consulta los permisos del usuario desde la base de datos (no incluidos en el JWT).
4. Spring Security evalúa `@PreAuthorize` en el endpoint para permitir o denegar acceso.

---

## 10. Flujo de cambio de contraseña

Para cambiar la contraseña de un usuario autenticado:

```
Usuario autenticado → Endpoint /change-password → Validación de sesión → Actualización en BD → Respuesta
```

1. Usuario envía nueva contraseña al endpoint protegido.
2. El filtro JWT verifica la sesión activa.
3. El Service valida la contraseña y actualiza el hash en la base de datos.
4. Retorna confirmación de cambio exitoso.

---

## 11. Flujo de recuperación de contraseña por correo

La recuperación no revela existencia de correos:

```
Solicitud → Genera token hash → Envía correo HTML → Usuario accede link → Valida token → Cambia contraseña
```

1. Usuario solicita recuperación con email.
2. Service genera token único, lo hashea y guarda con expiración en BD.
3. Envía correo HTML con plantilla Thymeleaf (sin confirmar existencia del email).
4. Usuario accede al link, valida token (de un solo uso), y cambia contraseña.

---

## 12. Manejo de errores

El manejo de errores es centralizado con `@RestControllerAdvice`:

- **Validación**: Errores en DTOs retornan 400 con detalles.
- **Negocio**: `BusinessException` para reglas violadas, retorna 400 o 409.
- **Autenticación/Autorización**: 401 para no autenticado, 403 para no autorizado.
- **Internos**: 500 para excepciones no manejadas.

Respuestas incluyen mensajes claros y códigos de error consistentes.

---

## 13. Inicializadores de datos y su orden

Los inicializadores cargan datos base al iniciar la aplicación:

1. **Permisos**: Crea permisos básicos (e.g., READ, WRITE).
2. **Roles**: Asigna permisos a roles (e.g., ADMIN, USER).
3. **Usuarios base**: Crea usuarios administrativos si no existen.
4. **Datos de negocio**: Inicializa tipos, áreas, sedes según necesidad.

El orden asegura dependencias: permisos antes de roles, roles antes de usuarios.

---

## 14. Beneficios de la arquitectura

- **Modularidad**: Separación clara facilita desarrollo y mantenimiento.
- **Seguridad**: JWT en cookies y permisos en BD evitan exposición de datos sensibles.
- **Escalabilidad**: Capas permiten agregar funcionalidades sin impacto global.
- **Mantenibilidad**: Reglas centralizadas en services, validaciones consistentes.
- **Robustez**: Manejo de errores centralizado y flujos definidos reducen bugs.