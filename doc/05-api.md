# API

## 1. Propósito

Este documento describe los endpoints REST implementados en el backend del sistema de gestión de casos jurídicos. Incluye recursos, métodos HTTP, rutas, permisos requeridos, cuerpos de solicitud y respuestas de ejemplo.

---

## 2. Convenciones

### Formato
La API utiliza JSON para entrada y salida.

### Prefijo base
`/api`

### Autenticación
- Utiliza cookies HttpOnly para JWT.
- El frontend debe enviar `credentials: "include"` o `withCredentials: true`.
- CORS configurado para permitir credenciales.

### Métodos HTTP
- `GET`: consulta
- `POST`: creación
- `PUT`: actualización completa
- `PATCH`: actualización parcial
- `DELETE`: eliminación

### Códigos de respuesta
- `200 OK`: éxito en consulta/actualización
- `201 Created`: recurso creado
- `204 No Content`: eliminación exitosa
- `400 Bad Request`: error de validación
- `401 Unauthorized`: no autenticado
- `403 Forbidden`: no autorizado
- `500 Internal Server Error`: error interno

---

## 3. Sección Auth

Base: `/api/auth`  
Controller: `AuthController`

### POST /api/auth/login
**Permiso requerido**: Ninguno (público)  
**Descripción**: Autentica al usuario y genera JWT en cookie HttpOnly.  
**Body**:
```json
{
  "username": "usuario@example.com",
  "password": "password123"
}
```
**Respuesta (200)**:
```json
{
  "usuarioId": 1,
  "username": "usuario@example.com",
  "rolId": 1,
  "rolNombre": "ADMIN",
  "perfilId": 1,
  "tipoPerfil": "Administrativo",
  "permisos": ["Gestionar usuarios", "Gestionar catálogos"]
}
```

### GET /api/auth/me
**Permiso requerido**: Autenticado  
**Descripción**: Retorna información del usuario autenticado.  
**Respuesta (200)**:
```json
{
  "id": 1,
  "username": "usuario@example.com",
  "activo": true,
  "rolId": 1,
  "rolNombre": "ADMIN",
  "perfilId": 1,
  "tipoPerfil": "Administrativo",
  "permisos": ["Gestionar usuarios", "Gestionar catálogos"]
}
```

### POST /api/auth/logout
**Permiso requerido**: Autenticado  
**Descripción**: Invalida la sesión eliminando la cookie.  
**Respuesta (200)**: Sin contenido

### PATCH /api/auth/cambiar-password
**Permiso requerido**: Autenticado  
**Descripción**: Cambia la contraseña del usuario autenticado.  
**Body**:
```json
{
  "passwordActual": "oldpassword",
  "passwordNueva": "newpassword"
}
```
**Respuesta (204)**: Sin contenido

### POST /api/auth/solicitar-recuperacion
**Permiso requerido**: Ninguno (público)  
**Descripción**: Solicita recuperación de contraseña por correo.  
**Body**:
```json
{
  "email": "usuario@example.com"
}
```
**Respuesta (200)**:
```json
{
  "message": "Si el correo existe, se enviarán instrucciones para recuperar la contraseña"
}
```

### POST /api/auth/restablecer-password
**Permiso requerido**: Ninguno (público)  
**Descripción**: Restablece la contraseña usando token.  
**Body**:
```json
{
  "token": "token-hash",
  "passwordNueva": "newpassword"
}
```
**Respuesta (200)**:
```json
{
  "message": "La contraseña se restableció correctamente"
}
```

---

## 4. Sección Permisos

Base: `/api/permisos`  
Controller: `PermisoController`  
**Permiso requerido**: `Gestionar permisos`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/permisos | Lista todos los permisos |
| GET | /api/permisos/activos | Lista permisos activos |
| GET | /api/permisos/{id} | Consulta permiso por ID |
| POST | /api/permisos | Crea un permiso |
| PUT | /api/permisos/{id} | Actualiza un permiso |
| PATCH | /api/permisos/{id}/activo?activo=false | Activa/desactiva permiso |

**Body para POST**:
```json
{
  "nombre": "READ_USER",
  "descripcion": "Permiso para leer usuarios"
}
```

---

## 5. Sección Roles

Base: `/api/roles`  
Controller: `RolController`  
**Permiso requerido**: `Gestionar roles`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/roles | Lista todos los roles |
| GET | /api/roles/activos | Lista roles activos |
| GET | /api/roles/{id} | Consulta rol por ID |
| POST | /api/roles | Crea un rol |
| PUT | /api/roles/{id} | Actualiza un rol |
| PATCH | /api/roles/{id}/activo?activo=false | Activa/desactiva rol |
| PATCH | /api/roles/{rolId}/permisos/{permisoId} | Asigna permiso a rol |
| DELETE | /api/roles/{rolId}/permisos/{permisoId} | Remueve permiso de rol |

**Body para POST**:
```json
{
  "nombre": "ADMIN",
  "descripcion": "Rol de administrador"
}
```

---

## 6. Sección UsuariosSistema

Base: `/api/usuarios-sistema`  
Controller: `UsuarioSistemaController`  
**Permiso requerido**: `Gestionar usuarios`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/usuarios-sistema | Lista todos los usuarios del sistema |
| GET | /api/usuarios-sistema/activos | Lista usuarios activos |
| GET | /api/usuarios-sistema/{id} | Consulta usuario por ID |
| PATCH | /api/usuarios-sistema/{id}/activo?activo=false | Activa/desactiva usuario |

---

## 7. Sección Catálogos

### TiposDocumento
Base: `/api/tipos-documento`  
Controller: `TipoDocumentoController`  
**Permiso requerido**: `Gestionar catálogos`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/tipos-documento | Lista tipos de documento |
| GET | /api/tipos-documento/activos | Lista tipos activos |
| GET | /api/tipos-documento/{id} | Consulta tipo por ID |
| POST | /api/tipos-documento | Crea tipo de documento |
| PUT | /api/tipos-documento/{id} | Actualiza tipo |
| PATCH | /api/tipos-documento/{id}/activo?activo=false | Activa/desactiva tipo |

**Nota**: No tiene DELETE para preservar integridad referencial.

### Sedes
Base: `/api/sedes`  
Controller: `SedeController`  
**Permiso requerido**: `Gestionar catálogos`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/sedes | Lista sedes |
| GET | /api/sedes/{id} | Consulta sede por ID |
| POST | /api/sedes | Crea sede |
| PUT | /api/sedes/{id} | Actualiza sede |
| DELETE | /api/sedes/{id} | Elimina sede |

### Areas
Base: `/api/areas`  
Controller: `AreaController`  
**Permiso requerido**: `Gestionar catálogos`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/areas | Lista áreas |
| GET | /api/areas/{id} | Consulta área por ID |
| POST | /api/areas | Crea área |
| PUT | /api/areas/{id} | Actualiza área |
| DELETE | /api/areas/{id} | Elimina área |

### Temas
Base: `/api/temas`  
Controller: `TemaController`  
**Permiso requerido**: `Gestionar catálogos`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/temas | Lista temas |
| GET | /api/temas/{id} | Consulta tema por ID |
| GET | /api/temas/area/{areaId} | Lista temas por área |
| POST | /api/temas | Crea tema |
| PUT | /api/temas/{id} | Actualiza tema |
| DELETE | /api/temas/{id} | Elimina tema |

### Tipos
Base: `/api/tipos`  
Controller: `TipoController`  
**Permiso requerido**: `Gestionar catálogos`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/tipos | Lista tipos |
| GET | /api/tipos/{id} | Consulta tipo por ID |
| GET | /api/tipos/tema/{temaId} | Lista tipos por tema |
| POST | /api/tipos | Crea tipo |
| PUT | /api/tipos/{id} | Actualiza tipo |
| DELETE | /api/tipos/{id} | Elimina tipo |

---

## 8. Sección Personas

Base: `/api/personas`  
Controller: `PersonaController`  
**Permiso requerido**: `Gestionar personas`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/personas | Lista personas |
| GET | /api/personas/{id} | Consulta persona por ID |
| POST | /api/personas | Crea persona |
| PUT | /api/personas/{id} | Actualiza persona |
| DELETE | /api/personas/{id} | Elimina persona |

