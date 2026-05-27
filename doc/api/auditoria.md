# API - Auditoría

Este documento describe el endpoint de consulta de auditoría.

La auditoría registra acciones relevantes del backend mediante AOP y permite consultar eventos por usuario de forma paginada.

## Base path

```text
/api/audit
```

## Autenticación y autorización

El endpoint requiere sesión válida y rol administrativo reconocido por Spring Security.

Protección aplicada:

```text
hasRole('ADMIN')
```

El frontend debe enviar:

```javascript
credentials: "include"
```

## DTO `AuditLogDTO`

Estructura de respuesta para registros de auditoría.

```json
{
  "id": 1,
  "username": "usuario",
  "action": "ACCION",
  "entityName": "Entidad",
  "entityId": "1",
  "timestamp": "fecha-hora",
  "details": "detalle técnico"
}
```

Los valores anteriores son ilustrativos y no corresponden a datos reales.

## Campos

| Campo | Tipo | Uso |
|---|---|---|
| `id` | Long | Identificador del registro de auditoría. |
| `username` | String | Usuario que ejecutó la acción. |
| `action` | String | Acción auditada. |
| `entityName` | String | Nombre lógico de la entidad afectada. |
| `entityId` | String | Identificador de entidad cuando pudo obtenerse. |
| `timestamp` | DateTime | Fecha y hora del registro. |
| `details` | String | Detalle técnico del evento auditado. |

## Resumen de endpoints

| Método | Ruta | Seguridad | Uso |
|---|---|---|---|
| GET | `/api/audit` | `hasRole('ADMIN')` | Consulta paginada de registros de auditoría. |

---

# GET `/api/audit`

Consulta registros de auditoría de forma paginada.

## Query params

| Parámetro | Tipo | Obligatorio | Valor por defecto | Uso |
|---|---|---|---|---|
| `page` | Integer | No | `0` | Página solicitada. |
| `size` | Integer | No | `20` | Tamaño de página. |
| `username` | String | No | - | Filtro parcial por username. |

Ejemplos conceptuales:

```text
GET /api/audit
GET /api/audit?page=0&size=20
GET /api/audit?page=0&size=20&username=admin
```

## Orden

La consulta retorna los resultados ordenados por:

```text
timestamp DESC
```

## Reglas

- requiere usuario autenticado;
- requiere rol administrativo compatible con `hasRole('ADMIN')`;
- si `username` viene vacío o ausente, lista todos los registros paginados;
- si `username` viene informado, filtra por coincidencia parcial ignorando mayúsculas/minúsculas;
- retorna una página de Spring Data.

## Response `200 OK`

```json
{
  "content": [
    {
      "id": 1,
      "username": "usuario",
      "action": "ACCION",
      "entityName": "Entidad",
      "entityId": "1",
      "timestamp": "fecha-hora",
      "details": "detalle técnico"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

La estructura puede incluir otros campos propios de paginación de Spring Data.

## Errores esperados

| Estado | Causa |
|---|---|
| `401 Unauthorized` | Sesión no válida. |
| `403 Forbidden` | Usuario autenticado sin rol requerido. |
| `500 Internal Server Error` | Error inesperado consultando auditoría. |

---

# Registro de auditoría

La consulta de auditoría depende de los registros generados por métodos anotados con:

```text
@Auditable
```

El backend registra eventos exitosos mediante un aspecto AOP.

Datos registrados:

| Dato | Fuente |
|---|---|
| Usuario | Contexto de seguridad. |
| Acción | Parámetro `action` de la anotación. |
| Entidad | Parámetro `entityName` de la anotación. |
| Identificador | Resultado del método cuando expone `getId()`. |
| Detalles | Nombre del método y argumentos. |

Si no hay usuario autenticado, el evento puede registrarse como:

```text
SISTEMA
```

## Consideraciones de privacidad

Los registros de auditoría pueden contener información técnica de operaciones internas.

Por seguridad:

- no copiar logs reales en documentación pública;
- no exponer datos personales reales desde capturas o ejemplos;
- no usar auditoría como fuente para publicar datos sensibles;
- filtrar o limitar visualización de `details` en frontend si contiene información técnica no necesaria.

## Notas para frontend

- Consumir la respuesta como página de Spring Data.
- Usar `content` para la tabla principal.
- Usar `totalElements`, `totalPages`, `number` y `size` para paginación.
- Enviar `username` solo cuando se aplique filtro.
- Manejar `403` como falta de autorización administrativa.
- Usar `credentials: "include"` en la petición.
- Evitar mostrar información sensible innecesaria si aparece en `details`.
