<!-- Diseño aprobado por el grupo. Las migraciones V1 y V2 del backend lo implementan. Una migración aplicada no se edita: los cambios van en V3, V4... -->

# Estructura de la base de datos — MVP Parqueadero

Nov 15, 2026 · @Juan Camilo Orozco Londoño

## Resumen

La base de datos tiene 11 tablas en PostgreSQL: 6 de configuración, que cambia el administrador, y 5 de operación, que se llenan con el uso del parqueadero. Sale directamente del [Product Backlog](https://claude.ai/artifact/Fd5cy3YPhZsHGKmTCzW3uu).

**Quién usa este documento:**

- **Valentina y Juan Manuel**, para escribir las migraciones del esqueleto del backend (TT-02).
- **Miguel**, como fuente para dibujar a mano el diagrama entidad-relación (DOC-04). Este documento no reemplaza el diagrama.
- **Valentina**, para el script SQL de la entrega (DOC-09).

**Criterios de diseño:**

| Criterio | Decisión | Por qué |
| --- | --- | --- |
| Identificadores | `UUID` en todas las tablas, salvo parametro | No son adivinables y no dependen del orden de inserción |
| Dinero | `NUMERIC(12,2)` | Evita errores de redondeo; nunca `FLOAT` ni `INT` |
| Fechas y horas | `TIMESTAMPTZ`, y `DATE` para el día operativo | La zona horaria queda guardada; el día operativo se consulta fácil |
| Estados y roles | `VARCHAR` con `CHECK` | Valores cerrados que se leen claro en las consultas |
| Restricciones | Todas con nombre (`pk_`, `fk_`, `uq_`, `ck_`) | Los errores dicen qué regla falló |
| Nombres | Tablas y columnas en español, en `snake_case` y singular | Coinciden con el lenguaje del dominio y del backlog |
| Tarifas | Nunca se actualizan: se cierra la vigente y se inserta una nueva | Cada cobro conserva la tarifa con que se calculó (RN-07) |

## Tablas de configuración

Estas seis tablas guardan lo que el administrador configura (HU-08) y lo que se precarga al instalar.

### sede

| Columna | Tipo | Restricciones | Para qué |
| --- | --- | --- | --- |
| id | UUID | `pk_sede` | Identificador |
| nombre | VARCHAR(80) | NOT NULL, `uq_sede_nombre` | “Principal” |
| activa | BOOLEAN | NOT NULL, por defecto `true` | Permite crecer a varias sedes |

### tipo\_vehiculo

| Columna | Tipo | Restricciones | Para qué |
| --- | --- | --- | --- |
| id | UUID | `pk_tipo_vehiculo` | Identificador |
| codigo | VARCHAR(20) | NOT NULL, `uq_tipo_vehiculo_codigo` | `CARRO`, `MOTO` |
| nombre | VARCHAR(40) | NOT NULL | Texto para la interfaz |
| patron\_placa | VARCHAR(60) | NOT NULL | Expresión regular del formato de placa (HU-01, CA-4 y CA-5) |

Agregar un tipo nuevo es insertar una fila, sin tocar el motor de cobro: es el ejemplo del principio abierto/cerrado.

### franja

| Columna | Tipo | Restricciones | Para qué |
| --- | --- | --- | --- |
| id | UUID | `pk_franja` | Identificador |
| codigo | VARCHAR(20) | NOT NULL, `uq_franja_codigo` | `DIURNA`, `NOCTURNA` |
| hora\_inicio | TIME | NOT NULL | 06:00 o 18:00 |
| hora\_fin | TIME | NOT NULL | 17:59 o 05:59; la nocturna cruza la medianoche |

### tarifa

| Columna | Tipo | Restricciones | Para qué |
| --- | --- | --- | --- |
| id | UUID | `pk_tarifa` | Identificador |
| sede\_id | UUID | NOT NULL, `fk_tarifa_sede` | Sede a la que aplica |
| tipo\_vehiculo\_id | UUID | NOT NULL, `fk_tarifa_tipo_vehiculo` | Carro o moto |
| franja\_id | UUID | NOT NULL, `fk_tarifa_franja` | Diurna o nocturna |
| valor | NUMERIC(12,2) | NOT NULL, `ck_tarifa_valor_no_negativo` (valor >= 0) | HU-08, CA-3 |
| vigente\_desde | TIMESTAMPTZ | NOT NULL, por defecto `now()` | Desde cuándo aplica |
| vigente\_hasta | TIMESTAMPTZ | NULL mientras esté vigente | Se llena al reemplazarla |

Índice único parcial `uq_tarifa_vigente` sobre (sede\_id, tipo\_vehiculo\_id, franja\_id) donde `vigente_hasta IS NULL`: solo una tarifa vigente por combinación. La regla RN-02 (nocturna menor que diurna) compara dos filas, así que se valida en el servicio, no con un `CHECK`.

### cupo

| Columna | Tipo | Restricciones | Para qué |
| --- | --- | --- | --- |
| id | UUID | `pk_cupo` | Identificador |
| sede\_id | UUID | NOT NULL, `fk_cupo_sede` | Sede |
| tipo\_vehiculo\_id | UUID | NOT NULL, `fk_cupo_tipo_vehiculo` | Carro o moto |
| capacidad | INTEGER | NOT NULL, `ck_cupo_capacidad_no_negativa` | 40 carros, 20 motos |

Restricción `uq_cupo_sede_tipo` sobre (sede\_id, tipo\_vehiculo\_id). Los cupos ocupados no se guardan: se cuentan las estancias abiertas, así nunca quedan desfasados.

### parametro

| Columna | Tipo | Restricciones | Para qué |
| --- | --- | --- | --- |
| clave | VARCHAR(60) | `pk_parametro` | Por ejemplo `umbral_confianza` |
| valor | VARCHAR(120) | NOT NULL | `0.80` |
| descripcion | VARCHAR(200) | NULL | Explicación para el administrador |

Es la única tabla con clave natural en vez de UUID, porque cada parámetro ya tiene un nombre único.

## Tablas de operación

Estas cinco tablas registran quién usa el sistema y lo que pasa en los carriles y en la caja.

### usuario

| Columna | Tipo | Restricciones | Para qué |
| --- | --- | --- | --- |
| id | UUID | `pk_usuario` | Identificador |
| nombre\_usuario | VARCHAR(40) | NOT NULL, `uq_usuario_nombre` | `operador1`, `admin` |
| contrasena\_hash | VARCHAR(100) | NOT NULL | Hash BCrypt; nunca la contraseña en texto plano (HU-05, CA-6) |
| rol | VARCHAR(20) | NOT NULL, `ck_usuario_rol` (`OPERADOR`, `ADMINISTRADOR`) | HU-05, CA-3 |
| activo | BOOLEAN | NOT NULL, por defecto `true` | Desactivar sin borrar |

### evento\_acceso

Cada lectura en un carril, se permita o no. Es la base de las consultas (HU-09).

| Columna | Tipo | Restricciones | Para qué |
| --- | --- | --- | --- |
| id | UUID | `pk_evento_acceso` | Identificador |
| sede\_id | UUID | NOT NULL, `fk_evento_sede` | Sede |
| carril | VARCHAR(10) | NOT NULL, `ck_evento_carril` (`ENTRADA`, `SALIDA`) | Dónde ocurrió |
| placa | VARCHAR(6) | NOT NULL | Placa normalizada (HU-01, CA-3) |
| placa\_leida | VARCHAR(20) | NOT NULL | Lo que envió el lector, antes de corregir (HU-06) |
| tipo\_vehiculo\_id | UUID | NOT NULL, `fk_evento_tipo_vehiculo` | Carro o moto |
| confianza | NUMERIC(3,2) | NOT NULL, `ck_evento_confianza` (entre 0 y 1) | HU-01, CA-1 |
| tipo\_lectura | VARCHAR(12) | NOT NULL, `ck_evento_tipo_lectura` (`AUTOMATICA`, `MANUAL`) | HU-01, CA-7 y HU-06, CA-6 |
| validado\_por\_id | UUID | NULL, `fk_evento_usuario` | Operador que validó la lectura manual |
| resultado | VARCHAR(25) | NOT NULL, `ck_evento_resultado` (`PERMITIDO`, `RECHAZADO`, `PENDIENTE_VALIDACION`) | Si la barrera abrió |
| motivo | VARCHAR(200) | NULL | “Cobro pendiente”, “LLENO”, “Sin ingreso registrado” |
| ocurrido\_en | TIMESTAMPTZ | NOT NULL | Fecha y hora |
| dia\_operativo | DATE | NOT NULL | De 06:00 a 05:59 (S-02) |

Índice `ix_evento_placa_fecha` sobre (placa, ocurrido\_en) para los filtros de consultas.

### estancia

Un vehículo que está adentro. Se abre con el ingreso y se cierra con la salida.

| Columna | Tipo | Restricciones | Para qué |
| --- | --- | --- | --- |
| id | UUID | `pk_estancia` | Identificador |
| sede\_id | UUID | NOT NULL, `fk_estancia_sede` | Sede |
| placa | VARCHAR(6) | NOT NULL | Vehículo |
| tipo\_vehiculo\_id | UUID | NOT NULL, `fk_estancia_tipo_vehiculo` | Para contar cupos por tipo |
| ingreso\_evento\_id | UUID | NOT NULL, `fk_estancia_evento_ingreso` | Evento que la abrió |
| salida\_evento\_id | UUID | NULL, `fk_estancia_evento_salida` | Evento que la cerró |
| ingreso\_en | TIMESTAMPTZ | NOT NULL | Hora de entrada |
| salida\_en | TIMESTAMPTZ | NULL mientras esté adentro | Hora de salida (HU-04, CA-7) |

Índice único parcial `uq_estancia_abierta` sobre (sede\_id, placa) donde `salida_en IS NULL`: un vehículo no puede estar dos veces adentro. Los cupos ocupados son el conteo de estancias abiertas por tipo (HU-07).

### cobro

| Columna | Tipo | Restricciones | Para qué |
| --- | --- | --- | --- |
| id | UUID | `pk_cobro` | Identificador |
| sede\_id | UUID | NOT NULL, `fk_cobro_sede` | Sede |
| placa | VARCHAR(6) | NOT NULL | Vehículo |
| tipo\_vehiculo\_id | UUID | NOT NULL, `fk_cobro_tipo_vehiculo` | Carro o moto |
| dia\_operativo | DATE | NOT NULL | Día al que pertenece |
| franja\_id | UUID | NOT NULL, `fk_cobro_franja` | Franja del primer ingreso (SG-02) |
| tarifa\_id | UUID | NOT NULL, `fk_cobro_tarifa` | Tarifa usada (RN-07) |
| valor | NUMERIC(12,2) | NOT NULL, `ck_cobro_valor_no_negativo` | Valor copiado de la tarifa |
| estado | VARCHAR(12) | NOT NULL, `ck_cobro_estado` (`PENDIENTE`, `PAGADO`, `VENCIDO`, `ANULADO`) | Patrón State |
| ingreso\_evento\_id | UUID | NOT NULL, `fk_cobro_evento` | Ingreso que lo generó |
| creado\_en | TIMESTAMPTZ | NOT NULL | Fecha de creación |

Restricción `uq_cobro_placa_sede_dia` sobre (placa, sede\_id, dia\_operativo): es la unicidad que exige F-03. Con tarifa $0, como las motos, no se crea fila.

### pago

| Columna | Tipo | Restricciones | Para qué |
| --- | --- | --- | --- |
| id | UUID | `pk_pago` | Identificador |
| cobro\_id | UUID | NOT NULL, `fk_pago_cobro`, `uq_pago_cobro` | Un solo pago por cobro: la base de la idempotencia (HU-03, CA-6) |
| valor | NUMERIC(12,2) | NOT NULL | Valor del cobro |
| valor\_recibido | NUMERIC(12,2) | NOT NULL, `ck_pago_recibido_suficiente` (valor\_recibido >= valor) | HU-03, CA-2 |
| cambio | NUMERIC(12,2) | NOT NULL, `ck_pago_cambio_no_negativo` | Lo que se devuelve |
| medio | VARCHAR(20) | NOT NULL, `ck_pago_medio` (`EFECTIVO`) | Se amplía si se agrega una pasarela |
| recibido\_por\_id | UUID | NOT NULL, `fk_pago_usuario` | Operador (HU-03, CA-3) |
| pagado\_en | TIMESTAMPTZ | NOT NULL | Fecha y hora |
| pase\_vigente\_hasta | TIMESTAMPTZ | NOT NULL | Fin del día operativo, 05:59 (RN-03) |

## Relaciones y reglas

Hay 19 relaciones de clave foránea; con esta tabla Miguel tiene lo necesario para dibujar el diagrama entidad-relación.

### Relaciones

| Tabla padre | Tabla hija | Cardinalidad | Columna |
| --- | --- | --- | --- |
| sede | tarifa, cupo, evento\_acceso, estancia, cobro | 1 a muchos | `sede_id` |
| tipo\_vehiculo | tarifa, cupo, evento\_acceso, estancia, cobro | 1 a muchos | `tipo_vehiculo_id` |
| franja | tarifa, cobro | 1 a muchos | `franja_id` |
| tarifa | cobro | 1 a muchos | `tarifa_id` |
| usuario | evento\_acceso | 1 a muchos (opcional) | `validado_por_id` |
| usuario | pago | 1 a muchos | `recibido_por_id` |
| evento\_acceso | estancia | 1 a 1 (ingreso) | `ingreso_evento_id` |
| evento\_acceso | estancia | 1 a 0..1 (salida) | `salida_evento_id` |
| evento\_acceso | cobro | 1 a 0..1 | `ingreso_evento_id` |
| cobro | pago | 1 a 0..1 | `cobro_id` |

`parametro` no se relaciona con ninguna tabla.

### Dónde se cumple cada regla

| Regla | Cómo la garantiza la base de datos |
| --- | --- |
| F-03: un cobro por placa, sede y día operativo | `uq_cobro_placa_sede_dia` |
| RN-07: el cobro guarda su tarifa | `cobro.tarifa_id` y `cobro.valor`; las tarifas nunca se sobrescriben |
| RN-03: pase vigente hasta el fin del día | `pago.pase_vigente_hasta` |
| HU-03, CA-6: pago idempotente | `uq_pago_cobro` |
| HU-03, CA-2: valor recibido suficiente | `ck_pago_recibido_suficiente` |
| HU-01, CA-1: confianza entre 0 y 1 | `ck_evento_confianza` |
| HU-07: cupos por tipo | Conteo de estancias con `salida_en IS NULL` y `uq_estancia_abierta` |
| HU-08, CA-3: tarifas no negativas | `ck_tarifa_valor_no_negativo` |
| RN-02: nocturna menor que diurna | No la cubre la base de datos: la valida el servicio de tarifas |

## Migraciones y datos iniciales

La base de datos se crea con dos migraciones de Flyway, en `backend/src/main/resources/db/migration/`, y los usuarios se crean al arrancar la aplicación para no guardar contraseñas en el repositorio.

### V1\_\_esquema\_inicial.sql

Crea las tablas en este orden, para que cada clave foránea apunte a una tabla que ya existe:

1. sede, tipo\_vehiculo, franja, usuario, parametro
2. tarifa, cupo
3. evento\_acceso
4. estancia, cobro
5. pago

Los `id` usan `DEFAULT gen_random_uuid()`, que viene incluido en PostgreSQL desde la versión 13.

### V2\_\_datos\_iniciales.sql

| Tabla | Datos |
| --- | --- |
| sede | Principal |
| tipo\_vehiculo | `CARRO` con patrón `^[A-Z]{3}[0-9]{3}$`; `MOTO` con patrón `^[A-Z]{3}[0-9]{2}[A-Z]$` |
| franja | `DIURNA` de 06:00 a 17:59; `NOCTURNA` de 18:00 a 05:59 |
| tarifa | Carro: diurna $5.000 y nocturna $3.000. Moto: $0 y $0 |
| cupo | 40 carros y 20 motos |
| parametro | `umbral_confianza` = 0.80 |

### Usuarios iniciales

No van en la migración. Al arrancar, el backend crea `admin` y `operador1` si no existen, con contraseñas leídas de variables de entorno y guardadas como hash BCrypt. Hay que agregar al `.env.example`:

- `ADMIN_USUARIO` y `ADMIN_CONTRASENA`
- `OPERADOR_USUARIO` y `OPERADOR_CONTRASENA`

### Reglas para el equipo

- **Una migración aplicada no se edita.** Cualquier cambio va en una nueva: `V3__...`, `V4__...`. Si se edita una ya aplicada, Flyway falla al arrancar en los equipos de los demás.
- **El script SQL de la entrega** (DOC-09) se arma al final uniendo V1, V2 y las siguientes.
