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
| HU-06 | Validar manualmente una lectura dudosa | Miguel | 5 | Por hacer |
| HU-07 | Controlar los cupos por tipo de vehículo | Juan Manuel | 2 | Por hacer |
| HU-08 | Administrar tarifas, franjas y cupos | Valentina | 3 | Por hacer |
| HU-09 | Consultar eventos y cobros | Juan | 2 | Por hacer |
| TT-11 | Lectura de placas con la cámara del celular (reemplaza a TT-10) | Juan | 2 | Por hacer |
| TT-08 | Release v1.0.0 hacia `main`, con commits de los cuatro integrantes | Juan Manuel | — | Por hacer |
| OBS-1 | Mostrar el cobro generado en la pantalla de entrada | Juan | — | Por hacer |
| OBS-2 | Rechazo limpio de ingresos simultáneos de la misma placa (bloqueo de la fila de cupo) | Juan | — | Por hacer |
| OBS-3 | Mover el clasificador de placas a `comun` | Juan | — | En revisión |
| OBS-5 | Pruebas de integración de las condiciones de carrera (si hay tiempo) | Juan Manuel | — | Por hacer |
| OBS-6 | Javadoc de `NuevoCobro` | Miguel | — | Por hacer |
| OBS-7 | Mensaje "No se pudo conectar con el servidor" en lugar de "Failed to fetch" | Juan Manuel | — | Por hacer |

Total de historias: 12 puntos, más TT-11 (2 puntos). Las observaciones (OBS) salen de la revisión de Juan Manuel al cierre del Sprint 1; la OBS-4 (cobros vencidos, SG-05) se decidió dejar fuera del MVP.

## Dailies

### Lunes 23 de noviembre

**Juan**
- Hice: decisiones del PO para el Sprint 2 (cámara del celular, SG-05 fuera del MVP y reparto de las observaciones). Refactor del clasificador de placas a `comun` con su PR abierto. Registro del cierre del Sprint 1.
- Haré: OBS-1 y OBS-2 en la pantalla y el carril de entrada, y empezar HU-09 (consultas).
- Bloqueos: ninguno.
- Pedido al equipo: crear las ramas del Sprint 2 después de que se fusione el refactor del clasificador.

## Review (domingo 29 de noviembre)

- Historias mostradas y resultado del PO (aceptada o devuelta):

## Retrospectiva

- Qué funcionó:
- Qué no funcionó:
- Acción concreta para el cierre del proyecto:

## Evidencia

- Captura del tablero al cierre:
- Captura del burn-up: