# Backend

## 1. Propósito del backend

El backend del sistema de gestión de casos jurídicos implementa la lógica de negocio, expone una API REST segura y gestiona la persistencia de datos. Recibe solicitudes HTTP, valida entradas, aplica reglas funcionales, interactúa con la base de datos y retorna respuestas estructuradas en JSON.

---

## 2. Tecnologías utilizadas

- **Java 21**: Lenguaje de programación principal.
- **Spring Boot 4.0.5**: Framework para desarrollo de aplicaciones Java.
- **Spring Web**: Para exposición de endpoints REST.
- **Spring Data JPA**: Para acceso a datos y mapeo objeto-relacional.
- **Hibernate**: Implementación de JPA.
- **Spring Security**: Para autenticación y autorización.
- **JWT (JJWT 0.12.5)**: Para tokens de autenticación.
- **BCrypt**: Para hashing de contraseñas.
- **Spring Boot Starter Mail**: Para envío de correos electrónicos.
- **Thymeleaf**: Para plantillas de correo HTML.
- **Maven**: Para gestión de dependencias y construcción.
- **PostgreSQL**: Base de datos relacional (configurada para producción).
- **H2 Database**: Base de datos en memoria para desarrollo y pruebas.
- **Jakarta Validation**: Para validaciones de entrada.
- **Lombok**: Para reducción de código boilerplate.
- **Spring Boot Starter Test**: Para pruebas unitarias e integración.

> Nota: Actualmente configurado con H2 para desarrollo; PostgreSQL para producción.

---

## 3. Configuración de la aplicación

### Variables de entorno y propiedades

- **Puerto**: 8080 (fijo).
- **Base de datos**: H2 en memoria para desarrollo (`jdbc:h2:mem:legal_cases`).
- **JWT**:
  - Secreto: `legal-cases-secret-key-development-123456789`.
  - Expiración: 3600000 ms (1 hora).
- **Correo**:
  - Host: smtp.gmail.com.
  - Puerto: 587.
  - Usuario: consultorio.info01@gmail.com.
  - TLS habilitado.
  - Nombre remitente: "Consultorio Juridico".
  - URL frontend para reset: http://localhost:3000/restablecer-password.
  - Expiración token reset: 15 minutos.

### Plantillas

- **Correo de recuperación de contraseña**: `src/main/resources/templates/emails/recuperacion-password.html` (Thymeleaf).

---

## 4. Organización real del proyecto

El proyecto está estructurado en módulos independientes para una mejor separación de responsabilidades:

```
src/main/java/co/edu/ufps/legal_cases/
├── business/
│   ├── config/
│   ├── controller/
│   ├── dto/
│   ├── model/
│   ├── repository/
│   └── service/
├── security/
│   ├── config/
│   ├── controller/
│   ├── dto/
│   ├── filter/
│   ├── model/
│   ├── repository/
│   └── service/
├── util/
└── exception/
```

- **business**: Lógica de negocio principal, incluyendo entidades del dominio y operaciones CRUD.
- **security**: Gestión de autenticación, autorización, roles y permisos.
- **util**: Utilidades para normalización y comparación de datos.
- **exception**: Manejo centralizado de errores.

---

## 5. Descripción del módulo business

Contiene la lógica de negocio del sistema, incluyendo entidades como TipoDocumento, Sede, Area, Tema, Tipo, Persona, Asesor, Monitor, Administrativo, Conciliador, Estudiante y Consulta.

### Componentes

- **config**: Inicializadores de datos base (AreaTemaTipoDataInitializer, CatalogosBaseDataInitializer, etc.).
- **controller**: Endpoints REST para operaciones CRUD (PersonaController, ConsultaController, etc.), protegidos con `@PreAuthorize`.
- **dto**: Objetos de transferencia con validaciones (PersonaDTO, ConsultaDTO, etc.).
- **model**: Entidades JPA (Persona, Consulta, etc.) con relaciones y constraints.
- **repository**: Interfaces JPA para consultas (PersonaRepository, ConsultaRepository, etc.).
- **service**: Lógica de negocio, validaciones y conversión (PersonaService, ConsultaService, etc.).

---

## 6. Descripción del módulo security

Gestiona la seguridad del sistema, incluyendo autenticación JWT stateless, autorización basada en roles y recuperación de contraseñas.

### Componentes

- **config**: 
  - SecurityConfig: Configura Spring Security con JWT, deshabilita CSRF/sesiones, maneja excepciones.
  - CorsConfig: Configuración CORS.
  - PasswordConfig: Bean para BCrypt.
  - SecurityDataInitializer: Datos iniciales de roles/permisos.
