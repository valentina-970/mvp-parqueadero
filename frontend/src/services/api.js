const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

async function pedir(ruta, opciones = {}) {
  const respuesta = await fetch(`${API_URL}${ruta}`, {
    headers: { 'Content-Type': 'application/json' },
    ...opciones,
  });
  if (!respuesta.ok) {
    throw new Error(`Error ${respuesta.status}`);
  }
  return respuesta.json();
}

export const get = (ruta) => pedir(ruta);
export const post = (ruta, cuerpo) =>
  pedir(ruta, { method: 'POST', body: JSON.stringify(cuerpo) });