-- 1. Sin dependencias
CREATE TABLE sede (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    nombre VARCHAR(80) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_sede PRIMARY KEY (id),
    CONSTRAINT uq_sede_nombre UNIQUE (nombre)
);

CREATE TABLE tipo_vehiculo (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(40) NOT NULL,
    patron_placa VARCHAR(60) NOT NULL,
    CONSTRAINT pk_tipo_vehiculo PRIMARY KEY (id),
    CONSTRAINT uq_tipo_vehiculo_codigo UNIQUE (codigo)
);

CREATE TABLE franja (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    codigo VARCHAR(20) NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    CONSTRAINT pk_franja PRIMARY KEY (id),
    CONSTRAINT uq_franja_codigo UNIQUE (codigo)
);

CREATE TABLE usuario (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    nombre_usuario VARCHAR(40) NOT NULL,
    contrasena_hash VARCHAR(100) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uq_usuario_nombre UNIQUE (nombre_usuario),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('OPERADOR', 'ADMINISTRADOR'))
);

CREATE TABLE parametro (
    clave VARCHAR(60) NOT NULL,
    valor VARCHAR(120) NOT NULL,
    descripcion VARCHAR(200),
    CONSTRAINT pk_parametro PRIMARY KEY (clave)
);

-- 2. Configuración que depende de las anteriores
CREATE TABLE tarifa (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    sede_id UUID NOT NULL,
    tipo_vehiculo_id UUID NOT NULL,
    franja_id UUID NOT NULL,
    valor NUMERIC(12,2) NOT NULL,
    vigente_desde TIMESTAMPTZ NOT NULL DEFAULT now(),
    vigente_hasta TIMESTAMPTZ,
    CONSTRAINT pk_tarifa PRIMARY KEY (id),
    CONSTRAINT fk_tarifa_sede FOREIGN KEY (sede_id) REFERENCES sede (id),
    CONSTRAINT fk_tarifa_tipo_vehiculo FOREIGN KEY (tipo_vehiculo_id) REFERENCES tipo_vehiculo (id),
    CONSTRAINT fk_tarifa_franja FOREIGN KEY (franja_id) REFERENCES franja (id),
    CONSTRAINT ck_tarifa_valor_no_negativo CHECK (valor >= 0)
);

CREATE UNIQUE INDEX uq_tarifa_vigente
    ON tarifa (sede_id, tipo_vehiculo_id, franja_id)
    WHERE vigente_hasta IS NULL;

CREATE TABLE cupo (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    sede_id UUID NOT NULL,
    tipo_vehiculo_id UUID NOT NULL,
    capacidad INTEGER NOT NULL,
    CONSTRAINT pk_cupo PRIMARY KEY (id),
    CONSTRAINT fk_cupo_sede FOREIGN KEY (sede_id) REFERENCES sede (id),
    CONSTRAINT fk_cupo_tipo_vehiculo FOREIGN KEY (tipo_vehiculo_id) REFERENCES tipo_vehiculo (id),
    CONSTRAINT ck_cupo_capacidad_no_negativa CHECK (capacidad >= 0),
    CONSTRAINT uq_cupo_sede_tipo UNIQUE (sede_id, tipo_vehiculo_id)
);

-- 3. Eventos de los carriles
CREATE TABLE evento_acceso (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    sede_id UUID NOT NULL,
    carril VARCHAR(10) NOT NULL,
    placa VARCHAR(6) NOT NULL,
    placa_leida VARCHAR(20) NOT NULL,
    tipo_vehiculo_id UUID NOT NULL,
    confianza NUMERIC(3,2) NOT NULL,
    tipo_lectura VARCHAR(12) NOT NULL,
    validado_por_id UUID,
    resultado VARCHAR(25) NOT NULL,
    motivo VARCHAR(200),
    ocurrido_en TIMESTAMPTZ NOT NULL,
    dia_operativo DATE NOT NULL,
    CONSTRAINT pk_evento_acceso PRIMARY KEY (id),
    CONSTRAINT fk_evento_sede FOREIGN KEY (sede_id) REFERENCES sede (id),
    CONSTRAINT fk_evento_tipo_vehiculo FOREIGN KEY (tipo_vehiculo_id) REFERENCES tipo_vehiculo (id),
    CONSTRAINT fk_evento_usuario FOREIGN KEY (validado_por_id) REFERENCES usuario (id),
    CONSTRAINT ck_evento_carril CHECK (carril IN ('ENTRADA', 'SALIDA')),
    CONSTRAINT ck_evento_confianza CHECK (confianza BETWEEN 0 AND 1),
    CONSTRAINT ck_evento_tipo_lectura CHECK (tipo_lectura IN ('AUTOMATICA', 'MANUAL')),
    CONSTRAINT ck_evento_resultado CHECK (resultado IN ('PERMITIDO', 'RECHAZADO', 'PENDIENTE_VALIDACION'))
);

