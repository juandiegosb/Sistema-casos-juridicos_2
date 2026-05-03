# Decisiones técnicas

Este documento recoge las decisiones técnicas del backend basadas en la implementación real del módulo `backend/app`.

## 1. Autenticación estateless con JWT

- Decisión: usar JWT para autenticar cada petición en lugar de sesiones de servidor.
- Justificación: `backend/app/src/main/java/co/edu/ufps/legal_cases/security/config/SecurityConfig.java` configura `SessionCreationPolicy.STATELESS`, deshabilita `formLogin` y `httpBasic`, y añade el filtro `JwtAuthenticationFilter` antes de `UsernamePasswordAuthenticationFilter`.
- Consecuencia: cada petición se valida con el token JWT contenido en la cookie `access_token`; no se mantiene estado de sesión en el servidor.

## 2. Uso de cookie `access_token` con HttpOnly

- Decisión: emitir el JWT dentro de una cookie llamada `access_token` desde `AuthController.login()`.
- Justificación: la cookie se marca `HttpOnly` y `sameSite=Lax`, lo que reduce el riesgo de robo de token desde JavaScript.
- Consecuencia: el frontend debe enviar cookies con credenciales habilitadas; el backend necesita CORS con `credentials=true` para funcionar.

## 3. Validación de usuario, rol y perfil en cada petición

- Decisión: no solo validar el JWT, sino también comprobar que el usuario, su rol y su perfil asociado estén activos.
- Justificación: `JwtAuthenticationFilter` carga el `UsuarioSistema` con rol, permisos y perfil asociado, y usa `usuarioActivo()`, `rolActivo()` y `perfilActivo()` antes de autenticar.
- Consecuencia: un usuario desactivado o con perfil inactivo queda bloqueado inmediatamente, aunque su token JWT siga siendo válido.

## 4. Modelo de autorización basado en rol y permisos dinámicos

- Decisión: usar un modelo de `Rol` con permisos (`Permiso`) en lugar de roles estáticos únicamente.
- Justificación: `UsuarioSistemaRepository` expone métodos que cargan `rol` y `rol.permisos` con `@EntityGraph`, y `JwtAuthenticationFilter` convierte permisos activos en `SimpleGrantedAuthority` para Spring Security.
- Consecuencia: los controladores pueden proteger acciones con `@PreAuthorize("hasAuthority('...')")` y los permisos se evalúan en tiempo de ejecución.

## 5. Habilitar seguridad de métodos con `@EnableMethodSecurity`

- Decisión: habilitar seguridad de método en `SecurityConfig`.
- Justificación: el proyecto usa `@PreAuthorize` en controladores como `PersonaController`, `RolController`, `TipoController`, `ConsultaController`, etc.
- Consecuencia: el acceso se controla tanto por URL públicas/privadas en `SecurityConfig` como por permisos en los métodos de los controladores.

## 6. Contraseñas cifradas con BCrypt

- Decisión: usar `BCryptPasswordEncoder` para todas las contraseñas.
- Justificación: `PasswordConfig` define un bean `PasswordEncoder` que devuelve `BCryptPasswordEncoder`; `AuthService` y `UsuarioSistemaRegistroService` lo usan para verificar y guardar contraseñas.
- Consecuencia: las contraseñas no se guardan en texto plano y las comparaciones se realizan mediante `passwordEncoder.matches(...)`.

## 7. Creación de usuarios del sistema a partir de perfiles de negocio

- Decisión: crear `UsuarioSistema` para cada perfil de negocio real y vincular exactamente un perfil por usuario.
- Justificación: `UsuarioSistemaRegistroService` tiene métodos específicos para `Asesor`, `Estudiante`, `Monitor`, `Administrativo` y `Conciliador`, y valida `validarUnSoloPerfil()`.
- Consecuencia: un usuario del sistema no puede tener múltiples perfiles asociados; el canal de autenticación está separado del modelo de negocio.

## 8. Contraseña inicial basada en número de documento

- Decisión: usar el documento del usuario normalizado como contraseña inicial al crear el usuario.
- Justificación: `UsuarioSistemaRegistroService.crearUsuarioBase()` normaliza el email para `username` y el documento para `passwordInicial`, luego cifra la contraseña con BCrypt.
- Consecuencia: el primer acceso es predecible, por lo que es importante forzar el cambio de contraseña luego del primer login o aplicar políticas de seguridad adicionales.

## 9. Recuperación de contraseña segura y un solo token válido

- Decisión: generar tokens de recuperación con `SecureRandom`, enviar el token en el enlace y almacenar solo su hash SHA-256 en la base de datos.
- Justificación: `PasswordResetService` genera un token seguro de 32 bytes codificado en Base64 y almacena su hash con `MessageDigest.getInstance("SHA-256")`.
- Consecuencia: si la base de datos se filtra, los tokens de recuperación no se pueden usar directamente; además, los tokens previos se invalidan y solo el último enlace es válido.

## 10. Expiración de tokens de recuperación configurada en propiedades

- Decisión: parametrizar la caducidad del token de recuperación en `application.properties`.
- Justificación: `PasswordResetService` usa `@Value("${app.password-reset.expiration-minutes}")` y `EmailTemplateService` muestra el tiempo de expiración en la plantilla.
- Consecuencia: el comportamiento es configurable sin cambiar código; en la configuración actual, la expiración es 15 minutos.

## 11. Enlace de restablecimiento del frontend configurable

- Decisión: construir el enlace de recuperación usando `app.frontend.reset-password-url`.
- Justificación: `PasswordResetService.construirEnlace()` concatena la URL del frontend con el token codificado.
- Consecuencia: la plantilla de correo y el backend dependen de que el frontend use la ruta configurada para completar el flujo de restablecimiento.

## 12. Separación de responsabilidades en servicios de correo

- Decisión: separar la generación de contenido de correo (Thymeleaf) de su envío.
- Justificación: `EmailTemplateService` construye el HTML de recuperación y `EmailService` envía el correo usando `JavaMailSender`.
- Consecuencia: el correo es más fácil de mantener y testear; la lógica de negocio no mezcla contenido con transporte.

## 13. Normalización de datos de entrada

- Decisión: normalizar el email de usuario en login y registro.
- Justificación: `AuthService.login()` y `UsuarioSistemaRegistroService.crearUsuarioBase()` usan `normalizarEmail(...)` para evitar diferencias de mayúsculas y espacios.
- Consecuencia: el sistema maneja de forma consistente los correos como identificadores únicos.

## 14. Diseño de repositorio para consultarlas optimizadas

- Decisión: usar `@EntityGraph` en `UsuarioSistemaRepository` para cargar `rol`, `rol.permisos` y perfil asociado en una sola consulta.
- Justificación: los métodos `findWithRolAndPermisosByUsernameIgnoreCase` y `findWithRolPermisosAndPerfilByUsername` evitan problemas de carga perezosa en el filtro de autenticación y en el login.
- Consecuencia: la autorización y la verificación de estado se realizan sin generar múltiples consultas adicionales.

---

Estas decisiones reflejan el diseño actual del backend y están alineadas con la implementación real del módulo `backend/app`.