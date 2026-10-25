# API de citas médicas

API REST para gestionar sucursales, especialidades, usuarios, médicos y citas médicas. Implementa un monolito modular con arquitectura en capas, seguridad mediante JWT y migraciones de PostgreSQL con Flyway.

## Arquitectura

El proyecto utiliza un reactor Maven con seis módulos:

| Módulo | Responsabilidad |
| --- | --- |
| `cita-medica-application` | Punto de entrada Spring Boot, configuración e integración de módulos. |
| `cita-medica-core` | Citas y asignaciones de médicos a especialidades y sucursales. |
| `cita-medica-catalogos` | Sucursales, especialidades y estados de las citas. |
| `cita-medica-usuarios` | Consulta y mantenimiento de usuarios e integración con seguridad. |
| `cita-medica-platform` | Configuración técnica, observabilidad, eventos y tareas Quartz. |
| `cita-medica-shared` | Excepciones, respuestas de error y paginación compartidas. |

Dentro de los módulos funcionales se separan las responsabilidades:

```text
HTTP → web/controller → application/service → persistence/repository → PostgreSQL
       DTO               lógica y mapeo        entidades JPA
```

Los controladores delegan en servicios y los servicios utilizan repositorios. MapStruct transforma DTO y entidades. `application` integra los módulos; `platform` depende de `core`, y `core` depende de `catalogos`. La seguridad se integra mediante la dependencia externa `mx.com.fredkor:security-starter:1.0.0-SNAPSHOT`.

Es una arquitectura en capas tradicional: los servicios utilizan DTO del paquete `web`, y algunos servicios de `core` acceden a repositorios de `catalogos`. Los límites entre módulos no se encuentran aislados mediante puertos ni verificados por pruebas de arquitectura.

## Requisitos

- JDK 25.
- Maven compatible con el proyecto, o el wrapper incluido (`./mvnw`).
- PostgreSQL accesible y una base creada para cada ambiente.
- Acceso al repositorio de snapshots de Spring: el POM usa Spring Boot `4.1.2-SNAPSHOT`.
- `security-starter:1.0.0-SNAPSHOT` instalado en el repositorio Maven local o disponible en un repositorio configurado.

Si tienes el starter en el directorio hermano `../security-starter`, puedes instalarlo con:

```bash
mvn -f ../security-starter/pom.xml install -DskipTests
```

## Configuración por ambiente

Los archivos están en `cita-medica-application/src/main/resources`. Spring carga `application.yaml` y aplica las propiedades del perfil seleccionado. YAML expresa las propiedades de Spring; no se requiere un archivo `.properties` adicional.

| Perfil | Archivo | Comportamiento |
| --- | --- | --- |
| `dev` (predeterminado) | `application-dev.yaml` | Logs de aplicación y SQL en DEBUG, Swagger habilitado, Actuator con health, info, metrics y loggers. |
| `test` | `application-test.yaml` | Conexión a base exclusiva de pruebas, Quartz sin arranque automático, Swagger deshabilitado. |
| `prod` | `application-prod.yaml` | Conexión y CORS obligatorios por ambiente, logs INFO sin SQL detallado, Swagger deshabilitado y Actuator limitado a health e info. |

En todos los perfiles, Hibernate valida el esquema (`ddl-auto: validate`) y Flyway aplica y valida las migraciones. El perfil `test` usa PostgreSQL real; no crea una base en memoria ni agrega pruebas automatizadas. Activa un solo perfil de ambiente a la vez.

| Variable | Uso / valor predeterminado |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `dev`, `test` o `prod`; sin selección explícita se usa `dev`. |
| `SERVER_PORT` | Puerto HTTP; `8080`. |
| `DB_URL` | URL JDBC; en dev `jdbc:postgresql://localhost:5460/citamedica`; obligatoria en prod. |
| `DB_USERNAME` | Usuario PostgreSQL; `citamedica` en dev; obligatorio en prod. |
| `DB_PASSWORD` | Contraseña local `citamedica123` en dev; obligatoria por variable en prod. |
| `DB_SCHEMA` | Esquema; `citamedica`. Ver restricción de migraciones debajo. |
| `JWT_SIGNING_KEY` | Clave de firma en Base64, obligatoria en todos los perfiles. |
| `JWT_ISSUER` | Emisor JWT; `cita-medica-api`. |
| `JWT_EXPIRATION` | Duración del token; `10m`. |
| `CORS_ALLOWED_ORIGINS` | Orígenes separados por comas; en dev/test `http://localhost:4200`; obligatorio en prod. |
| `TEST_DB_URL` | URL de pruebas; `jdbc:postgresql://localhost:5460/citamedica_test`. |
| `TEST_DB_USERNAME` | Usuario de pruebas; `citamedica`. |
| `TEST_DB_PASSWORD` | Contraseña obligatoria para el perfil test. |

