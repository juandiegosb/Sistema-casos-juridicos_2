# API - Personas

Este documento describe los endpoints del módulo de personas.

El módulo administra información de identificación, datos personales, contacto, caracterización, vivienda, aspectos económicos, acudiente y datos de servicio.

## Base path

```text
/api/personas
```

## Autenticación

Todos los endpoints requieren sesión válida.

El frontend debe enviar:

```javascript
credentials: "include"
```

## Permisos

| Permiso | Uso |
|---|---|
| `Ver personas` | Consulta de personas. |
| `Crear personas` | Creación de personas. |
| `Editar personas` | Actualización de datos de personas. |
| `Cambiar estado personas` | Desactivación y reactivación de personas. |
| `Gestionar personas` | Permiso amplio de gestión de personas. |

## Resumen de endpoints

| Método | Ruta | Permiso | Uso |
|---|---|---|---|
| GET | `/api/personas` | `Ver personas` o `Gestionar personas` | Lista personas. |
| GET | `/api/personas/activos` | `Ver personas` o `Gestionar personas` | Lista personas activas. |
| GET | `/api/personas/{id}` | `Ver personas` o `Gestionar personas` | Consulta persona por id. |
| POST | `/api/personas` | `Crear personas` o `Gestionar personas` | Crea persona. |
| PUT | `/api/personas/{id}` | `Editar personas` o `Gestionar personas` | Actualiza persona. |
| PATCH | `/api/personas/{id}/desactivar` | `Cambiar estado personas` o `Gestionar personas` | Desactiva persona. |
| PATCH | `/api/personas/{id}/reactivar` | `Cambiar estado personas` o `Gestionar personas` | Reactiva persona. |

## DTO `PersonaDTO`

Estructura general:

```json
{
  "id": 1,
  "tipoPersonaId": 1,
  "tipoDocumento": "TipoDocumento",
  "numeroDocumento": "Documento",
  "fechaExpedicion": "YYYY-MM-DD",
  "ciudadExpedicion": "Ciudad de expedición",
  "nombres": "Nombres",
  "apellidos": "Apellidos",
  "nombreIdentitario": "Nombre identitario",
  "pronombre": "Pronombre",
  "sexo": "Sexo",
  "genero": "Género",
  "orientacionSexual": "Orientación sexual",
  "fechaNacimiento": "YYYY-MM-DD",
  "telefono": "Teléfono",
  "correo": "correo@dominio",
  "nacionalidadId": 1,
  "estadoCivil": "Estado civil",
  "escolaridad": "Escolaridad",
  "grupoEtnico": "Grupo étnico",
  "condicionActualId": 1,
  "sabeLeerEscribir": true,
  "discapacidad": "Discapacidad",
  "caracterizacionPcd": "Caracterización PCD",
  "necesitaAjustePcd": false,
  "departamentoId": 1,
  "municipioId": 1,
  "barrioId": 1,
  "direccion": "Dirección",
  "comuna": "Comuna",
  "localidad": "Localidad",
  "estrato": 1,
  "tipoVivienda": "Tipo de vivienda",
  "zona": "Zona",
  "tenencia": "Tenencia",
  "numeroPersonasACargo": 0,
  "ingresosAdicionales": false,
  "energiaElectrica": true,
  "acueducto": true,
  "alcantarillado": true,
  "ocupacionId": 1,
  "empresaId": 1,
  "salario": 0,
  "cargo": "Cargo",
  "direccionEmpresa": "Dirección empresa",
  "telefonoEmpresa": "Teléfono empresa",
  "nombreCompletoAcudiente": "Nombre acudiente",
  "relacionAcudiente": "Relación acudiente",
  "telefonoAcudiente": "Teléfono acudiente",
  "correoAcudiente": "correo.acudiente@dominio",
  "direccionAcudiente": "Dirección acudiente",
  "comoSeEntero": "Canal por el que conoció el servicio",
  "relacionConUniversidad": "Relación con la universidad",
  "activo": true
}
```

Los valores anteriores son ilustrativos y no representan datos reales.

## Bloques del DTO

### Información básica

| Campo | Tipo | Regla |
|---|---|---|
| `tipoPersonaId` | Long | Obligatorio. |
| `tipoDocumento` | String | Obligatorio. Máximo 10 caracteres. |
| `numeroDocumento` | String | Obligatorio. Máximo 30 caracteres. Debe ser único. |
| `fechaExpedicion` | Date | Obligatoria. |
| `ciudadExpedicion` | String | Obligatoria. Máximo 100 caracteres. |
| `nombres` | String | Obligatorio. Máximo 100 caracteres. |
| `apellidos` | String | Obligatorio. Máximo 100 caracteres. |
| `nombreIdentitario` | String | Obligatorio. Máximo 100 caracteres. |
| `pronombre` | String | Obligatorio. Máximo 50 caracteres. |
| `sexo` | String | Obligatorio. Máximo 20 caracteres. |
| `genero` | String | Obligatorio. Máximo 20 caracteres. |
| `orientacionSexual` | String | Obligatoria. Máximo 50 caracteres. |
| `fechaNacimiento` | Date | Obligatoria. |

