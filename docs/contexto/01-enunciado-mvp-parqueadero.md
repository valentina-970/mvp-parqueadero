# MVP Proyecto – III Corte

| | |
|---|---|
| **Departamento** | Ciencias Computacionales |
| **Asignatura** | Ingeniería de Software II |
| **Docente** | Diana Henao |
| **Fecha** | 16/04/2025 |

## Contexto

Durante el corte se trabajará el caso de un parqueadero que quiere automatizar el ingreso y la salida de vehículos a partir de la lectura de la placa. Los dos documentos base describen el problema, las reglas de negocio (RN-01 a RN-07), los supuestos adoptados (S-01 a S-07), los requisitos no funcionales y una arquitectura de referencia con gateway en la sede y servicios en la nube. Esta entrega lleva ese diseño a un producto mínimo viable (MVP): una versión pequeña del sistema que funcione de punta a punta, esté bien diseñada por dentro y deje claro cómo llegaría a producción.

## Objetivo

Construir y exponer un MVP funcional del sistema de parqueadero con el stack tecnológico que el grupo seleccionó, aplicando los principios SOLID, patrones de diseño, Scrum, Gitflow y pruebas unitarias y presentar una propuesta de infraestructura para el entorno productivo con los pasos necesarios para desplegarlo.

## Restricciones

- **Stack libre.** Cada grupo usa el stack que eligió (lenguaje, framework, base de datos, frontend). Debe justificar la elección en la exposición.
- **Costo cero.** El MVP se construye solo con recursos gratuitos: todo en equipos locales (por ejemplo, Docker Compose), en capas gratuitas de la nube, o una combinación.
- **Hardware simulado.** No se requiere cámara LPR ni barrera física. La cámara se puede reemplazar por carga de imágenes, la webcam del portátil o un simulador que envíe placas con su nivel de confianza. La barrera puede ser un indicador visual en la interfaz.
- **Servicios externos en modo prueba.** La pasarela de pagos puede ser un sandbox (Wompi, Mercado Pago, PayU) o un adaptador simulado. La facturación electrónica queda fuera del MVP; basta con dejar la interfaz definida.
- **Debe funcionar el día de la exposición.** Si el MVP depende de la nube, el grupo debe tener un plan B: ejecución local. Las capas gratuitas suelen apagar los servicios inactivos, así que conviene "despertarlos" antes de exponer.

## Opciones gratuitas de referencia

Son ejemplos, no obligaciones. Las condiciones de las capas gratuitas cambian: verifíquelas con el proveedor antes de usarlas.

| Necesidad | Local | Nube (capa gratuita) |
|---|---|---|
| Backend / API | Contenedor con Docker Compose | Render, Koyeb, Google Cloud Run |
| Frontend | Servidor de desarrollo o Nginx | Vercel, Netlify, Cloudflare Pages |
| Base de datos | PostgreSQL en contenedor | Neon, Supabase |
| Fotos de placas | MinIO o disco local | Supabase Storage, Cloudflare R2 |
| Lectura de placas | EasyOCR, PaddleOCR, YOLO, fast-alpr | Plate Recognizer (plan gratuito con límite mensual) |
| Pagos | Adaptador simulado | Sandbox de Wompi, Mercado Pago o PayU |
| Tablero Scrum | No aplica | GitHub Projects, Jira (plan Free), Trello |

## Alcance del MVP

No se espera el sistema completo. **Se valora más un MVP pequeño que funcione bien y esté bien diseñado que uno grande e inestable.** Las funcionalidades obligatorias son la base de la nota de funcionalidad; las opcionales suman en los criterios de escalabilidad e infraestructura.

### Funcionalidades obligatorias

