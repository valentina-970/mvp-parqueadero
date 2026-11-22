const ZONA = 'America/Bogota';

/** Da formato de pesos colombianos sin decimales: 5000 se muestra como "$5.000". */
export function formatearPesos(valor) {
  const numero = Math.round(Number(valor) || 0);
  return `$${numero.toLocaleString('es-CO')}`;
}

/** Convierte una fecha "2026-11-14" (día operativo) en "14 nov 2026". */
export function formatearDia(fechaIso) {
  const [anio, mes, dia] = fechaIso.split('-').map(Number);
  return new Date(anio, mes - 1, dia)
    .toLocaleDateString('es-CO', { day: 'numeric', month: 'short', year: 'numeric' })
    .replace('.', '').replace(/ de /g, ' ');
}

/** Convierte un instante en "15 nov 2026 · 5:59 a. m." con la hora de Colombia. */
export function formatearFechaHora(instanteIso) {
  const fecha = new Date(instanteIso);
  const dia = fecha
    .toLocaleDateString('es-CO', { day: 'numeric', month: 'short', year: 'numeric', timeZone: ZONA })
    .replace('.', '').replace(/ de /g, ' ');
  const hora = fecha.toLocaleTimeString('es-CO', { hour: 'numeric', minute: '2-digit', timeZone: ZONA });
  return `${dia} · ${hora}`;
}

/** Deja solo los dígitos de lo que escribe el operador: "$10.000" se vuelve 10000. */
export function soloDigitos(texto) {
  return Number(String(texto).replace(/\D/g, '')) || 0;
}
