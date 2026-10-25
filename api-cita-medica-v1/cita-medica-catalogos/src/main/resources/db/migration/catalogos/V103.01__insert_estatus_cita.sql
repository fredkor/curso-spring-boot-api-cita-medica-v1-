INSERT INTO citamedica.estatus_cita (clave, descripcion)
VALUES
    ('RESERVADA', 'Indica que la cita ha sido reservada'),
    ('CONFIRMADA', 'Indica que la cita ha sido confirmada'),
    ('ATENDIDA', 'Indica que la cita ha sido atendida'),
    ('PAGADA', 'Indica que la cita ha sido pagada despues de que el médico atendió a la consulta'),
    ('REPROGRAMADA', 'Indica que la cita ha sido reprogramada'),
    ('CANCELADA', 'Indica que la cita ha sido cancelada');