**Body para POST** (ejemplo simplificado):
```json
{
  "tipoUsuario": "Demandante",
  "tipoDocumento": "CC",
  "numeroDocumento": "12345678",
  "fechaExpedicion": "2020-01-01",
  "ciudadExpedicion": "Bogotá",
  "nombres": "Juan",
  "apellidos": "Pérez",
  "nombreIdentitario": "Juan Pérez",
  "pronombre": "él/ellos",
  "sexo": "Masculino",
  "genero": "Hombre",
  "orientacionSexual": "Heterosexual",
  "fechaNacimiento": "1990-01-01",
  "telefono": "123456789",
  "correo": "juan@example.com",
  "nacionalidad": "Colombiana",
  "estadoCivil": "Soltero",
  "escolaridad": "Universitaria",
  "grupoEtnico": "Mestizo",
  "condicionActual": "Estudiante",
  "sabeLeerEscribir": true,
  "discapacidad": "Ninguna",
  "caracterizacionPcd": "No aplica",
  "necesitaAjustePcd": false,
  "departamento": "Cundinamarca",
  "municipio": "Bogotá",
  "barrio": "Centro",
  "direccion": "Calle 123 #45-67",
  "comuna": "Comuna 1",
  "localidad": "Usaquén",
  "estrato": 3,
  "tipoVivienda": "Casa",
  "zona": "Urbana",
  "tenencia": "Propia",
  "numeroPersonasACargo": 0,
  "ingresosAdicionales": false,
  "energiaElectrica": true,
  "acueducto": true,
  "alcantarillado": true,
  "ocupacion": "Estudiante",
  "empresa": "Universidad",
  "salario": 0,
  "cargo": "Estudiante",
  "direccionEmpresa": "Carrera 1 #1-1",
  "telefonoEmpresa": "123456789",
  "comoSeEntero": "Redes sociales",
  "relacionConUniversidad": "Estudiante activo"
}
```

---

## 9. Sección Usuarios Internos

### Asesores
Base: `/api/asesores`  
Controller: `AsesorController`  
**Permiso requerido**: `Gestionar usuarios`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/asesores | Lista asesores |
| GET | /api/asesores/activos | Lista asesores activos |
| GET | /api/asesores/{id} | Consulta asesor por ID |
| POST | /api/asesores | Crea asesor |
| PUT | /api/asesores/{id} | Actualiza asesor |
| PATCH | /api/asesores/{id}/activo?activo=false | Activa/desactiva asesor |
| DELETE | /api/asesores/{id} | Elimina asesor |

**Body para POST**:
```json
{
  "personaId": 1,
  "email": "asesor@example.com",
  "telefono": "123456789",
  "usuario": "asesor1",
  "sedeId": 1,
  "codigo": "ASE001",
  "areaId": 1
}
```

### Monitores
Base: `/api/monitores`  
Controller: `MonitorController`  
**Permiso requerido**: `Gestionar usuarios`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/monitores | Lista monitores |
| GET | /api/monitores/activos | Lista monitores activos |
| GET | /api/monitores/{id} | Consulta monitor por ID |
| POST | /api/monitores | Crea monitor |
| PUT | /api/monitores/{id} | Actualiza monitor |
| PATCH | /api/monitores/{id}/activo?activo=false | Activa/desactiva monitor |
| DELETE | /api/monitores/{id} | Elimina monitor |

### Administrativos
Base: `/api/administrativos`  
Controller: `AdministrativoController`  
**Permiso requerido**: `Gestionar usuarios`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/administrativos | Lista administrativos |
| GET | /api/administrativos/activos | Lista administrativos activos |
| GET | /api/administrativos/directoras | Lista directoras |
| GET | /api/administrativos/{id} | Consulta administrativo por ID |
| POST | /api/administrativos | Crea administrativo |
| PUT | /api/administrativos/{id} | Actualiza administrativo |
| PATCH | /api/administrativos/{id}/activo?activo=false | Activa/desactiva administrativo |
| PATCH | /api/administrativos/{id}/directora?directora=true | Cambia estado directora |
| DELETE | /api/administrativos/{id} | Elimina administrativo |

### Conciliadores
Base: `/api/conciliadores`  
Controller: `ConciliadorController`  
**Permiso requerido**: `Gestionar usuarios`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/conciliadores | Lista conciliadores |
| GET | /api/conciliadores/activos | Lista conciliadores activos |
| GET | /api/conciliadores/{id} | Consulta conciliador por ID |
| POST | /api/conciliadores | Crea conciliador |
| PUT | /api/conciliadores/{id} | Actualiza conciliador |
| PATCH | /api/conciliadores/{id}/activo?activo=false | Activa/desactiva conciliador |
| DELETE | /api/conciliadores/{id} | Elimina conciliador |

### Estudiantes
Base: `/api/estudiantes`  
Controller: `EstudianteController`  
**Permiso requerido**: `Gestionar usuarios`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/estudiantes | Lista estudiantes |
| GET | /api/estudiantes/activos | Lista estudiantes activos |
| GET | /api/estudiantes/{id} | Consulta estudiante por ID |
| POST | /api/estudiantes | Crea estudiante |
| PUT | /api/estudiantes/{id} | Actualiza estudiante |
| PATCH | /api/estudiantes/{id}/activo?activo=false | Activa/desactiva estudiante |
| PATCH | /api/estudiantes/{id}/conciliacion?conciliacion=true | Cambia estado conciliación |
| DELETE | /api/estudiantes/{id} | Elimina estudiante |

