# API - Catálogos

Este documento describe los endpoints de catálogos generales y catálogos auxiliares usados por el sistema.

Los catálogos permiten alimentar formularios, normalizar información y mantener relaciones jerárquicas entre datos.

## Autenticación

Todos los endpoints documentados requieren sesión válida.

El frontend debe enviar:

```javascript
credentials: "include"
```

## Permisos generales

| Permiso | Uso |
|---|---|
| `Ver catálogos` | Consulta de catálogos activos y registros por id. |
| `Gestionar catálogos` | Administración de catálogos: crear, actualizar, listar todos, cambiar estado y desactivar. |

## Convención de endpoints

La mayoría de catálogos usa esta estructura:

| Método | Ruta | Uso |
|---|---|---|
| GET | `/api/{catalogo}` | Lista registros activos. |
| GET | `/api/{catalogo}/todos` | Lista registros activos e inactivos. |
| GET | `/api/{catalogo}/{id}` | Consulta registro por id. |
| POST | `/api/{catalogo}` | Crea registro. |
| PUT | `/api/{catalogo}/{id}` | Actualiza registro. |
| PATCH | `/api/{catalogo}/{id}/activo?activo=` | Cambia estado activo. |
| DELETE | `/api/{catalogo}/{id}` | Desactiva lógicamente. |

## DTO base de catálogos simples

Los catálogos simples usan una estructura general:

```json
{
  "id": 1,
  "nombre": "Nombre del registro",
  "activo": true
}
```

Para creación:

```json
{
  "nombre": "Nombre del registro"
}
```

Para actualización:

```json
{
  "id": 1,
  "nombre": "Nombre actualizado",
  "activo": true
}
```

Reglas generales:

- `id` no se envía en creación;
- si se envía `id` en actualización, debe coincidir con la ruta;
- `nombre` es obligatorio;
- `nombre` se normaliza;
- no se permiten duplicados según la regla de cada catálogo;
- `activo` se cambia mediante endpoint específico.

## Catálogos generales

| Catálogo | Base path | DTO |
|---|---|---|
| Áreas | `/api/areas` | `AreaDTO` |
| Departamentos | `/api/departamentos` | `DepartamentoDTO` |
| Nacionalidades | `/api/nacionalidades` | `NacionalidadDTO` |
| Sedes | `/api/sedes` | `SedeDTO` |
| Tipos de documento | `/api/tipos-documento` | `TipoDocumentoDTO` |

## Catálogos jerárquicos

| Catálogo | Base path | Relación |
|---|---|---|
| Municipios | `/api/municipios` | Municipio pertenece a departamento. |
| Barrios | `/api/barrios` | Barrio pertenece a municipio. |
| Temas | `/api/temas` | Tema pertenece a área. |
| Tipos | `/api/tipos` | Tipo pertenece a tema. |

## Catálogos propios de persona

| Catálogo | Base path | DTO |
|---|---|---|
| Condiciones | `/api/condiciones` | `CondicionDTO` |
| Empresas | `/api/empresas` | `EmpresaDTO` |
| Ocupaciones | `/api/ocupaciones` | `OcupacionDTO` |
| Tipos de persona | `/api/tipos-persona` | `TipoPersonaDTO` |

---

# Áreas

Base path:

```text
/api/areas
```

## Endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/areas` | `Ver catálogos` o `Gestionar catálogos` | Lista áreas activas. |
| GET | `/api/areas/todos` | `Gestionar catálogos` | Lista todas las áreas. |
| GET | `/api/areas/{id}` | `Ver catálogos` o `Gestionar catálogos` | Consulta área por id. |
| POST | `/api/areas` | `Gestionar catálogos` | Crea área. |
| PUT | `/api/areas/{id}` | `Gestionar catálogos` | Actualiza área. |
| PATCH | `/api/areas/{id}/activo?activo=` | `Gestionar catálogos` | Cambia estado activo. |
| DELETE | `/api/areas/{id}` | `Gestionar catálogos` | Desactiva área. |

## Request de creación

```json
{
  "nombre": "Nombre del área"
}
```

## Response

