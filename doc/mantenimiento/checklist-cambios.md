# Checklist de cambios

Este checklist debe usarse antes de commitear cambios funcionales, técnicos o documentales.

## 1. Identificación del cambio

Antes de actualizar documentación, identificar el tipo de cambio:

- endpoint;
- DTO;
- entidad;
- repository;
- service;
- validator;
- mapper;
- permiso;
- alcance;
- estado;
- configuración;
- frontend;
- base de datos;
- archivo;
- decisión técnica.

## 2. Checklist general

Antes de hacer commit:

- [ ] El código compila o fue validado según el alcance del cambio.
- [ ] La documentación afectada fue revisada.
- [ ] No se documentaron valores sensibles.
- [ ] No se agregaron archivos generados.
- [ ] No se agregaron rutas privadas locales.
- [ ] No se agregaron usuarios reales de prueba.
- [ ] No se documentaron endpoints inexistentes.
- [ ] No se documentaron reglas futuras como vigentes.
- [ ] Los ejemplos usan datos genéricos.
- [ ] Los permisos documentados coinciden con el código.
- [ ] Los estados documentados coinciden con enums o catálogos reales.
- [ ] Los request/response coinciden con DTOs actuales.

## 3. Checklist para endpoints

Cuando se crea, modifica o elimina un endpoint:

- [ ] Revisar controller.
- [ ] Revisar `@RequestMapping`, método y ruta.
- [ ] Revisar `@PreAuthorize`.
- [ ] Revisar parámetros de path.
- [ ] Revisar query params.
- [ ] Revisar body JSON.
- [ ] Revisar multipart si aplica.
- [ ] Revisar response.
- [ ] Revisar errores de negocio esperados.
- [ ] Actualizar `doc/api/<modulo>.md`.
- [ ] Actualizar backend del módulo si cambia responsabilidad.
- [ ] Actualizar reglas si cambia comportamiento.

## 4. Checklist para DTOs

Cuando cambia un DTO:

- [ ] Revisar campos nuevos o eliminados.
- [ ] Revisar tipos de dato.
- [ ] Revisar validaciones Jakarta.
- [ ] Revisar campos obligatorios.
- [ ] Revisar campos opcionales.
- [ ] Revisar ejemplos JSON.
- [ ] Actualizar documento API del módulo.
- [ ] Actualizar backend del módulo si afecta reglas.
- [ ] Revisar frontend si consume el DTO.

## 5. Checklist para reglas de negocio

Cuando cambia una regla:

- [ ] Identificar validator o service modificado.
- [ ] Identificar endpoints afectados.
- [ ] Identificar errores esperados.
- [ ] Identificar permisos o alcance afectados.
- [ ] Actualizar `doc/reglas/<modulo>.md`.
- [ ] Actualizar `doc/backend/<modulo>.md`.
- [ ] Actualizar `doc/api/<modulo>.md`.
- [ ] Actualizar decisiones técnicas si cambia criterio estructural.
- [ ] Revisar base de datos si cambia nullable, único o relación.

## 6. Checklist para permisos

Cuando cambia un permiso:

- [ ] Revisar `PermisoNombre`.
- [ ] Revisar `SecurityDataInitializer`.
- [ ] Revisar controllers con `@PreAuthorize`.
- [ ] Revisar access services.
- [ ] Revisar alcance.
- [ ] Revisar asignación rol-permiso.
- [ ] Revisar frontend si el permiso afecta navegación.
- [ ] Actualizar `doc/reglas/permisos.md`.
- [ ] Actualizar `doc/decisiones/permisos-y-alcance.md`.
- [ ] Actualizar API del módulo afectado.

## 7. Checklist para estados

Cuando cambia un estado:

- [ ] Revisar enum o catálogo.
- [ ] Revisar validators.
- [ ] Revisar services de estado.
- [ ] Revisar cierre de consulta si aplica.
- [ ] Revisar endpoints que reciben estado.
- [ ] Revisar DTOs de respuesta.
- [ ] Actualizar `doc/base-datos/estados-y-catalogos.md`.
- [ ] Actualizar reglas del módulo.
- [ ] Actualizar API del módulo.
- [ ] Actualizar backend del módulo.
- [ ] Actualizar decisiones si cambia criterio de diseño.

## 8. Checklist para base de datos

Cuando cambia una entidad o tabla:

- [ ] Revisar entidad JPA.
- [ ] Revisar relaciones.
- [ ] Revisar nullable.
- [ ] Revisar unique constraints.
- [ ] Revisar defaults.
- [ ] Revisar repositories.
- [ ] Revisar services que dependen del campo.
- [ ] Actualizar `doc/base-datos/entidades-principales.md`.
- [ ] Actualizar `doc/base-datos/estados-y-catalogos.md` si afecta estados/catálogos.
- [ ] Actualizar API si se expone el campo.
- [ ] Actualizar reglas si cambia comportamiento.

## 9. Checklist para archivos

Cuando cambia manejo de archivos:

- [ ] Revisar `FileUploadController`.
- [ ] Revisar `FileStorageService`.
- [ ] Revisar validaciones de extensión o content type.
- [ ] Revisar ruta estándar del módulo.
- [ ] Revisar endpoints multipart.
- [ ] Revisar response.
- [ ] Actualizar `doc/api/archivos.md`.
- [ ] Actualizar `doc/backend/archivos.md`.
- [ ] Actualizar `doc/reglas/archivos.md`.
- [ ] Actualizar API del módulo que consume el archivo.

## 10. Checklist para frontend

Cuando cambia frontend:

- [ ] Identificar si cambia contrato con backend.
- [ ] Si cambia endpoint consumido, actualizar API.
- [ ] Si cambia manejo de sesión, actualizar autenticación.
- [ ] Si cambia navegación por permisos, actualizar permisos.
- [ ] Si cambia estructura interna estable, actualizar documentación frontend.
- [ ] Verificar que no se documenten secretos en variables `NEXT_PUBLIC_*`.

## 11. Checklist de seguridad documental

Antes de cerrar documentación:

- [ ] No hay contraseñas reales.
- [ ] No hay tokens reales.
- [ ] No hay API keys reales.
- [ ] No hay secretos JWT reales.
- [ ] No hay usuarios reales de prueba con contraseña.
- [ ] No hay datos personales reales.
- [ ] No hay rutas privadas locales.
- [ ] No hay capturas con datos sensibles.
- [ ] Los ejemplos usan placeholders o valores genéricos.

## 12. Checklist de commit documental

Antes de commit:

- [ ] `git status` muestra solo archivos esperados.
- [ ] No hay archivos generados no deseados.
- [ ] La documentación nueva se ubica en carpeta correcta.
- [ ] El mensaje de commit indica área afectada.
- [ ] Si el cambio fue funcional, la documentación acompaña el commit o queda en commit inmediatamente posterior.

Ejemplos de commit:

```bash
git commit -m "docs(api): actualizar contrato de consultas"
```

```bash
git commit -m "docs(reglas): actualizar reglas de procesos"
```

```bash
git commit -m "docs(db): actualizar estados de conciliacion"
```
