# Sistema de gestión de casos jurídicos 
> (nombre por decidir)
> Servidores en Vercel y Railway

## Ejecutar localmente

### Por docker compose

Es necesario tener docker instalado. También hay que asegurarse de tener el plugin
para Docker Compose, simplemente probar en la terminal una vez instalado:

```bash
docker --version
docker compose --version 
# quizás aparece como "docker-compose", con "-"
```

Desde la raíz del proyecto (donde está este README), ejecutar:

```bash
docker compose up --build
```

> (necesita permisos de sudo en linux, o administrador en windows)

### Individualmente

#### backend

El backend utiliza Maven. Asegurarse de tener instalado maven con:

```bash
mvn --version
```

y ubicarse en la raíz del módulo a ejecutar (donde se encuentra el pom.xml).

Primero, compilar: `mvn compile`, y luego:

```bash
./mvnw spring-boot:run
```

#### frontend

El frontend utiliza Nextjs. Cuenta con el perfil default de development, así que
siempre y cuando se cuente con Next instalado, hay que ejecutar dentro del directorio:

La instalación de las dependencias:
```bash
npm i
```

Y ejecutar el entorno de dev: 
```bash
npm run dev
```