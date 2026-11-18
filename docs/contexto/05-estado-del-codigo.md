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
- **Rutas:** `/login`, `/entrada`, `/salida`, `/administracion` y `/consultas`. Hoy solo `/login` tiene interfaz completa; las demás son marcadores (`components/Pagina`) pendientes de cada historia.
- **`components/Navegacion`:** barra superior con los enlaces a las pantallas.
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
- Las pruebas unitarias cubren por ahora HU-05; `ParqueaderoApplicationTests` (arranque completo) necesita la base de datos levantada.
- Las historias HU-01 a HU-04 y HU-06 a HU-09 y la vista de la sede (TT-09) no están implementadas.
- Una migración ya aplicada no se edita: los cambios al esquema van en `V3__...` en adelante.
