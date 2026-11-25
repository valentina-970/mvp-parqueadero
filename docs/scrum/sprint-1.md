# Sprint 1 (16 al 22 de noviembre de 2026)
 
## Objetivo
 
Un carro y una moto pueden entrar, generar su cobro, pagar y salir de punta a punta, con acceso por roles.
 
## Sprint Planning (lunes 16 de noviembre)
 
- Facilitador: Juan Manuel (Scrum Master)
- Product Owner: Juan
- Asistentes: Juan, Juan Manuel, Miguel y Valentina
- Historias que entran: HU-01 a HU-05 (24 puntos), más las tareas TT-06, TT-07 y TT-09
- Estimación: Planning Poker del mismo día (ver `planning-poker.md`), con HU-05 como pivote de 3 puntos
- Capacidad del equipo: 4 integrantes que trabajan de forma individual en las tardes y noches, según el horario de cada uno. Varios tienen clases nocturnas de lunes a jueves, por lo que las reuniones de grupo se hacen en la tarde entre semana o desde el viernes. Los fines de semana dependen de la disponibilidad de cada uno. Meta: 24 puntos de historias en 7 días, más las tareas técnicas.
- Acuerdos:
  - Daily asíncrono en el grupo de WhatsApp, todos los días antes de las 9:00 p. m.: qué hice, qué haré y qué me bloquea. Cualquier contratiempo se avisa ahí.
  - Cada integrante programa su historia completa (backend, frontend, base de datos y pruebas), en su rama `feature/HU-xx-descripcion` creada desde `develop`.
  - Todo cambio entra a `develop` por pull request, aprobado por otro integrante, rotando quién revisa. Los pull requests no llevan descripción obligatoria; el enlace al PR se pega en el campo Evidencia de la tarjeta.
  - Commits con formato Conventional Commits, sin la historia entre paréntesis.
  - Frontend en pareja: Miguel y Valentina hacen su primera pantalla junto a Juan o Juan Manuel.
  - Al cerrar cada historia, su dueño le pasa a Miguel las clases y relaciones nuevas para el diagrama, y a Juan las capturas de sus pantallas para el manual de usuario.
  - Una historia solo se considera terminada cuando cumple la Definición de Terminado y el PO la aprueba en la review.
  - Si hay dos dailies seguidos sin avance de un integrante, el Scrum Master redistribuye tareas.
  - Review, retrospectiva, release v0.1.0 y planning del Sprint 2 el domingo 22 de noviembre.
## Sprint backlog
 
| ID | Historia o tarea | Responsable | Puntos | Estado |
|---|---|---|---|---|
| HU-01 | Registrar el ingreso y clasificar el vehículo | Juan | 5 | Terminado |
| HU-02 | Generar el cobro del día operativo | Miguel | 8 | Terminado |
| HU-03 | Registrar el pago en efectivo | Valentina | 5 | Terminado |
| HU-04 | Autorizar o negar la salida | Juan Manuel | 3 | Terminado |
| HU-05 | Iniciar sesión según el rol | Juan Manuel | 3 | Terminado |
| TT-06 | Pipeline de pruebas en cada pull request | Juan Manuel | — | Terminado |
| TT-07 | Release v0.1.0 | Juan Manuel | — | Terminado |
| TT-09 | Vista de la sede (versión simple) | Juan y Juan Manuel | — | Terminado |
 
Total de historias: 24 puntos.
 
## Dailies
 
### Lunes 16 de noviembre
 
**Juan**
- Hice: Planning Poker y Sprint Planning del Sprint 1.
- Haré: empezar HU-01 (ingreso y clasificación del vehículo).
- Bloqueos: ninguno.
**Juan Manuel**
- Hice: Planning Poker y Sprint Planning del Sprint 1.
- Haré: empezar HU-05 (inicio de sesión por rol).
- Bloqueos: ninguno.
**Miguel**
- Hice: Planning Poker y Sprint Planning del Sprint 1.
- Haré: empezar HU-02 (cobro del día operativo).
- Bloqueos: ninguno.
**Valentina**
- Hice: Planning Poker y Sprint Planning del Sprint 1.
- Haré: empezar HU-03 (pago en efectivo).
- Bloqueos: ninguno.
### Martes 17 de noviembre
 