---

## 10. Sección de Errores

### 400 Bad Request
Error de validación en datos de entrada.  
**Respuesta**:
```json
{
  "error": "Bad Request",
  "message": "Campo requerido: nombre",
  "status": 400
}
```

### 401 Unauthorized
Usuario no autenticado.  
**Respuesta**:
```json
{
  "error": "Unauthorized",
  "message": "Credenciales inválidas",
  "status": 401
}
```

### 403 Forbidden
Usuario autenticado pero sin permisos.  
**Respuesta**:
```json
{
  "error": "Forbidden",
  "message": "Permiso requerido: Gestionar usuarios",
  "status": 403
}
```

### 500 Internal Server Error
Error interno del servidor.
}
```
**Respuesta (200)**:
```json
{
  "message": "Contraseña restablecida exitosamente"
}
```

---

## 4. Sección Permisos

Base: `/api/permisos`  
**Permiso requerido**: Gestionar permisos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/permisos | Lista todos los permisos |
| GET | /api/permisos/activos | Lista permisos activos |
| GET | /api/permisos/{id} | Consulta permiso por ID |
| POST | /api/permisos | Crea un permiso |
| PUT | /api/permisos/{id} | Actualiza un permiso |
| PATCH | /api/permisos/{id}/activo?activo=false | Activa/desactiva permiso |

**Body para POST**:
```json
{
  "nombre": "READ_USER",
  "descripcion": "Permiso para leer usuarios"
}
```

---

## 5. Sección Roles

Base: `/api/roles`  
**Permiso requerido**: Gestionar roles

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/roles | Lista todos los roles |
| GET | /api/roles/activos | Lista roles activos |
| GET | /api/roles/{id} | Consulta rol por ID |
| POST | /api/roles | Crea un rol |
| PUT | /api/roles/{id} | Actualiza un rol |
| PATCH | /api/roles/{id}/activo?activo=false | Activa/desactiva rol |
| PATCH | /api/roles/{rolId}/permisos/{permisoId} | Asigna permiso a rol |
| DELETE | /api/roles/{rolId}/permisos/{permisoId} | Remueve permiso de rol |

**Body para POST**:
```json
{
  "nombre": "ADMIN",
  "descripcion": "Rol de administrador"
}
```

---

## 6. Sección UsuariosSistema

Base: `/api/usuarios-sistema`  
**Permiso requerido**: Gestionar usuarios

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/usuarios-sistema | Lista todos los usuarios del sistema |
| GET | /api/usuarios-sistema/activos | Lista usuarios activos |
| GET | /api/usuarios-sistema/{id} | Consulta usuario por ID |
| PATCH | /api/usuarios-sistema/{id}/activo?activo=false | Activa/desactiva usuario |

---

## 7. Sección Catálogos

### TiposDocumento
Base: `/api/tipos-documento`  
**Permiso requerido**: Gestionar catálogos  
**Nota**: Si está habilitado en el controller.

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/tipos-documento | Lista tipos de documento |
| GET | /api/tipos-documento/{id} | Consulta tipo por ID |
| POST | /api/tipos-documento | Crea tipo de documento |
| PUT | /api/tipos-documento/{id} | Actualiza tipo |
| DELETE | /api/tipos-documento/{id} | Elimina tipo |

### Sedes
Base: `/api/sedes`  
**Permiso requerido**: Gestionar catálogos  
**Nota**: Si está habilitado en el controller.

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/sedes | Lista sedes |
| GET | /api/sedes/{id} | Consulta sede por ID |
| POST | /api/sedes | Crea sede |
| PUT | /api/sedes/{id} | Actualiza sede |
| DELETE | /api/sedes/{id} | Elimina sede |

### Areas
Base: `/api/areas`  
**Permiso requerido**: Gestionar catálogos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/areas | Lista áreas |
| GET | /api/areas/{id} | Consulta área por ID |
| POST | /api/areas | Crea área |
| PUT | /api/areas/{id} | Actualiza área |
| DELETE | /api/areas/{id} | Elimina área |

### Temas
Base: `/api/temas`  
**Permiso requerido**: Gestionar catálogos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/temas | Lista temas |
| GET | /api/temas/{id} | Consulta tema por ID |
| GET | /api/temas/area/{areaId} | Lista temas por área |
| POST | /api/temas | Crea tema |
| PUT | /api/temas/{id} | Actualiza tema |
| DELETE | /api/temas/{id} | Elimina tema |

### Tipos
Base: `/api/tipos`  
**Permiso requerido**: Gestionar catálogos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/tipos | Lista tipos |
| GET | /api/tipos/{id} | Consulta tipo por ID |
| GET | /api/tipos/tema/{temaId} | Lista tipos por tema |
| POST | /api/tipos | Crea tipo |
| PUT | /api/tipos/{id} | Actualiza tipo |
| DELETE | /api/tipos/{id} | Elimina tipo |

---

## 8. Sección Personas

Base: `/api/personas`  
**Permiso requerido**: Gestionar personas

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/personas | Lista personas |
| GET | /api/personas/{id} | Consulta persona por ID |
| POST | /api/personas | Crea persona |
| PUT | /api/personas/{id} | Actualiza persona |
| DELETE | /api/personas/{id} | Elimina persona |

**Body para POST**:
```json
{
  "nombre": "Juan",
  "apellido": "Pérez",
  "documento": "12345678",
  "tipoDocumentoId": 1
}
```

---

## 9. Sección Usuarios Internos

### Asesores
Base: `/api/asesores`  
**Permiso requerido**: Gestionar usuarios

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/asesores | Lista asesores |
| GET | /api/asesores/{id} | Consulta asesor por ID |
| POST | /api/asesores | Crea asesor |
| PUT | /api/asesores/{id} | Actualiza asesor |
| DELETE | /api/asesores/{id} | Elimina asesor |

**Body para POST**:
```json
{
  "personaId": 1,
  "email": "asesor@example.com",
  "telefono": "123456789"
}
```

### Monitores
Base: `/api/monitores`  
**Permiso requerido**: Gestionar usuarios

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/monitores | Lista monitores |
| GET | /api/monitores/{id} | Consulta monitor por ID |
| POST | /api/monitores | Crea monitor |
| PUT | /api/monitores/{id} | Actualiza monitor |
| DELETE | /api/monitores/{id} | Elimina monitor |

### Administrativos
Base: `/api/administrativos`  
**Permiso requerido**: Gestionar usuarios

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/administrativos | Lista administrativos |
| GET | /api/administrativos/{id} | Consulta administrativo por ID |
| POST | /api/administrativos | Crea administrativo |
| PUT | /api/administrativos/{id} | Actualiza administrativo |
| DELETE | /api/administrativos/{id} | Elimina administrativo |

### Conciliadores
Base: `/api/conciliadores`  
**Permiso requerido**: Gestionar usuarios

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/conciliadores | Lista conciliadores |
| GET | /api/conciliadores/{id} | Consulta conciliador por ID |
| POST | /api/conciliadores | Crea conciliador |
| PUT | /api/conciliadores/{id} | Actualiza conciliador |
| DELETE | /api/conciliadores/{id} | Elimina conciliador |

### Estudiantes
Base: `/api/estudiantes`  
**Permiso requerido**: Gestionar usuarios

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/estudiantes | Lista estudiantes |
| GET | /api/estudiantes/{id} | Consulta estudiante por ID |
| POST | /api/estudiantes | Crea estudiante |
| PUT | /api/estudiantes/{id} | Actualiza estudiante |
| DELETE | /api/estudiantes/{id} | Elimina estudiante |

---

## 10. Sección de Errores

### 400 Bad Request
Error de validación en datos de entrada.  
**Respuesta**:
```json
{
  "error": "Bad Request",
  "message": "Campo requerido: nombre",
  "status": 400
}
```

### 401 Unauthorized
Usuario no autenticado.  
**Respuesta**:
```json
{
  "error": "Unauthorized",
  "message": "Credenciales inválidas",
  "status": 401
}
```

### 403 Forbidden
Usuario autenticado pero sin permisos.  
**Respuesta**:
```json
{
  "error": "Forbidden",
  "message": "Permiso requerido: Gestionar usuarios",
  "status": 403
}
```

### 500 Internal Server Error
Error interno del servidor.  
**Respuesta**:
```json
{
  "error": "Internal Server Error",
  "message": "Error inesperado",
  "status": 500
}
```

---

## 11. Alcance

Este documento describe los endpoints implementados y su estructura. Las validaciones específicas, reglas de negocio y detalles de modelos se documentan por separado.