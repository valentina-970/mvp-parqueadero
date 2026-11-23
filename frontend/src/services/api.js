const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

/** Error de la API con su código HTTP y el mensaje que envió el servidor. */
export class ErrorApi extends Error {
  constructor(estado, mensaje) {
    super(mensaje);
    this.estado = estado;
  }
}

let alSesionExpirada = () => {};

/** Registra qué hacer cuando la API responde 401 con una sesión que ya estaba abierta. */
export function registrarSesionExpirada(funcion) {
  alSesionExpirada = funcion;
}

async function pedir(ruta, opciones = {}) {
  const respuesta = await fetch(`${API_URL}${ruta}`, {
    headers: { 'Content-Type': 'application/json' },
    // Envía la cookie de sesión que crea el login
    credentials: 'include',
    ...opciones,
  });
  if (!respuesta.ok) {
    let mensaje = `Error ${respuesta.status}`;
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
