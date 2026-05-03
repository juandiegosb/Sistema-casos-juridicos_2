# Validaciones y reglas

## 1. Propósito

Este documento describe las validaciones y reglas implementadas en el backend del sistema de gestión de casos jurídicos. Incluye validaciones de DTO, reglas de negocio en services, normalización de datos y reglas de seguridad, basadas en el código fuente real.

## 2. Diferencia entre validaciones de DTO y reglas de negocio en services

- **Validaciones de DTO**: Usan anotaciones Jakarta Validation (@NotBlank, @NotNull, @Email, @Size) para validar formato, obligatoriedad y límites básicos antes de procesar la lógica.
- **Reglas de negocio en services**: Lógica funcional implementada en métodos de service, incluyendo normalización de datos, validación de duplicados, existencia de relaciones, cambios reales y reglas específicas del dominio.

## 3. Normalización de datos

Se utiliza `NormalizacionUtils` para estandarizar datos antes de guardar:

- `normalizarTexto`: Remueve espacios extra, convierte a null si vacío.
- `normalizarNumeroDocumento`: Remueve puntos, guiones y espacios.
- `normalizarTelefono`: Remueve caracteres no numéricos.
- `normalizarEmail`: Convierte a minúsculas.
- `normalizarUsuario`: Convierte a minúsculas.
- `normalizarCodigo`: Convierte a mayúsculas.
- `estaInformado`: Verifica si el texto está informado (no null y no "No informa").

## 4. Reglas generales CRUD

### No enviar id en creación
- DTOs no incluyen `id` en creación (se autogenera).
- Si se envía `id` != null, lanza `BusinessException("El id no debe enviarse en la creación")`.

### No cambiar id
- En actualización, si `dto.getId() != null && !dto.getId().equals(existente.getId())`, lanza `BusinessException("No se permite cambiar el id del asesor")` (ejemplo para Asesor).

### Validación de duplicados
- Creación: Verifica unicidad de campos únicos (documento, email, telefono, usuario, codigo).
- Actualización: Excluye el registro actual en la verificación (`existsByCampoAndIdNot`).

### Validación de relaciones
- IDs de entidades relacionadas deben existir en BD.
- Ejemplo: `obtenerTipoDocumento(dto.getTipoDocumentoId())` lanza excepción si no encontrado.

### Validación de cambios reales
- Compara campos normalizados con `equalsIgnoreCase` o `Objects.equals`.
- Si no hay cambios, lanza `BusinessException("No hay cambios para actualizar")`.

### Activar/desactivar
- Campo `activo` booleano (por defecto true).
- `cambiarEstado` valida que `activo != null` y no sea igual al actual.

## 5. Reglas por módulo

### TipoDocumento
- `displayName`: @NotBlank, máximo 50 caracteres.
- `activo`: Por defecto true.
- Normalización: No aplica (catálogo simple).

### Sede
- `nombre`: @NotBlank, máximo 50 caracteres.
- Normalización: `normalizarTexto`.
- Duplicados: No verificados en código actual.

### Area
- `nombre`: @NotBlank, máximo 50 caracteres.
- Normalización: `normalizarTexto`.
- Duplicados: `existsByNombreIgnoreCase` en creación y actualización.

### Tema
- `nombre`: @NotBlank, máximo 80 caracteres.
- `areaId`: @NotNull, debe existir.
- Normalización: `normalizarTexto`.
- Duplicados: No verificados (permitidos temas con mismo nombre en áreas diferentes).

### Tipo
- `nombre`: @NotBlank, máximo 100 caracteres.
- `temaId`: @NotNull, debe existir.
- Normalización: `normalizarTexto`.
- Duplicados: No verificados.

### Persona
- Campos extensos con validaciones @NotBlank/@Size según tipo.
- Normalización: `normalizarTexto`, `normalizarNumeroDocumento`, `normalizarTelefono`, `normalizarEmail`.
- No genera UsuarioSistema.

### Asesor
- Campos: nombre (@NotBlank, 150), documento (@NotBlank, 30), email (@NotBlank, @Email, 120), telefono (@NotBlank, 30), usuario (@NotBlank, 50), codigo (@NotBlank, 30).
- Relaciones: tipoDocumentoId (@NotNull), sedeId (@NotNull), areaId (@NotNull).
- Normalización: nombre, documento, email, telefono, usuario, codigo.
- Duplicados: documento, email, telefono, usuario, codigo.
- Genera UsuarioSistema automáticamente via `UsuarioSistemaRegistroService.crearParaAsesor`.

### Monitor
- Similar a Asesor, sin area.
- Genera UsuarioSistema automáticamente.

### Administrativo
- Campos similares, pero tipoDocumentoId, documento, sedeId opcionales.
- email, telefono, usuario, codigo obligatorios.
- `directora`: Booleano por defecto false.
- Genera UsuarioSistema automáticamente.

### Conciliador
- Similar a Asesor, sin area.
- Genera UsuarioSistema automáticamente.

### Estudiante
- Similar a Asesor, con `asesorId` (@NotNull).
- `conciliacion`: Booleano por defecto false.
- Genera UsuarioSistema automáticamente.

## 6. Reglas de security

### Permiso
- `nombre`: @NotBlank, único.
- `descripcion`: @NotBlank.
- `activo`: Por defecto true.
- Normalización: No aplica.

### Rol
- `nombre`: @NotBlank, único.
- `descripcion`: @NotBlank.
- `activo`: Por defecto true.
- Relación N:N con Permiso.
- Normalización: No aplica.

### UsuarioSistema
- `username`: Email único (normalizado a minúsculas).
- `passwordHash`: BCrypt.
- `activo`: Por defecto true.
- `rolId`: @NotNull, debe existir.
- Relación 1:1 opcional con perfiles (solo uno informado).
- Username = email del perfil.

## 7. Reglas de autenticación

### Login
- `username`: @NotBlank, normalizado a minúsculas.
- `password`: @NotBlank.
- Usuario debe existir, ser activo, rol activo, perfil activo.
- `passwordEncoder.matches(password, usuario.getPasswordHash())`.

### Validación de usuario activo
- `usuario.getActivo() == true`.

### Rol activo
- `usuario.getRol() != null && usuario.getRol().getActivo() == true`.

### Perfil activo
- Asesor, Estudiante, Monitor, Administrativo, Conciliador activo según corresponda.

## 8. Reglas de JWT/cookie

### JWT en cookie HttpOnly
- Token generado con `Jwts.builder().subject(username).issuedAt().expiration().signWith(HMAC-SHA)`.

### JWT solo identifica usuario
- Subject = username (email).

### Permisos se consultan desde BD
- En cada request, se carga usuario con rol y permisos desde BD.

## 9. Reglas de autorización

### @PreAuthorize
- `@PreAuthorize("hasAuthority('Gestionar usuarios')")` para controllers de perfiles.
- `@PreAuthorize("hasAuthority('Gestionar catálogos')")` para catálogos.
- `@PreAuthorize("hasAuthority('Gestionar permisos')")` para permisos.
- `@PreAuthorize("hasAuthority('Gestionar roles')")` para roles.
- `@PreAuthorize("hasAuthority('Gestionar personas')")` para personas.
- `@PreAuthorize("hasAuthority('Gestionar consultas')")` para consultas.

### 401
- No hay sesión activa (token null/vacío).

### 403
- Usuario no tiene autoridad requerida.

## 10. Reglas de cambio de contraseña

- `passwordActual`: @NotBlank.
- `passwordNueva`: @NotBlank, @Size(8-100).
- Usuario autenticado via token.
- `passwordEncoder.matches(passwordActual, usuario.getPasswordHash())`.
- Nueva != actual.
- Actualiza `passwordHash` con `passwordEncoder.encode(passwordNueva)`.

## 11. Reglas de recuperación de contraseña

- Existe en código: Sí, `PasswordResetService`.
- `username`: @NotBlank, normalizado a minúsculas.
- Usuario debe existir, ser activo, rol activo, perfil activo.
- Respuesta genérica: No informa si usuario existe o no.
- Token hash: Genera token seguro Base64, guarda SHA-256 hash.
- Expiración: Configurable (minutos), por defecto expira.
- Un solo uso: `usado = false`, marca como usado al restablecer.
- Invalida tokens anteriores del usuario.
- Enlace: `${frontend.reset-password-url}?token=${token}`.
- Password nueva: 8-100 caracteres, != actual, confirma == nueva.

## 12. Reglas de correo

- `EmailService` envía correos con `JavaMailSender`.
- Plantilla Thymeleaf: `emails/recuperacion-password.html`.
- Variables: enlace, nombreSistema, minutosExpiracion.
- From: Configurado en properties.
- Subject: "Recuperación de contraseña".
- HTML con estilos.