# Estado del código

Descripción de lo construido al cierre del Sprint 0 (16 de noviembre de 2026). **Si el código difiere de este documento, manda el código**; avisa al grupo para actualizarlo.

## Estructura del repositorio

```
mvp-parqueadero/
├── backend/                 API en Spring Boot (Maven)
│   ├── Dockerfile
│   └── src/main/
│       ├── java/com/uam/parqueadero/
│       │   ├── ParqueaderoApplication.java
│       │   ├── config/        SecurityConfig, UsuariosIniciales
│       │   └── controller/    HealthController
│       └── resources/
│           ├── application.yml
│           └── db/migration/  V1__esquema_inicial.sql, V2__datos_iniciales.sql
├── frontend/                React + Vite (JavaScript)
│   ├── Dockerfile
│   ├── nginx.conf
│   └── src/
│       ├── components/        Navegacion, Pagina
│       ├── pages/             Login
│       └── services/          api.js
├── docs/
│   ├── contexto/            esta carpeta
│   ├── scrum/               registro de sprints y planning poker
│   └── versiones-herramientas.md
├── docker-compose.yml
├── .env.example
└── README.md
```

## Backend

- **Java 21, Spring Boot 4.1.2**, Maven (`mvnw`). Grupo `com.uam`, artefacto `parqueadero`, paquete base `com.uam.parqueadero`.
- Dependencias: Spring Web, Spring Data JPA, PostgreSQL Driver, Flyway, Spring Security y Validation.
- **Base de datos:** PostgreSQL 16. El esquema lo crea Flyway al arrancar; Hibernate solo valida (`ddl-auto: validate`). El detalle de las tablas está en `04-base-de-datos.md`.
- **Migraciones:** `V1__esquema_inicial.sql` (11 tablas) y `V2__datos_iniciales.sql` (sede "Principal", tipos CARRO y MOTO con su patrón de placa, franjas DIURNA y NOCTURNA, tarifas, cupos de 40 carros y 20 motos, y el parámetro `umbral_confianza` = 0.80).

- **Acceso por roles (HU-05):** sesión HTTP con cookie. `usuario/` tiene `Rol`, la entidad `Usuario` y `UsuarioRepository`; `seguridad/` tiene `UsuarioDetailsService` (carga el usuario para Spring Security) y `AuthController`.
  - `POST /api/auth/login` (usuario y contraseña; 401 con "Usuario o contraseña incorrectos"), `POST /api/auth/logout` y `GET /api/auth/yo` (usuario y rol de la sesión).
  - **`config/SecurityConfig`:** `/api/health`, el login y Swagger son públicos; `/api/admin/**` exige rol ADMINISTRADOR; el resto exige sesión. Sin sesión responde 401 y con rol insuficiente 403, ambos en JSON. CSRF desactivado y CORS con credenciales desde `http://localhost:5173`. Define `PasswordEncoder` (BCrypt) y `AuthenticationManager`.
  - **`config/UsuariosIniciales`:** `ApplicationRunner` que, al arrancar, crea `admin` (ADMINISTRADOR) y `operador1` (OPERADOR) si no existen, con las contraseñas del entorno guardadas con BCrypt, usando `UsuarioRepository`. Si falta una contraseña, no crea el usuario y avisa en el log.
  - `controller/AdminTarifasController` es un marcador solo para administradores; HU-08 lo reemplaza.

- **Carril de salida (HU-04):** paquete `salida/`. `POST /api/salida` recibe `{placa, confianza}` y responde `{abre, motivo, barrera, ...}`; `GET /api/salida/ultima` devuelve el último intento (204 si no hay) para la pantalla del operador.
  - **Facade:** `OperacionCarrilSalida` es la única operación que usa el controlador: normaliza la placa, ejecuta la cadena, registra el evento, cierra la estancia y mueve la barrera, todo en una transacción.
  - **Chain of Responsibility:** `PasoSalida` y sus pasos `ValidarPlacaPaso` → `VerificarConfianzaSalidaPaso` → `VerificarIngresoPaso` → `VerificarDeudaPaso` (RN-06), armados en `CadenaSalidaConfig`.Una regla nueva es un paso más, sin tocar los demás. Con confianza baja la salida queda pendiente (`ContextoSalida.dejarPendiente`): la barrera no se mueve y el evento se guarda como `PENDIENTE_VALIDACION` (HU-06).
  - **Puertos (principios D e I):** `Estancias`, `CobrosDelDia` (solo lectura), `RegistroEventos` y `Barrera`. Sus adaptadores están en `salida/infraestructura/` (JDBC y `BarreraSimulada`). Leen y escriben las tablas del esquema, así que no chocan con las entidades que creen HU-01 y HU-02; si esas historias ofrecen sus propios repositorios, solo se cambia el adaptador.
  - **Reutilizable por todos (`comun/`):** el puerto `ClasificadorPlacas` (con `TipoVehiculoDetectado` y su adaptador `ClasificadorPlacasJdbc`, que clasifica con los patrones de `tipo_vehiculo`), `Placa.normalizar`, `DiaOperativo.de` (06:00–05:59), `Dinero.formatear`, el puerto `SedeActual`, el puerto `UmbralConfianza` (con `UmbralConfianzaJdbc`, que usan los dos carriles), el registro `LecturaPendiente` y el bean `Clock`
  - Una placa con formato inválido se rechaza sin registrar evento, porque `evento_acceso` exige el tipo de vehículo. El cupo se libera al cerrar la estancia, ya que los cupos ocupados son las estancias abiertas.

- **Carril de entrada (HU-01):** paquete `entrada/`. `POST /api/entrada` recibe `{placa, confianza}` y responde `{resultado, motivo, placa, tipoVehiculo, confianza, ocurridoEn}`, con `resultado` = `PERMITIDO`, `PENDIENTE_VALIDACION` o `RECHAZADO`; `GET /api/entrada/ultimo` devuelve la última lectura (204 si no hay) para la pantalla del operador. Responde 400 solo si el cuerpo es inválido (confianza fuera de 0 a 1).
  - **Adapter:** `LectorPlacas` es el puerto del lector; `infraestructura/LectorSimulado` lo implementa y convierte lo que envía el simulador en una `LecturaPlaca` con la placa normalizada. Una cámara real sería otro adaptador (principio L).
  - **Facade:** `OperacionCarrilEntrada` lee la placa, ejecuta la cadena, registra el evento, abre la estancia y publica el aviso, todo en una transacción.
  - **Chain of Responsibility:** `PasoEntrada` y sus pasos `ValidarFormatoPaso` → `VerificarConfianzaPaso` → `BloquearCupoPaso` → `VerificarNoAdentroPaso`, armados en `CadenaEntradaConfig`. A diferencia de la salida, el contexto (`ContextoEntrada`) guarda un `EstadoIngreso`; el primer paso que decide (rechaza o deja pendiente) detiene la cadena. `BloquearCupoPaso` bloquea la fila de `cupo` del tipo (`FOR UPDATE`, puerto `BloqueoCupos`), así que dos ingresos simultáneos de la misma placa se atienden uno tras otro y el segundo recibe un rechazo limpio.
  - **Puertos:** `EstanciasEntrada` y `EventosEntrada`, con adaptadores JDBC en `entrada/infraestructura/`. Usa `ClasificadorPlacas` y `UmbralConfianza` (lee `parametro.umbral_confianza`, 0,80 si no existe) de `comun/`, igual que la salida.
  - **Observer:** un ingreso permitido publica `IngresoRegistrado` (estancia, sede, placa, tipo de vehículo, hora y día operativo). HU-02 lo escucha con `@EventListener` para generar el cobro, sin que el carril la conozca.
  - Placa inválida: se rechaza sin registrar evento (CA-6). Confianza menor que el umbral: se registra el evento como `PENDIENTE_VALIDACION`, sin estancia; HU-06 lo resuelve con la validación manual. Vehículo con estancia abierta: se rechaza y el intento queda registrado.

