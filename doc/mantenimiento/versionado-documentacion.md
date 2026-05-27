# Versionado de documentación

Este documento define cómo versionar documentación dentro del repositorio.

## Principio

La documentación debe evolucionar junto con el código.

No se busca mantener documentos históricos dentro del mismo archivo. La documentación vigente describe el estado actual del sistema.

El historial de Git conserva versiones anteriores cuando sea necesario consultarlas.

## Tipos de cambios documentales

| Tipo | Ejemplo de commit |
|---|---|
| Documentación general | `docs: actualizar documentacion del sistema` |
| API | `docs(api): actualizar contrato de consultas` |
| Backend | `docs(backend): actualizar modulo de seguimientos` |
| Reglas | `docs(reglas): actualizar reglas de conciliaciones` |
| Base de datos | `docs(db): actualizar entidades y estados` |
| Decisiones | `docs(decisiones): documentar permisos y alcance` |
| Mantenimiento | `docs(mantenimiento): actualizar matriz documental` |

## Commits separados o conjuntos

### Cambio pequeño

Si un cambio funcional altera una única regla o endpoint, puede ir en el mismo commit que la documentación asociada.

Ejemplo:

```text
fix(procesos): validar radicado en estados finales
docs(api): actualizar contrato de procesos
```

También puede hacerse en dos commits consecutivos.

### Cambio grande

Si el cambio afecta varios módulos, es preferible separar:

1. commit de código;
2. commit de documentación impactada;
3. commit de pruebas si aplica.

## Cuándo actualizar documentación

Actualizar documentación cuando cambie:

- endpoint;
- DTO;
- permiso;
- alcance;
- estado;
- entidad;
- relación;
- validación;
- regla de negocio;
- formato de error;
- archivo o ruta de documento;
- configuración relevante;
- decisión técnica.

## Qué no versionar

No versionar:

- archivos generados;
- reportes locales;
- resultados de pruebas;
- `.env` con valores reales;
- configuraciones privadas de IDE;
- archivos subidos por usuarios;
- builds;
- carpetas `target`;
- carpetas `.next`.

Estos elementos se controlan mediante `.gitignore`.

## Documentos vigentes

La documentación vigente está en:

```text
doc/
```

Estructura principal:

```text
doc/backend
doc/api
doc/reglas
doc/base-datos
doc/decisiones
doc/mantenimiento
```

## Documentos reservados

La documentación interna detallada del frontend se realiza cuando la estructura del frontend esté estable.

Mientras tanto, la documentación vigente cubre:

- contratos API;
- reglas que el frontend debe respetar;
- autenticación;
- permisos;
- configuración general;
- recomendaciones de consumo.

## Relación con DeepWiki u otras herramientas

Las herramientas automáticas deben analizar documentación vigente y código fuente.

Para evitar resultados desactualizados:

- no mantener documentos antiguos contradictorios;
- no versionar reportes generados;
- no mantener ejemplos con credenciales;
- no documentar reglas futuras como vigentes;
- actualizar documentos afectados cuando cambie el código.

## Control de obsolescencia

Un documento se considera obsoleto cuando:

- contradice endpoints reales;
- menciona configuración ya no usada;
- expone valores sensibles;
- describe funcionalidades no implementadas como vigentes;
- omite cambios de contrato relevantes;
- conserva reglas reemplazadas.

Cuando un documento quede obsoleto, debe actualizarse o eliminarse.

## Revisión antes de entrega

Antes de una entrega formal:

1. Revisar que `doc/` no tenga información sensible.
2. Revisar que API coincida con controllers.
3. Revisar que reglas coincidan con validators y services.
4. Revisar que base de datos coincida con entidades.
5. Revisar que decisiones no contradigan comportamiento implementado.
6. Revisar que frontend no esté documentado como definitivo si está en refactorización.
