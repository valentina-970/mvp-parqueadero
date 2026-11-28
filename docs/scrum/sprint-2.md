# Sprint 2 (23 al 29 de noviembre de 2026)

## Objetivo

Completar las funcionalidades obligatorias (lectura manual, cupos, administración y consultas) y dejar listos la suite de pruebas y el release v1.0.0.

## Sprint Planning (domingo 22 de noviembre)

- Facilitador: Juan Manuel (Scrum Master)
- Product Owner: Juan
- Asistentes: Juan, Juan Manuel, Miguel y Valentina
- Historias que entran: HU-06 a HU-09 (12 puntos), más TT-11 (cámara del celular), TT-08 (release v1.0.0) y las observaciones de la revisión del Sprint 1
- Capacidad del equipo: la misma del Sprint 1. Los cuatro trabajan de forma individual en las tardes y noches, según el tiempo que les dejan las demás materias, que esta semana también están en cierre. Meta: 12 puntos de historias más las tareas técnicas.
- Acuerdos:
  - Se mantienen los acuerdos del Sprint 1 (daily asíncrono, una rama por historia, PR revisado por otro integrante, Conventional Commits sin paréntesis).
  - La lectura de placas pasa a hacerse con la cámara del celular (TT-11), que reemplaza a la vista estilo videojuego (TT-10). El simulador manual queda como plan B.
  - SG-05 (cobros vencidos) queda fuera del MVP; se documenta como trabajo futuro.
  - Primero se fusiona el refactor del clasificador (OBS-3); las ramas del Sprint 2 se crean después, desde `develop` actualizado.
  - Orden de fusión: OBS-2 (bloqueo de cupo) → HU-07 → HU-06; HU-08, HU-09 y TT-11 entran cuando estén aprobadas.
  - PR de cada historia a más tardar el miércoles 25; funcionalidades congeladas el sábado 28 a las 12:00; review, retrospectiva y release v1.0.0 el domingo 29.

## Sprint backlog

| ID | Historia o tarea | Responsable | Puntos | Estado |
|---|---|---|---|---|
| HU-06 | Validar manualmente una lectura dudosa | Miguel | 5 | En revisión |
| HU-07 | Controlar los cupos por tipo de vehículo | Juan Manuel | 2 | En revisión |
| HU-08 | Administrar tarifas, franjas y cupos | Valentina | 3 | En revisión |
| HU-09 | Consultar eventos y cobros | Juan | 2 | En revisión |
| TT-11 | Lectura de placas con la cámara del celular (reemplaza a TT-10) | Juan | 2 | En revisión |
| TT-08 | Release v1.0.0 hacia `main`, con commits de los cuatro integrantes | Juan Manuel | — | Por hacer |
| OBS-1 | Mostrar el cobro generado en la pantalla de entrada | Juan | — | Terminado |
| OBS-2 | Rechazo limpio de ingresos simultáneos de la misma placa (bloqueo de la fila de cupo) | Juan | — | Terminado |
| OBS-3 | Mover el clasificador de placas a `comun` | Juan | — | Terminado |
| OBS-5 | Pruebas de integración de las condiciones de carrera (si hay tiempo) | Juan Manuel | — | Terminado |
| OBS-6 | Javadoc de `NuevoCobro` | Miguel | — | Terminado |
| OBS-7 | Mensaje "No se pudo conectar con el servidor" en lugar de "Failed to fetch" | Juan Manuel | — | Terminado |

Total de historias: 12 puntos, más TT-11 (2 puntos). Las observaciones (OBS) salen de la revisión de Juan Manuel al cierre del Sprint 1; la OBS-4 (cobros vencidos, SG-05) se decidió dejar fuera del MVP.

## Dailies

### Lunes 23 de noviembre

