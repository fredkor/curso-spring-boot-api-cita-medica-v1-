-- Flyway se ejecuta antes del ApplicationRunner que sincroniza los permisos
-- del YAML. Los INSERT ... SELECT de V202 no asignan roles en una base nueva
-- porque el catálogo de roles todavía está vacío en ese momento.
INSERT INTO citamedica.rol (clave, descripcion, activo)
SELECT datos.clave, datos.clave, TRUE
FROM (VALUES ('ADMINISTRADOR'), ('MEDICO'), ('PACIENTE')) AS datos(clave)
WHERE NOT EXISTS (
    SELECT 1 FROM citamedica.rol r WHERE upper(r.clave) = datos.clave
);

-- Conserva la intención de los datos iniciales: todos los roles para admin,
-- médico y paciente para los médicos, y paciente para los demás usuarios.
-- No altera asignaciones existentes ni utiliza IDs de usuarios o roles.
INSERT INTO citamedica.usuario_rol (id_usuario, id_rol, fecha_creacion)
SELECT u.id_usuario, r.id_rol, CURRENT_TIMESTAMP
FROM (VALUES
    ('admin', 'ADMINISTRADOR'),
    ('admin', 'MEDICO'),
    ('admin', 'PACIENTE'),
    ('emiliano.vh', 'MEDICO'), ('emiliano.vh', 'PACIENTE'),
    ('maria.jr', 'MEDICO'), ('maria.jr', 'PACIENTE'),
    ('cinthya.cm', 'MEDICO'), ('cinthya.cm', 'PACIENTE'),
    ('samantha.ch', 'MEDICO'), ('samantha.ch', 'PACIENTE'),
    ('yanelli.vs', 'PACIENTE'),
    ('aldo.vj', 'PACIENTE'),
    ('diana.vj', 'PACIENTE'),
    ('araceli.pn', 'PACIENTE')
) AS datos(username, clave)
JOIN citamedica.usuario u ON upper(u.username) = upper(datos.username)
JOIN citamedica.rol r ON upper(r.clave) = datos.clave
WHERE NOT EXISTS (
    SELECT 1 FROM citamedica.usuario_rol ur
    WHERE ur.id_usuario = u.id_usuario AND ur.id_rol = r.id_rol
);
