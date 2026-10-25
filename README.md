# Proyecto de citas médicas

Backend para gestionar usuarios, sucursales, especialidades, asignaciones de médicos y citas médicas, con autenticación JWT. Este repositorio reúne el código, la infraestructura local y las colecciones para probar la API.

## Distribución del repositorio

```text
.
├── 01-Projects/
│   ├── api-cita-medica-v1/          # Backend Spring Boot modular
│   │   ├── cita-medica-application/ # Arranque, configuración e integración
│   │   ├── cita-medica-catalogos/   # Sucursales y especialidades
│   │   ├── cita-medica-core/        # Citas y asignaciones médicas
│   │   ├── cita-medica-platform/    # Observabilidad, eventos y tareas
│   │   ├── cita-medica-shared/      # Errores y paginación compartidos
│   │   ├── cita-medica-usuarios/    # Gestión de usuarios
│   │   └── docs/diagramas/         # Diagramas del negocio y de la BD
│   └── security-starter/           # Seguridad, login, JWT, roles y permisos
├── 02-Infra/
│   ├── compose.yaml               # PostgreSQL para desarrollo local
│   └── README.md
├── 03-Collections/
│   ├── cita-medica.postman_collection.json
│   ├── local.postman_environment.json
│   ├── health.postman_collection.json
│   └── README.md
└── README.md
```

| Documentación | Contenido |
| --- | --- |
| [Backend](01-Projects/api-cita-medica-v1/README.md) | Arquitectura, perfiles, variables, seguridad, reglas de negocio y pruebas. |
| [Infraestructura](02-Infra/README.md) | Inicio, conexión, logs y persistencia de PostgreSQL. |
| [Colecciones](03-Collections/README.md) | Preparación y ejecución de la suite en Postman o Newman. |

## Requisitos

- JDK 25.
- Maven o el wrapper incluido en cada proyecto.
- Docker con Docker Compose y el motor iniciado.
- Postman para ejecutar las colecciones; Newman es opcional.
- Acceso a los repositorios Maven configurados: el proyecto utiliza Spring Boot `4.1.2-SNAPSHOT`.

## 1. Levantar la base de datos

Desde la raíz del repositorio:

```bash
docker compose -f 02-Infra/compose.yaml up -d
docker compose -f 02-Infra/compose.yaml ps
docker compose -f 02-Infra/compose.yaml logs -f pg-postgres
```

Esperar a que PostgreSQL esté listo para aceptar conexiones. Salir de los logs con `Ctrl+C` conserva el contenedor en ejecución.

| Parámetro | Valor local |
| --- | --- |
| Host | `localhost` |
| Puerto | `5460` |
| Base de datos | `citamedica` |
| Usuario | `citamedica` |
| Contraseña | `citamedica123` |
| Esquema de la aplicación | `citamedica` |
| Zona horaria del contenedor | `America/Lima` |

Compose crea la base `citamedica`; Flyway crea el esquema y aplica las migraciones al iniciar el backend. Los datos se conservan en el volumen `pg-votes-data`. Las credenciales incluidas son para desarrollo local.

## 2. Configurar y arrancar el backend

En Bash, desde la raíz, instalar primero el starter de seguridad en el repositorio Maven local:

```bash
cd 01-Projects/security-starter
bash ./mvnw install -DskipTests
cd ../api-cita-medica-v1
```

Crear el archivo de configuración local si todavía no existe:

```bash
cp -n .env.example .env
```

Editar `.env` y ajustar estos valores para usar la base de `02-Infra`:

```dotenv
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080
DB_URL=jdbc:postgresql://localhost:5460/citamedica
DB_USERNAME=citamedica
DB_PASSWORD=citamedica123
DB_SCHEMA=citamedica
JWT_SIGNING_KEY=REEMPLAZAR_CON_UNA_CLAVE_BASE64
```

Generar la clave con el siguiente comando y copiar el resultado a `JWT_SIGNING_KEY`:

```bash
openssl rand -base64 32
```

El archivo `.env.example` y los valores predeterminados del backend coinciden con Compose. Mantener `DB_SCHEMA=citamedica`, porque las migraciones de negocio contienen referencias explícitas a ese esquema. `.env` está excluido de Git.

Cargar las variables, compilar y arrancar desde `01-Projects/api-cita-medica-v1`:

```bash
set -a
source .env
set +a
bash ./mvnw clean package -DskipTests
java -jar cita-medica-application/target/cita-medica-application-0.0.1-SNAPSHOT.jar
```

Spring Boot no carga `.env` automáticamente; las variables deben estar exportadas en la terminal que inicia Java. El backend ejecuta las migraciones de Flyway y valida el esquema con Hibernate durante el arranque.

## 3. Consultar la API

Con el backend iniciado en el perfil `dev`:

| Recurso | Dirección |
| --- | --- |
| Swagger UI | [Abrir Swagger](http://localhost:8080/swagger-ui/index.html) |
| Contrato OpenAPI | [Abrir OpenAPI](http://localhost:8080/v3/api-docs) |
| Salud | [Consultar salud](http://localhost:8080/actuator/health) |
| Login | `POST http://localhost:8080/api/v1/auth/login` |

El login recibe `username` y `password`, y devuelve `token`. Las operaciones protegidas utilizan `Authorization: Bearer <token>`.

El indicador de salud también consulta una página externa. Una falla de esa dependencia puede producir `DOWN` y HTTP 503; revisar los componentes de la respuesta y los logs para identificar la causa.

## 4. Probar con Postman

1. Importar [la colección principal](03-Collections/cita-medica.postman_collection.json) y [el entorno local](03-Collections/local.postman_environment.json).
2. Seleccionar **Cita Médica - Local** con las credenciales locales incluidas: usuario `admin` y contraseña `123456`. `baseUrl` está configurado como `http://localhost:8080`.
3. Revisar las variables de colección `medicoId`, `rolPacienteId` y `estatusCitaId` contra la base: deben identificar un médico, el rol PACIENTE y el estado RESERVADA, respectivamente.
4. Ejecutar las carpetas **01 a 11** en orden desde Collection Runner, con una iteración.

La suite contiene 46 peticiones: login, operaciones CRUD, confirmación, atención y cancelación de citas, validaciones y limpieza. Guarda automáticamente el token y los IDs creados. Genera fechas futuras para las citas y elimina los datos de la ejecución al final.

Usar una base de desarrollo. Si el flujo falla antes de la limpieza, pueden quedar registros pendientes; consultar [la guía de colecciones](03-Collections/README.md) antes de repetir o limpiar la ejecución. El usuario `admin` de la aplicación debe aceptar la contraseña `123456`; el entorno Postman no modifica las credenciales almacenadas en la base.

La [colección de salud](03-Collections/health.postman_collection.json) se ejecuta por separado por su dependencia externa. La guía de colecciones también incluye el comando para Newman.

## Verificación del código

Desde `01-Projects/api-cita-medica-v1`, con el starter instalado:

```bash
bash ./mvnw verify
```

Las pruebas automatizadas del proyecto y las colecciones HTTP cubren distintos niveles. Consultar el README del backend para su alcance. El perfil `test` utiliza una conexión PostgreSQL independiente; el Compose incluido no crea la base `citamedica_test`.

## Detener el entorno local

Detener el backend con `Ctrl+C` en la terminal donde se ejecuta Java. Desde la raíz del repositorio, detener PostgreSQL:

```bash
docker compose -f 02-Infra/compose.yaml down
```

Este comando conserva los datos del volumen para el siguiente arranque.
