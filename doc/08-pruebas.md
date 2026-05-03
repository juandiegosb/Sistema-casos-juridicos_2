# Pruebas

## 1. Propósito

Este documento describe los casos de prueba manuales/API para validar el comportamiento del backend del sistema de gestión de casos jurídicos. Las pruebas se basan en los endpoints implementados y las reglas de negocio del código fuente.

## 2. Enfoque

Las pruebas son manuales/API, ejecutadas mediante herramientas como Postman o curl, verificando respuestas HTTP, códigos de estado y contenido JSON.

## 3. Criterios de prueba

Cada caso incluye:
- **Endpoint**: URL y método HTTP.
- **Condición inicial**: Estado del sistema.
- **Datos enviados**: Body, headers, cookies.
- **Resultado esperado**: Código HTTP, respuesta, efectos en BD.

## 4. Pruebas de autenticación

### Login correcto
- **Endpoint**: POST /api/auth/login
- **Condición inicial**: Usuario existente, activo, rol activo, perfil activo.
- **Datos enviados**: {"username": "usuario@ejemplo.com", "password": "contraseña_correcta"}
- **Resultado esperado**: 200 OK, cookie "access_token" HttpOnly creada, body con id, username, rol, perfil, permisos.

### Login incorrecto (usuario/contraseña inválidos)
- **Endpoint**: POST /api/auth/login
- **Condición inicial**: Usuario existente.
- **Datos enviados**: {"username": "usuario@ejemplo.com", "password": "contraseña_incorrecta"}
- **Resultado esperado**: 400 Bad Request, mensaje "Usuario o contraseña incorrectos", sin cookie.

### Login usuario inactivo
- **Endpoint**: POST /api/auth/login
- **Condición inicial**: Usuario con activo=false.
- **Datos enviados**: Credenciales válidas.
- **Resultado esperado**: 400 Bad Request, mensaje "El usuario se encuentra inactivo".

### Login rol inactivo
- **Endpoint**: POST /api/auth/login
- **Condición inicial**: Usuario con rol activo=false.
- **Datos enviados**: Credenciales válidas.
- **Resultado esperado**: 400 Bad Request, mensaje "El rol del usuario se encuentra inactivo".

### Login perfil inactivo
- **Endpoint**: POST /api/auth/login
- **Condición inicial**: Usuario con perfil (asesor, etc.) activo=false.
- **Datos enviados**: Credenciales válidas.
- **Resultado esperado**: 400 Bad Request, mensaje "El asesor asociado se encuentra inactivo" (ejemplo para asesor).

### Cookie creada
- **Endpoint**: POST /api/auth/login
- **Condición inicial**: Login exitoso.
- **Datos enviados**: Credenciales válidas.
- **Resultado esperado**: Cookie "access_token" con HttpOnly=true, Secure=false, Path=/, Max-Age=3600, SameSite=Lax.

### /me con cookie válida
- **Endpoint**: GET /api/auth/me
- **Condición inicial**: Cookie "access_token" válida.
- **Datos enviados**: Cookie válida.
- **Resultado esperado**: 200 OK, body con id, username, rol, permisos, perfil.

### /me sin cookie
- **Endpoint**: GET /api/auth/me
- **Condición inicial**: Sin cookie.
- **Datos enviados**: Sin cookie.
- **Resultado esperado**: 400 Bad Request, mensaje "No hay sesión activa".

### /me con cookie expirada
- **Endpoint**: GET /api/auth/me
- **Condición inicial**: Cookie con token expirado.
- **Datos enviados**: Cookie expirada.
- **Resultado esperado**: 400 Bad Request, mensaje "Token inválido o expirado".

### Logout
- **Endpoint**: POST /api/auth/logout
- **Condición inicial**: Cookie válida.
- **Datos enviados**: Sin body.
- **Resultado esperado**: 200 OK, cookie "access_token" con Max-Age=0 (eliminada).

## 5. Pruebas de autorización

### Sin sesión -> 401
- **Endpoint**: Cualquier endpoint protegido (ej. GET /api/asesores)
- **Condición inicial**: Sin cookie.
- **Datos enviados**: Sin cookie.
- **Resultado esperado**: 401 Unauthorized, mensaje personalizado.

### Con sesión pero sin permiso -> 403
- **Endpoint**: Cualquier endpoint con @PreAuthorize (ej. GET /api/asesores)
- **Condición inicial**: Usuario logueado sin permiso "Gestionar usuarios".
- **Datos enviados**: Cookie válida.
- **Resultado esperado**: 403 Forbidden, mensaje personalizado.

### Con permiso -> acceso correcto
- **Endpoint**: Cualquier endpoint con @PreAuthorize (ej. GET /api/asesores)
- **Condición inicial**: Usuario logueado con permiso requerido.
- **Datos enviados**: Cookie válida.
- **Resultado esperado**: 200 OK, respuesta normal.

## 6. Pruebas de permisos

### Listar permisos
- **Endpoint**: GET /api/permisos
- **Condición inicial**: Usuario con "Gestionar permisos".
- **Datos enviados**: Cookie válida.
- **Resultado esperado**: 200 OK, lista de permisos con id, nombre, descripcion, activo.

### Crear permiso válido
- **Endpoint**: POST /api/permisos
- **Condición inicial**: Usuario con "Gestionar permisos".
- **Datos enviados**: {"nombre": "Nuevo permiso", "descripcion": "Descripción"}
- **Resultado esperado**: 201 Created, permiso creado con id autogenerado, activo=true.

### Crear permiso duplicado
- **Endpoint**: POST /api/permisos
- **Condición inicial**: Permiso con nombre existente.
- **Datos enviados**: {"nombre": "Permiso existente", "descripcion": "Descripción"}
- **Resultado esperado**: 400 Bad Request, mensaje "Ya existe un permiso con ese nombre".

### Actualizar permiso
- **Endpoint**: PUT /api/permisos/{id}
- **Condición inicial**: Permiso existente.
- **Datos enviados**: {"nombre": "Nombre actualizado", "descripcion": "Descripción actualizada"}
- **Resultado esperado**: 200 OK, permiso actualizado.

### Activar/desactivar permiso
- **Endpoint**: PATCH /api/permisos/{id}/activo
- **Condición inicial**: Permiso existente.
- **Datos enviados**: ?activo=false
- **Resultado esperado**: 200 OK, permiso con activo=false.

## 7. Pruebas de roles

### Listar roles
- **Endpoint**: GET /api/roles
- **Condición inicial**: Usuario con "Gestionar roles".
- **Datos enviados**: Cookie válida.
- **Resultado esperado**: 200 OK, lista de roles con permisos.

### Crear rol válido
- **Endpoint**: POST /api/roles
- **Condición inicial**: Usuario con "Gestionar roles".
- **Datos enviados**: {"nombre": "Nuevo rol", "descripcion": "Descripción"}
- **Resultado esperado**: 201 Created, rol creado con permisos vacíos.

### Crear rol duplicado
- **Endpoint**: POST /api/roles
- **Condición inicial**: Rol con nombre existente.
- **Datos enviados**: {"nombre": "Rol existente", "descripcion": "Descripción"}
- **Resultado esperado**: 400 Bad Request, mensaje "Ya existe un rol con ese nombre".

### Actualizar rol
- **Endpoint**: PUT /api/roles/{id}
- **Condición inicial**: Rol existente.
- **Datos enviados**: {"nombre": "Nombre actualizado", "descripcion": "Descripción actualizada"}
- **Resultado esperado**: 200 OK, rol actualizado.

### Activar/desactivar rol
- **Endpoint**: PATCH /api/roles/{id}/activo
- **Condición inicial**: Rol existente.
- **Datos enviados**: ?activo=false
- **Resultado esperado**: 200 OK, rol con activo=false.

### Asignar permiso a rol
- **Endpoint**: POST /api/roles/{id}/permisos
- **Condición inicial**: Rol y permiso existentes.
- **Datos enviados**: {"permisoId": 1}
- **Resultado esperado**: 200 OK, permiso agregado al rol.

### Quitar permiso de rol
- **Endpoint**: DELETE /api/roles/{id}/permisos/{permisoId}
- **Condición inicial**: Rol con permiso asignado.
- **Datos enviados**: Sin body.
- **Resultado esperado**: 204 No Content, permiso removido del rol.

## 8. Pruebas de usuarios del sistema

### Listar usuarios
- **Endpoint**: GET /api/usuarios-sistema
- **Condición inicial**: Usuario con "Gestionar usuarios".
- **Datos enviados**: Cookie válida.
- **Resultado esperado**: 200 OK, lista de usuarios con rol y perfil.

### Listar usuarios activos
- **Endpoint**: GET /api/usuarios-sistema/activos
- **Condición inicial**: Usuario con "Gestionar usuarios".
- **Datos enviados**: Cookie válida.
- **Resultado esperado**: 200 OK, lista filtrada por activo=true.

### Obtener usuario por id
- **Endpoint**: GET /api/usuarios-sistema/{id}
- **Condición inicial**: Usuario existente.
- **Datos enviados**: Cookie válida.
- **Resultado esperado**: 200 OK, usuario con rol y perfil.

### Cambiar estado usuario
- **Endpoint**: PATCH /api/usuarios-sistema/{id}/activo
- **Condición inicial**: Usuario existente.
- **Datos enviados**: ?activo=false
- **Resultado esperado**: 200 OK, usuario con activo=false.

## 9. Pruebas CRUD de usuarios internos

### Asesor

#### Crear asesor válido
- **Endpoint**: POST /api/asesores
- **Condición inicial**: Usuario con "Gestionar usuarios", tipoDocumento, sede, area existentes.
- **Datos enviados**: Datos completos válidos.
- **Resultado esperado**: 201 Created, asesor creado, UsuarioSistema generado automáticamente.

#### Crear asesor con duplicados
- **Endpoint**: POST /api/asesores
- **Condición inicial**: Documento/email/telefono/usuario/codigo existente.
- **Datos enviados**: Datos con duplicado.
- **Resultado esperado**: 400 Bad Request, mensaje específico de duplicado.

#### Listar asesores
- **Endpoint**: GET /api/asesores
- **Condición inicial**: Usuario con "Gestionar usuarios".
- **Datos enviados**: Cookie válida.
- **Resultado esperado**: 200 OK, lista de asesores.

#### Obtener asesor por id
- **Endpoint**: GET /api/asesores/{id}
- **Condición inicial**: Asesor existente.
- **Datos enviados**: Cookie válida.
- **Resultado esperado**: 200 OK, asesor específico.

#### Actualizar asesor
- **Endpoint**: PUT /api/asesores/{id}
- **Condición inicial**: Asesor existente.
- **Datos enviados**: Datos actualizados válidos.
- **Resultado esperado**: 200 OK, asesor actualizado.

#### Activar/desactivar asesor
- **Endpoint**: PATCH /api/asesores/{id}/activo
- **Condición inicial**: Asesor existente.
- **Datos enviados**: ?activo=false
- **Resultado esperado**: 200 OK, asesor desactivado.

#### Verificar creación automática de UsuarioSistema
- **Endpoint**: POST /api/asesores
- **Condición inicial**: Creación exitosa.
- **Datos enviados**: Datos válidos.
- **Resultado esperado**: UsuarioSistema creado con username=email, rol por defecto.

#### Probar login con usuario creado
- **Endpoint**: POST /api/auth/login
- **Condición inicial**: Asesor creado.
- **Datos enviados**: username=email, password=generada.
- **Resultado esperado**: 200 OK, login exitoso.

### Monitor
- Pruebas similares a Asesor, sin area.

### Administrativo
- Pruebas similares a Asesor, campos opcionales (tipoDocumento, documento, sede).

### Conciliador
- Pruebas similares a Asesor, sin area.

### Estudiante
- Pruebas similares a Asesor, con asesorId obligatorio.

## 10. Pruebas de catálogos

### TipoDocumento
- **Crear**: POST /api/tipos-documento, datos válidos -> 201 Created.
- **Duplicados**: Nombre duplicado -> 400 Bad Request.
- **Listar**: GET /api/tipos-documento -> 200 OK.
- **Actualizar**: PUT /api/tipos-documento/{id} -> 200 OK.
- **Activar/desactivar**: PATCH /api/tipos-documento/{id}/activo -> 200 OK.

### Sede
- Pruebas similares a TipoDocumento.

### Area
- **Crear**: POST /api/areas, nombre único -> 201 Created.
- **Duplicados**: Nombre existente -> 400 Bad Request.
- **Listar**: GET /api/areas -> 200 OK.
- **Actualizar**: PUT /api/areas/{id}, nombre único -> 200 OK.
- **Activar/desactivar**: PATCH /api/areas/{id}/activo -> 200 OK.

### Tema
- **Crear**: POST /api/temas, nombre + areaId existente -> 201 Created.
- **Listar**: GET /api/temas -> 200 OK.
- **Actualizar**: PUT /api/temas/{id} -> 200 OK.
- **Activar/desactivar**: PATCH /api/temas/{id}/activo -> 200 OK.

### Tipo
- **Crear**: POST /api/tipos, nombre + temaId existente -> 201 Created.
- **Listar**: GET /api/tipos -> 200 OK.
- **Actualizar**: PUT /api/tipos/{id} -> 200 OK.
- **Activar/desactivar**: PATCH /api/tipos/{id}/activo -> 200 OK.

