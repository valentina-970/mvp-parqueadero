# Product Backlog — MVP Parqueadero

9 historias de usuario que cubren las funcionalidades obligatorias F-01 a F-10: 5 en el Sprint 1 y 4 en el Sprint 2. Los puntos salen del Planning Poker del 16 de noviembre de 2026 (detalle y capturas en `docs/scrum/planning-poker.md`). HU-05 fue el pivote con 3 puntos.

Los casos de prueba (CP) salen de los criterios de aceptación (CA) y son la especificación de las pruebas unitarias: cada uno debe tener su prueba automatizada.

## Resumen

Total: 36 puntos (Sprint 1: 24; Sprint 2: 12). El orden es el orden de prioridad. El Sprint 1 construye el flujo entrada → cobro → pago → salida; el Sprint 2 lo completa.

| ID | Historia | Cubre | Prioridad | Sprint | Responsable | Puntos | Rama |
|---|---|---|---|---|---|---|---|
| HU-01 | Registrar el ingreso y clasificar el vehículo | F-01, F-02 | Alta | 1 | Juan | 5 | `feature/HU-01-ingreso-placa` |
| HU-02 | Generar el cobro del día operativo | F-03 | Alta | 1 | Miguel | 8 | `feature/HU-02-generacion-cobro` |
| HU-03 | Registrar el pago en efectivo | F-04 | Alta | 1 | Valentina | 5 | `feature/HU-03-registro-pago` |
| HU-04 | Autorizar o negar la salida | F-05 | Alta | 1 | Juan Manuel | 3 | `feature/HU-04-salida` |
| HU-05 | Iniciar sesión según el rol | F-10 | Alta | 1 | Juan Manuel | 3 | `feature/HU-05-acceso-roles` |
| HU-06 | Validar manualmente una lectura dudosa | F-06 | Media | 2 | Miguel | 5 | `feature/HU-06-lectura-manual` |
| HU-07 | Controlar los cupos por tipo de vehículo | F-07 | Media | 2 | Juan Manuel | 2 | `feature/HU-07-cupos` |
| HU-08 | Administrar tarifas, franjas y cupos | F-08 | Media | 2 | Valentina | 3 | `feature/HU-08-admin-tarifas` |
| HU-09 | Consultar eventos y cobros | F-09 | Media | 2 | Juan | 2 | `feature/HU-09-consultas` |

Las referencias SG-xx (supuestos del grupo) están en `02-decisiones-del-grupo.md`.

---

# Sprint 1 — Flujo principal

**Objetivo:** un carro y una moto pueden entrar, generar su cobro, pagar y salir de punta a punta, con acceso por roles.

## HU-01 · Registrar el ingreso y clasificar el vehículo

**Como** operador del carril de entrada, **quiero** que el sistema registre el ingreso de un vehículo a partir de la lectura de su placa, **para** saber quién entra, cuándo y en qué sede.

**Criterios de aceptación**

- CA-1: El carril recibe la placa desde el simulador o la carga de una imagen, junto con su nivel de confianza entre 0 y 1.
- CA-2: El ingreso se registra con placa, fecha, hora, sede y nivel de confianza.
- CA-3: Antes de validarse, la placa se normaliza: mayúsculas, sin espacios ni guiones.
- CA-4: Una placa con formato AAA123 se clasifica como carro.
- CA-5: Una placa con formato AAA12A se clasifica como moto.
- CA-6: Una placa que no cumple ninguno de los dos formatos se rechaza, no se registra el ingreso y se le pide al operador digitarla (SG-06).
- CA-7: Si la confianza es igual o mayor que el umbral, el ingreso queda marcado como "lectura automática".
- CA-8: La interfaz muestra la placa, el tipo de vehículo y la hora del ingreso registrado.

**Casos de prueba**

- CP-01-01 (éxito): placa ABC123 con confianza 0,95 → ingreso registrado como carro y lectura automática. [CA 1, 2, 4, 7]
- CP-01-02 (éxito): placa XYZ12D con confianza 0,90 → ingreso registrado como moto. [CA 5]
- CP-01-03 (éxito): placa " abc-123 " → se normaliza a ABC123 y se registra como carro. [CA 3]
- CP-01-04 (error): placa AB1234 → se rechaza con el mensaje "Formato de placa no válido"; no se registra el ingreso. [CA 6]
- CP-01-05 (error): placa vacía → se rechaza; no se registra el ingreso. [CA 6]
- CP-01-06 (error): confianza 1,5 → se rechaza por confianza fuera de rango. [CA 1]