CREATE INDEX ix_evento_placa_fecha ON evento_acceso (placa, ocurrido_en);

-- 4. Estancia y cobro
CREATE TABLE estancia (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    sede_id UUID NOT NULL,
    placa VARCHAR(6) NOT NULL,
    tipo_vehiculo_id UUID NOT NULL,
    ingreso_evento_id UUID NOT NULL,
    salida_evento_id UUID,
    ingreso_en TIMESTAMPTZ NOT NULL,
    salida_en TIMESTAMPTZ,
    CONSTRAINT pk_estancia PRIMARY KEY (id),
    CONSTRAINT fk_estancia_sede FOREIGN KEY (sede_id) REFERENCES sede (id),
    CONSTRAINT fk_estancia_tipo_vehiculo FOREIGN KEY (tipo_vehiculo_id) REFERENCES tipo_vehiculo (id),
    CONSTRAINT fk_estancia_evento_ingreso FOREIGN KEY (ingreso_evento_id) REFERENCES evento_acceso (id),
    CONSTRAINT fk_estancia_evento_salida FOREIGN KEY (salida_evento_id) REFERENCES evento_acceso (id)
);

CREATE UNIQUE INDEX uq_estancia_abierta
    ON estancia (sede_id, placa)
    WHERE salida_en IS NULL;

CREATE TABLE cobro (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    sede_id UUID NOT NULL,
    placa VARCHAR(6) NOT NULL,
    tipo_vehiculo_id UUID NOT NULL,
    dia_operativo DATE NOT NULL,
    franja_id UUID NOT NULL,
    tarifa_id UUID NOT NULL,
    valor NUMERIC(12,2) NOT NULL,
    estado VARCHAR(12) NOT NULL,
    ingreso_evento_id UUID NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_cobro PRIMARY KEY (id),
    CONSTRAINT fk_cobro_sede FOREIGN KEY (sede_id) REFERENCES sede (id),
    CONSTRAINT fk_cobro_tipo_vehiculo FOREIGN KEY (tipo_vehiculo_id) REFERENCES tipo_vehiculo (id),
    CONSTRAINT fk_cobro_franja FOREIGN KEY (franja_id) REFERENCES franja (id),
    CONSTRAINT fk_cobro_tarifa FOREIGN KEY (tarifa_id) REFERENCES tarifa (id),
    CONSTRAINT fk_cobro_evento FOREIGN KEY (ingreso_evento_id) REFERENCES evento_acceso (id),
    CONSTRAINT ck_cobro_valor_no_negativo CHECK (valor >= 0),
    CONSTRAINT ck_cobro_estado CHECK (estado IN ('PENDIENTE', 'PAGADO', 'VENCIDO', 'ANULADO')),
    CONSTRAINT uq_cobro_placa_sede_dia UNIQUE (placa, sede_id, dia_operativo)
);

-- 5. Pago
CREATE TABLE pago (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    cobro_id UUID NOT NULL,
    valor NUMERIC(12,2) NOT NULL,
    valor_recibido NUMERIC(12,2) NOT NULL,
    cambio NUMERIC(12,2) NOT NULL,
    medio VARCHAR(20) NOT NULL,
    recibido_por_id UUID NOT NULL,
    pagado_en TIMESTAMPTZ NOT NULL,
    pase_vigente_hasta TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_pago PRIMARY KEY (id),
    CONSTRAINT fk_pago_cobro FOREIGN KEY (cobro_id) REFERENCES cobro (id),
    CONSTRAINT uq_pago_cobro UNIQUE (cobro_id),
    CONSTRAINT fk_pago_usuario FOREIGN KEY (recibido_por_id) REFERENCES usuario (id),
    CONSTRAINT ck_pago_recibido_suficiente CHECK (valor_recibido >= valor),
    CONSTRAINT ck_pago_cambio_no_negativo CHECK (cambio >= 0),
    CONSTRAINT ck_pago_medio CHECK (medio IN ('EFECTIVO'))
);