| ID | Funcionalidad | Criterio de aceptación |
|---|---|---|
| F-01 | Ingreso por placa | El carril de entrada recibe una placa (imagen, webcam o simulador) con su nivel de confianza y registra el evento con fecha, hora y sede. |
| F-02 | Clasificación moto, carro | Como mínimo por formato de placa colombiano: AAA123 es carro, AAA12A es moto. |
| F-03 | Generación del cobro | Aplica RN-01 (moto con tarifa $0 configurable), RN-04 y RN-05. El día operativo va de 06:00 a 05:59 (S-02). La unicidad (placa, sede, día operativo) se garantiza con una restricción en la base de datos. |
| F-04 | Registro de pago | Como mínimo pago en efectivo desde la caja del operador. El pago es idempotente y deja un pase vigente hasta el fin del día operativo (RN-03). |
| F-05 | Salida | La barrera (simulada) abre solo si el cobro del día está pagado o no existe (RN-06). La interfaz muestra claramente si abre o no y por qué. |
| F-06 | Lectura manual | Si la confianza está por debajo de un umbral configurable, el operador valida o digita la placa y el evento queda marcado como "lectura manual". |
| F-07 | Cupos | Antes de abrir se valida el cupo por tipo de vehículo. Si no hay cupo se muestra "LLENO" y la barrera no abre. |
| F-08 | Administración de tarifas | El administrador configura tarifas por tipo de vehículo y franja, horarios de franja y cupos. El sistema rechaza una tarifa nocturna mayor o igual a la diurna (RN-02). Cada cobro guarda la tarifa con la que se calculó (RN-07). |
| F-09 | Consulta de eventos y cobros | Listado de ingresos, salidas y cobros con filtros básicos por placa, fecha y estado. |
| F-10 | Acceso por roles | Autenticación básica con al menos dos roles: operador y administrador. |

### Pruebas unitarias de código

(El enunciado original solo incluye este encabezado, sin contenido. Las pruebas unitarias se exigen en los requisitos de ingeniería y en la Definición de Terminado.)

### Funcionalidades opcionales

- Lectura de placas real con OCR sobre imágenes o video.
- Gateway de sede como proceso separado, con caché de pases y cola local de eventos (operación sin conexión).
- Alerta anti-passback cuando una placa que figura adentro intenta entrar de nuevo.
- Pago en el sandbox de una pasarela, con webhook idempotente.
- Almacenamiento de fotos de la lectura con política de retención.
- Varias sedes con tarifas y cupos independientes.
- Reportes (ingresos por franja, ocupación) y auditoría de acciones manuales.
- Pipeline de integración continua que ejecute pruebas y linter en cada pull request.
- Observabilidad básica: logs estructurados, endpoint de salud y métricas.

## Requisitos de ingeniería

Esta sección es el centro de la evaluación del diseño. Lo que no se pueda mostrar en el código o en el repositorio no cuenta como aplicado.

### Principios SOLID

Para cada principio, el grupo muestra en la exposición **al menos un fragmento de código real** del MVP y explica qué problema tendría el código si no se hubiera aplicado.

| Principio | Qué se espera ver (ejemplos en el dominio) |
|---|---|
| **S**: responsabilidad única | El cálculo de la tarifa está separado del registro del evento y del controlador HTTP. Cada clase o módulo tiene una sola razón para cambiar. |
| **O**: abierto / cerrado | Agregar un tipo de vehículo (camioneta, bicicleta) o una franja nueva no obliga a modificar el motor de cobro. |
| **L**: sustitución de Liskov | Cualquier lector de placas (OCR real, simulador, digitación manual) se puede intercambiar sin que el flujo del carril cambie ni falle. |
| **I**: segregación de interfaces | El carril de salida no depende de operaciones de administración de tarifas; cada consumidor ve solo lo que usa. |
| **D**: inversión de dependencias | El dominio depende de abstracciones (repositorio, pasarela de pagos, reloj, lector LPR) y no del ORM, la base de datos o el proveedor concreto. |

### Patrones de diseño

Se exigen **mínimo tres patrones** implementados en el MVP. Para cada uno: qué problema resuelve en este sistema, dónde está en el código y un diagrama de clases breve. Un patrón aplicado sin necesidad resta en lugar de sumar. Candidatos naturales para este problema:

| Patrón | Dónde encaja en el parqueadero |
|---|---|
| Strategy | Cálculo de la tarifa según tipo de vehículo y franja. |
| State | Ciclo de vida del cobro: Pendiente → Pagado → Vencido, o Pendiente → Anulado. |
| Chain of Responsibility | Validaciones del carril de entrada en orden: confianza de la lectura → anti-passback → cupo → pase vigente. |
| Adapter | Integración con la pasarela de pagos o con el motor de OCR sin acoplar el dominio a su API. |
| Repository | Acceso a datos de vehículos, cobros, pagos y eventos detrás de una interfaz. |
| Observer / Publish-Subscribe | Reacción a eventos del dominio: un pago confirmado actualiza el pase vigente y notifica al carril. |
| Factory | Creación del lector de placas o del medio de pago según la configuración. |
| Facade | Un servicio de acceso que orquesta lectura, reglas y barrera detrás de una sola operación. |

### Scrum

- **Roles** definidos: Product Owner, Scrum Master y equipo de desarrollo.
- **Product backlog** con historias de usuario en formato "Como… quiero… para…", criterios de aceptación, prioridad y estimación en puntos.
- **Dos sprints de una semana** como mínimo (ver cronograma). Para cada sprint: objetivo, sprint backlog, registro breve de la review y de la retrospectiva.
- **Tablero** en una herramienta gratuita con historial visible y un burndown o gráfico equivalente.
- **Definición de terminado (DoD)** explícita. Por ejemplo: código revisado en un pull request, pruebas pasando e integrado en develop.

> **Nota:** para el día de la sustentación deben hacer un release a la main donde se evidencie la integración de código de todos los integrantes del grupo. Revisar antes en "contributors" del repositorio de código.

### Gitflow

- Un repositorio en GitHub o GitLab con acceso para el docente.
- Ramas: **main** (solo versiones liberadas), **develop** (integración), **feature/`<id-historia>`-`<descripción>`**, **release/x.y.z** y **hotfix/x.y.z** si aplica.
- Todo cambio entra a develop por pull request con al menos una revisión de otro integrante. No se hacen commits directos a main ni a develop.
- Al menos una rama release que termine en main con el tag **v1.0.0** (versionado semántico).
- Mensajes de commit descriptivos que referencien la historia de usuario (se recomienda Conventional Commits).
- Commits de todos los integrantes: el aporte individual debe verse en el historial.
- Ningún secreto en el repositorio. Se versiona un archivo `.env.example` y se documentan las variables.

## Propuesta de infraestructura para producción

El MVP es gratuito, pero la propuesta de producción **no tiene esa restricción**: debe describir cómo operaría el sistema real, partiendo de la arquitectura de referencia del documento 02 y ajustada al stack del grupo. Debe incluir:

- **Diagrama del entorno productivo**: gateway de sede, servicios en la nube, bases de datos, almacenamiento, bus de eventos, servicios externos y observabilidad.
- **Tabla MVP vs. producción**: qué se usó en el MVP para cada componente, con qué se reemplaza en producción y por qué.
- **Estrategia de escalabilidad**: qué cambia al pasar de una sede a N sedes y de un carril a varios; escalado horizontal de servicios sin estado, réplicas de lectura, bus de eventos y particionado de datos históricos.
- **Paso a paso del despliegue a producción**, como mínimo:

| # | Paso | Qué debe explicar |
|---|---|---|
| 1 | Entornos | Desarrollo, staging y producción, y cómo se promueve una versión entre ellos. |
| 2 | Contenedores | Imágenes versionadas y publicadas en un registro. |
| 3 | Infraestructura como código | Herramienta (Terraform, Pulumi u otra) y qué recursos crea. |
| 4 | CI/CD | Pipeline que prueba cada pull request y despliega desde main o desde un tag. |
| 5 | Base de datos | Migraciones versionadas, respaldos automáticos y prueba de restauración. |
| 6 | Secretos | Dónde viven las credenciales y cómo llegan a la aplicación. |
| 7 | Red y seguridad | Dominio, TLS, VPN o canal cifrado entre sede y nube, autenticación de dispositivos. |
| 8 | Observabilidad | Logs, métricas, alertas y quién las atiende. |
| 9 | Despliegue sin caída | Estrategia (rolling, blue-green o canary) y cómo se hace rollback. |
| 10 | Alta de una sede nueva | Pasos para instalar y registrar un gateway sin cambiar código. |

- **Seguridad y cumplimiento**: roles, auditoría de acciones manuales y tratamiento de datos personales según la Ley 1581 de 2012 (retención de fotos, aviso de privacidad).
- **Costo mensual estimado** para una sede y para cinco sedes, usando la calculadora del proveedor elegido.

