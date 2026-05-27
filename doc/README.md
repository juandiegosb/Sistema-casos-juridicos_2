# Documentación del sistema

Esta carpeta contiene la documentación vigente del sistema de gestión de casos jurídicos.

La documentación está organizada para describir el sistema desde una perspectiva técnica y funcional, con base en el código fuente actual.

## Índice de la Fase 1

| Documento | Propósito |
|---|---|
| `00-vision-general.md` | Presenta el propósito, alcance, módulos y flujo general del sistema. |
| `01-arquitectura.md` | Describe la arquitectura general de backend y frontend. |
| `02-configuracion-seguridad.md` | Documenta configuración, variables de entorno y reglas de seguridad documental. |
| `03-autenticacion-autorizacion.md` | Explica autenticación, sesión, JWT, cookie, permisos y alcance. |
| `04-permisos-roles-alcance.md` | Describe roles, permisos funcionales, permisos de navegación y reglas de alcance. |
| `05-estandar-api-errores.md` | Define convenciones de API, códigos HTTP, errores y manejo de archivos. |

## Criterios de documentación

La documentación debe mantenerse alineada con el código fuente vigente.

No se deben documentar valores reales de:

- contraseñas;
- secretos JWT;
- API keys;
- tokens;
- firmas;
- llaves privadas;
- cadenas de conexión con credenciales;
- usuarios reales de prueba;
- datos personales reales;
- rutas privadas del equipo.

Cuando sea necesario explicar configuración sensible, se debe documentar el nombre de la variable y su propósito, no su valor real.

## Fuente de verdad

La fuente principal de verdad es el código fuente actual.

La documentación debe acompañar el comportamiento implementado en backend y frontend, especialmente en:

- controllers;
- services;
- validators;
- access services;
- DTOs;
- entidades;
- configuración;
- permisos;
- componentes de frontend;
- configuración de API.