- **Generación del cobro (HU-02):** paquete `cobro/`. No tiene endpoint: `GeneradorCobro` escucha `IngresoRegistrado` con `@EventListener` (Observer) dentro de la transacción del ingreso y crea el cobro del día operativo en estado `PENDIENTE`.
  - **Strategy:** `PoliticaTarifa` calcula la tarifa; la estrategia del MVP es `TarifaPorFranja` (tipo de vehículo y franja de la hora de entrada, con franjas que cruzan la medianoche). Otra forma de cobrar sería otra implementación (principio O).
  - **Chain of Responsibility:** `PasoCobro` y sus pasos `VerificarCobroDelDiaPaso` → `AplicarTarifaPaso` → `VerificarTarifaCeroPaso`, armados en `CadenaCobroConfig`.
  - **Puertos:** `TarifasVigentes` y `RegistroCobros`, con adaptadores JDBC en `cobro/infraestructura/`. El cobro guarda el valor y el id de la tarifa (RN-07). La base impide dos cobros por placa, sede y día (`uq_cobro_placa_sede_dia`).
  - `IngresoRegistrado` ahora incluye `eventoIngresoId`, que el cobro referencia.

- **Caja y pago en efectivo (HU-03):** paquete `pago/`. `GET /api/caja/cobros/{placa}` busca el cobro del día operativo (404 "No hay cobro pendiente para esta placa") y `POST /api/caja/pagos` registra el pago con `{placa, valorRecibido}`: 201 con el pago y el cambio, 200 con el pago existente si el cobro ya estaba pagado, 422 si el efectivo no alcanza y 409 si el cobro no se puede pagar (anulado o vencido).
  - **State:** `CobroPorPagar` es el contexto y delega en su `EstadoDelCobro` (`CobroPendiente`, `CobroPagado`, `CobroVencido`, `CobroAnulado`). Pendiente crea el `Pago` con su cambio y el pase vigente hasta las 05:59:59 del día operativo (`DiaOperativo.fin`) y pasa a Pagado; Pagado devuelve el pago ya guardado (idempotencia); Anulado y Vencido rechazan con su motivo. Un estado nuevo es una clase nueva (principio O).
  - **Puertos pequeños (principios I y D):** `BuscadorDeCobros` (solo lectura, con y sin bloqueo), `RegistroPagos` y `OperadoresCaja`. Sus adaptadores están en `pago/infraestructura/` y leen y escriben las tablas `cobro`, `pago` y `usuario`; la caja no depende de cómo HU-02 crea los cobros.
  - **Concurrencia:** al pagar se bloquea la fila del cobro (`FOR UPDATE`), y el pago ya existente se lee en una segunda consulta después del bloqueo. Probado con 8 pagos simultáneos del mismo cobro: se crea 1 pago, los otros 7 reciben el existente, y la restricción `uq_pago_cobro` queda como última defensa.
  - Si el vehículo cruzó las 06:00 con el cobro del día anterior pendiente, la caja también lo encuentra, para que pueda pagar y salir (la salida de HU-04 hace la misma comprobación).
- **Consultas (HU-09):** paquete `consulta/`, solo lectura. `GET /api/consultas/eventos` (filtros `placa`, `desde`, `hasta`) y `GET /api/consultas/cobros` (los mismos más `estado`), ambos con `pagina` desde 0. Responden `{contenido, pagina, totalPaginas, totalElementos}` en páginas de 20, del más reciente al más antiguo; 400 si la fecha final es anterior a la inicial o el estado no existe. Operador y administrador.
  - `FiltroConsulta` normaliza la placa y valida el rango; los puertos `ConsultaEventos` y `ConsultaCobros` tienen sus adaptadores JDBC en `consulta/infraestructura/`, y `CondicionesSql` arma el `WHERE` solo con los filtros que llegan.