### Contacto y caracterización

| Campo | Tipo | Regla |
|---|---|---|
| `telefono` | String | Máximo 30 caracteres. |
| `correo` | String | Debe tener formato de correo. Máximo 120 caracteres. |
| `nacionalidadId` | Long | Obligatorio. |
| `estadoCivil` | String | Obligatorio. Máximo 30 caracteres. |
| `escolaridad` | String | Obligatoria. Máximo 100 caracteres. |
| `grupoEtnico` | String | Obligatorio. Máximo 100 caracteres. |
| `condicionActualId` | Long | Obligatorio. |
| `sabeLeerEscribir` | Boolean | Obligatorio. |
| `discapacidad` | String | Obligatoria. Máximo 100 caracteres. |
| `caracterizacionPcd` | String | Obligatoria. Máximo 150 caracteres. |
| `necesitaAjustePcd` | Boolean | Obligatorio. |

### Vivienda

| Campo | Tipo | Regla |
|---|---|---|
| `departamentoId` | Long | Informativo para frontend. El backend resuelve departamento desde municipio. |
| `municipioId` | Long | Obligatorio. |
| `barrioId` | Long | Obligatorio. Debe pertenecer al municipio. |
| `direccion` | String | Obligatoria. Máximo 150 caracteres. |
| `comuna` | String | Obligatoria. Máximo 100 caracteres. |
| `localidad` | String | Obligatoria. |
| `estrato` | Integer | Según validación del DTO. |
| `tipoVivienda` | String | Según validación del DTO. |
| `zona` | String | Según validación del DTO. |
| `tenencia` | String | Según validación del DTO. |
| `numeroPersonasACargo` | Integer | Según validación del DTO. |
| `ingresosAdicionales` | Boolean | Según validación del DTO. |
| `energiaElectrica` | Boolean | Según validación del DTO. |
| `acueducto` | Boolean | Según validación del DTO. |
| `alcantarillado` | Boolean | Según validación del DTO. |

### Aspectos económicos

| Campo | Tipo | Regla |
|---|---|---|
| `ocupacionId` | Long | Catálogo de ocupación. |
| `empresaId` | Long | Catálogo de empresa. |
| `salario` | Integer | Según validación del DTO. |
| `cargo` | String | Según validación del DTO. |
| `direccionEmpresa` | String | Según validación del DTO. |
| `telefonoEmpresa` | String | Según validación del DTO. |

### Acudiente

| Campo | Tipo | Regla |
|---|---|---|
| `nombreCompletoAcudiente` | String | Requerido si la persona es menor de edad. |
| `relacionAcudiente` | String | Requerido si la persona es menor de edad. |
| `telefonoAcudiente` | String | Requerido con correo de acudiente como medio de contacto para menor. |
| `correoAcudiente` | String | Debe tener formato de correo si se informa. |
| `direccionAcudiente` | String | Según validación del DTO. |

### Servicio

| Campo | Tipo | Regla |
|---|---|---|
| `comoSeEntero` | String | Según validación del DTO. |
| `relacionConUniversidad` | String | Según validación del DTO. |
| `activo` | Boolean | Estado lógico. Se controla por endpoints específicos. |

## GET `/api/personas`

Lista personas.

### Response `200 OK`

```json
[
  {
    "id": 1,
    "tipoPersonaId": 1,
    "numeroDocumento": "Documento",
    "nombres": "Nombres",
    "apellidos": "Apellidos",
    "correo": "correo@dominio",
    "telefono": "Teléfono",
    "activo": true
  }
]
```

La respuesta real contiene los campos de `PersonaDTO`.

## GET `/api/personas/activos`

Lista personas activas.

### Response `200 OK`

Retorna lista de `PersonaDTO`.

## GET `/api/personas/{id}`

Consulta una persona por id.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |

### Response `200 OK`

Retorna `PersonaDTO`.

### Errores esperados

| Estado | Causa |
|---|---|
| `400 Bad Request` | Id obligatorio ausente o persona no encontrada. |
| `403 Forbidden` | Usuario sin permiso. |

## POST `/api/personas`

Crea una persona.

### Request

Content-Type:

```text
application/json
```

Body:

```json
{
  "tipoPersonaId": 1,
  "tipoDocumento": "TipoDocumento",
  "numeroDocumento": "Documento",
  "fechaExpedicion": "YYYY-MM-DD",
  "ciudadExpedicion": "Ciudad",
  "nombres": "Nombres",
  "apellidos": "Apellidos",
  "nombreIdentitario": "Nombre identitario",
  "pronombre": "Pronombre",
  "sexo": "Sexo",
  "genero": "Género",
  "orientacionSexual": "Orientación sexual",
  "fechaNacimiento": "YYYY-MM-DD",
  "telefono": "Teléfono",
  "correo": "correo@dominio",
  "nacionalidadId": 1,
  "estadoCivil": "Estado civil",
  "escolaridad": "Escolaridad",
  "grupoEtnico": "Grupo étnico",
  "condicionActualId": 1,
  "sabeLeerEscribir": true,
  "discapacidad": "Discapacidad",
  "caracterizacionPcd": "Caracterización PCD",
  "necesitaAjustePcd": false,
  "municipioId": 1,
  "barrioId": 1,
  "direccion": "Dirección",
  "comuna": "Comuna",
  "localidad": "Localidad",
  "estrato": 1,
  "tipoVivienda": "Tipo vivienda",
  "zona": "Zona",
  "tenencia": "Tenencia",
  "numeroPersonasACargo": 0,
  "ingresosAdicionales": false,
  "energiaElectrica": true,
  "acueducto": true,
  "alcantarillado": true,
  "ocupacionId": 1,
  "empresaId": 1,
  "salario": 0,
  "cargo": "Cargo",
  "direccionEmpresa": "Dirección empresa",
  "telefonoEmpresa": "Teléfono empresa",
  "nombreCompletoAcudiente": "Nombre acudiente",
  "relacionAcudiente": "Relación acudiente",
  "telefonoAcudiente": "Teléfono acudiente",
  "correoAcudiente": "correo.acudiente@dominio",
  "direccionAcudiente": "Dirección acudiente",
  "comoSeEntero": "Canal",
  "relacionConUniversidad": "Relación"
}
```

### Reglas

- no enviar `id`;
- `numeroDocumento` debe ser único;
- la persona debe tener al menos teléfono o correo;
- las relaciones de catálogo deben existir y estar activas;
- `barrioId` debe pertenecer al `municipioId`;
- si la persona es menor de edad, debe informarse acudiente según regla del backend.

### Response `201 Created`

Retorna `PersonaDTO`.

## PUT `/api/personas/{id}`

Actualiza una persona.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |

### Request

Mismo formato que `PersonaDTO`.

Si se envía `id` en el body, debe coincidir con el id de la ruta.

### Reglas

- la persona debe existir;
- el id no puede cambiar;
- si cambia `numeroDocumento`, debe seguir siendo único;
- las relaciones deben existir y estar activas;
- barrio debe pertenecer al municipio;
- debe cumplirse la regla de contacto;
- si aplica por edad, debe cumplirse la regla de acudiente.

### Response `200 OK`

Retorna `PersonaDTO`.

## PATCH `/api/personas/{id}/desactivar`

Desactiva una persona.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |

### Reglas

- la persona debe existir;
- la persona debe estar activa;
- se aplica desactivación lógica.

### Response `200 OK`

Retorna `PersonaDTO`.

## PATCH `/api/personas/{id}/reactivar`

Reactiva una persona.

### Parámetros

| Parámetro | Tipo | Ubicación |
|---|---|---|
| `id` | Long | Path |

### Reglas

- la persona debe existir;
- la persona debe estar inactiva;
- se reactiva el registro.

### Response `200 OK`

Retorna `PersonaDTO`.

## Errores comunes

| Estado | Causa |
|---|---|
| `400 Bad Request` | DTO obligatorio ausente. |
| `400 Bad Request` | Id enviado en creación. |
| `400 Bad Request` | Id del body diferente al id de ruta. |
| `400 Bad Request` | Documento duplicado. |
| `400 Bad Request` | Relación de catálogo inexistente o inactiva. |
| `400 Bad Request` | Barrio no pertenece al municipio. |
| `400 Bad Request` | Falta medio de contacto. |
| `400 Bad Request` | Faltan datos de acudiente cuando aplican. |
| `400 Bad Request` | Persona no encontrada. |
| `401 Unauthorized` | Sesión no válida. |
| `403 Forbidden` | Usuario sin permiso. |

## Notas para frontend

- Usar catálogos activos para construir los formularios.
- Cargar municipios según departamento seleccionado.
- Cargar barrios según municipio seleccionado.
- Validar en UI duplicados obvios y campos obligatorios, pero conservar validación final en backend.
- En creación, no enviar `id`.
- En actualización, enviar `id` solo si coincide con la ruta.
- En desactivar/reactivar, usar los endpoints específicos.
- No cambiar estado activo desde `PUT`.
- Usar `credentials: "include"` en peticiones protegidas.
- Manejar errores de validación por campo desde `detalles`.