El placeholder `securitySchema` de Flyway utiliza el mismo `DB_SCHEMA` que JPA. Sin embargo, las migraciones de negocio existentes contienen referencias explícitas a `citamedica` y algunas sentencias de inserción no califican el esquema. **Mantén `DB_SCHEMA=citamedica` en los tres ambientes**, usando bases distintas. Cambiar de esquema requiere un trabajo adicional de migración; los perfiles por sí solos no lo resuelven.

## Arranque local

1. Desde la raíz del repositorio, inicia la base local con Docker Compose:

```bash
docker compose -f 02-Infra/compose.yaml up -d
```

Compose crea la base `citamedica`, accesible en `localhost:5460` con usuario `citamedica` y contraseña `citamedica123`. Flyway crea el esquema `citamedica` al arrancar el backend. La configuración de Docker está en [compose.yaml](../../02-Infra/compose.yaml); no se utiliza un Dockerfile propio para PostgreSQL.

Los siguientes comandos se ejecutan desde `01-Projects/api-cita-medica-v1`.
2. Copia el ejemplo y completa los valores vacíos:

```bash
cp .env.example .env
```

Puedes generar una clave JWT aleatoria en Base64 y colocar el resultado en `JWT_SIGNING_KEY` dentro de `.env`:

```bash
openssl rand -base64 32
```

3. Carga las variables en Bash y compila:

```bash
set -a
source .env
set +a
mvn clean package -DskipTests
```

4. Ejecuta el artefacto:

```bash
java -jar cita-medica-application/target/cita-medica-application-0.0.1-SNAPSHOT.jar
```

Spring Boot no carga `.env` automáticamente: el ejemplo anterior lo carga en el entorno del proceso. Usa sintaxis Bash y comillas para valores con espacios o caracteres especiales. `.env` está excluido de Git; `.env.example` se comparte sin secretos.

En desarrollo:

- Swagger: `http://localhost:8080/swagger-ui/index.html`.
- OpenAPI: `http://localhost:8080/v3/api-docs`.
- Salud: `http://localhost:8080/actuator/health`.
- Autenticación: ruta base `/api/v1/auth`.
- Recursos: `/api/v1/citas`; consulta Swagger para el resto de endpoints y sus contratos.

## Otros ambientes

Para el perfil test, crea primero la base `citamedica_test`, configura `TEST_DB_PASSWORD` y conserva `JWT_SIGNING_KEY` en el entorno:

```bash
SPRING_PROFILES_ACTIVE=test java -jar cita-medica-application/target/cita-medica-application-0.0.1-SNAPSHOT.jar
```

Para producción, inyecta `SPRING_PROFILES_ACTIVE=prod`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SIGNING_KEY` y `CORS_ALLOWED_ORIGINS` desde el servicio de despliegue. Usa un origen concreto como `https://citas.ejemplo.com`. Después ejecuta el mismo JAR:

```bash
java -jar cita-medica-application/target/cita-medica-application-0.0.1-SNAPSHOT.jar
```

Las migraciones actuales incluyen usuarios y citas de ejemplo en `core`. Revisa esos datos y sus credenciales antes de utilizar el proyecto en producción. Separar datos de demostración de migraciones estructurales requiere considerar las bases que ya tienen historial Flyway; no se modificaron migraciones existentes en este ajuste.

## Verificación

```bash
mvn verify
```

Esta orden compila los módulos y ejecuta las pruebas que existan. Se incluyen pruebas de validación de DTO, parámetros HTTP, respuestas de error, documentación OpenAPI y creación de citas con repositorios simulados. No requieren PostgreSQL. Para comprobar cada perfil en ejecución necesitas PostgreSQL y sus variables; valida el arranque, las migraciones y `/actuator/health`. En prod, `/v3/api-docs` y Swagger deben quedar deshabilitados.

## Diagramas

- [Diagrama de negocio](docs/diagramas/negocio.png).
- [Diagrama de base de datos](docs/diagramas/base-de-datos.png).
- Versiones escalables: [negocio SVG](docs/diagramas/negocio.svg) y [base de datos SVG](docs/diagramas/base-de-datos.svg).

Si tienes Graphviz y el código del starter en el directorio hermano, puedes regenerarlos mediante `python3 docs/diagramas/generar.py`.

## Estado funcional

La organización modular y la configuración por ambiente no implican que todos los casos de negocio estén terminados. El alta y la edición de citas validan los datos de entrada y resuelven sus referencias en base de datos. La confirmación, cancelación y atención tienen transiciones controladas; el envío de correo permanece pendiente. Quartz incluye una cancelación diaria a las 23:01 según la zona del scheduler para citas RESERVADAS cuyo inicio sea menor o igual a ahora + 24 horas, incluidas las vencidas.