```json
{
  "id": 1,
  "nombre": "Nombre del área",
  "activo": true
}
```

---

# Departamentos

Base path:

```text
/api/departamentos
```

## Endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/departamentos` | `Ver catálogos` o `Gestionar catálogos` | Lista departamentos activos. |
| GET | `/api/departamentos/todos` | `Gestionar catálogos` | Lista todos los departamentos. |
| GET | `/api/departamentos/{id}` | `Ver catálogos` o `Gestionar catálogos` | Consulta departamento por id. |
| POST | `/api/departamentos` | `Gestionar catálogos` | Crea departamento. |
| PUT | `/api/departamentos/{id}` | `Gestionar catálogos` | Actualiza departamento. |
| PATCH | `/api/departamentos/{id}/activo?activo=` | `Gestionar catálogos` | Cambia estado activo. |
| DELETE | `/api/departamentos/{id}` | `Gestionar catálogos` | Desactiva departamento. |

## Request de creación

```json
{
  "nombre": "Nombre del departamento"
}
```

---

# Municipios

Base path:

```text
/api/municipios
```

## Endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/municipios` | `Ver catálogos` o `Gestionar catálogos` | Lista municipios activos. |
| GET | `/api/municipios/todos` | `Gestionar catálogos` | Lista todos los municipios. |
| GET | `/api/municipios/{id}` | `Ver catálogos` o `Gestionar catálogos` | Consulta municipio por id. |
| GET | `/api/municipios/departamento/{departamentoId}` | `Ver catálogos` o `Gestionar catálogos` | Lista municipios activos de un departamento activo. |
| GET | `/api/municipios/departamento/{departamentoId}/todos` | `Gestionar catálogos` | Lista todos los municipios de un departamento. |
| POST | `/api/municipios` | `Gestionar catálogos` | Crea municipio. |
| PUT | `/api/municipios/{id}` | `Gestionar catálogos` | Actualiza municipio. |
| PATCH | `/api/municipios/{id}/activo?activo=` | `Gestionar catálogos` | Cambia estado activo. |
| DELETE | `/api/municipios/{id}` | `Gestionar catálogos` | Desactiva municipio. |

## Request de creación

```json
{
  "nombre": "Nombre del municipio",
  "departamentoId": 1
}
```

## Response

```json
{
  "id": 1,
  "nombre": "Nombre del municipio",
  "departamentoId": 1,
  "activo": true
}
```

---

# Barrios

Base path:

```text
/api/barrios
```

## Endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/barrios` | `Ver catálogos` o `Gestionar catálogos` | Lista barrios activos. |
| GET | `/api/barrios/todos` | `Gestionar catálogos` | Lista todos los barrios. |
| GET | `/api/barrios/{id}` | `Ver catálogos` o `Gestionar catálogos` | Consulta barrio por id. |
| GET | `/api/barrios/municipio/{municipioId}` | `Ver catálogos` o `Gestionar catálogos` | Lista barrios activos de un municipio activo. |
| GET | `/api/barrios/municipio/{municipioId}/todos` | `Gestionar catálogos` | Lista todos los barrios de un municipio. |
| POST | `/api/barrios` | `Gestionar catálogos` | Crea barrio. |
| PUT | `/api/barrios/{id}` | `Gestionar catálogos` | Actualiza barrio. |
| PATCH | `/api/barrios/{id}/activo?activo=` | `Gestionar catálogos` | Cambia estado activo. |
| DELETE | `/api/barrios/{id}` | `Gestionar catálogos` | Desactiva barrio. |

## Request de creación

```json
{
  "nombre": "Nombre del barrio",
  "municipioId": 1
}
```

## Response

```json
{
  "id": 1,
  "nombre": "Nombre del barrio",
  "municipioId": 1,
  "activo": true
}
```

---

# Sedes

Base path:

```text
/api/sedes
```

## Endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/sedes` | `Ver catálogos` o `Gestionar catálogos` | Lista sedes activas. |
| GET | `/api/sedes/todos` | `Gestionar catálogos` | Lista todas las sedes. |
| GET | `/api/sedes/{id}` | `Ver catálogos` o `Gestionar catálogos` | Consulta sede por id. |
| POST | `/api/sedes` | `Gestionar catálogos` | Crea sede. |
| PUT | `/api/sedes/{id}` | `Gestionar catálogos` | Actualiza sede. |
| PATCH | `/api/sedes/{id}/activo?activo=` | `Gestionar catálogos` | Cambia estado activo. |
| DELETE | `/api/sedes/{id}` | `Gestionar catálogos` | Desactiva sede. |

## Request de creación

```json
{
  "nombre": "Nombre de la sede"
}
```

---

# Nacionalidades

Base path:

```text
/api/nacionalidades
```

## Endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/nacionalidades` | `Ver catálogos` o `Gestionar catálogos` | Lista nacionalidades activas. |
| GET | `/api/nacionalidades/todos` | `Gestionar catálogos` | Lista todas las nacionalidades. |
| GET | `/api/nacionalidades/{id}` | `Ver catálogos` o `Gestionar catálogos` | Consulta nacionalidad por id. |
| POST | `/api/nacionalidades` | `Gestionar catálogos` | Crea nacionalidad. |
| PUT | `/api/nacionalidades/{id}` | `Gestionar catálogos` | Actualiza nacionalidad. |
| PATCH | `/api/nacionalidades/{id}/activo?activo=` | `Gestionar catálogos` | Cambia estado activo. |
| DELETE | `/api/nacionalidades/{id}` | `Gestionar catálogos` | Desactiva nacionalidad. |

## Request de creación

```json
{
  "nombre": "Nombre de la nacionalidad"
}
```

---

# Temas

Base path:

```text
/api/temas
```

## Endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/temas` | `Ver catálogos` o `Gestionar catálogos` | Lista temas activos. |
| GET | `/api/temas/todos` | `Gestionar catálogos` | Lista todos los temas. |
| GET | `/api/temas/{id}` | `Ver catálogos` o `Gestionar catálogos` | Consulta tema por id. |
| GET | `/api/temas/area/{areaId}` | `Ver catálogos` o `Gestionar catálogos` | Lista temas activos de un área activa. |
| GET | `/api/temas/area/{areaId}/todos` | `Gestionar catálogos` | Lista todos los temas de un área. |
| POST | `/api/temas` | `Gestionar catálogos` | Crea tema. |
| PUT | `/api/temas/{id}` | `Gestionar catálogos` | Actualiza tema. |
| PATCH | `/api/temas/{id}/activo?activo=` | `Gestionar catálogos` | Cambia estado activo. |
| DELETE | `/api/temas/{id}` | `Gestionar catálogos` | Desactiva tema. |

## Request de creación

```json
{
  "nombre": "Nombre del tema",
  "areaId": 1
}
```

## Response

```json
{
  "id": 1,
  "nombre": "Nombre del tema",
  "areaId": 1,
  "activo": true
}
```

---

# Tipos

Base path:

```text
/api/tipos
```

## Endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/tipos` | `Ver catálogos` o `Gestionar catálogos` | Lista tipos activos. |
| GET | `/api/tipos/todos` | `Gestionar catálogos` | Lista todos los tipos. |
| GET | `/api/tipos/{id}` | `Ver catálogos` o `Gestionar catálogos` | Consulta tipo por id. |
| GET | `/api/tipos/tema/{temaId}` | `Ver catálogos` o `Gestionar catálogos` | Lista tipos activos de un tema activo. |
| GET | `/api/tipos/tema/{temaId}/todos` | `Gestionar catálogos` | Lista todos los tipos de un tema. |
| POST | `/api/tipos` | `Gestionar catálogos` | Crea tipo. |
| PUT | `/api/tipos/{id}` | `Gestionar catálogos` | Actualiza tipo. |
| PATCH | `/api/tipos/{id}/activo?activo=` | `Gestionar catálogos` | Cambia estado activo. |
| DELETE | `/api/tipos/{id}` | `Gestionar catálogos` | Desactiva tipo. |

## Request de creación

```json
{
  "nombre": "Nombre del tipo",
  "temaId": 1
}
```

## Response