## 11. Pruebas de persona

### Crear persona válida
- **Endpoint**: POST /api/personas
- **Condición inicial**: Usuario con "Gestionar personas".
- **Datos enviados**: Datos completos válidos.
- **Resultado esperado**: 201 Created, persona creada.

### Crear persona con documento duplicado
- **Endpoint**: POST /api/personas
- **Condición inicial**: Documento existente.
- **Datos enviados**: Datos con documento duplicado.
- **Resultado esperado**: 400 Bad Request, mensaje "Ya existe una persona con ese documento".

### Listar personas
- **Endpoint**: GET /api/personas
- **Condición inicial**: Usuario con "Gestionar personas".
- **Datos enviados**: Cookie válida.
- **Resultado esperado**: 200 OK, lista de personas.

### Actualizar persona
- **Endpoint**: PUT /api/personas/{id}
- **Condición inicial**: Persona existente.
- **Datos enviados**: Datos actualizados.
- **Resultado esperado**: 200 OK, persona actualizada.

## 12. Pruebas de cambio de contraseña

### Cambiar contraseña válida
- **Endpoint**: PATCH /api/auth/cambiar-password
- **Condición inicial**: Usuario logueado.
- **Datos enviados**: Cookie válida, {"passwordActual": "actual", "passwordNueva": "nueva123"}
- **Resultado esperado**: 204 No Content, contraseña actualizada en BD.

### Cambiar contraseña con actual incorrecta
- **Endpoint**: PATCH /api/auth/cambiar-password
- **Condición inicial**: Usuario logueado.
- **Datos enviados**: Cookie válida, passwordActual incorrecta.
- **Resultado esperado**: 400 Bad Request, mensaje "La contraseña actual no es correcta".

### Cambiar contraseña igual a actual
- **Endpoint**: PATCH /api/auth/cambiar-password
- **Condición inicial**: Usuario logueado.
- **Datos enviados**: Cookie válida, passwordNueva = passwordActual.
- **Resultado esperado**: 400 Bad Request, mensaje "La nueva contraseña no puede ser igual a la actual".

## 13. Pruebas de recuperación de contraseña

### Solicitar recuperación con email existente
- **Endpoint**: POST /api/auth/solicitar-recuperacion
- **Condición inicial**: Usuario existente, activo, rol activo, perfil activo.
- **Datos enviados**: {"username": "usuario@ejemplo.com"}
- **Resultado esperado**: 200 OK, mensaje genérico, correo enviado (verificar SMTP).

### Solicitar recuperación con email inexistente
- **Endpoint**: POST /api/auth/solicitar-recuperacion
- **Condición inicial**: Email no existe.
- **Datos enviados**: {"username": "noexiste@ejemplo.com"}
- **Resultado esperado**: 200 OK, mensaje genérico (sin revelar existencia).

### Correo enviado
- **Condición inicial**: Solicitud exitosa.
- **Resultado esperado**: Correo recibido con enlace ${frontend.reset-password-url}?token=${token}.

### Token válido
- **Endpoint**: POST /api/auth/restablecer-password
- **Condición inicial**: Token válido, no expirado, no usado.
- **Datos enviados**: {"token": "token_valido", "passwordNueva": "nueva123", "confirmarPassword": "nueva123"}
- **Resultado esperado**: 200 OK, mensaje "La contraseña se restableció correctamente", contraseña actualizada.

### Token reutilizado
- **Endpoint**: POST /api/auth/restablecer-password
- **Condición inicial**: Token ya usado.
- **Datos enviados**: Token usado.
- **Resultado esperado**: 400 Bad Request, mensaje "Token inválido o expirado".

### Token inválido
- **Endpoint**: POST /api/auth/restablecer-password
- **Condición inicial**: Token no existe o malformado.
- **Datos enviados**: Token inválido.
- **Resultado esperado**: 400 Bad Request, mensaje "Token inválido o expirado".

### Login con nueva contraseña
- **Endpoint**: POST /api/auth/login
- **Condición inicial**: Contraseña restablecida.
- **Datos enviados**: username y nueva contraseña.
- **Resultado esperado**: 200 OK, login exitoso.

# Plan de pruebas

## 1. Propósito

Este documento describe los casos de prueba manuales/API para validar el comportamiento del backend del sistema de gestión de casos jurídicos. Las pruebas se basan en los endpoints implementados y las reglas de negocio del código fuente.

## 2. Enfoque

Las pruebas son manuales/API, ejecutadas mediante herramientas como Postman o curl, verificando respuestas HTTP, códigos de estado y contenido JSON.