## Validaciones y errores en Swagger

Con el perfil `dev`, abre `/swagger-ui/index.html`. Los esquemas muestran las restricciones de los DTO y los endpoints documentan los cuerpos y ejemplos de errores. Usa **Authorize** con tu JWT para ejecutar operaciones protegidas.

- Los textos obligatorios rechazan valores en blanco; sus longitudes respetan las columnas SQL.
- Los correos deben tener formato válido y los identificadores deben ser positivos.
- La paginación requiere `page >= 0` y `1 <= size <= 100`; `orderBy` tiene una lista permitida por recurso y `orderDir` acepta `asc` o `desc`.
- Los importes de una cita son no negativos, con hasta ocho enteros y dos decimales. El término debe ser posterior al inicio y `total = precio + iva`. El IVA es un importe.
- Los IDs relacionados deben existir. Se comprueba su existencia al guardar las asignaciones y las citas.

El cuerpo de error de la API contiene `traceId`, `status`, `error`, `message`, `path`, `timestamp` y `fieldErrors`. Los errores emitidos por los filtros del starter de seguridad utilizan `SecurityErrorResponse`, sin `fieldErrors`; ambos contratos están documentados.

| Estado | Significado |
| --- | --- |
| 400 | DTO, parámetros, formato de JSON o regla de negocio inválidos. |
| 401 | Autenticación requerida o credenciales inválidas. |
| 403 | El usuario no tiene el permiso requerido. |
| 404 | El recurso solicitado o una referencia no existe. |
| 409 | Conflicto de integridad, como un duplicado o un recurso referenciado. |
| 415 | Tipo de contenido no admitido. |
| 500 | Error inesperado; el detalle técnico queda en el log. |

El contrato de creación y actualización de citas ahora recibe estos campos (sustituye los IDs por registros existentes):

```json
{
  "medicoSucursalId": 1,
  "pacienteId": 6,
  "estatusCitaId": 1,
  "fechaHoraInicio": "2027-01-20T09:00:00",
  "fechaHoraTermino": "2027-01-20T09:30:00",
  "precio": 500.00,
  "iva": 80.00,
  "total": 580.00
}
```

Este contrato reemplaza los antiguos campos `nombre` y `direccion` del cuerpo de citas. Las respuestas conservan fecha y hora completas. Para probar un error 400 en Swagger, envía un término anterior al inicio, un total incorrecto o una sucursal con `nombre` en blanco. La validación de solapamiento de agenda requiere reglas de negocio adicionales. Las transiciones disponibles se describen debajo.

## Transacciones, auditoría y paginación de usuarios

Los servicios de citas, usuarios, catálogos y asignaciones médicas utilizan `@Transactional(readOnly = true)` para lecturas. Las operaciones de creación, actualización y eliminación sobrescriben esa configuración con `@Transactional`; la cancelación masiva de citas también utiliza una transacción de escritura. Las validaciones de referencias, las modificaciones y el mapeo de relaciones se ejecutan dentro de la misma unidad de trabajo. Las excepciones de ejecución provocan rollback.

La creación, actualización y eliminación individual de citas publican `CitaAuditEvent` dentro de la transacción. `CitaAuditListener` registra el evento en logs únicamente en la fase `AFTER_COMMIT`, con acción, ID de cita, usuario autenticado, fecha UTC y `traceId`. Si no existe un usuario autenticado, el actor es `SISTEMA`. Los eventos de transacciones revertidas o publicados sin transacción no se registran como operaciones confirmadas. Esta auditoría se almacena en logs, sin una tabla de historial ni datos clínicos en el evento.

Ejemplo de registro:

```text
[AUDIT] action=CITA_UPDATE citaId=12 actor=administrador timestamp=2027-01-01T00:00:00Z traceId=...
```

El listado de usuarios pagina primero los registros en SQL, sin hacer fetch de una colección. Después carga los roles de los IDs de esa página mediante `LEFT JOIN FETCH` sin paginación. Así conserva el orden, incluye usuarios sin roles y evita una consulta de roles por cada usuario. Una página completa requiere la consulta de usuarios, el conteo y la carga de roles; Spring Data puede omitir el conteo cuando puede deducir el total.

Las pruebas de persistencia usan H2 únicamente en el alcance de test. Verifican paginación y roles, rollback de cambios ante una referencia inválida y auditoría después del commit. No reemplazan las pruebas de despliegue contra PostgreSQL.


## Autenticación y autorización del proceso