**Juan**
- Hice: decisiones del PO para el Sprint 2 (cámara del celular, SG-05 fuera del MVP y reparto de las observaciones). Refactor del clasificador a `comun` (PR #29) y registro del cierre del Sprint 1 (PR #30). OBS-1 y OBS-2: cobro en la pantalla de entrada y bloqueo de cupo para ingresos simultáneos (PR #31). HU-09 completa: consultas de eventos y cobros, 14 pruebas y pantalla (PR #32). Todo fusionado en `develop`, 126 pruebas en verde.
- Haré: TT-11, lectura de placas con la cámara del celular.
- Bloqueos: ninguno.
- Pedido al equipo: crear las ramas del Sprint 2 desde `develop` actualizado. Juan Manuel: el paso de cupos de HU-07 va después de `BloquearCupoPaso`.
**Juan Manuel**
- Hice: revisé y fusioné los pull requests #29 (clasificador de placas en `comun`), #30 (registro del Sprint 1 y planning del Sprint 2) y #32 (HU-09, consultas). Leí el plan del equipo para el Sprint 2 y los criterios CA-1 a CA-7 de HU-07.
- Haré: crear la rama de HU-07 y construir el paso de cupos en la cadena de la entrada, después de `BloquearCupoPaso`.
- Bloqueos: HU-07 esperaba el bloqueo de cupo de Juan (OBS-2), que se fusionó el lunes por la noche con el PR #31. Ya no hay bloqueo.
**Valentina**
- Hice: revisión de la asignación de HU-08 (administración, 3 puntos) y preparación del entorno para arrancar el desarrollo.
- Haré: crear la rama `feature/HU-08-admin-tarifas`, leer los criterios CA-1 a CA-7 en `03-product-backlog.md` y coordinar con Miguel dónde se guarda el umbral de confianza (HU-08 lo edita y HU-06 lo lee) y con Juan Manuel cómo contar los vehículos que están adentro (CA-5).
- Bloqueos: falta definir con Miguel y Juan Manuel esos dos puntos técnicos antes de codificar la validación.
(pegar el daily de Miguel)

### Martes 24 de noviembre

**Juan**
- Hice: revisión del avance del sprint frente al plan. OBS-1, OBS-2 y HU-09 quedaron en `develop` dos días antes de lo previsto.
- Haré: TT-11 (cámara del celular) el miércoles; jueves y viernes, borrador del documento de análisis A y B y estructura del manual de usuario.
- Bloqueos: ninguno.
- Pedido al equipo: subir a GitHub lo que lleven de HU-06, HU-07 y HU-08, aunque esté a medias, para ver el avance. Fecha de los PR: miércoles 25.
**Juan Manuel**
- Hice: validé el estado del repositorio frente al plan: `develop` con 126 pruebas en verde contra la base real, lint y build del frontend limpios, y el sistema arrancando con Docker. Confirmé que las ramas de HU-06 y HU-07 todavía no existen y que la de HU-08 ya está creada. Respondí los dos puntos técnicos de Valentina: el umbral de confianza vive en la fila `umbral_confianza` de la tabla `parametro`, que la entrada ya lee con el puerto `UmbralConfianza`, y los vehículos que están adentro son las estancias sin hora de salida, por tipo de vehículo.
- Hice además: HU-07 (control de cupos) completa en `feature/HU-07-cupos`. Paquete `cupo/` con el modelo `Cupo`, el puerto de solo lectura `CuposDeLaSede` y `GET /api/cupos`; los ocupados son las estancias abiertas, así que cada salida libera su cupo sin guardar nada. `VerificarCupoPaso` es el último eslabón de la cadena de la entrada, después de `BloquearCupoPaso` y del anti-passback: sin lugar rechaza como LLENO, sin abrir la estancia, sin generar cobro y dejando el rechazo en eventos. En pantalla, la tarjeta de cupos y el banner "LLENO · Barrera cerrada" en la entrada, y los chips y el aviso en el simulador del celular. 143 pruebas en verde, con una prueba de integración contra la base real para la liberación del cupo, y probado de punta a punta en Docker con los 5 casos de prueba (CP-07-01 a CP-07-05).
- Haré: abrir el PR de HU-07 hoy y revisar el que me toque; OBS-7 (mensaje "No se pudo conectar con el servidor") el miércoles.
- Bloqueos: ninguno.
- Pedido al equipo: Valentina, para el CA-5 de HU-08 reutiliza el conteo de estancias abiertas; HU-07 ya dejó ese puerto: `CuposDeLaSede` (en `cupo/`) devuelve la capacidad y los ocupados por tipo; se usa apenas se fusione el PR. Miguel, acuerda el umbral con ella según lo anterior y recuerda hacer `git merge develop` antes de fusionar HU-06.
**Valentina**
- Hice: creé la rama `feature/HU-08-admin-tarifas` y leí los criterios CA-1 a CA-7 de HU-08. Con lo que respondió Juan Manuel ya quedaron definidos los dos puntos técnicos: el umbral de confianza se edita en la fila `umbral_confianza` de la tabla `parametro` y los vehículos que están adentro se cuentan con las estancias sin hora de salida.
- Haré: el backend de HU-08 solo para el administrador: tarifas con la regla de que la nocturna sea menor que la diurna (salvo las motos en $0) y ninguna negativa, franjas que cubran las 24 horas sin solaparse, cupos que no queden por debajo de los vehículos que están adentro y umbral entre 0 y 1. Después, sus pruebas y la pantalla `/administracion`.
- Bloqueos: ninguno.
(pegar el daily de Miguel)

### Miércoles 25 de noviembre

**Juan**
- Hice: TT-11, lectura de placas con la cámara del celular, en `feature/TT-11-camara-celular`. El botón "Leer con la cámara" del simulador toma la foto y la lee en el navegador con Tesseract.js, sin cambios en el backend. La primera versión leía la foto completa y falló con placas reales (letras falsas en la rejilla, el piso y los logos), así que ahora primero localiza la placa amarilla, rechaza las fotos con reflejo de flash y lee solo los seis caracteres (`procesadorPlaca.js`). Probado desde el celular por la red local con placas reales: carro 94 %, moto 89 %, carro de lejos 93 %; con flash pide otra foto y sin placa avisa que no la encontró. Supuesto nuevo registrado en `02-decisiones-del-grupo.md`: si el último carácter es 1/I o 0/O, decide el tipo elegido en el carril. Lint y build limpios (PR #35).
- Haré: jueves y viernes, borrador del documento de análisis A y B y estructura del manual de usuario. Revisar los PR que me asignen.
- Bloqueos: ninguno.
- Pedido al equipo: revisar el PR de TT-11 (Juan Manuel o Valentina). Miguel, TT-11 también modifica `Simulador.jsx` (botón de cámara y mensajes); tu cambio en `interpretar` no se cruza con el mío, pero haz `git merge develop` antes de fusionar HU-06.
(pegar el daily de Juan Manuel)
(pegar el daily de Valentina)
(pegar el daily de Miguel)

### Jueves 26 de noviembre

**Juan**
- Ausente por otras actividades académicas.
(pegar el daily de Juan Manuel)
(pegar el daily de Valentina)
(pegar el daily de Miguel)

### Viernes 27 de noviembre

**Juan**
- Ausente por otras actividades académicas.
(pegar el daily de Juan Manuel)
(pegar el daily de Valentina)
(pegar el daily de Miguel)

### Sábado 28 de noviembre

**Juan**
- Hice: revisé y aprobé el PR de HU-06 (#36) después de correr la suite completa contra la base real (166 pruebas en verde). Hoy verifiqué `develop` con todo lo fusionado el jueves (HU-08, OBS-5, OBS-7 y los contratos de pasarela de pagos y facturación electrónica, PR #37 a #40): 188 pruebas en verde, lint y build del frontend limpios. Con eso queda todo el código del sprint en `develop`. Actualicé el tablero de este documento.
- Haré: ensayo de los 8 escenarios de la demo con Docker desde cero y con el celular, y de ahí las capturas del manual de usuario. Borrador del documento de análisis A y B. El domingo, la review como PO y el release v1.0.0 con Juan Manuel.
- Bloqueos: no hay fecha oficial de entrega; trabajamos con la sustentación el miércoles 2 de diciembre, así que el `.zip` debe quedar listo el martes 1.
- Pedido al equipo: desde hoy no entra código nuevo a `develop`, salvo correcciones que salgan del ensayo. Cada uno envía su parte del documento de análisis y de los manuales a más tardar el domingo.
(pegar el daily de Juan Manuel)
(pegar el daily de Valentina)
(pegar el daily de Miguel)

## Review (domingo 29 de noviembre)

- Historias mostradas y resultado del PO (aceptada o devuelta):

## Retrospectiva

- Qué funcionó:
- Qué no funcionó:
- Acción concreta para el cierre del proyecto:

## Evidencia

- Captura del tablero al cierre:
- Captura del burn-up: