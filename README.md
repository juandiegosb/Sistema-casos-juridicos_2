# Frontend — Sistema de gestión de casos jurídicos

Aplicación frontend del consultorio jurídico UFPS. Consume el backend principal mediante endpoints REST y gestiona la sesión a través de cookie HTTP-only.

## Tecnologías

- **Next.js 15** (App Router)
- **React 19**
- **Tailwind CSS** + shadcn/ui (Radix UI)
- **react-hook-form** para formularios
- **Fetch API** con `credentials: "include"`
- **Playwright** para pruebas e2e

## Requisitos

- Node.js 22 o superior
- npm

## Instalación

```bash
npm install
```

## Variables de entorno

Copia `.env.example` a `.env.local` y ajusta los valores:

```bash
cp .env.example .env.local
```

| Variable | Propósito | Valor por defecto |
|---|---|---|
| `NEXT_PUBLIC_API_URL_BASE` | URL base del backend principal (incluye `/api`) | `http://localhost:8080/api` |
| `NEXT_PUBLIC_FILE_STORAGE_API_URL_BASE` | URL base del servicio de archivos | igual a `NEXT_PUBLIC_API_URL_BASE` |

> Las variables `NEXT_PUBLIC_*` son visibles para el navegador. No incluir tokens, claves ni credenciales en ellas.

## Ejecución local

```bash
npm run dev
```

La aplicación queda disponible en `http://localhost:3000`.

## Compilación y producción

```bash
npm run build   # compila para producción
npm run start   # sirve la compilación
```

## Estructura de carpetas relevante

```
src/
├── app/                    # Rutas de Next.js (App Router)
│   ├── layout.js           # Layout raíz (tema, fuentes, metadata)
│   └── (dashboard)/        # Grupo de rutas protegidas
├── components/
│   ├── app-sidebar.jsx     # Barra lateral de navegación
│   ├── auth/               # Formulario de login
│   ├── forms/              # Formularios de cada módulo
│   └── ui/                 # Componentes base de shadcn/ui
├── hooks/
│   ├── useApiForm.js       # Hook para envíos de formulario al backend
│   └── use-mobile.js       # Hook para detectar viewport móvil
└── lib/
    ├── api.js              # Helpers para leer y normalizar respuestas HTTP
    ├── apiClient.js        # Cliente HTTP centralizado (wrapper de fetch)
    ├── authz.js            # Funciones de autorización (tienePermiso, etc.)
    ├── config.js           # URLs del backend desde variables de entorno
    ├── form-validation.js  # Reglas reutilizables para react-hook-form
    ├── list-utils.js       # Ordenamiento y paginación de listas
    ├── permission.js       # Constantes de permisos del sistema
    └── utils.js            # Helper cn() para clases Tailwind
```

## Autenticación y sesión

La sesión se maneja mediante cookie HTTP-only. Todas las peticiones protegidas deben incluir:

```javascript
credentials: "include"
```

Usar el cliente centralizado `lib/apiClient.js` que lo hace automáticamente:

```javascript
import { apiClient } from "@/lib/apiClient";

const res = await apiClient.get("/auth/me");
const user = await res.json();
```

### Códigos de estado de sesión

| Código | Significado | Acción en el frontend |
|---|---|---|
| `401` | Sesión no válida o expirada | Redirigir a `/` (login) |
| `403` | Sin permiso sobre el recurso | Mostrar error, no redirigir |

## Permisos

Los permisos del usuario se obtienen del campo `permisos` de `/api/auth/me`. Las constantes están en `lib/permission.js` agrupadas por módulo. La verificación se hace con las funciones de `lib/authz.js`:

```javascript
import { PERMISOS } from "@/lib/permission";
import { tienePermiso } from "@/lib/authz";

if (tienePermiso(user, PERMISOS.VER_CONSULTAS)) {
  // mostrar sección
}
```

> La visibilidad en el frontend no reemplaza la seguridad del backend. El backend valida permisos y alcance real en cada endpoint.

## Objeto de usuario (`/api/auth/me`)

```json
{
  "id": 1,
  "username": "usuario@correo.com",
  "nombre": "Nombre Apellido",
  "tipoPerfil": "ASESOR",
  "rolNombre": "Asesor",
  "rolId": 2,
  "perfilId": 5,
  "permisos": [
    { "id": 1, "nombre": "Ver consultas" },
    { "id": 2, "nombre": "Acceder inicio" }
  ]
}
```

## Formato de errores del backend

```json
{
  "fecha": "2026-05-30T10:00:00",
  "estado": 400,
  "error": "Bad Request",
  "mensaje": "Mensaje descriptivo para el usuario",
  "ruta": "/api/endpoint",
  "detalles": {
    "campo": "Mensaje de validación del campo"
  }
}
```

Usar `lib/api.js` para extraer mensajes:

```javascript
import { readResponseBody, getApiErrorTitle, getApiErrorDescription } from "@/lib/api";

const payload = await readResponseBody(response);
const titulo = getApiErrorTitle(payload);
const descripcion = getApiErrorDescription(payload);
```

## Pruebas

```bash
npm run test           # ejecuta las pruebas Playwright en modo headless
npm run test:ui        # abre la interfaz visual de Playwright
npm run test:headed    # ejecuta con navegador visible
```

## Convenciones de desarrollo

- Usar `API_URL_BASE` de `lib/config.js` para todos los endpoints (nunca hardcodear URLs).
- Usar `apiClient` de `lib/apiClient.js` en código nuevo para centralizar headers y credenciales.
- Usar `cn()` de `lib/utils.js` para combinar clases Tailwind con condiciones.
- Documentar componentes y funciones con JSDoc siguiendo el patrón de `lib/authz.js`.
- Los archivos de test van en `src/__tests__/` con nombre `NombreComponente.test.jsx`.