El starter proporciona `POST /api/v1/auth/login`. Envía `username` y `password`; usa el `token` devuelto en `Authorization: Bearer <token>`. Los endpoints de usuarios exigen `USUARIO_VER`, `USUARIO_CREAR`, `USUARIO_MODIFICAR` o `USUARIO_ELIMINAR`, según la operación. Estos permisos están asignados al administrador; un paciente o médico no puede administrar usuarios ni asignarse roles mediante esos endpoints. Los servicios de usuarios también validan los permisos mediante seguridad de métodos.

`POST /api/v1/usuarios` requiere además `password` (de 6 a 72 caracteres), que se almacena con BCrypt. En `PUT /api/v1/usuarios/{id}`, omitir `password` conserva la contraseña actual; enviarla la cambia. La contraseña se excluye de las respuestas y del texto del DTO registrado en logs.

En citas, se exige tanto el permiso de la operación como acceso al registro:

| Operación | Administrador | Paciente | Médico |
| --- | --- | --- | --- |
| Consultar/listar | Todas, con `CITA_VER_TODO` | Citas propias | Citas asignadas o propias como paciente |
| Crear | Para cualquier paciente | Para sí mismo | Para su asignación médica o para sí mismo como paciente |
| Editar | Citas RESERVADAS o REPROGRAMADAS | Sin permiso en la configuración actual | Citas accesibles RESERVADAS o REPROGRAMADAS; no puede cambiar paciente, asignación ni importes |
| Confirmar/cancelar | Citas accesibles con el permiso correspondiente | Citas propias | Citas asignadas o propias como paciente |
| Atender | Con `CITA_ATENDER` | Sin permiso | Solo si es el médico asignado |
| Eliminar físicamente | Con `CITA_ELIMINAR` y `CITA_VER_TODO` | Sin permiso | Sin permiso |

`CITA_VER_TODO` habilita el alcance global de administración; no reemplaza los permisos específicos de las otras operaciones. El listado aplica el alcance del usuario en SQL antes de paginar. Las operaciones individuales validan la pertenencia dentro del servicio.

Las citas nuevas deben iniciar en el futuro y utilizar el estado RESERVADA. El `PUT /api/v1/citas/{id}` conserva el estado y solo edita citas RESERVADAS o REPROGRAMADAS con inicio futuro. Los estados se cambian mediante estas operaciones sin cuerpo:

| Endpoint | Permiso | Transición |
| --- | --- | --- |
| `PATCH /api/v1/citas/{id}/confirmar` | `CITA_CONFIRMAR` | RESERVADA o REPROGRAMADA → CONFIRMADA |
| `PATCH /api/v1/citas/{id}/cancelar` | `CITA_CANCELAR` | RESERVADA, REPROGRAMADA o CONFIRMADA → CANCELADA |
| `PATCH /api/v1/citas/{id}/atender` | `CITA_ATENDER` | CONFIRMADA → ATENDIDA |

Una transición inválida devuelve 400; acceso insuficiente devuelve 403. Los cambios de estado y la edición bloquean la fila durante su transacción para serializar cambios concurrentes. Se emiten eventos de auditoría después del commit para estas nuevas acciones. No se agregaron operaciones para reprogramar ni pagar.

Las pruebas de integración levantan la aplicación con H2 y ejercitan login y JWT reales, permisos de usuarios, acceso a citas ajenas, filtros de alcance, creación y transiciones de estado. Reinicia la aplicación para que el inicializador sincronice los permisos nuevos del administrador. También se alineó el nombre del executor asíncrono que utiliza el listener de creación para permitir completar el flujo de alta.

## Health check de la página de acceso

El indicador `externalServiceHealthCheck` realiza un GET a `https://jolly-desert-0a969b710.1.azurestaticapps.net/auth/login`. Reporta `UP` únicamente si recibe HTTP 200; otros estados, errores de conexión y tiempos de espera producen `DOWN`. No sigue redirecciones ni envía credenciales.

Puedes configurar la URL con `EXTERNAL_SERVICE_HEALTH_URL` y el tiempo límite en milisegundos con `EXTERNAL_SERVICE_HEALTH_TIMEOUT_MS` (3000 por defecto). El cliente limita la conexión y la espera de respuesta; no necesita descargar el HTML completo.

Consulta `/actuator/health` para el estado agregado. En desarrollo, los detalles autorizados incluyen URL, estado esperado y estado HTTP recibido, o la razón de indisponibilidad. En producción los detalles están ocultos. Un indicador `DOWN` hace que la salud agregada reporte `DOWN` y normalmente HTTP 503.

Esta comprobación confirma la respuesta HTTP de la página; no ejecuta JavaScript ni valida el inicio de sesión. Si la ruta `/auth/login` es una ruta del frontend, el hosting debe servirla directamente con HTTP 200. Al verificarla durante este cambio respondió HTTP 404; no se cambió el hosting externo.