## 3. Criterios de prueba

Cada caso incluye:
- **Endpoint**: URL y método HTTP.
- **Condición inicial**: Estado del sistema.
- **Datos enviados**: Body, headers, cookies.
- **Resultado esperado**: Código HTTP, respuesta, efectos en BD.

## 4. Pruebas de autenticación

### Login correcto
- **Endpoint**: POST /api/auth/login
- **Condición inicial**: Usuario existente, activo, rol activo, perfil activo.
- **Datos enviados**: {"username": "usuario@ejemplo.com", "password": "contraseña_correcta"}
- **Resultado esperado**: 200 OK, cookie "access_token" HttpOnly creada, body con id, username, rol, perfil, permisos.

### Login incorrecto (usuario/contraseña inválidos)
- **Endpoint**: POST /api/auth/login
- **Condición inicial**: Usuario existente.
- **Datos enviados**: {"username": "usuario@ejemplo.com", "password": "contraseña_incorrecta"}
- **Resultado esperado**: 400 Bad Request, mensaje "Usuario o contraseña incorrectos", sin cookie.

### Login usuario inactivo
- **Endpoint**: POST /api/auth/login
- **Condición inicial**: Usuario con activo=false.
- **Datos enviados**: Credenciales válidas.
- **Resultado esperado**: 400 Bad Request, mensaje "El usuario se encuentra inactivo".

### Login rol inactivo
- **Endpoint**: POST /api/auth/login
- **Condición inicial**: Usuario con rol activo=false.
- **Datos enviados**: Credenciales válidas.
- **Resultado esperado**: 400 Bad Request, mensaje "El rol del usuario se encuentra inactivo".

### Login perfil inactivo
- **Endpoint**: POST /api/auth/login
- **Condición inicial**: Usuario con perfil (asesor, etc.) activo=false.
- **Datos enviados**: Credenciales válidas.
- **Resultado esperado**: 400 Bad Request, mensaje "El asesor asociado se encuentra inactivo" (ejemplo para asesor).

### Cookie creada
- **Endpoint**: POST /api/auth/login
- **Condición inicial**: Login exitoso.
- **Datos enviados**: Credenciales válidas.
- **Resultado esperado**: Cookie "access_token" con HttpOnly=true, Secure=false, Path=/, Max-Age=3600, SameSite=Lax.

### /me con cookie válida
- **Endpoint**: GET /api/auth/me
- **Condición inicial**: Cookie "access_token" válida.
- **Datos enviados**: Cookie válida.
- **Resultado esperado**: 200 OK, body con id, username, rol, permisos, perfil.

### /me sin cookie
- **Endpoint**: GET /api/auth/me
- **Condición inicial**: Sin cookie.
- **Datos enviados**: Sin cookie.
- **Resultado esperado**: 400 Bad Request, mensaje "No hay sesión activa".

### /me con cookie expirada
- **Endpoint**: GET /api/auth/me
- **Condición inicial**: Cookie con token expirado.
- **Datos enviados**: Cookie expirada.
- **Resultado esperado**: 400 Bad Request, mensaje "Token inválido o expirado".

### Logout
- **Endpoint**: POST /api/auth/logout
- **Condición inicial**: Cookie válida.
- **Datos enviados**: Sin body.
- **Resultado esperado**: 200 OK, cookie "access_token" con Max-Age=0 (eliminada).

## 5. Pruebas de autorización

### Sin sesión -> 401
- **Endpoint**: Cualquier endpoint protegido (ej. GET /api/asesores)
- **Condición inicial**: Sin cookie.
- **Datos enviados**: Sin cookie.
- **Resultado esperado**: 401 Unauthorized, mensaje personalizado.

### Con sesión pero sin permiso -> 403
- **Endpoint**: Cualquier endpoint con @PreAuthorize (ej. GET /api/asesores)
- **Condición inicial**: Usuario logueado sin permiso "Gestionar usuarios".
- **Datos enviados**: Cookie válida.
- **Resultado esperado**: 403 Forbidden, mensaje personalizado.

### Con permiso -> acceso correcto
- **Endpoint**: Cualquier endpoint con @PreAuthorize (ej. GET /api/asesores)
- **Condición inicial**: Usuario logueado con permiso requerido.
- **Datos enviados**: Cookie válida.
- **Resultado esperado**: 200 OK, respuesta normal.

## 6. Pruebas de seguridad: permisos, roles y usuarios del sistema

### Permisos