```json
{
  "id": 1,
  "nombre": "Nombre del tipo",
  "temaId": 1,
  "activo": true
}
```

---

# Tipos de documento

Base path:

```text
/api/tipos-documento
```

## Endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/tipos-documento` | `Ver catálogos` o `Gestionar catálogos` | Lista tipos de documento. |
| GET | `/api/tipos-documento/activos` | `Ver catálogos` o `Gestionar catálogos` | Lista tipos de documento activos. |
| GET | `/api/tipos-documento/{id}` | `Ver catálogos` o `Gestionar catálogos` | Consulta tipo de documento por id. |
| POST | `/api/tipos-documento` | `Gestionar catálogos` | Crea tipo de documento. |
| PUT | `/api/tipos-documento/{id}` | `Gestionar catálogos` | Actualiza tipo de documento. |
| PATCH | `/api/tipos-documento/{id}/activo?activo=` | `Gestionar catálogos` | Cambia estado activo. |

## Request de creación

```json
{
  "nombre": "Nombre del tipo de documento"
}
```

---

# Catálogos propios de persona

Los catálogos propios de persona son:

```text
/api/condiciones
/api/empresas
/api/ocupaciones
/api/tipos-persona
```

## Endpoints por catálogo

Cada uno usa la misma estructura:

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/{base}` | `Ver catálogos` o `Gestionar catálogos` | Lista registros activos. |
| GET | `/{base}/todos` | `Gestionar catálogos` | Lista todos los registros. |
| GET | `/{base}/{id}` | `Ver catálogos` o `Gestionar catálogos` | Consulta por id. |
| POST | `/{base}` | `Gestionar catálogos` | Crea registro. |
| PUT | `/{base}/{id}` | `Gestionar catálogos` | Actualiza registro. |
| PATCH | `/{base}/{id}/activo?activo=` | `Gestionar catálogos` | Cambia estado activo. |
| DELETE | `/{base}/{id}` | `Gestionar catálogos` | Desactiva registro. |

## Request de creación

```json
{
  "nombre": "Nombre del registro"
}
```

## Response

```json
{
  "id": 1,
  "nombre": "Nombre del registro",
  "activo": true
}
```

## Bases disponibles

| Catálogo | Base |
|---|---|
| Condiciones | `/api/condiciones` |
| Empresas | `/api/empresas` |
| Ocupaciones | `/api/ocupaciones` |
| Tipos de persona | `/api/tipos-persona` |

## Reglas de duplicado

| Recurso | Regla |
|---|---|
| Área | Nombre único. |
| Departamento | Nombre único. |
| Sede | Nombre único. |
| Nacionalidad | Nombre único. |
| Tipo de documento | Nombre único. |
| Municipio | Nombre único dentro del departamento. |
| Barrio | Nombre único dentro del municipio. |
| Tema | Nombre único dentro del área. |
| Tipo | Nombre único dentro del tema. |
| Condición | Nombre único. |
| Empresa | Nombre único. |
| Ocupación | Nombre único. |
| Tipo de persona | Nombre único. |

## Errores comunes

| Estado | Causa |
|---|---|
| `400 Bad Request` | DTO ausente. |
| `400 Bad Request` | Nombre obligatorio ausente. |
| `400 Bad Request` | Id enviado en creación. |
| `400 Bad Request` | Id del DTO diferente al id de la ruta. |
| `400 Bad Request` | Registro duplicado. |
| `400 Bad Request` | Padre inexistente o inactivo. |
| `400 Bad Request` | Cambio al mismo estado activo. |
| `400 Bad Request` | Sin cambios para actualizar. |
| `401 Unauthorized` | Sesión no válida. |
| `403 Forbidden` | Usuario sin permiso. |

## Notas para frontend

- Usar listados activos para formularios y combos.
- Usar `/todos` en pantallas administrativas.
- Para jerarquías, cargar hijos después de seleccionar el padre.
- Para tipos de documento, usar `/activos` cuando se necesiten únicamente activos.
- Usar `credentials: "include"` en todas las peticiones protegidas.
- Manejar errores de duplicado y relaciones inactivas desde el mensaje del backend.
