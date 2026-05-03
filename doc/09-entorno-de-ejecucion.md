# Entorno de ejecución

## 1. Propósito

Este documento describe cómo ejecutar y configurar el backend localmente en el módulo `backend/app` del proyecto. Incluye los requisitos, la compilación, la configuración de la base de datos, JWT, CORS, correo, recuperación de contraseña y Docker Compose.

## 2. Requisitos

- Java 21
- Maven o Maven Wrapper (`mvnw` / `mvnw.cmd`)
- PostgreSQL para entornos con datos persistentes
- Docker y Docker Compose para desarrollo local opcional
- SMTP si se usa la recuperación de contraseña por correo

## 3. Ejecución local

### Desde el directorio del backend

Ir a `backend/app`:

```bash
cd backend/app
```

Ejecutar con Maven Wrapper:

```bash
./mvnw spring-boot:run
```

En Windows PowerShell:

```powershell
./mvnw.cmd spring-boot:run
```

Si tienes Maven instalado globalmente:

```bash
mvn spring-boot:run
```

El backend arranca por defecto en el puerto `8080`.

### Ejecución con Docker Compose

Desde la raíz del repositorio:

```bash
docker compose up --build
```

Esto construye y ejecuta:

- `backend-app` a partir de `backend/app/Dockerfile`
- `frontend` a partir de `frontend/Dockerfile`

## 4. Compilación

Desde `backend/app`:

```bash
./mvnw -DskipTests package
```

O con Maven instalado:

```bash
mvn -DskipTests package
```

El JAR resultante queda en `backend/app/target/*.jar`.

Para construir solo la imagen Docker del backend:

```bash
cd backend/app
docker build -t backend-app .
```

## 5. Configuración de base de datos

### Configuración por defecto

El archivo `backend/app/src/main/resources/application.properties` usa H2 en memoria:

```properties
spring.datasource.url=jdbc:h2:mem:legal_cases
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### Configuración para PostgreSQL

Para usar PostgreSQL localmente, reemplaza o extiende estas propiedades con:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/legal_cases
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=<usuario>
spring.datasource.password=<contraseña>
spring.jpa.hibernate.ddl-auto=update
```

### Observaciones

- El proyecto incluye la dependencia `org.postgresql:postgresql` en `backend/app/pom.xml`.
- El valor `spring.jpa.hibernate.ddl-auto=update` sincroniza el esquema automáticamente.
- La consola H2 está habilitada en `http://localhost:8080/h2-console` en la configuración por defecto.

## 6. Configuración JWT

Las propiedades reales en `application.properties` son:

```properties
app.jwt.secret=legal-cases-secret-key-development-123456789
app.jwt.expiration-ms=3600000
```

- `app.jwt.secret`: clave para firmar los tokens JWT. No debe subirse a un repositorio ni compartirse.
- `app.jwt.expiration-ms`: tiempo de expiración del token en milisegundos. Aquí está configurado para 1 hora.

## 7. Configuración CORS

La configuración de CORS se define en `backend/app/src/main/java/co/edu/ufps/legal_cases/security/config/CorsConfig.java`.

Configuración actual:

- Origen permitido: `http://localhost:3000`
- Métodos permitidos: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`
- Cabeceras permitidas: `*`
- Credenciales permitidas: `true`
- `maxAge`: `3600`

Esto significa que el frontend local en `localhost:3000` puede hacer peticiones al backend y enviar cookies.

## 8. Configuración de correo

Las propiedades reales para correo en `application.properties` son:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=consultorio.info01@gmail.com
spring.mail.password=evlb cuxwwqmoddds
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

- `spring.mail.host`: servidor SMTP.
- `spring.mail.port`: puerto SMTP.
- `spring.mail.username`: usuario SMTP.
- `spring.mail.password`: contraseña SMTP.
- `spring.mail.properties.mail.smtp.auth`: autenticación SMTP activada.
- `spring.mail.properties.mail.smtp.starttls.enable`: STARTTLS activado.

> No debes subir `spring.mail.username` ni `spring.mail.password` reales al repositorio. Usa variables de entorno o un archivo local no versionado.

## 9. Configuración de recuperación de contraseña

Las propiedades detectadas son:

```properties
app.frontend.reset-password-url=http://localhost:3000/restablecer-password
app.password-reset.expiration-minutes=15
```

- `app.frontend.reset-password-url`: URL del frontend para el formulario de restablecimiento.
- `app.password-reset.expiration-minutes`: caducidad del token de recuperación en minutos.

## 10. Plantillas de correo

La plantilla de correo real para recuperación de contraseña está en:

- `backend/app/src/main/resources/templates/emails/recuperacion-password.html`

Se trata de una plantilla Thymeleaf con un botón de restablecimiento y valores dinámicos como `${enlace}` y `${minutosExpiracion}`.

## 11. Docker Compose

El archivo `docker-compose.yml` en la raíz define los servicios:

- `backend-app`
  - contexto: `./backend/app`
  - Dockerfile: `backend/app/Dockerfile`
  - puertos: `8080:8080`
- `frontend`
  - contexto: `./frontend`
  - Dockerfile: `frontend/Dockerfile`
  - puertos: `3000:3000`
  - environment: `NEXT_PUBLIC_API_URL=http://backend-app:8080`
  - depends_on: `backend-app`

### Nota

- El `docker-compose.yml` no incluye un servicio PostgreSQL.
- Para usar PostgreSQL en el entorno de Docker, se debe agregar un contenedor `postgres` y ajustar las propiedades de conexión.

## 12. Variables sensibles que no deben subirse

No debes versionar valores sensibles ni secretos.

Propiedades que deben mantenerse fuera del control de versiones:

- `app.jwt.secret`
- `spring.mail.username`
- `spring.mail.password`
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

Recomendación: usa un archivo local no versionado o variables de entorno para estas propiedades.

## 13. Endpoints para verificar que el backend está funcionando

- `http://localhost:8080/api/auth/login` (POST): verificar respuesta del backend.
- `http://localhost:8080/api/auth/me` (GET): verificar sesión activa con cookie JWT.
- `http://localhost:8080/h2-console` (GET): disponible en la configuración por defecto para H2.

## 14. Estado actual

- El backend está preparado para ejecución local.
- No hay configuración productiva de despliegue en este repositorio.
- La orquestación `docker-compose.yml` está pensada para desarrollo local y contiene backend y frontend.
- PostgreSQL no está definido como servicio en Docker Compose; su uso requiere configuración adicional.