- **Control de cupos (HU-07):** paquete `cupo/`. `GET /api/cupos` devuelve, por tipo de vehículo, `{capacidad, ocupados, disponibles, lleno}`.
  - **Los ocupados no se guardan:** son las estancias sin hora de salida (`CuposDeLaSedeJdbc`), así que cada ingreso ocupa un cupo y cada salida con la barrera abierta lo libera sin escribir nada. La capacidad sale de la tabla `cupo`.
  - **Chain of Responsibility:** `VerificarCupoPaso` es el último eslabón de la cadena de la entrada (formato → confianza → `BloquearCupoPaso` → anti-passback → cupo), como pide el enunciado. Va después del bloqueo de la fila de `cupo`, así que dos ingresos simultáneos no pueden ocupar el último lugar a la vez. Si no hay lugar, el ingreso se rechaza como `RECHAZADO` con el motivo `LLENO: no hay cupos disponibles para carros`: no se abre la estancia, no se publica `IngresoRegistrado` (por eso HU-02 no genera cobro) y el intento queda en `evento_acceso`. `ResultadoIngreso` trae `lleno=true` para que la pantalla muestre LLENO.
  - **Un tipo lleno no afecta al otro:** la consulta es siempre por el tipo del vehículo que entra. Un tipo sin cupo definido se trata como lleno hasta que el administrador le asigne uno (HU-08).
  - **Puerto reutilizable (principios I y D):** `CuposDeLaSede` es de solo lectura; HU-08 lo usa para no bajar un cupo por debajo de los vehículos que están adentro.
  - **Pruebas:** `CupoEnLaEntradaTest` cubre CP-07-01 a CP-07-05 con la cadena real; `CuposIntegracionTest` repite los criterios CA-3, CA-5 y CA-7 contra la base real, dentro de una transacción que se revierte.
  - **Validación manual (HU-06):** paquete `validacion/`. `GET /api/validaciones?carril=ENTRADA|SALIDA` lista las lecturas pendientes (placa leída, confianza, hora) y `POST /api/validaciones/{eventoId}` con `{placa}` las confirma o corrige; responde `{carril, placa, permitido, motivo}`, 404 si ya se validó y 422 si la placa no tiene formato válido (la lectura sigue pendiente).
  - **Sin reglas repetidas:** `ValidacionManual` toma la lectura con `FOR UPDATE` (dos operadores no la validan a la vez) y se la entrega a la fachada de su carril (`OperacionCarrilEntrada.validar` u `OperacionCarrilSalida.validar`), que vuelve a aplicar su cadena con la placa del operador, salvo el filtro de confianza (`ContextoEntrada.manual`, `ContextoSalida.manual`).
  - **El mismo evento se resuelve:** `EventosEntrada.resolverManual` y `RegistroEventos.resolverManual` actualizan el evento pendiente con la placa validada, el resultado final, `tipo_lectura = MANUAL` y `validado_por_id` (CA-6). La placa leída y la confianza originales se conservan.
  - **Pruebas:** `ValidacionEnLaEntradaTest`, `ValidacionEnLaSalidaTest`, `VerificarConfianzaPasoTest`, `VerificarConfianzaSalidaPasoTest`, `ValidacionManualTest` y `ValidacionControllerTest` cubren CP-06-01 a CP-06-06; `ValidacionIntegracionTest` repite CP-06-01 contra la base real.
- **Swagger:** springdoc en `/swagger-ui.html` y `/v3/api-docs`. Todo endpoint nuevo se documenta con `@Tag` y `@Operation`.
- **`controller/HealthController`:** `GET /api/health` responde `{"estado":"ok"}`.
- **`application.yml`:** toda la configuración sale de variables de entorno, con valores por defecto para desarrollo local.

### Variables de entorno