#### Listar permisos
- **Endpoint**: GET /api/permisos
- **Condición inicial**: Usuario con "Gestionar permisos".
- **Datos enviados**: Cookie válida.
- **Resultado esperado**: 200 OK, lista de permisos con id, nombre, descripcion, activo.

#### Crear permiso válido
- **Endpoint**: POST /api/permisos
- **Condición inicial**: Usuario con "Gestionar permisos".
- **Datos enviados**: {"nombre": "Nuevo permiso", "descripcion": "Descripción"}
- **Resultado esperado**: 201 Created, permiso creado con id autogenerado, activo=true.

#### Crear permiso duplicado
- **Endpoint**: POST /api/permisos
- **Condición inicial**: Permiso con nombre existente.
- **Datos enviados**: {"nombre": "Permiso existente", "descripcion": "Descripción"}
- **Resultado esperado**: 400 Bad Request, mensaje "Ya existe un permiso con ese nombre".

#### Actualizar permiso
- **Endpoint**: PUT /api/permisos/{id}
- **Condición inicial**: Permiso existente.
- **Datos enviados**: {"nombre": "Nombre actualizado", "descripcion": "Descripción actualizada"}
- **Resultado esperado**: 200 OK, permiso actualizado.

#### Activar/desactivar permiso
- **Endpoint**: PATCH /api/permisos/{id}/activo
- **Condición inicial**: Permiso existente.
- **Datos enviados**: ?activo=false
- **Resultado esperado**: 200 OK, permiso con activo=false.

### Roles

#### Listar roles
- **Endpoint**: GET /api/roles
- **Condición inicial**: Usuario con "Gestionar roles".
- **Datos enviados**: Cookie válida.
- **Resultado esperado**: 200 OK, lista de roles con permisos.

#### Crear rol válido
- **Endpoint**: POST /api/roles
- **Condición inicial**: Usuario con "Gestionar roles".
- **Datos enviados**: {"nombre": "Nuevo rol", "descripcion": "Descripción"}
- **Resultado esperado**: 201 Created, rol creado con permisos vacíos.

#### Crear rol duplicado
- **Endpoint**: POST /api/roles
- **Condición inicial**: Rol con nombre existente.
- **Datos enviados**: {"nombre": "Rol existente", "descripcion": "Descripción"}
- **Resultado esperado**: 400 Bad Request, mensaje "Ya existe un rol con ese nombre".

#### Actualizar rol
- **Endpoint**: PUT /api/roles/{id}
- **Condición inicial**: Rol existente.
- **Datos enviados**: {"nombre": "Nombre actualizado", "descripcion": "Descripción actualizada"}
- **Resultado esperado**: 200 OK, rol actualizado.

#### Activar/desactivar rol
- **Endpoint**: PATCH /api/roles/{id}/activo
- **Condición inicial**: Rol existente.
- **Datos enviados**: ?activo=false
- **Resultado esperado**: 200 OK, rol con activo=false.

#### Asignar permiso a rol
- **Endpoint**: POST /api/roles/{id}/permisos
- **Condición inicial**: Rol y permiso existentes.
- **Datos enviados**: {"permisoId": 1}
- **Resultado esperado**: 200 OK, permiso agregado al rol.

#### Quitar permiso de rol
- **Endpoint**: DELETE /api/roles/{id}/permisos/{permisoId}
- **Condición inicial**: Rol con permiso asignado.
- **Datos enviados**: Sin body.
- **Resultado esperado**: 204 No Content, permiso removido del rol.

### Usuarios del sistema

#### Listar usuarios
- **Endpoint**: GET /api/usuarios-sistema
- **Condición inicial**: Usuario con "Gestionar usuarios".
- **Datos enviados**: Cookie válida.
- **Resultado esperado**: 200 OK, lista de usuarios con rol y perfil.

#### Listar usuarios activos
- **Endpoint**: GET /api/usuarios-sistema/activos
- **Condición inicial**: Usuario con "Gestionar usuarios".
- **Datos enviados**: Cookie válida.
- **Resultado esperado**: 200 OK, lista filtrada por activo=true.

#### Obtener usuario por id
- **Endpoint**: GET /api/usuarios-sistema/{id}
- **Condición inicial**: Usuario existente.
- **Datos enviados**: Cookie válida.
- **Resultado esperado**: 200 OK, usuario con rol y perfil.

#### Cambiar estado usuario
- **Endpoint**: PATCH /api/usuarios-sistema/{id}/activo
- **Condición inicial**: Usuario existente.
- **Datos enviados**: ?activo=false
- **Resultado esperado**: 200 OK, usuario con activo=false.

