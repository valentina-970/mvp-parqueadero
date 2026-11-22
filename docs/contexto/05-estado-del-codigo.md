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
  - **Chain of Responsibility:** `PasoSalida` y sus pasos `ValidarPlacaPaso` → `VerificarIngresoPaso` → `VerificarDeudaPaso` (RN-06), armados en `CadenaSalidaConfig`. Una regla nueva (cupo, confianza de HU-06) es un paso más, sin tocar los demás.
  - **Puertos (principios D e I):** `ClasificadorPlacas`, `Estancias`, `CobrosDelDia` (solo lectura), `RegistroEventos` y `Barrera`. Sus adaptadores están en `salida/infraestructura/` (JDBC y `BarreraSimulada`). Leen y escriben las tablas del esquema, así que no chocan con las entidades que creen HU-01 y HU-02; si esas historias ofrecen sus propios repositorios, solo se cambia el adaptador.
  - **Reutilizable por todos (`comun/`):** `Placa.normalizar`, `DiaOperativo.de` (06:00–05:59), `Dinero.formatear`, el puerto `SedeActual` y el bean `Clock` de `config/RelojConfig` (zona America/Bogota).
  - Una placa con formato inválido se rechaza sin registrar evento, porque `evento_acceso` exige el tipo de vehículo. El cupo se libera al cerrar la estancia, ya que los cupos ocupados son las estancias abiertas.

- **Carril de entrada (HU-01):** paquete `entrada/`. `POST /api/entrada` recibe `{placa, confianza}` y responde `{resultado, motivo, placa, tipoVehiculo, confianza, ocurridoEn}`, con `resultado` = `PERMITIDO`, `PENDIENTE_VALIDACION` o `RECHAZADO`; `GET /api/entrada/ultimo` devuelve la última lectura (204 si no hay) para la pantalla del operador. Responde 400 solo si el cuerpo es inválido (confianza fuera de 0 a 1).
  - **Adapter:** `LectorPlacas` es el puerto del lector; `infraestructura/LectorSimulado` lo implementa y convierte lo que envía el simulador en una `LecturaPlaca` con la placa normalizada. Una cámara real sería otro adaptador (principio L).
  - **Facade:** `OperacionCarrilEntrada` lee la placa, ejecuta la cadena, registra el evento, abre la estancia y publica el aviso, todo en una transacción.
  - **Chain of Responsibility:** `PasoEntrada` y sus pasos `ValidarFormatoPaso` → `VerificarConfianzaPaso` → `VerificarNoAdentroPaso`, armados en `CadenaEntradaConfig`. A diferencia de la salida, el contexto (`ContextoEntrada`) guarda un `EstadoIngreso`; el primer paso que decide (rechaza o deja pendiente) detiene la cadena.
  - **Puertos:** `UmbralConfianza` (lee `parametro.umbral_confianza`, 0,80 si no existe), `EstanciasEntrada` y `EventosEntrada`, con adaptadores JDBC en `entrada/infraestructura/`. Reutiliza `ClasificadorPlacas` y `TipoVehiculoDetectado` del paquete `salida`.
  - **Observer:** un ingreso permitido publica `IngresoRegistrado` (estancia, sede, placa, tipo de vehículo, hora y día operativo). HU-02 lo escucha con `@EventListener` para generar el cobro, sin que el carril la conozca.
  - Placa inválida: se rechaza sin registrar evento (CA-6). Confianza menor que el umbral: se registra el evento como `PENDIENTE_VALIDACION`, sin estancia; HU-06 lo completa. Vehículo con estancia abierta: se rechaza y el intento queda registrado.

