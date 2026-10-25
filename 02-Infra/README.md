# Base de datos local

Desde la raíz del repositorio, levantar PostgreSQL:

```bash
docker compose -f 02-Infra/compose.yaml up -d
```

Consultar el estado y los logs:

```bash
docker compose -f 02-Infra/compose.yaml ps
docker compose -f 02-Infra/compose.yaml logs -f pg-postgres
```

Conexión: `localhost:5460`, base `citamedica`, usuario `citamedica` y contraseña `citamedica123`. Estas credenciales son para desarrollo local.

Para conectar el backend, configurar estas variables en su archivo `.env` (ver las instrucciones de carga en `../01-Projects/api-cita-medica-v1/README.md`):

```dotenv
DB_URL=jdbc:postgresql://localhost:5460/citamedica
DB_USERNAME=citamedica
DB_PASSWORD=citamedica123
DB_SCHEMA=citamedica
```

Flyway crea el esquema y ejecuta las migraciones al iniciar el backend. Esta configuración no crea la base `citamedica_test` utilizada por el perfil de pruebas.

Detener los servicios conservando los datos:

```bash
docker compose -f 02-Infra/compose.yaml down
```

Los datos persisten en el volumen `pg-votes-data`. La imagen usa la etiqueta `latest`, como en la configuración solicitada; para entornos reproducibles se debe fijar una versión disponible.

La configuración de Docker se encuentra en `compose.yaml`; PostgreSQL utiliza la imagen publicada y no requiere un Dockerfile propio.

Si el volumen ya se inicializó con otro nombre de base, usuario o contraseña, cambiar Compose no modifica esos datos existentes. En ese caso, crear o ajustar el usuario `citamedica` con contraseña `citamedica123`, crear la base `citamedica` si falta y conceder al usuario permisos para crear el esquema y ejecutar las migraciones desde una sesión administrativa con las credenciales anteriores, o respaldar los datos antes de recrear el volumen. No eliminar el volumen si se necesita conservar su contenido. Si el backend ya tiene un archivo `.env`, actualizar también su conexión; copiar `.env.example` sin sobrescribir no cambia un `.env` existente.