## HU-02 · Generar el cobro del día operativo

**Como** administrador del parqueadero, **quiero** que el sistema genere el cobro del día cuando un vehículo entra, **para** cobrar según el tipo de vehículo y la franja sin cálculos manuales.

**Criterios de aceptación**

- CA-1: El primer ingreso de un vehículo en un día operativo crea un cobro en estado Pendiente (SG-02).
- CA-2: El día operativo va de 06:00 a 05:59 del día siguiente (S-02). Un ingreso a las 02:00 del 12 de noviembre pertenece al día operativo del 11.
- CA-3: La tarifa se elige según el tipo de vehículo y la franja de la hora del primer ingreso (SG-02, SG-03).
- CA-4: Si la tarifa del vehículo es $0, como ocurre con las motos (RN-01), no se crea cobro.
- CA-5: Si el vehículo vuelve a entrar el mismo día operativo, no se crea un segundo cobro (SG-01).
- CA-6: La base de datos impide dos cobros con la misma placa, sede y día operativo mediante una restricción de unicidad.
- CA-7: Cada cobro guarda su valor y la tarifa con la que se calculó. Un cambio posterior de tarifa no modifica los cobros existentes (RN-07).

**Casos de prueba**

- CP-02-01 (éxito): carro entra a las 10:00 → cobro Pendiente con la tarifa diurna de carro. [CA 1, 3]
- CP-02-02 (éxito): carro entra a las 20:00 → cobro Pendiente con la tarifa nocturna de carro. [CA 3]
- CP-02-03 (éxito): carro entra a las 05:59 y otro a las 06:00 del 12 de noviembre → el primero pertenece al día operativo del 11 y el segundo al del 12. [CA 2]
- CP-02-04 (éxito): moto entra con tarifa $0 → no se crea cobro. [CA 4]
- CP-02-05 (éxito): carro con cobro del día sale y vuelve a entrar → no se crea un segundo cobro. [CA 5]
- CP-02-06 (error): se intenta guardar un segundo cobro con la misma placa, sede y día → la base de datos rechaza el registro. [CA 6]
- CP-02-07 (éxito): se cambia la tarifa diurna después de crear un cobro → el cobro conserva su valor y su tarifa originales. [CA 7]

## HU-03 · Registrar el pago en efectivo

**Como** operador de caja, **quiero** registrar el pago en efectivo del cobro de un vehículo, **para** que pueda salir y volver a entrar durante el día sin pagar de nuevo.

**Criterios de aceptación**

- CA-1: El operador busca por placa el cobro Pendiente del día operativo.
- CA-2: El operador ingresa el valor recibido. Si es menor que el cobro, el pago se rechaza; si es mayor, se muestra el cambio.
- CA-3: El pago guarda valor, fecha, hora y operador que lo recibió.
- CA-4: Al registrar el pago, el cobro pasa de Pendiente a Pagado.
- CA-5: El pago deja un pase vigente hasta el fin del día operativo, a las 05:59 (RN-03).
- CA-6: Registrar otra vez el pago de un cobro ya Pagado no crea un segundo pago; el sistema devuelve el pago existente.
- CA-7: Un cobro Anulado no se puede pagar y el sistema muestra el motivo.
- CA-8: Si la placa no tiene cobro Pendiente, el sistema lo informa.

**Casos de prueba**

- CP-03-01 (éxito): cobro Pendiente de $5.000; se reciben $10.000 → cobro Pagado, cambio de $5.000 y pase vigente hasta las 05:59. [CA 2, 3, 4, 5]
- CP-03-02 (éxito): se repite el registro del mismo pago → se devuelve el pago existente; no hay duplicado. [CA 6]
- CP-03-03 (éxito): carro pagado sale y vuelve a entrar → no se crea un nuevo cobro. [CA 5]
- CP-03-04 (error): cobro de $5.000; se reciben $3.000 → pago rechazado por valor insuficiente. [CA 2]
- CP-03-05 (error): cobro en estado Anulado → pago rechazado con el motivo. [CA 7]
- CP-03-06 (error): placa sin cobro Pendiente → mensaje "No hay cobro pendiente para esta placa". [CA 8]

## HU-04 · Autorizar o negar la salida

**Como** operador del carril de salida, **quiero** que la barrera abra solo cuando el vehículo no tenga deuda del día, **para** evitar que salgan vehículos sin pagar.

**Criterios de aceptación**

- CA-1: El carril de salida recibe la placa desde el simulador o la carga de una imagen, junto con su nivel de confianza.
- CA-2: Si el cobro del día está Pagado, la barrera abre (RN-06).
- CA-3: Si no existe cobro del día, por ejemplo en una moto, la barrera abre (RN-06).
- CA-4: Si el cobro del día está Pendiente, la barrera no abre.
- CA-5: Si la placa no tiene un ingreso registrado, la barrera no abre y se avisa al operador.
- CA-6: La interfaz muestra claramente ABRE o NO ABRE y el motivo, por ejemplo "Cobro pendiente por $5.000".
- CA-7: Cuando la barrera abre, el vehículo queda marcado como fuera y libera su cupo.
- CA-8: Cada intento de salida queda registrado con placa, fecha, hora, sede, resultado y motivo.

**Casos de prueba**

- CP-04-01 (éxito): moto adentro, sin cobro → ABRE; la moto queda fuera. [CA 3, 6, 7]
- CP-04-02 (éxito): carro con cobro Pagado → ABRE. [CA 2]
- CP-04-03 (éxito): cualquier salida → el intento queda en el registro de eventos con su resultado y motivo. [CA 8]
- CP-04-04 (error): carro con cobro Pendiente → NO ABRE con el motivo "Cobro pendiente" y el valor. [CA 4, 6]
- CP-04-05 (error): placa sin ingreso registrado → NO ABRE con el motivo "Sin ingreso registrado". [CA 5]

## HU-05 · Iniciar sesión según el rol

**Como** usuario del sistema, **quiero** iniciar sesión con mi usuario y contraseña, **para** acceder solo a las funciones de mi rol.

**Criterios de aceptación**

- CA-1: El usuario inicia sesión con usuario y contraseña.
- CA-2: Con credenciales incorrectas se muestra "Usuario o contraseña incorrectos", sin decir cuál de los dos falló.
- CA-3: Existen dos roles: operador y administrador.
- CA-4: El operador accede a los carriles, la caja y las consultas, pero no a la administración de tarifas.
- CA-5: El administrador accede a todas las funciones.
- CA-6: Las contraseñas se guardan cifradas con un hash, nunca en texto plano.
- CA-7: Una petición a la API sin sesión válida responde 401, y una con rol insuficiente responde 403.
- CA-8: El usuario puede cerrar sesión.

**Casos de prueba**

- CP-05-01 (éxito): administrador con credenciales correctas → entra y ve la opción de administración. [CA 1, 5]
- CP-05-02 (éxito): operador con credenciales correctas → entra y no ve la opción de administración. [CA 1, 4]
- CP-05-03 (éxito): revisión de la tabla de usuarios → las contraseñas aparecen como hash. [CA 6]
- CP-05-04 (error): contraseña incorrecta → mensaje genérico de credenciales inválidas. [CA 2]
- CP-05-05 (error): operador llama al endpoint de tarifas → respuesta 403. [CA 4, 7]
- CP-05-06 (error): petición sin sesión → respuesta 401. [CA 7]

---

# Sprint 2 — Complementos

**Objetivo:** completar las funcionalidades obligatorias (lectura manual, cupos, administración y consultas) y dejar listos la suite de pruebas y el release v1.0.0.

## HU-06 · Validar manualmente una lectura dudosa

**Como** operador del carril, **quiero** validar o digitar la placa cuando la lectura tiene baja confianza, **para** no registrar movimientos con una placa equivocada.

**Criterios de aceptación**

- CA-1: Si la confianza es menor que el umbral (SG-04), el evento no se procesa automáticamente y pasa a validación manual.
- CA-2: Una confianza igual al umbral se procesa de forma automática.
- CA-3: La interfaz muestra al operador la placa leída y su confianza, y la imagen si existe.
- CA-4: El operador puede confirmar la placa leída o corregirla digitándola.
- CA-5: La placa digitada pasa por las mismas validaciones de formato que una lectura automática.
- CA-6: El evento queda marcado como "lectura manual" junto con el usuario del operador que la validó.
- CA-7: La validación manual aplica tanto en el carril de entrada como en el de salida.
- CA-8: El umbral usado es el que configuró el administrador (HU-08).

**Casos de prueba**

- CP-06-01 (éxito): confianza 0,60 con umbral 0,80; el operador confirma → ingreso registrado como lectura manual con el usuario del operador. [CA 1, 4, 6]
- CP-06-02 (éxito): lectura ABC128 que el operador corrige a ABC123 → ingreso registrado con la placa corregida. [CA 4]
- CP-06-03 (éxito): confianza 0,80 con umbral 0,80 → se procesa automáticamente. [CA 2]
- CP-06-04 (éxito): umbral cambiado a 0,90; lectura con 0,85 → pasa a validación manual. [CA 8]
- CP-06-05 (éxito): salida con confianza 0,50 → se pide validación antes de decidir la barrera. [CA 7]
- CP-06-06 (error): el operador digita AB12 → se rechaza por formato no válido. [CA 5]

## HU-07 · Controlar los cupos por tipo de vehículo

**Como** operador del carril de entrada, **quiero** que el sistema verifique el cupo antes de abrir la barrera, **para** no dejar entrar vehículos cuando no hay espacio.

**Criterios de aceptación**

- CA-1: Los cupos se definen por tipo de vehículo y arrancan con los valores precargados.
- CA-2: Antes de abrir la barrera de entrada, el sistema verifica que haya cupo para ese tipo de vehículo.
- CA-3: Si no hay cupo, la interfaz muestra "LLENO", la barrera no abre y no se registran ingreso ni cobro.
- CA-4: Que un tipo esté lleno no afecta al otro.
- CA-5: Cada ingreso ocupa un cupo y cada salida con barrera abierta lo libera.
- CA-6: La interfaz muestra los cupos disponibles por tipo de vehículo.
- CA-7: Cada rechazo por cupo queda en el registro de eventos.

**Casos de prueba**

- CP-07-01 (éxito): queda 1 cupo de carro; entra un carro → ingresa y los cupos disponibles de carro quedan en 0. [CA 2, 5, 6]
- CP-07-02 (éxito): carros llenos; entra una moto con cupo → la moto ingresa. [CA 4]
- CP-07-03 (éxito): carros llenos; sale un carro → se libera un cupo y el siguiente carro puede entrar. [CA 5]
- CP-07-04 (error): carros llenos; entra un carro → se muestra LLENO, la barrera no abre y no se crea cobro. [CA 3]
- CP-07-05 (error): rechazo por cupo → el rechazo aparece en el registro de eventos. [CA 7]

**Nota de diseño:** la verificación de cupo y la de confianza de HU-06 son pasos de la misma cadena de validaciones del carril (patrón Chain of Responsibility). Los cupos ocupados no se guardan: se cuentan las estancias abiertas.

## HU-08 · Administrar tarifas, franjas y cupos

**Como** administrador, **quiero** configurar tarifas, horarios de franja, cupos y el umbral de confianza, **para** ajustar la operación sin modificar el sistema.

**Criterios de aceptación**

- CA-1: El administrador consulta y edita la tarifa de cada tipo de vehículo en cada franja.
- CA-2: El sistema rechaza una tarifa nocturna mayor o igual que la diurna del mismo tipo y muestra el motivo (RN-02). Se exceptúan las tarifas en $0 de las motos (RN-01).
- CA-3: Ninguna tarifa puede ser negativa.
- CA-4: El administrador configura el inicio y el fin de cada franja; las franjas deben cubrir las 24 horas sin solaparse.
- CA-5: El administrador configura los cupos por tipo; un cupo no puede quedar por debajo de los vehículos que ya están adentro.
- CA-6: El administrador configura el umbral de confianza entre 0 y 1.
- CA-7: Los cambios de tarifa solo aplican a cobros nuevos (RN-07).

**Casos de prueba**

- CP-08-01 (éxito): carro con diurna $5.000 y nocturna $3.000 → se guarda. [CA 1]
- CP-08-02 (éxito): moto con diurna $0 y nocturna $0 → se guarda. [CA 2]
- CP-08-03 (éxito): se cambia la tarifa diurna con cobros ya creados → los cobros anteriores conservan su valor; los nuevos usan la tarifa nueva. [CA 7]
- CP-08-04 (error): carro con nocturna $5.000 igual a la diurna → se rechaza citando RN-02. [CA 2]
- CP-08-05 (error): carro con nocturna $6.000 mayor que la diurna → se rechaza citando RN-02. [CA 2]
- CP-08-06 (error): tarifa de −$1.000 → se rechaza. [CA 3]
- CP-08-07 (error): franjas que se solapan → se rechaza con el motivo. [CA 4]
- CP-08-08 (error): cupo de carros en 2 con 3 carros adentro → se rechaza con el motivo. [CA 5]

**Nota de diseño:** las tarifas nunca se actualizan: se cierra la vigente (`vigente_hasta`) y se inserta una nueva. La regla RN-02 compara dos filas, así que se valida en el servicio y no con un `CHECK` de la base de datos.

## HU-09 · Consultar eventos y cobros

**Como** operador o administrador, **quiero** consultar los ingresos, salidas y cobros con filtros, **para** revisar lo que ha pasado en el parqueadero.

**Criterios de aceptación**

- CA-1: El listado de eventos muestra placa, tipo de vehículo, fecha, hora, sede, tipo de lectura y resultado.
- CA-2: El listado de cobros muestra placa, día operativo, franja, valor, tarifa aplicada y estado.
- CA-3: Se puede filtrar por placa, por rango de fechas y por estado del cobro.
- CA-4: Los filtros se pueden combinar.
- CA-5: Los resultados van del más reciente al más antiguo, en páginas de 20 registros.
- CA-6: Si no hay resultados, se muestra "No hay registros".
- CA-7: Si la fecha final es anterior a la inicial, se muestra un mensaje y no se consulta.
- CA-8: Ambos roles pueden consultar.

**Casos de prueba**

- CP-09-01 (éxito): filtro por placa ABC123 → solo aparecen los eventos y cobros de ABC123. [CA 1, 2, 3]
- CP-09-02 (éxito): filtro por estado Pendiente → solo aparecen cobros pendientes. [CA 3]
- CP-09-03 (éxito): filtro por placa y rango de fechas → aparece la intersección de ambos filtros. [CA 4]
- CP-09-04 (éxito): más de 20 resultados → se muestran en páginas de 20, del más reciente al más antiguo. [CA 5]
- CP-09-05 (éxito): placa sin registros → mensaje "No hay registros". [CA 6]
- CP-09-06 (error): fecha final anterior a la inicial → mensaje de rango inválido; no se consulta. [CA 7]

---

# Tareas técnicas y de documentación

| ID | Tarea | Responsable | Sprint |
|---|---|---|---|
| TT-01 | Repositorio público con `main` y `develop` protegidas y `.env.example` | Valentina y Juan Manuel | 0 |
| TT-02 | Esqueleto del backend con PostgreSQL, migraciones y datos iniciales | Valentina y Juan Manuel | 0 |
| TT-03 | Esqueleto del frontend: navegación, pantalla de login y conexión a la API | Juan Manuel y Valentina | 0 |
| TT-04 | Docker Compose con base de datos, backend y frontend | Valentina | 0 |
| TT-05 | Tablero en GitHub Projects con campos de sprint, fechas, puntos, prioridad y evidencia | Juan Manuel | 0 |
| TT-06 | Pipeline que ejecuta las pruebas en cada pull request | Juan Manuel | 1 |
| TT-07 | Release v0.1.0 hacia `main` | Juan Manuel | 1 |
| TT-08 | Release v1.0.0 hacia `main`, con commits de los cuatro integrantes | Juan Manuel | Cierre |
| TT-09 | Vista de la sede en el celular, versión simple: carril de entrada (Juan) y carril de salida con barrera (Juan Manuel) | Juan y Juan Manuel | 1 |
| TT-10 | Vista de la sede estilo videojuego (opcional; máximo 6 horas por persona y solo con el sprint al día) | Juan y Juan Manuel | 2 |

Las tareas de documentación (DOC-01 a DOC-16) corresponden a los entregables del documento de análisis, los manuales y el ensamblaje final.

---

# Matriz de trazabilidad

| Funcionalidad | Historia |
|---|---|
| F-01 Ingreso por placa | HU-01 |
| F-02 Clasificación moto/carro | HU-01 |
| F-03 Generación del cobro | HU-02 |
| F-04 Registro de pago | HU-03 |
| F-05 Salida | HU-04 |
| F-06 Lectura manual | HU-06 |
| F-07 Cupos | HU-07 |
| F-08 Administración de tarifas | HU-08 |
| F-09 Consulta de eventos y cobros | HU-09 |
| F-10 Acceso por roles | HU-05 |

| Escenario de la demo | Casos de prueba |
|---|---|
| Una moto entra y sale sin cobro | CP-01-02, CP-02-04, CP-04-01 |
| Un carro entra en franja diurna y queda con cobro pendiente | CP-02-01 |
| El mismo carro intenta salir sin pagar y la barrera no abre | CP-04-04 |
| El operador registra el pago; el carro sale y vuelve a entrar sin cobro nuevo | CP-03-01, CP-04-02, CP-03-03 |
| Una lectura con baja confianza pasa a validación manual | CP-06-01 |
| El parqueadero se llena para un tipo de vehículo y se muestra LLENO | CP-07-04 |
| El administrador intenta guardar una tarifa nocturna mayor que la diurna | CP-08-05 |
| Ejecución de la suite de pruebas automatizadas | Todos los casos |