## 7. Pruebas de usuarios internos

### Asesor

#### Crear asesor válido
- **Endpoint**: POST /api/asesores
- **Condición inicial**: Usuario con "Gestionar usuarios", tipoDocumento, sede, area existentes.
- **Datos enviados**: Datos completos válidos.
- **Resultado esperado**: 201 Created, asesor creado, UsuarioSistema generado automáticamente.

#### Crear asesor con duplicados
- **Endpoint**: POST /api/asesores
- **Condición inicial**: Documento/email/telefono/usuario/codigo existente.
- **Datos enviados**: Datos con duplicado.
- **Resultado esperado**: 400 Bad Request, mensaje específico de duplicado.

#### Listar asesores
- **Endpoint**: GET /api/asesores
- **Condición inicial**: Usuario con "Gestionar usuarios".
- **Datos enviados**: Cookie válida.
- **Resultado esperado**: 200 OK, lista de asesores.

#### Obtener asesor por id
- **Endpoint**: GET /api/asesores/{id}
- **Condición inicial**: Asesor existente.
- **Datos enviados**: Cookie válida.
- **Resultado esperado**: 200 OK, asesor específico.

#### Actualizar asesor
- **Endpoint**: PUT /api/asesores/{id}
- **Condición inicial**: Asesor existente.
- **Datos enviados**: Datos actualizados válidos.
- **Resultado esperado**: 200 OK, asesor actualizado.

#### Activar/desactivar asesor
- **Endpoint**: PATCH /api/asesores/{id}/activo
- **Condición inicial**: Asesor existente.
- **Datos enviados**: ?activo=false
- **Resultado esperado**: 200 OK, asesor desactivado.

#### Verificar creación automática de UsuarioSistema
- **Endpoint**: POST /api/asesores
- **Condición inicial**: Creación exitosa.
- **Datos enviados**: Datos válidos.
- **Resultado esperado**: UsuarioSistema creado con username=email, rol por defecto.

#### Probar login con usuario creado
- **Endpoint**: POST /api/auth/login
- **Condición inicial**: Asesor creado.
- **Datos enviados**: username=email, password=generada.
- **Resultado esperado**: 200 OK, login exitoso.

### Monitor
- Pruebas similares a Asesor, sin area.

### Administrativo
- Pruebas similares a Asesor, campos opcionales (tipoDocumento, documento, sede).

### Conciliador
- Pruebas similares a Asesor, sin area.

### Estudiante
- Pruebas similares a Asesor, con asesorId obligatorio.

## 8. Pruebas de catálogos

### TipoDocumento
- **Crear**: POST /api/tipos-documento, datos válidos -> 201 Created.
- **Duplicados**: Nombre duplicado -> 400 Bad Request.
- **Listar**: GET /api/tipos-documento -> 200 OK.
- **Actualizar**: PUT /api/tipos-documento/{id} -> 200 OK.

### Sede
- Pruebas similares a TipoDocumento.

### Area
- **Crear**: POST /api/areas, nombre único -> 201 Created.
- **Duplicados**: Nombre existente -> 400 Bad Request.
- **Listar**: GET /api/areas -> 200 OK.
- **Actualizar**: PUT /api/areas/{id}, nombre único -> 200 OK.

### Tema
- **Crear**: POST /api/temas, nombre + areaId existente -> 201 Created.
- **Listar**: GET /api/temas -> 200 OK.
- **Actualizar**: PUT /api/temas/{id} -> 200 OK.

### Tipo
- **Crear**: POST /api/tipos, nombre + temaId existente -> 201 Created.
- **Listar**: GET /api/tipos -> 200 OK.
- **Actualizar**: PUT /api/tipos/{id} -> 200 OK.

## 9. Pruebas de persona

### Crear persona válida
- **Endpoint**: POST /api/personas
- **Condición inicial**: Usuario con "Gestionar personas".
- **Datos enviados**: Datos completos válidos.
- **Resultado esperado**: 201 Created, persona creada.

### Crear persona con documento duplicado
- **Endpoint**: POST /api/personas
- **Condición inicial**: Documento existente.
- **Datos enviados**: Datos con documento duplicado.
- **Resultado esperado**: 400 Bad Request, mensaje "Ya existe una persona con ese documento".

### Listar personas
- **Endpoint**: GET /api/personas
- **Condición inicial**: Usuario con "Gestionar personas".
- **Datos enviados**: Cookie válida.
- **Resultado esperado**: 200 OK, lista de personas.