## Escenarios mínimos de la demo

1. Una moto entra y sale sin cobro.
2. Un carro entra en franja diurna y queda con cobro pendiente.
3. El mismo carro intenta salir sin pagar y la barrera no abre.
4. El operador registra el pago; el carro sale y vuelve a entrar sin cobro nuevo.
5. Una lectura con baja confianza pasa a validación manual del operador.
6. El parqueadero se llena para un tipo de vehículo y se muestra "LLENO".
7. El administrador intenta guardar una tarifa nocturna mayor que la diurna y el sistema lo impide.
8. Ejecución de la suite de pruebas automatizadas.

## Estructura de entrega final del proyecto

La entrega debe estar contenida en una carpeta principal comprimida, organizada de la siguiente manera.

El nombre de la carpeta de entrega debe ser: **PROYECTO MVP – NOMBRE GRUPO**

### Estructura de la carpeta de entrega

La carpeta raíz del proyecto debe contener:

- Carpeta **"DOCUMENTO ANÁLISIS"** (el núcleo documental, extensión .pdf).
- Carpeta **"MANUALES"** (con tres documentos independientes, extensión .pdf).
- Carpeta **"EJECUTABLE"** (el producto final instalable).

### 1. Carpeta: DOCUMENTO DE ANÁLISIS

#### A. Definición del proyecto

- **Introducción:** breve descripción del problema y la solución propuesta.
- **Oportunidad de negocio:** por qué es necesaria esta aplicación hoy.
- **Declaración de visión:** el objetivo principal del producto y el impacto esperado a largo plazo.
- **Stakeholders:** identificación de usuarios (clientes, administradores, usuarios finales, etc.).

#### B. Alcance y requisitos

- **Visión general:** funcionalidades principales del sistema.
- **Beneficios:** valor agregado para el cliente.
- **Alcance:** qué hace y qué no hace el MVP.
- **Restricciones:** tecnologías obligatorias, tiempos de entrega y normativas.
- **Requisitos y restricciones:** listado de requerimientos funcionales y no funcionales, junto con las limitaciones de desarrollo (tecnología, tiempo, presupuesto).
- **Roles y responsabilidades:** por cada integrante del grupo (Scrum).

#### C. Diseño y modelado

- **Diagramas:**
  - Casos de uso (interacción usuario-sistema). Para el diagrama de casos de uso se debe anexar la especificación de la estrategia de desarrollo por ciclos (ejemplo):

    | Caso de uso | I | II | III |
    |---|---|---|---|
    | Visualizar información general | X | | |
    | Registrarse como usuario | X | | |
    | Autenticarse en el sistema | | X | |
    | Consultar información Servicios | | | X |

  - Diagrama de clases (estructura lógica bajo SOLID).
  - Diagrama de base de datos (modelo entidad-relación).
- **Historias de usuario:** formato *Como [rol], quiero [acción], para [beneficio]* con criterios de aceptación (4 a 8 por historia).
- En la herramienta de gestión cada requisito debe aplicar la técnica Gitflow, debe estar asociado a su rama de código, además debe tener especificado: sprint, fecha de inicio, fecha de fin, persona asignada, estimación, priorización, diseño de pruebas de éxito y error (recordar que estas salen a partir de los criterios de aceptación) y evidencia del trabajo.
- **Mockups:** diseño visual de las pantallas principales (Figma o similar). Prototipos de alta fidelidad de la interfaz de usuario.

#### D. Gestión y estimación

- **Link herramienta de gestión:** Trello, Jira o GitHub Projects.
- **Link repositorio de código:** Bitbucket o GitHub.
- **Link video de funcionamiento:** Drive (público), YouTube, etc.
- **Técnica de estimación:** por ejemplo Planning Poker o puntos de historia (con evidencia).
- **Tabla de iteraciones:** cronograma de los sprints realizados.
- **Script de creación de BD:** código SQL completo para la generación de la información necesaria para el funcionamiento del proyecto.
- **Documentación de endpoints:** listado de endpoints, o enlace a la documentación automática (Swagger).

### 2. Carpeta: MANUALES

Esta carpeta debe contener tres documentos PDF separados con un nivel de detalle instructivo.

#### A. Manual de instalación

- **Tabla de herramientas:** logo, nombre del lenguaje o herramienta, descripción, versión exacta utilizada y link directo de descarga oficial.
- Este es un ejemplo de cómo debe integrarse la tabla de herramientas dentro del manual de instalación. Esta tabla permite que el cliente o el equipo de soporte técnico replique el entorno de desarrollo con exactitud.

  **Tabla de herramientas y tecnologías (ejemplo)**

  | Logo | Herramienta / Lenguaje | Descripción | Versión | Link de descarga oficial |
  |---|---|---|---|---|
  | (imagen) | IntelliJ IDEA | Entorno de desarrollo integrado (IDE) utilizado para la codificación y refactorización. | 2023.2.5 | ww.link.com |

- **Guía de configuración:** paso a paso detallado para preparar el entorno (variables de entorno `.env`, configuración de puertos, etc.).
- **Despliegue de BD:** instrucciones para ejecutar el script y verificar la conexión.

#### B. Manual de administración

- **Configuración del sistema:** gestión de roles, permisos y usuarios de alto nivel.
- **Mantenimiento:** instrucciones para backups, limpieza de logs o actualización de datos maestros.

#### C. Manual de usuario

- **Guía de uso paso a paso:** descripción de cada funcionalidad con capturas de pantalla del aplicativo real.
- **Solución de problemas:** preguntas frecuentes y errores comunes del usuario final.

### 3. Carpeta: EJECUTABLE

- **Archivo .exe o Docker Compose:** el archivo autoejecutable o instalador final que garantice el funcionamiento del MVP en la vida real.
- **Imagen icono:** icono de la instalación de escritorio.
- **Script SQL:** código SQL completo para la generación de la información necesaria para el funcionamiento del proyecto.
- **Funcionalidad:** el aplicativo debe ser un MVP completamente funcional que cumpla con los requisitos solicitados y la lógica de negocio.

## Requisitos técnicos del desarrollo

El aplicativo no solo debe funcionar, sino estar construido bajo estándares profesionales de industria y de los conocimientos impartidos en clase:

- Metodología Scrum
- Principios SOLID
- Clean Code
- Patrones de diseño
- Refactorización
- GitFlow
- Documentación de código
- Técnicas de estimación
- Pruebas unitarias

## Presentación del documento y cumplimiento de plazos

Para efectos de la evaluación, además del contenido técnico y académico, se valorará la **presentación formal** del documento y la **entrega en las fechas estipuladas**.

### Presentación del documento

- Uso de portada y subportada institucional.
- Redacción profesional y coherente.
- Inclusión de gráficos, diagramas y tablas con formato estandarizado.
- Uso correcto de normas **APA 7.ª edición** para citas y referencias bibliográficas.
- Inclusión de infografía con el manual de marca institucional UAM.

### Cumplimiento de la entrega

- La entrega deberá realizarse a través de **UAM Virtual**. El archivo debe enviarse en **formato .zip** y nombrarse de la siguiente manera: **PROYECTO MVP – NOMBRE GRUPO.ZIP**
  - Ejemplo: si el nombre del grupo es GRUPO PRUEBA, el archivo deberá nombrarse así: **PROYECTO MVP – GRUPO PRUEBA.ZIP**
- La fecha límite es la definida en el calendario.
- Grupos de 4 estudiantes.
- No se permiten softwares repetidos.
- No se admitirán entregas posteriores a esta fecha.
- **Información a tener en cuenta:** el proyecto será analizado por la herramienta antiplagio de la universidad, utilizada para detectar similitudes en documentos académicos y detectar plagio, así como el uso excesivo de inteligencia artificial. Se debe citar derechos de autor. **La inteligencia artificial no está permitida en este proyecto para la generación de diagramas.**

## Referencias

Las referencias utilizadas para la elaboración de este informe deberán presentarse en **norma APA, séptima edición**.

Ejemplo:

- Pressman, R. S., & Maxim, B. R. (2021). *Ingeniería del software: un enfoque práctico* (9.a ed.). McGraw-Hill.
- Sommerville, I. (2011). *Ingeniería del software* (9.a ed.). Pearson Educación.