| Variable | Uso |
|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT` | Conexión a PostgreSQL |
| `DB_HOST` | Servidor de la base (`localhost` en desarrollo; `db` dentro de Docker) |
| `BACKEND_PORT`, `FRONTEND_PORT` | Puertos expuestos |
| `ADMIN_USUARIO`, `ADMIN_CONTRASENA` | Usuario administrador inicial |
| `OPERADOR_USUARIO`, `OPERADOR_CONTRASENA` | Usuario operador inicial |

El archivo `.env` es local y **no se sube**; `.env.example` documenta las variables, sin contraseñas.

## Frontend

- **React con Vite, JavaScript** (no TypeScript), con React-Bootstrap, Bootstrap, React Router y ESLint.
- **Rutas:** `/login`, `/entrada`, `/salida`, `/administracion` y `/consultas`. Todas tienen interfaz menos `/administracion`, que sigue siendo un marcador (`components/Pagina`) hasta HU-08.
- **Componentes compartidos según los mockups:** `PlacaVisual` (placa amarilla), `BannerResultado` (verde, rojo o amarillo), `IndicadorBarrera`. Están pensados para que los usen las demás pantallas.
- **Vista de la sede para el celular (TT-09):** `pages/Simulador` en `/simulador/salida` y `/simulador/entrada` (`/simulador` lleva a la salida). Tema oscuro y pantalla de celular, según el mockup: `components/EscenaSede` dibuja la vía, el vehículo con su placa y la barrera (se levanta y se pone verde cuando abre), un banner "ABRE · motivo" o "NO ABRE · motivo", y el formulario con tipo de vehículo, placa y confianza. Llama a `/api/salida` o `/api/entrada` y adapta cada respuesta (la salida responde `abre`; la entrada, `resultado`). Requiere sesión, pero no muestra la barra superior. Desde el celular se abre con la IP del computador: `http://IP-DEL-PC:5173/simulador/salida`; al iniciar sesión vuelve a la pantalla que se pidió.
- **Lectura con la cámara del celular (TT-11):** en el simulador, el botón "📷 Leer con la cámara" toma una foto con la cámara trasera y la lee en el navegador con Tesseract.js, sin servidor de OCR. `services/procesadorPlaca.js` (funciones puras sobre los píxeles) busca la zona amarilla con forma de placa, rechaza la foto si el flash la dejó blanca y deja solo los seis caracteres grandes, sin ciudad, tornillos, borde ni sello. `services/lectorCamara.js` es el adaptador: pasa esos caracteres al OCR como una sola línea, busca el patrón AAA123 o AAA12A, corrige confusiones típicas (O y 0, B y 8…) y devuelve `{ placa, confianza }` con la confianza entre 0 y 1, o un motivo (`SIN_PLACA` o `REFLEJO`). El simulador envía la lectura al carril como cualquier otra, así que el backend no cambia. Si el último carácter es 1/I o 0/O, decide el tipo elegido (Carro o Moto); si es una letra clara, el tipo cambia solo a Moto.
- **`pages/Entrada`:** carril de entrada (`components/CarrilEntrada`, consulta `/api/entrada/ultimo` cada 2 segundos) y, a la derecha, `components/SimuladorLectura`, un formulario de placa y confianza que envía la lectura a la ruta que recibe (sirve también para la salida). El banner amarillo (`.banner-aviso`) marca las lecturas que requieren validación manual. Cuando el ingreso es permitido, `components/CobroGenerado` consulta la caja y muestra el cobro generado o "Sin cobro".
- **`pages/Salida`:** carril de salida (`components/CarrilSalida`, consulta `/api/salida/ultima` cada 2 segundos) y, a la derecha, `components/CajaPago`: busca la placa, muestra día operativo, franja, tarifa, valor y estado (`components/EstadoBadge`), calcula el cambio mientras se escribe el efectivo (`utils/formato.js`), registra el pago y muestra el resumen con el pase vigente y el operador. Si el cobro está pagado, anulado o vencido, lo explica en lugar de pedir el efectivo.
- **Validación manual (HU-06):** `components/ValidacionManual`, debajo del carril en `/entrada` y en `/salida`. Consulta las lecturas pendientes de su carril cada 2 segundos y solo aparece si hay alguna: placa leída, confianza y un campo con la placa, con el botón Confirmar o, si el operador la cambia, Corregir y validar. El carril muestra después "Lectura: Manual (operador)", y la salida pendiente se ve como VALIDACIÓN MANUAL, también en el simulador del celular.
- **Cupos en pantalla (HU-07):** `components/CuposDisponibles` (tarjeta "Cupos disponibles" con la insignia LLENO) en `/entrada`, y los chips de cupos ("Carros 12 libres", "Motos LLENO") en el simulador del celular; ambos usan el hook `hooks/useCupos`. Cuando el ingreso se rechaza por cupo, `CarrilEntrada` muestra el banner "LLENO · Barrera cerrada" y el simulador "LLENO · no hay cupos disponibles para carros".
- **`pages/Consultas`:** pestañas Eventos y Cobros, filtros por placa, rango de fechas y estado (solo cobros), tabla y `components/Paginador`. Si la fecha final es anterior a la inicial, avisa sin consultar.
- **`pages/Login`:** inicio de sesión real contra `/api/auth/login`, según el mockup (mensaje de error genérico).
- **`auth/`:** `AuthProvider` guarda el usuario de la sesión (la recupera con `/api/auth/yo` al abrir la app) y ofrece `iniciarSesion` y `cerrarSesion`; `useAuth` lo expone a los componentes.
- **`components/RutaProtegida`:** sin sesión lleva a `/login`; con rol insuficiente (por ejemplo, un operador en `/administracion`) vuelve a `/entrada`. **`components/Navegacion`** solo muestra Administración al administrador, el usuario y su rol, y "Cerrar sesión".
- **`services/api.js`:** cliente de la API (`get` y `post`) que envía la cookie de sesión y lanza `ErrorApi` con el mensaje del servidor. Un fallo de conexión o una respuesta de proxy 502/503/504 sin mensaje de la API se muestra como «No se pudo conectar con el servidor» (OBS-7). La URL base sale de `VITE_API_URL`.
- **`estilos.css`:** estilos globales según los mockups del equipo.
- **Dos modos de ejecución:**
  - Desarrollo (`npm run dev`): `VITE_API_URL=http://localhost:8080`.
  - Docker: la API se llama por ruta relativa (`/api`) y Nginx la reenvía al backend. Así funciona igual desde el portátil y desde el celular del simulador.