### Actualizar persona
- **Endpoint**: PUT /api/personas/{id}
- **Condición inicial**: Persona existente.
- **Datos enviados**: Datos actualizados.
- **Resultado esperado**: 200 OK, persona actualizada.

## 10. Pruebas de consulta

Esta sección cubre endpoints de consulta específicos no incluidos en otras secciones, como búsquedas avanzadas o reportes.

## 11. Pruebas de cambio de contraseña

### Cambiar contraseña válida
- **Endpoint**: PATCH /api/auth/cambiar-password
- **Condición inicial**: Usuario logueado.
- **Datos enviados**: Cookie válida, {"passwordActual": "actual", "passwordNueva": "nueva123"}
- **Resultado esperado**: 204 No Content, contraseña actualizada en BD.

### Cambiar contraseña con actual incorrecta
- **Endpoint**: PATCH /api/auth/cambiar-password
- **Condición inicial**: Usuario logueado.
- **Datos enviados**: Cookie válida, passwordActual incorrecta.
- **Resultado esperado**: 400 Bad Request, mensaje "La contraseña actual no es correcta".

### Cambiar contraseña igual a actual
- **Endpoint**: PATCH /api/auth/cambiar-password
- **Condición inicial**: Usuario logueado.
- **Datos enviados**: Cookie válida, passwordNueva = passwordActual.
- **Resultado esperado**: 400 Bad Request, mensaje "La nueva contraseña no puede ser igual a la actual".

## 12. Pruebas de recuperación de contraseña

### Solicitar recuperación con email existente
- **Endpoint**: POST /api/auth/solicitar-recuperacion
- **Condición inicial**: Usuario existente, activo, rol activo, perfil activo.
- **Datos enviados**: {"username": "usuario@ejemplo.com"}
- **Resultado esperado**: 200 OK, mensaje genérico, correo enviado (verificar SMTP).

### Solicitar recuperación con email inexistente
- **Endpoint**: POST /api/auth/solicitar-recuperacion
- **Condición inicial**: Email no existe.
- **Datos enviados**: {"username": "noexiste@ejemplo.com"}
- **Resultado esperado**: 200 OK, mensaje genérico (sin revelar existencia).

### Correo enviado
- **Condición inicial**: Solicitud exitosa.
- **Resultado esperado**: Correo recibido con enlace ${frontend.reset-password-url}?token=${token}.

### Token válido
- **Endpoint**: POST /api/auth/restablecer-password
- **Condición inicial**: Token válido, no expirado, no usado.
- **Datos enviados**: {"token": "token_valido", "passwordNueva": "nueva123", "confirmarPassword": "nueva123"}
- **Resultado esperado**: 200 OK, mensaje "La contraseña se restableció correctamente", contraseña actualizada.

### Token reutilizado
- **Endpoint**: POST /api/auth/restablecer-password
- **Condición inicial**: Token ya usado.
- **Datos enviados**: Token usado.
- **Resultado esperado**: 400 Bad Request, mensaje "Token inválido o expirado".

### Token inválido
- **Endpoint**: POST /api/auth/restablecer-password
- **Condición inicial**: Token no existe o malformado.
- **Datos enviados**: Token inválido.
- **Resultado esperado**: 400 Bad Request, mensaje "Token inválido o expirado".

### Login con nueva contraseña
- **Endpoint**: POST /api/auth/login
- **Condición inicial**: Contraseña restablecida.
- **Datos enviados**: username y nueva contraseña.
- **Resultado esperado**: 200 OK, login exitoso.

## 13. Pruebas de integración frontend-backend

Esta sección cubre pruebas de integración entre el frontend React/Next.js y el backend Spring Boot, incluyendo formularios, navegación y manejo de errores.

## 14. Hallazgos y observaciones

### 403 manejado correctamente
- **Descripción**: Excepciones 403 personalizadas en SecurityExceptionHandler.
- **Prueba**: Acceso sin permiso -> 403 con mensaje personalizado.

### CORS con credenciales
- **Descripción**: Configuración CORS permite cookies.
- **Prueba**: Request con Origin y credentials -> headers CORS correctos.

### SMTP funcionando
- **Descripción**: EmailService envía correos via JavaMailSender.
- **Prueba**: Solicitar recuperación -> correo recibido en bandeja.

## 15. Alcance

Las pruebas cubren todos los endpoints implementados en el backend, incluyendo autenticación, autorización, gestión de usuarios, catálogos y funcionalidades específicas del dominio. Se priorizan casos críticos de seguridad y validaciones de negocio.