# Testing - Pruebas Automatizadas

## 📋 Descripción

Este proyecto incluye pruebas automatizadas usando **Playwright** para validar:
- Autenticación y login
- Asignación y cambio de roles de usuarios
- Funcionalidad del formulario de usuarios del sistema

## 🚀 Instalación

Ya está instalado en el proyecto. Si necesitas reinstalar:

```bash
npm install -D @playwright/test
```

## 🧪 Ejecutar Pruebas

### Modo básico (headless - sin GUI)
```bash
npm run test
```

### Modo UI interactivo
```bash
npm run test:ui
```

### Modo headed (con navegador visible)
```bash
npm run test:headed
```

### Modo debug
```bash
npm run test:debug
```

## 📝 Credenciales de Prueba

Para las pruebas automatizadas, usa:

- **Email**: `juandiegosb@ufps.edu.co`
- **Contraseña**: `1234`
- **Otros campos**: Rellenar con `a` (según sea necesario)

## 📂 Estructura de Pruebas

### 1. `e2e/auth.spec.js`
Pruebas de autenticación:
- ✅ Login exitoso con credenciales válidas
- ❌ Login fallido con credenciales inválidas
- ⚠️ Validación de campos requeridos
- 🚪 Logout

### 2. `e2e/usuario-sistema-roles.spec.js`
Pruebas de gestión de roles:
- ✅ Cargar página de usuarios del sistema
- ✅ Mostrar lista de personas en dropdown
- ✅ Mostrar campos específicos según rol seleccionado
- ✅ Validar campos requeridos
- ✅ Actualizar rol exitosamente
- ✅ Limpiar formulario

## 🔧 Configuración

La configuración está en `playwright.config.js`:

```javascript
// Base URL para las pruebas
baseURL: 'http://localhost:3000'

// El servidor se inicia automáticamente antes de las pruebas
webServer: {
  command: 'npm run dev',
  url: 'http://localhost:3000',
}
```

## 📊 Ver Resultados

Después de ejecutar las pruebas, los reportes en HTML se generan en:

```
frontend/playwright-report/
```

Para ver el reporte:

```bash
npx playwright show-report
```

## ⚙️ Notas Importantes

1. **Backend debe estar corriendo** en `http://localhost:8080` para que las pruebas funcionen correctamente
2. **Frontend debe estar disponible** en `http://localhost:3000`
3. **El servidor dev se inicia automáticamente** si usas `npm run test`
4. **Las pruebas son independientes** entre sí (cada una hace login de nuevo)

## 🐛 Troubleshooting

### Las pruebas fallan porque no encuentra elementos
- Asegúrate de que el servidor está corriendo
- Verifica que los selectores correspondan con tu HTML actual
- Usa `npm run test:headed` para ver qué está pasando

### Timeout esperando navegación
- El backend puede estar lento
- Aumenta el timeout en `playwright.config.js` si es necesario

### Error de conexión al backend
- Verifica que el backend está corriendo en puerto 8080
- Revisa la configuración en `API_URL` dentro de los componentes

## 📚 Recursos

- [Documentación de Playwright](https://playwright.dev/)
- [Guía de Best Practices](https://playwright.dev/docs/best-practices)

