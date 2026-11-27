const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';
const SIN_CONEXION = 'No se pudo conectar con el servidor';
const ESTADOS_NO_DISPONIBLE = [502, 503, 504];

/** Error de la API con su código HTTP, o sin código cuando no llegó una respuesta. */
export class ErrorApi extends Error {
  constructor(estado, mensaje, opciones) {
    super(mensaje, opciones);
    this.estado = estado;
  }
}

let alSesionExpirada = () => {};

/** Registra qué hacer cuando la API responde 401 con una sesión que ya estaba abierta. */
export function registrarSesionExpirada(funcion) {
  alSesionExpirada = funcion;
}

async function pedir(ruta, opciones = {}) {
  let respuesta;
  try {
    respuesta = await fetch(`${API_URL}${ruta}`, {
      headers: { 'Content-Type': 'application/json' },
      // Envía la cookie de sesión que crea el login
      credentials: 'include',
      ...opciones,
    });
  } catch (causa) {
    throw new ErrorApi(null, SIN_CONEXION, { cause: causa });
  }
  if (!respuesta.ok) {
    let mensaje = ESTADOS_NO_DISPONIBLE.includes(respuesta.status)
      ? SIN_CONEXION
      : `Error ${respuesta.status}`;
    try {
      const cuerpo = await respuesta.json();
      if (cuerpo.mensaje) mensaje = cuerpo.mensaje;
    } catch {
      // La respuesta de error no traía JSON: se deja el mensaje por defecto
    }
    if (respuesta.status === 401 && !ruta.startsWith('/api/auth/')) {
      alSesionExpirada();
    }
    throw new ErrorApi(respuesta.status, mensaje);
  }
  return respuesta.status === 204 ? null : respuesta.json();
}

export const get = (ruta) => pedir(ruta);
export const post = (ruta, cuerpo) =>
  pedir(ruta, { method: 'POST', body: cuerpo ? JSON.stringify(cuerpo) : undefined });