- **controller**: AuthController con endpoints login, logout, me, cambiar-password, recuperación.
- **dto**: DTOs para login, cambio de password, recuperación.
- **filter**: JwtAuthenticationFilter para validar tokens en cookies.
- **model**: UsuarioSistema (con relaciones a perfiles), Rol, Permiso, PasswordResetToken.
- **repository**: Repositorios para usuarios, roles, permisos, tokens.
- **service**: AuthService (JWT, login), PasswordResetService (correos).

### Flujo de autenticación

1. Login: Valida credenciales, genera JWT, envía en cookie httpOnly.
2. Requests: Filtro valida token en cookie, establece contexto de seguridad.
3. Logout: Borra cookie.
4. Recuperación: Envía enlace con token único a correo.

---

## 7. Utilidades de normalización

El módulo `util` proporciona clases para normalización de datos:

- **NormalizacionUtils**: Conversión a minúsculas, eliminación de espacios, normalización de textos.
- **ComparacionUtils**: Comparaciones insensibles a mayúsculas/minúsculas.

Usadas en servicios para asegurar consistencia en procesamiento de datos.

---

## 8. Manejo centralizado de errores

Implementado con `@RestControllerAdvice` en GlobalExceptionHandler:

- **BusinessException**: Errores de negocio (409).
- **Validation errors**: 400 con detalles de campos.
- **Security exceptions**: 401/403 manejados por SecurityExceptionHandler.
- **Internal errors**: 500 con logging.

Retorna ErrorResponse JSON consistente.

---

## 8. Seguridad

La seguridad se basa en Spring Security con JWT en cookies HttpOnly.

### Componentes principales

- **Login**: Endpoint que valida credenciales y genera JWT en cookie HttpOnly.
- **JWT**: Token que contiene solo el username, almacenado en cookie `access_token`.
- **Cookie HttpOnly**: Almacena el JWT de forma segura, inaccesible desde JavaScript.
- **Endpoint /me**: Retorna información del usuario autenticado.
- **Logout**: Invalida la sesión eliminando la cookie.
- **Filtro JWT**: Intercepta peticiones, valida el JWT y establece el contexto de seguridad.
- **Spring Security**: Framework base para configuración de autenticación y autorización.
- **@PreAuthorize**: Anotación para proteger endpoints basados en permisos consultados en BD.

El frontend debe usar `credentials: "include"` o `withCredentials: true` para incluir cookies en peticiones.

---

## 9. Roles, permisos y UsuarioSistema

- **Permiso**: Entidad que define acciones específicas (e.g., READ, WRITE).
- **Rol**: Agrupa permisos y se asigna a usuarios.
- **UsuarioSistema**: Entidad que representa usuarios del sistema, con username (email), contraseña hasheada y rol asignado.

Los permisos se consultan en tiempo real desde la base de datos, no se incluyen en el JWT.

---

## 10. Creación automática de UsuarioSistema al crear perfiles internos

Al crear Asesor, Monitor, Administrativo, Conciliador o Estudiante, el backend genera automáticamente un UsuarioSistema:
- Username: email del perfil.
- Contraseña inicial: documento cifrado con BCrypt.
- Rol: asignado automáticamente según el tipo de perfil (frontend no envía rolId).

Persona no genera UsuarioSistema.

---

## 11. Cambio de contraseña

Endpoint protegido que permite a usuarios autenticados cambiar su contraseña. Valida la sesión activa, actualiza el hash en BD y retorna confirmación.

---

## 12. Recuperación de contraseña por correo

Proceso que no revela si el correo existe:
1. Usuario solicita recuperación con email.
2. Se genera token único, se hashea y guarda en BD con expiración.
3. Se envía correo HTML (sin confirmar existencia).
4. Usuario accede link, valida token (de un solo uso) y cambia contraseña.

---

## 13. Correo HTML con plantilla externa

Los correos de recuperación usan plantillas Thymeleaf externas, permitiendo personalización y separación de lógica de presentación.

---

## 14. CORS con credenciales

Configurado para permitir orígenes específicos con credenciales, necesario para envío de cookies HttpOnly.

---

## 15. Inicializadores

Clases que cargan datos base al iniciar la aplicación, en orden: permisos, roles, usuarios base y datos de negocio.

---

## 16. Estado actual del backend

Implementado: CRUD de conciliador, administrativo, monitor, estudiante, asesor; CRUD de permisos y roles; módulo de usuarios del sistema; login, JWT, endpoints /me y logout; Spring Security, filtro JWT, @PreAuthorize, CORS con credenciales, cambio y recuperación de contraseña.

---

## 17. Documentación relacionada

- `01-contexto-y-alcance.md`: Contexto general del proyecto.
- `02-arquitectura.md`: Arquitectura detallada.
- `05-api.md`: Detalles de la API REST.
- `06-modelo-datos.md`: Modelo de datos.
- `07-validaciones-y-reglas.md`: Validaciones y reglas de negocio.
- `08-pruebas.md`: Pruebas del backend.