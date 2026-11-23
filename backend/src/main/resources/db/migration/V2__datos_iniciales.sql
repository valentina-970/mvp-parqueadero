INSERT INTO sede (nombre) VALUES ('Principal');

INSERT INTO tipo_vehiculo (codigo, nombre, patron_placa) VALUES
    ('CARRO', 'Carro', '^[A-Z]{3}[0-9]{3}$'),
    ('MOTO', 'Moto', '^[A-Z]{3}[0-9]{2}[A-Z]$');

INSERT INTO franja (codigo, hora_inicio, hora_fin) VALUES
    ('DIURNA', '06:00', '17:59'),
    ('NOCTURNA', '18:00', '05:59');

INSERT INTO tarifa (sede_id, tipo_vehiculo_id, franja_id, valor)
SELECT s.id, t.id, f.id, v.valor
FROM (VALUES
        ('CARRO', 'DIURNA', 5000),
        ('CARRO', 'NOCTURNA', 3000),
        ('MOTO', 'DIURNA', 0),
        ('MOTO', 'NOCTURNA', 0)
     ) AS v(tipo, franja, valor)
JOIN tipo_vehiculo t ON t.codigo = v.tipo
JOIN franja f ON f.codigo = v.franja
CROSS JOIN sede s;

INSERT INTO cupo (sede_id, tipo_vehiculo_id, capacidad)
SELECT s.id, t.id, v.capacidad
FROM (VALUES ('CARRO', 40), ('MOTO', 20)) AS v(tipo, capacidad)
JOIN tipo_vehiculo t ON t.codigo = v.tipo
CROSS JOIN sede s;

INSERT INTO parametro (clave, valor, descripcion) VALUES
    ('umbral_confianza', '0.80',
     'Confianza mínima para procesar una lectura automáticamente');