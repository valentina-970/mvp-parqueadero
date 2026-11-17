# Decisiones del grupo

Resumen de lo acordado antes de empezar y durante el Sprint 0. Fuente: Guía de preparación y Product Backlog del proyecto.

## Equipo y roles

| Integrante | Rol Scrum | Observación |
|---|---|---|
| Juan | Product Owner | Decide qué se construye y aprueba o rechaza cada historia en la review |
| Juan Manuel | Scrum Master | Facilita las ceremonias, vigila la Definición de Terminado y Gitflow |
| Miguel | Desarrollador | Dibuja a mano los diagramas |
| Valentina | Desarrolladora | |

Los cuatro programan **historias completas** (backend, frontend, base de datos y pruebas).

## Reparto por integrante

| | Juan | Juan Manuel | Miguel | Valentina |
|---|---|---|---|---|
| Sprint 1 | HU-01 Ingreso y clasificación; carril de entrada de la vista de la sede | HU-04 Salida y barrera de la vista de la sede; HU-05 Roles | HU-02 Cobro | HU-03 Pago |
| Sprint 2 | HU-09 Consultas | HU-07 Cupos | HU-06 Lectura manual | HU-08 Administración |
| Patrón que implementa | Adapter (lector de placas) | Facade (operación del carril) y Chain of Responsibility | Strategy (tarifas) y Chain of Responsibility | State (ciclo del cobro) |
| Principio SOLID que expone | S y L | D | O | I |

## Stack

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot (4.1.x) |
| Seguridad | Spring Security |
| Persistencia | Spring Data JPA y Flyway |
| Base de datos | PostgreSQL 16 en Docker |
| Documentación de la API | springdoc (Swagger) |
| Pruebas | JUnit 5 y Mockito |
| Frontend | React con Vite, JavaScript y React-Bootstrap |
| Ejecutable | Docker Compose |
| Gestión | GitHub, GitHub Projects y GitHub Actions |

## Hardware simulado

El MVP no usa cámara ni barrera físicas. La cámara se reemplaza por un simulador (o por la carga de una imagen) y la barrera por un indicador en pantalla. Lector y barrera van detrás de **interfaces** desde el Sprint 1: Juan define la del lector (HU-01) y Juan Manuel la de la barrera (HU-04). Así, pasar del simulador a una cámara real no cambia el flujo del carril.

**Vista de la sede (simulador en el celular):** ruta `/simulador` del mismo proyecto React, pensada para pantalla de celular, que actúa como un cliente más de la API (envía placa y confianza como lo haría una cámara). Sprint 1: versión simple (elegir vehículo, placa y confianza, y ver ABRE o NO ABRE). Sprint 2, opcional: estilo videojuego, máximo 6 horas por persona y solo si el sprint va al día.

## Gitflow

- `main` solo recibe versiones liberadas; `develop` es la rama de integración. Ambas protegidas: solo cambian con un pull request aprobado por otro integrante.
- Ramas de trabajo: `feature/HU-xx-descripcion`.
- Releases: `release/0.1.0` al cerrar el Sprint 1 y `release/1.0.0` al final, cada una terminada en `main` con su tag (`v0.1.0` y `v1.0.0`).
- Hotfix: `hotfix/x.y.z`, solo si aparece un error en `main`.
- Commits: Conventional Commits. **Decisión del grupo:** sin la historia entre paréntesis.
- Los pull requests no llevan descripción obligatoria. El enlace al PR se pega en el campo **Evidencia** de la tarjeta del tablero.
- Secretos: ninguno en el repositorio; solo un `.env.example`.

## Tablero (GitHub Projects)

- Columnas: Backlog → Por hacer → En progreso → En revisión → Terminado.
- Campos de cada tarjeta: sprint, fecha de inicio, fecha de fin, responsable, puntos, prioridad y evidencia.
- Los criterios de aceptación y los casos de prueba van en la descripción del issue.
- El PO acepta la historia en la review; solo entonces se cierra el issue.

## Definición de Terminado

Una historia está terminada cuando:

1. Cumple todos sus criterios de aceptación y el PO la aprueba.
2. Tiene pruebas unitarias de éxito y error, y todas pasan.
3. Su pull request fue revisado y aprobado por otro integrante.
4. Está integrada en `develop` y funciona con Docker Compose.
5. Sus endpoints están en Swagger y su código público está documentado.
6. Su tarjeta tiene la evidencia (pull request y capturas) y Miguel ya recibió sus clases para el diagrama.

## Supuestos del grupo (lo que el enunciado no detalla)

| ID | Supuesto |
|---|---|
| SG-01 | El cobro es una tarifa plana por día operativo: un vehículo tiene como máximo un cobro por placa, sede y día operativo, sin importar cuántas veces entre. |
| SG-02 | La tarifa se elige según la franja de la hora del primer ingreso del día operativo. Ese ingreso crea el cobro en estado Pendiente. |
| SG-03 | Franjas por defecto: diurna de 06:00 a 17:59 y nocturna de 18:00 a 05:59. El administrador puede cambiarlas. |
| SG-04 | El umbral de confianza por defecto es 0,80. Por debajo de ese valor la lectura pasa a validación manual. |
| SG-05 | Un cobro que sigue Pendiente al terminar su día operativo pasa a Vencido. |
| SG-06 | Una placa que no cumple el formato AAA123 ni AAA12A se rechaza y se pide al operador digitarla. |

Otras decisiones: la historia HU-01 une F-01 y F-02; la primera migración precarga una sede, tarifas, franjas, cupos y un usuario por rol; una sola sede en el MVP, pero el modelo guarda la sede en cada registro para poder crecer.

## Reglas de negocio que se usan en el código

- **Día operativo:** de 06:00 a 05:59 del día siguiente. Un ingreso a las 02:00 del 12 de noviembre pertenece al día operativo del 11.
- **RN-01:** la moto tiene tarifa $0 configurable; si la tarifa es $0, no se crea cobro.
- **RN-02:** una tarifa nocturna mayor o igual que la diurna del mismo tipo se rechaza (se exceptúan las tarifas $0 de las motos).
- **RN-03:** el pago deja un pase vigente hasta el fin del día operativo (05:59).
- **RN-06:** la barrera de salida abre solo si el cobro del día está Pagado o no existe.
- **RN-07:** cada cobro guarda la tarifa con la que se calculó; un cambio de tarifa no modifica cobros existentes.
- RN-04, RN-05 y los supuestos S-01 a S-07 están en los documentos base de la docente, que no se incluyen aquí.

## Calendario

| Etapa | Fechas |
|---|---|
| Sprint 0 | 11 al 15 de noviembre |
| Sprint 1 | 16 al 22 de noviembre |
| Sprint 2 | 23 al 29 de noviembre |
| Cierre | 30 de noviembre al 1 de diciembre (release v1.0.0, manuales, video, .zip y ensayo) |
| Sustentación | 2 o 3 de diciembre |

## Escenarios de la demo

1. Una moto entra y sale sin cobro.
2. Un carro entra en franja diurna y queda con cobro pendiente.
3. El mismo carro intenta salir sin pagar y la barrera no abre.
4. El operador registra el pago; el carro sale y vuelve a entrar sin cobro nuevo.
5. Una lectura con baja confianza pasa a validación manual.
6. El parqueadero se llena para un tipo de vehículo y se muestra LLENO.
7. El administrador intenta guardar una tarifa nocturna mayor que la diurna y el sistema lo impide.
8. Ejecución de la suite de pruebas automatizadas.
