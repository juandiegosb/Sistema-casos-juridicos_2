# Backend - Auditoría

El módulo de auditoría registra eventos relevantes del backend mediante AOP.

La auditoría permite conocer qué usuario ejecutó una acción, sobre qué entidad, con qué identificador y en qué momento.

## Paquetes principales

```text
audit/aop/log
audit/controller/log
audit/dto/log
audit/model/log
audit/repository/log
audit/service/log
```

## Componentes principales

| Componente | Responsabilidad |
|---|---|
| `Auditable` | Anotación para marcar métodos auditables. |
| `AuditAspect` | Aspecto AOP que intercepta métodos anotados. |
| `AuditLog` | Entidad persistente del registro de auditoría. |
| `AuditLogDTO` | DTO de salida para consultar auditoría. |
| `AuditLogRepository` | Repositorio JPA de auditoría. |
| `AuditLogService` | Guarda y consulta registros de auditoría. |
| `AuditLogController` | Expone endpoint de consulta de logs. |

## Anotación `Auditable`

La anotación se aplica sobre métodos de service que representan acciones auditables.

Parámetros:

| Parámetro | Uso |
|---|---|
| `action` | Tipo de acción realizada. |
| `entityName` | Nombre lógico de la entidad afectada. |

Ejemplo conceptual:

```text
@Auditable(action = "CREAR_RECURSO", entityName = "Recurso")
```

La documentación no debe usar ejemplos con datos reales de usuarios ni identificadores sensibles.

## Aspecto de auditoría

`AuditAspect` intercepta métodos anotados con `@Auditable`.

Punto de ejecución:

```text
@AfterReturning
```

Esto significa que el registro de auditoría se crea después de una ejecución exitosa del método auditado.

Información recolectada:

| Dato | Fuente |
|---|---|
| Usuario | `SecurityContextHolder`. |
| Acción | Parámetro `action` de `@Auditable`. |
| Entidad | Parámetro `entityName` de `@Auditable`. |
| Identificador | Resultado del método cuando expone `getId()`. |
| Detalles | Nombre del método y argumentos recibidos. |

Si no hay usuario autenticado, se registra:

```text
SISTEMA
```

## Extracción de identificador

El aspecto intenta obtener el identificador de la entidad auditada usando reflexión sobre el resultado del método.

Regla:

- si el resultado tiene método `getId()` y retorna valor, ese valor se guarda como `entityId`;
- si no se puede obtener, `entityId` queda nulo.

## Servicio de auditoría

`AuditLogService` centraliza almacenamiento y consulta.

### Registro de acción

Método:

```text
logAction(username, action, entityName, entityId, details)
```

Características:

- crea una entidad `AuditLog`;
- guarda usuario, acción, entidad, identificador y detalles;
- persiste el registro mediante `AuditLogRepository`;
- se ejecuta de forma asíncrona con `@Async`;
- usa transacción para persistencia.

La ejecución asíncrona evita bloquear el flujo principal de negocio.

### Consulta de logs

Métodos:

| Método | Uso |
|---|---|
| `getAuditLogs(pageable)` | Consulta paginada de registros. |
| `getAuditLogsByUsername(username, pageable)` | Consulta paginada filtrada por usuario. |

## Entidad `AuditLog`

Tabla:

```text
audit_logs
```

Campos:

| Campo | Uso |
|---|---|
| `id` | Identificador del log. |
| `username` | Usuario que ejecutó la acción. |
| `action` | Acción auditada. |
| `entityName` | Entidad lógica afectada. |
| `entityId` | Identificador de la entidad afectada, cuando se obtiene. |
| `timestamp` | Fecha y hora del registro. |
| `details` | Detalle técnico del evento. |

Características:

- usa `AuditingEntityListener`;
- `timestamp` se llena con `@CreatedDate`;
- los campos están marcados como no actualizables desde la aplicación.

## DTO `AuditLogDTO`

Expone:

| Campo | Uso |
|---|---|
| `id` | Identificador. |
| `username` | Usuario. |
| `action` | Acción. |
| `entityName` | Entidad. |
| `entityId` | Identificador de entidad. |
| `timestamp` | Fecha y hora. |
| `details` | Detalles del evento. |

Incluye método estático:

```text
fromEntity
```

para convertir entidad a DTO.

## Repository

`AuditLogRepository` extiende `JpaRepository`.

Consultas disponibles:

| Método | Uso |
|---|---|
| `findByUsernameContainingIgnoreCase` | Filtra logs por usuario. |
| `findByAction` | Filtra logs por acción. |
| `findByTimestampBetween` | Filtra logs por rango de tiempo. |

## Controller

Base path:

```text
/api/audit
```

Endpoint:

| Método | Ruta | Seguridad | Uso |
|---|---|---|---|
| GET | `/api/audit` | `hasRole('ADMIN')` | Consulta paginada de registros de auditoría. |

Parámetros:

| Parámetro | Obligatorio | Valor por defecto | Uso |
|---|---|---|---|
| `page` | No | `0` | Página solicitada. |
| `size` | No | `20` | Tamaño de página. |
| `username` | No | - | Filtro parcial por usuario. |

Orden:

```text
timestamp DESC
```

## Respuesta

El endpoint devuelve una página de `AuditLogDTO`.

Estructura general:

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
      "details": "detalle"
    }
  ],
  "pageable": {},
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

Los valores mostrados son ilustrativos y no representan usuarios reales.

## Seguridad de auditoría

La consulta de logs está restringida a rol administrativo con autoridad de Spring Security:

```text
hasRole('ADMIN')
```

El acceso a auditoría debe mantenerse limitado porque los logs pueden contener información técnica de operaciones internas.

## Consideraciones de privacidad

Los registros de auditoría pueden contener:

- nombres de métodos;
- argumentos recibidos;
- usuario autenticado;
- identificador de entidad;
- acción ejecutada.

Por esta razón:

- no se deben usar logs de auditoría como documentación pública de datos reales;
- no se deben copiar registros reales a documentos;
- no se deben exponer logs con datos personales en repositorios públicos;
- las pruebas o ejemplos deben usar datos ficticios.

## Relación con servicios de negocio

Los services que requieran trazabilidad pueden usar `@Auditable` sobre métodos exitosos.

El patrón mantiene la lógica de auditoría separada de la lógica de negocio.

## Consideraciones para frontend

- La consulta de auditoría es paginada.
- El frontend puede enviar `page`, `size` y `username`.
- La UI debe manejar respuesta paginada de Spring.
- El acceso debe limitarse a usuarios autorizados.
- No se deben mostrar datos sensibles innecesarios si aparecen en `details`.
