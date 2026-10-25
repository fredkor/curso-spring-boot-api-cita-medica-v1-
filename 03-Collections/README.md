# Suite Postman del backend

Importar en Postman:

- `cita-medica.postman_collection.json`: 46 peticiones con comprobaciones HTTP.
- `local.postman_environment.json`: entorno local.
- `health.postman_collection.json`: comprobación opcional de salud, separada porque la dependencia externa puede provocar HTTP 503.

## Preparación

1. Iniciar PostgreSQL y el backend con sus migraciones aplicadas (ver `../02-Infra/README.md` y el README del backend).

   La conexión local del backend es `jdbc:postgresql://localhost:5460/citamedica`, con usuario `citamedica`, contraseña `citamedica123` y esquema `citamedica`. La contraseña del login HTTP es la del usuario de la aplicación y se configura por separado en Postman.
2. Seleccionar el entorno **Cita Médica - Local**. El entorno incluye `username=admin` y `password=123456` para pruebas locales. Ajustar `baseUrl` si el backend usa otra dirección. El usuario debe existir en la aplicación y aceptar esas credenciales.
3. Revisar las variables de la colección: `medicoId=2` debe corresponder a un médico existente, `rolPacienteId=3` al rol PACIENTE y `estatusCitaId=1` al estado RESERVADA. Son valores iniciales para revisar contra la base; los IDs pueden variar.
4. Evitar variables de entorno con los mismos nombres que las variables de colección para no ocultar los IDs y el token guardados automáticamente.

## Ejecución

Usar Collection Runner con una iteración, ejecutando las carpetas 01 a 11 en orden. El perfil `dev` permite la petición OpenAPI. Login guarda el token Bearer y genera un sufijo para los registros de la ejecución. Cada alta guarda su ID para consultas, modificaciones y limpieza. Las fechas de citas se generan siete días hacia el futuro en la zona local del cliente.

El flujo crea sucursal, especialidad, paciente, asignación médica y citas; confirma y atiende una cita, cancela otra y verifica errores 400, 401 y 404. La carpeta final elimina los registros creados, respetando las dependencias. Ejecutar contra una base de desarrollo: el flujo modifica datos. Si una petición falla, revisar antes de continuar; una ejecución incompleta puede dejar registros para limpiar con los IDs guardados. No ejecutar limpieza después de un alta fallida que conserve un ID de una ejecución anterior.

El alta del paciente envía `password=123456`. El backend exige una contraseña de 6 a 72 caracteres al crear usuarios y la almacena con BCrypt. Al actualizar, omitir `password` conserva la contraseña existente; enviarla permite cambiarla. Las respuestas no incluyen la contraseña.

La petición **Crear otra cita para cancelar** genera un horario futuro al menos un día posterior al de la primera cita y descarta el `citaCancelarId` anterior antes del alta. La base impide duplicar paciente, asignación médica y hora de inicio, independientemente del estado de la cita. La cancelación y la limpieza utilizan el ID devuelto por esta segunda alta.

Las peticiones se pueden ejecutar individualmente después del login y de crear sus dependencias. Para editar una cita, debe seguir RESERVADA: una vez atendida, repetir primero su creación. Un token vencido requiere repetir login; esto también cambia el sufijo utilizado por las altas.

Con Newman instalado, desde la raíz del repositorio:

```bash
newman run 03-Collections/cita-medica.postman_collection.json \
  -e 03-Collections/local.postman_environment.json \
  --bail
```

`--bail` detiene la ejecución al fallar una comprobación; en ese caso la limpieza puede quedar pendiente. Usar Postman para conservar y revisar los IDs durante el diagnóstico. Las credenciales incluidas son de prueba local; no guardar credenciales de producción ni tokens reales en los archivos versionados.

Los archivos y scripts se validaron estáticamente. Las pruebas HTTP requieren el backend levantado y credenciales válidas. Esta suite no incluye una matriz de autorización entre médicos y pacientes; requiere preparar cuentas y citas específicas para cada identidad.