- **Generación del cobro (HU-02):** paquete `cobro/`. No tiene endpoint: `GeneradorCobro` escucha `IngresoRegistrado` con `@EventListener` (Observer) dentro de la transacción del ingreso y crea el cobro del día operativo en estado `PENDIENTE`.
  - **Strategy:** `PoliticaTarifa` calcula la tarifa; la estrategia del MVP es `TarifaPorFranja` (tipo de vehículo y franja de la hora de entrada, con franjas que cruzan la medianoche). Otra forma de cobrar sería otra implementación (principio O).
  - **Chain of Responsibility:** `PasoCobro` y sus pasos `VerificarCobroDelDiaPaso` → `AplicarTarifaPaso` → `VerificarTarifaCeroPaso`, armados en `CadenaCobroConfig`.
  - **Puertos:** `TarifasVigentes` y `RegistroCobros`, con adaptadores JDBC en `cobro/infraestructura/`. El cobro guarda el valor y el id de la tarifa (RN-07). La base impide dos cobros por placa, sede y día (`uq_cobro_placa_sede_dia`).
  - `IngresoRegistrado` ahora incluye `eventoIngresoId`, que el cobro referencia.

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
- **Rutas:** `/login`, `/entrada`, `/salida`, `/administracion` y `/consultas`. `/login`, `/entrada` y `/salida` tienen interfaz; las demás son marcadores (`components/Pagina`) pendientes de cada historia.
- **Componentes compartidos según los mockups:** `PlacaVisual` (placa amarilla), `BannerResultado` (verde, rojo o amarillo), `IndicadorBarrera`. Están pensados para que los usen las demás pantallas.
- **Vista de la sede para el celular (TT-09):** `pages/Simulador` en `/simulador/salida` y `/simulador/entrada` (`/simulador` lleva a la salida). Tema oscuro y pantalla de celular, según el mockup: `components/EscenaSede` dibuja la vía, el vehículo con su placa y la barrera (se levanta y se pone verde cuando abre), un banner "ABRE · motivo" o "NO ABRE · motivo", y el formulario con tipo de vehículo, placa y confianza. Llama a `/api/salida` o `/api/entrada` y adapta cada respuesta (la salida responde `abre`; la entrada, `resultado`). Requiere sesión, pero no muestra la barra superior. Desde el celular se abre con la IP del computador: `http://IP-DEL-PC:5173/simulador/salida`; al iniciar sesión vuelve a la pantalla que se pidió.
- **`pages/Entrada`:** carril de entrada (`components/CarrilEntrada`, consulta `/api/entrada/ultimo` cada 2 segundos) y, a la derecha, `components/SimuladorLectura`, un formulario de placa y confianza que envía la lectura a la ruta que recibe (sirve también para la salida). El banner amarillo (`.banner-aviso`) marca las lecturas que requieren validación manual.
- **`pages/Salida`:** carril de salida (`components/CarrilSalida`, consulta `/api/salida/ultima` cada 2 segundos) y, a la derecha, el espacio de la caja que implementa HU-03.
- **`pages/Login`:** inicio de sesión real contra `/api/auth/login`, según el mockup (mensaje de error genérico).
- **`auth/`:** `AuthProvider` guarda el usuario de la sesión (la recupera con `/api/auth/yo` al abrir la app) y ofrece `iniciarSesion` y `cerrarSesion`; `useAuth` lo expone a los componentes.
- **`components/RutaProtegida`:** sin sesión lleva a `/login`; con rol insuficiente (por ejemplo, un operador en `/administracion`) vuelve a `/entrada`. **`components/Navegacion`** solo muestra Administración al administrador, el usuario y su rol, y "Cerrar sesión".
- **`services/api.js`:** cliente de la API (`get` y `post`) que envía la cookie de sesión y lanza `ErrorApi` con el mensaje del servidor. La URL base sale de `VITE_API_URL`.
- **`estilos.css`:** estilos globales según los mockups del equipo.
- **Dos modos de ejecución:**
  - Desarrollo (`npm run dev`): `VITE_API_URL=http://localhost:8080`.
  - Docker: la API se llama por ruta relativa (`/api`) y Nginx la reenvía al backend. Así funciona igual desde el portátil y desde el celular del simulador.

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

Para apagar: `docker compose down` (con `-v` también borra los datos de la base).

**Modo desarrollo (día a día, con recarga rápida)**

```powershell
docker compose up -d db          # solo la base de datos

cd backend
$env:ADMIN_CONTRASENA = "..."
$env:OPERADOR_CONTRASENA = "..."
.\mvnw spring-boot:run

cd ..\frontend
npm install
npm run dev
```

No corras `npm run dev` y el contenedor `frontend` a la vez: usan el mismo puerto (5173).

## Pendientes técnicos conocidos

- Falta el pipeline de integración continua que corre las pruebas en cada pull request (TT-06).
- Las pruebas unitarias cubren HU-01, HU-04 y HU-05; `ParqueaderoApplicationTests` (arranque completo) necesita la base de datos levantada y el `.env` cargado en la terminal.
- Las historias HU-02, HU-03, HU-06 a HU-09 y la vista de la sede (TT-09) no están implementadas.
- Una migración ya aplicada no se edita: los cambios al esquema van en `V3__...` en adelante.