## Integración continua (TT-06)

`.github/workflows/ci.yml` corre en cada pull request hacia `develop` o `main` y tiene dos trabajos:

| Trabajo | Qué hace |
|---|---|
| `backend` | Levanta un PostgreSQL 16 descartable, instala Java 21 y ejecuta `./mvnw -B verify` (todas las pruebas, incluido el arranque completo, y el empaquetado). Guarda el informe de pruebas como artefacto. |
| `frontend` | Instala Node 24 y ejecuta `npm ci`, `npm run lint` y `npm run build`. |

Las credenciales de la base y de los usuarios son valores de prueba escritos en el workflow: la base existe solo mientras corre el pipeline y no es un secreto real. Para que el pipeline bloquee los merge con pruebas rotas, en GitHub hay que marcar los trabajos `backend` y `frontend` como obligatorios en la protección de `develop` y `main` (Settings → Branches).

## Docker Compose

Tres servicios:

| Servicio | Contenedor | Detalle |
|---|---|---|
| `db` | `parqueadero-db` | `postgres:16`, volumen `pgdata`, con `healthcheck` |
| `backend` | `parqueadero-backend` | Se construye desde `backend/`; espera a que `db` esté sano; usa `DB_HOST=db` |
| `frontend` | `parqueadero-frontend` | Se construye desde `frontend/`; Nginx sirve la aplicación y reenvía `/api/` al backend |

## Cómo levantarlo

**Sistema completo (para verificar antes de un PR y para la demo)**

```powershell
Copy-Item .env.example .env      # y completar las contraseñas
docker compose up -d --build
docker compose ps
```

Comprobaciones: `http://localhost:8080/api/health` responde `{"estado":"ok"}` y `http://localhost:5173/login` muestra "API: conectada".

**Desde el celular (simulador y cámara)**

El celular debe estar en la misma red que el computador. Con `ipconfig` se busca la dirección IPv4 del computador y en el celular se abre `http://IP-DEL-PC:5173/simulador/entrada`. La primera vez hay que abrir el puerto en el firewall de Windows, desde una PowerShell como administrador y con la red en perfil privado:

```powershell
New-NetFirewallRule -DisplayName "Parqueadero 5173" -Direction Inbound -Protocol TCP -LocalPort 5173 -Action Allow -Profile Private
```

La primera lectura con la cámara tarda más (10 a 20 segundos) porque el celular descarga Tesseract.js, así que necesita internet además de la red local.

Para apagar: `docker compose down` (con `-v` también borra los datos de la base).

**Modo desarrollo (día a día, con recarga rápida)**

```powershell
docker compose up -d db          # solo la base de datos

cd backend
$env:POSTGRES_PASSWORD = "..."    # la misma que pusiste en .env; el código ya no trae un valor por defecto
$env:ADMIN_CONTRASENA = "..."
$env:OPERADOR_CONTRASENA = "..."
.\mvnw spring-boot:run

cd ..\frontend
npm install
npm run dev
```

No corras `npm run dev` y el contenedor `frontend` a la vez: usan el mismo puerto (5173).

## Pendientes técnicos conocidos

- `ParqueaderoApplicationTests` (arranque completo) necesita la base de datos levantada y las variables del `.env` en la terminal; en el pipeline las pone el propio workflow.
- La historia HU-08 no está implementada (Sprint 2).
- Una migración ya aplicada no se edita: los cambios al esquema van en `V3__...` en adelante.