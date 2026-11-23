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

### Miércoles 18 de noviembre
 
**Juan**
- Hice: HU-01 completa: paquete `entrada/` con Adapter (lector de placas), Chain of Responsibility (formato → confianza → vehículo no adentro), Facade y Observer (`IngresoRegistrado`), 15 pruebas unitarias, pantalla `/entrada` con simulador y estado del código actualizado. PR #21 abierto hacia `develop`.
- Haré: atender la revisión del PR #21, revisar los PR que me toquen y preparar la aceptación de las historias para la review.
- Bloqueos: ninguno. La demostración de punta a punta necesita HU-02 y HU-03.

### Jueves 19 de noviembre
 
**Juan**
- Hice: HU-01 aprobada y fusionada en `develop` (PR #21). Preparé con Miguel la guía de HU-02, incluido el ajuste para que `IngresoRegistrado` lleve el evento de ingreso que exige la tabla `cobro`. Avancé poco por la carga de otras materias.
- Haré: revisar el PR de HU-02 y tomar las capturas de la pantalla de entrada para el manual de usuario.
- Bloqueos: ninguno.

### Viernes 20 de noviembre
 
**Juan**
- Hice: revisé y fusioné el PR #22 (HU-02, generación del cobro del día operativo).
- Haré: revisar los PR pendientes del fin de semana (seguridad de la salida, pipeline, vista de la sede y HU-03).
- Bloqueos: ninguno.

### Sábado 21 de noviembre
 
**Juan**
- Hice: seguimiento del avance del equipo y de los PR abiertos; preparación de la review como PO.
- Haré: revisar y fusionar los PR #23 (seguridad de la salida), #24 (pipeline, TT-06), #25 (vista de la sede, TT-09) y #26 (HU-03).
- Bloqueos: ninguno.

### Domingo 22 de noviembre
 
**Juan**
- Hice: revisé y fusioné los PR #23, #24, #25 y #26. Review del Sprint 1 como PO: HU-01 a HU-05 aceptadas. Release v0.1.0 en `main`.
- Haré: decisiones del PO para el Sprint 2 y empezar HU-09.
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