**Juan**
- Hice: dejé listo el entorno en mi PC (JDK 21, Docker y Node), cloné el repositorio y levanté el proyecto. El backend no arrancaba porque el PostgreSQL instalado en Windows ocupaba el puerto 5432; lo resolví usando el 5433 para el contenedor. Traje `develop` con HU-04 a mi rama y ajusté el diseño de HU-01 para reutilizar las clases de `comun/`.
- Haré: terminar HU-01 (backend, pruebas unitarias y pantalla del carril de entrada) y abrir su pull request.
- Bloqueos: ninguno; el del puerto de la base de datos quedó resuelto.
**Juan Manuel**
- Hice: HU-05 (inicio de sesión por rol) terminada y mergeada en `develop` con el PR #19. HU-04 (autorizar o negar la salida) completa, con pruebas unitarias y probada de punta a punta con Docker; su pull request ya está abierto para revisión. Le pasé a Miguel las clases de HU-04 y HU-05 para el diagrama y preparé las capturas de las pantallas para el manual de usuario.
- Haré: atender la revisión del PR de HU-04 y revisar los PR de los demás. Mañana empiezo TT-06 (pipeline de pruebas) y TT-09 (simulador del carril de salida).
- Bloqueos: ninguno. La demostración completa de HU-04 (pagar y salir) necesita HU-01, HU-02 y HU-03 integradas.
- Pedido al equipo: reutilicen las clases de `comun/` (`Placa`, `DiaOperativo`, `Dinero`, `SedeActual`) y el bean `Clock` de `RelojConfig` en vez de crear otros.
**Miguel**
- Hice: dejé listo el entorno en mi PC (JDK 21, Docker y Node), cloné el repositorio y levanté el proyecto. El backend no arrancaba porque el PostgreSQL instalado en Windows ocupaba el puerto 5432; lo resolví usando el 5433 para el contenedor.
- Haré: hacer el pull del repositorio a mi computador de los pull requests aceptados por el equipo.
- Bloqueos: ninguno. El del puerto de la base de datos quedó resuelto.
**Valentina**
- Hice: dejé configurado todo mi entorno de desarrollo y ayudé a Juan Manuel a revisar las migraciones y los campos de la base de datos.
- Haré: preparar HU-03 (pago en efectivo) mientras Miguel avanza con HU-02, de la que depende.
- Bloqueos: ninguno por ahora. HU-03 necesita el cobro que genera HU-02, así que su desarrollo arranca cuando esa historia esté integrada.

### Miércoles 18 de noviembre
 
