# Base de datos y estados

Esta carpeta documenta la estructura principal de persistencia del sistema de gestión de casos jurídicos.

La documentación está basada en las entidades JPA del backend actual y en los estados funcionales usados por los módulos principales.

## Archivos

| Documento | Contenido |
|---|---|
| `entidades-principales.md` | Entidades, tablas, relaciones principales y propósito de cada grupo de tablas. |
| `estados-y-catalogos.md` | Estados funcionales, catálogos, enumeraciones y diferencia entre estado funcional y activo lógico. |

## Esquema

El backend configura un esquema por defecto para Hibernate:

```text
DB_consultorioJuridico
```

La documentación se refiere a los nombres lógicos de tablas y columnas definidos en las entidades JPA.

## Convenciones generales

El sistema usa varias convenciones de persistencia:

| Convención | Descripción |
|---|---|
| `id` | Identificador principal de entidades. |
| `activo` | Estado lógico usado para desactivación sin eliminar historial. |
| `estado` | Estado funcional del recurso cuando existe ciclo de vida operativo. |
| `fecha_creacion` | Fecha de creación del registro cuando el módulo la maneja. |
| `fecha_actualizacion` | Fecha de última actualización cuando el módulo la maneja. |
| Relaciones `ManyToOne` | Referencias a catálogos, perfiles, usuarios o entidades principales. |
| Relaciones `ManyToMany` | Asociaciones de consulta con partes y contrapartes. |
| Enums `STRING` | Estados guardados como texto para claridad en base de datos. |

## Estado funcional y activo lógico

El sistema diferencia dos conceptos:

| Concepto | Ejemplo | Uso |
|---|---|---|
| Estado funcional | `Consulta.estado`, `Proceso.estado`, `Seguimiento.estado` | Representa el ciclo operativo o resultado del recurso. |
| Activo lógico | `activo=true/false` | Representa disponibilidad operativa o desactivación lógica. |

Estos dos conceptos no deben mezclarse.

Ejemplos:

- una consulta `ARCHIVADO` no es lo mismo que un registro con `activo=false`;
- un proceso puede estar `PENDIENTE` y `activo=true`;
- una conciliación puede estar finalizada y seguir `activo=true` como historial;
- un catálogo puede estar `activo=false` sin borrar datos relacionados.

## Seguridad documental

Esta documentación no incluye:

- contraseñas;
- tokens;
- API keys;
- secretos JWT;
- firmas;
- rutas privadas reales;
- cadenas de conexión con credenciales;
- datos personales reales.

Los ejemplos son estructurales y no representan datos reales.
