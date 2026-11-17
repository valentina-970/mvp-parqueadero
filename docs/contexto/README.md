# Contexto del proyecto — MVP Parqueadero

Esta carpeta reúne lo que necesita una persona (o una herramienta de IA) para entender el proyecto sin tener que preguntar. Está pensada para leerse en este orden:

| Archivo | Qué contiene | Cuándo leerlo |
|---|---|---|
| `05-estado-del-codigo.md` | Qué hay construido hoy, cómo está organizado y cómo levantarlo | Siempre, primero |
| `03-product-backlog.md` | Las 9 historias de usuario con criterios de aceptación y casos de prueba | Antes de programar una historia |
| `04-base-de-datos.md` | Diseño de la base de datos (11 tablas) y reglas que garantiza | Al tocar datos o migraciones |
| `02-decisiones-del-grupo.md` | Stack, roles, Gitflow, Definición de Terminado, supuestos | Ante cualquier duda de reglas |
| `01-enunciado-mvp-parqueadero.md` | Enunciado de la docente (MVP III Corte) | Para saber qué se evalúa |

Las estimaciones del Planning Poker están en `docs/scrum/planning-poker.md`.

## El proyecto en un párrafo

Sistema de parqueadero que automatiza el ingreso y la salida de vehículos a partir de la lectura de la placa. Se construye como MVP en dos sprints de una semana (16–22 y 23–29 de noviembre de 2026), con Java 21, Spring Boot, PostgreSQL y React, todo ejecutable con Docker Compose. La cámara y la barrera son simuladas. Se sustenta el 2 o el 3 de diciembre.

## Reglas para trabajar con IA en este repositorio

**Para programar**

1. Trabaja **una historia a la vez**, en su rama `feature/HU-xx-descripcion`, creada desde `develop`. Antes de empezar, lee la historia completa en `03-product-backlog.md`.
2. Los **casos de prueba** (CP-xx-yy) del backlog son la especificación: cada uno debe tener una prueba unitaria automatizada, de éxito o de error según corresponda (JUnit 5 y Mockito).
3. Respeta el **patrón y el principio SOLID** asignados a cada integrante (tabla en `02-decisiones-del-grupo.md`). Un patrón aplicado sin necesidad resta puntos.
4. El dominio depende de **abstracciones** (repositorio, reloj, lector de placas, pasarela), no de la base de datos ni del proveedor concreto.
5. **Una migración de Flyway ya aplicada no se edita.** Los cambios al esquema van en una migración nueva (`V3__...`).
6. **Ningún secreto en el repositorio.** Las variables van en `.env` (que no se sube) y se documentan en `.env.example`.
7. Todo endpoint nuevo debe quedar documentado (Swagger) y el código público, comentado.
8. Si el código y estos documentos no coinciden, **manda el código**; avisa al grupo para actualizar el documento.

**Flujo de Git**

- Nunca commits directos a `main` ni a `develop`: ambas están protegidas.
- Commits tipo Conventional Commits, por ejemplo `feat: autorizar o negar la salida` o `test: validar pago idempotente`. El grupo decidió no poner la historia entre paréntesis.
- Todo cambio entra por pull request a `develop`, aprobado por **otro** integrante.

**Lo que la IA no debe hacer en este proyecto**

- **No generar diagramas.** La docente prohíbe la IA para los diagramas (clases, entidad-relación, casos de uso, patrones). Los dibuja Miguel a mano en draw.io o StarUML.
- **No redactar por sí sola los documentos de la entrega.** El proyecto pasa por una herramienta antiplagio que también detecta uso excesivo de IA. Los textos finales se escriben con palabras propias y las referencias van en APA 7.
- **No tomar decisiones de diseño sin que el dueño de la historia las entienda.** En la sustentación cada integrante debe poder explicar su código, su patrón y su principio SOLID.