**Juan**
- Hice: HU-01 completa: paquete `entrada/` con Adapter (lector de placas), Chain of Responsibility (formato → confianza → vehículo no adentro), Facade y Observer (`IngresoRegistrado`), 15 pruebas unitarias, pantalla `/entrada` con simulador y estado del código actualizado. PR #21 abierto hacia `develop`.
- Haré: atender la revisión del PR #21, revisar los PR que me toquen y preparar la aceptación de las historias para la review.
- Bloqueos: ninguno. La demostración de punta a punta necesita HU-02 y HU-03.
**Juan Manuel**
- Hice: mis PR de HU-05 (#19) y HU-04 (#20) quedaron mergeados en `develop`. Revisé la integración con HU-01 de Juan y probé el flujo real de entrada y salida en Docker: la moto y el carro entran, salen y no pueden salir dos veces. Pasé una revisión de calidad y seguridad a mi código y corregí cuatro puntos: la fijación de sesión en el inicio de sesión, la cookie de sesión (`HttpOnly` y `SameSite=Lax`), el origen CORS configurable y la barrera de salida, que ahora se mueve solo cuando la transacción se confirma; además dos salidas simultáneas de la misma placa ya no pueden abrir las dos. También corregí Nginx para que Swagger se pueda abrir desde el puerto 5173 y puse el contenedor del backend a correr sin root. Quedan 71 pruebas unitarias en verde. Entregué a Juan las capturas de login, menús y salida para el manual de usuario.
- Haré: abrir el PR de esas correcciones y empezar TT-06 (pipeline de pruebas con PostgreSQL) y TT-09 (carril de salida del simulador, reutilizando `SimuladorLectura` de Juan).
- Bloqueos: ninguno. La demostración completa (cobro pendiente, pago y salida) necesita HU-02 y HU-03 integradas.
- Pedido al equipo: la contraseña de la base de datos aparece escrita como valor por defecto en `application.yml` desde el esqueleto. Hay que quitarla y cambiarla, porque el repositorio es público.
**Miguel**
- Hice: estuve pendiente de los avances del equipo y de los cambios acordados sobre el flujo de trabajo y las fechas.
- Haré: hacer el pull en mi computador del pull request de HU-01, hecho por Juan.
- Bloqueos: ninguno.
**Valentina**
- Hice: seguí con mi entorno listo y con el apoyo en las migraciones y los campos de la base de datos. Todavía no tengo código de HU-03, porque depende de HU-02.
- Haré: empezar HU-03 en cuanto HU-02 de Miguel esté integrada en `develop`; mientras tanto, revisar los pull requests que me toquen.
- Bloqueos: HU-03 espera a HU-02 (el cobro que se paga lo genera esa historia).

### Jueves 19 de noviembre
 
**Juan**
- Hice: HU-01 aprobada y fusionada en `develop` (PR #21). Preparé con Miguel la guía de HU-02, incluido el ajuste para que `IngresoRegistrado` lleve el evento de ingreso que exige la tabla `cobro`. Avancé poco por la carga de otras materias.
- Haré: revisar el PR de HU-02 y tomar las capturas de la pantalla de entrada para el manual de usuario.
- Bloqueos: ninguno.
**Juan Manuel**
- Hice: mi pull request de correcciones de seguridad y calidad (`fix/endurecer-seguridad-salida`) quedó en GitHub esperando revisión. No hubo avances nuevos de código ese día.
- Haré: seguir pendiente de la revisión de ese PR y de la entrega de HU-02 para probar el flujo completo.
- Bloqueos: ninguno. Seguimos esperando HU-02 y HU-03.
**Miguel**
- Hice: estuve pendiente de los avances del equipo y de los cambios acordados sobre el flujo de trabajo y las fechas.
- Haré: preparar todo para iniciar el desarrollo de HU-02.
- Bloqueos: ninguno.
**Valentina**
- Hice: estuve pendiente del desarrollo del código.
- Haré: indagar sobre nuevos cambios en el proyecto para desarrollar mi módulo.
- Bloqueos: ninguno.
- Pedido al equipo: le solicité a Miguel el desarrollo de su módulo.

### Viernes 20 de noviembre
 
**Juan**
- Hice: revisé y fusioné el PR #22 (HU-02, generación del cobro del día operativo).
- Haré: revisar los PR pendientes del fin de semana (seguridad de la salida, pipeline, vista de la sede y HU-03).
- Bloqueos: ninguno.
**Juan Manuel**
- Hice: sin avances nuevos de código; seguí pendiente de HU-02 de Miguel.
- Haré: integrar HU-02 en cuanto llegue a `develop`, probar el flujo completo de entrada, cobro y salida, y cerrar TT-06 y TT-09 en el fin de semana.
- Bloqueos: ninguno.
**Miguel**
- Hice: avancé HU-02 (generación del cobro del día operativo) en la rama `feature/HU-02-generacion-cobro`. Agregué `eventoIngresoId` al aviso `IngresoRegistrado`. Implementé el paquete `cobro/`: Strategy (`PoliticaTarifa` y `TarifaPorFranja`), Chain of Responsibility (`PasoCobro` con 3 pasos) y `GeneradorCobro` con `@EventListener` (Observer), más los adaptadores JDBC. Probé de punta a punta: el carro genera cobro PENDIENTE, la moto no genera cobro (tarifa $0) y la salida responde "Cobro pendiente". Dejé el entorno funcionando: configuré JDK 21 y levanté backend, frontend y base de datos. Falta crear las 9 pruebas unitarias, correr toda la suite, actualizar `05-estado-del-codigo.md` y abrir el PR a `develop`.
- Haré: seguir con los diagramas pendientes.
- Bloqueos: ninguno por ahora. Tuve problemas de entorno (versión de Java y conexión a la base), ya resueltos.
**Valentina**
- Hice: estuve investigando sobre parqueaderos para desarrollar mi módulo.
- Haré: traer los cambios del código para iniciar el desarrollo de HU-03.
- Bloqueos: ninguno.

### Sábado 21 de noviembre
 
**Juan**
- Hice: seguimiento del avance del equipo y de los PR abiertos; preparación de la review como PO.
- Haré: revisar y fusionar los PR #23 (seguridad de la salida), #24 (pipeline, TT-06), #25 (vista de la sede, TT-09) y #26 (HU-03).
- Bloqueos: ninguno.
**Juan Manuel**
- Hice: sin avances nuevos de código ese día.
- Haré: el domingo, probar el flujo con HU-02, cerrar TT-06 (pipeline de pruebas) y TT-09 (simulador del carril de salida) y empezar HU-03.
- Bloqueos: ninguno.
**Miguel**
- Hice: seguí con los diagramas pendientes, dibujados a mano, con las clases de HU-01, HU-02, HU-04 y HU-05.
- Haré: terminar los diagramas y atender la review del domingo.
- Bloqueos: ninguno.
**Valentina**
- Hice: traje a mi computador los cambios de `develop` (HU-01, HU-02, HU-04 y HU-05) y revisé las clases compartidas de `comun/` y el paquete `cobro/` de Miguel para preparar HU-03.
- Haré: desarrollar HU-03 el domingo, junto con Juan Manuel.
- Bloqueos: ninguno. HU-02 ya está integrada en `develop`.

### Domingo 22 de noviembre
 
**Juan**
- Hice: revisé y fusioné los PR #23, #24, #25 y #26. Review del Sprint 1 como PO: HU-01 a HU-05 aceptadas. Release v0.1.0 en `main`.
- Haré: decisiones del PO para el Sprint 2 y empezar HU-09.
- Bloqueos: ninguno.
**Juan Manuel**
- Hice: el PR #23 de correcciones quedó aceptado y mergeado en `develop`, con la contraseña por defecto de la base ya fuera del repositorio. Integré HU-02 y probé el flujo real con la base: la moto entra y sale sin cobro, el carro entra, queda con cobro pendiente de $5.000 y no puede salir, y al quedar pagado sale y vuelve a entrar sin un segundo cobro (el pago lo simulé en la base porque HU-03 todavía no existía). TT-06: pipeline de GitHub Actions con PostgreSQL y el frontend, que en una simulación local pasó las 78 pruebas y el arranque del contexto (PR #24, integrado). TT-09: vista de la sede para el celular con el simulador de entrada y salida, probada en un navegador con pantalla de celular (PR #25, integrado). Con ambos en `develop` comprobé que el frontend compila sin errores del linter y que las 81 pruebas del backend pasan. Entregué a Juan las capturas para el manual de usuario. Después desarrollé HU-03 (pago en efectivo, de Valentina) junto con ella en mi computador, con el patrón State y los criterios del backlog, y comprobé que el escenario completo de la demo funciona: el carro entra, no puede salir, paga con cambio en la caja, sale y vuelve a entrar sin un segundo cobro.
- Hice además: el pull request de HU-03 (#26) quedó integrado y publiqué el release v0.1.0: PR #27 a `main` con el tag `v0.1.0` y PR #28 de vuelta a `develop`. También quité las advertencias de Spring 7 del código.
- Haré: empezar el Sprint 2 con HU-07 (control de cupos), que es un paso más de la cadena de validaciones del carril de entrada.
- Bloqueos: ninguno.
**Miguel**
- Hice: estuve pendiente de los avances del equipo y de los nuevos cambios acordados sobre el flujo de trabajo y las fechas.
- Haré: hacer el pull en mi computador del pull request hecho por mi equipo.
- Bloqueos: ninguno.
**Valentina**
- Hice: HU-03 (registrar el pago en efectivo) completa, desarrollada junto con Juan Manuel desde su computador. Backend en `pago/` con el patrón State (`CobroPendiente`, `CobroPagado`, `CobroVencido` y `CobroAnulado`) y puertos pequeños (principio I), con pruebas unitarias en verde para los 6 casos de prueba. Pantalla de caja en `/salida` según el mockup. Probada de punta a punta con la base real, incluidos 8 pagos simultáneos del mismo cobro. Su rama quedó subida.
- Hice además: el pull request de HU-03 (#26) quedó aprobado e integrado en `develop`.
- Haré: tomar las capturas de la caja para el manual de usuario y preparar HU-08 (administración de tarifas, franjas y cupos) del Sprint 2.
- Bloqueos: ninguno.

## Review (domingo 22 de noviembre)

- Historias mostradas y resultado del PO:

| Historia | Resultado |
|---|---|
| HU-01 Registrar el ingreso y clasificar el vehículo | Aceptada |
| HU-02 Generar el cobro del día operativo | Aceptada |
| HU-03 Registrar el pago en efectivo | Aceptada |
| HU-04 Autorizar o negar la salida | Aceptada |
| HU-05 Iniciar sesión según el rol | Aceptada |

- Tareas técnicas: TT-06 (pipeline de pruebas), TT-07 (release v0.1.0) y TT-09 (vista de la sede en el celular) terminadas.
- Escenarios de la demo que ya funcionan de punta a punta: 1 a 4 (moto sin cobro, carro con cobro pendiente, salida negada sin pago, pago y salida).
- Observaciones de la revisión técnica (Juan Manuel), que pasan al Sprint 2 como tareas OBS: el cobro no se muestra en la pantalla de entrada; dos ingresos simultáneos de la misma placa darían error 500 en vez de un rechazo; el clasificador de placas vive en `salida` aunque lo usan varios módulos; ningún proceso marca los cobros como vencidos (SG-05); los accesos a la base no tienen pruebas de integración; `NuevoCobro` no tiene Javadoc; el simulador muestra "Failed to fetch" sin traducir.

## Retrospectiva

- Qué funcionó:
  - Todas las historias del sprint quedaron terminadas y el release v0.1.0 salió a tiempo.
  - Una misma forma de construir (puertos y adaptadores, cadenas de reglas, fachadas) y las guías paso a paso hicieron que las historias de los cuatro encajaran sin conflictos grandes.
  - La revisión cruzada de los pull requests y el pipeline encontraron problemas antes de llegar a `main`.
- Qué no funcionó:
  - Los dailies no quedaron registrados en el repositorio el mismo día, y hubo que reconstruirlos al final.
  - Mucho trabajo se concentró en el último día (HU-03, pipeline, vista de la sede y release), por la carga de otras materias.
  - Las condiciones de carrera solo aparecieron probando a mano; no hay pruebas automáticas de los accesos a la base.
  - El formateador automático del editor mezcló cambios de formato con cambios de código en algunos PR.
- Acciones concretas para el Sprint 2 y el cierre:
  - Registrar el daily en `sprint-2.md` el mismo día.
  - Abrir el PR de cada historia a más tardar el miércoles 25.
  - Congelar funcionalidades el sábado 28 a las 12:00; después solo se corrigen errores.
  - Desactivar el formato automático al guardar para no reformatear archivos ajenos.
  - Primer borrador de cada parte de la documentación el viernes 27.

## Evidencia
 
- Captura del tablero al cierre: `capturas/sprint-1-tablero.png`
- Captura del burn-up: `capturas/sprint-1-burnup.png`