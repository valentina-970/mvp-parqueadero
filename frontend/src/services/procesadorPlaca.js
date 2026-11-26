/**
 * Procesamiento de la foto antes del OCR (TT-11). Son funciones puras sobre una imagen
 * { data, width, height } con píxeles RGBA, el mismo formato de ImageData del navegador, así se
 * pueden probar sin navegador. El lector (lectorCamara.js) las usa en este orden:
 * localizarPlaca → tieneReflejo → aislarCaracteres.
 */

const BRILLO_REFLEJO = 185;
const SENSIBILIDAD = 0.75;

/** Amarillo de placa, también con luz tenue o cálida, cuando se ve casi naranja. */
function esAmarillo(r, g, b) {
  return r > 100 && g > 50 && g > r * 0.4 && b < g * 0.65 && r - b > 60 && r - g < 90;
}

function esBlanco(r, g, b) {
  return Math.min(r, g, b) > BRILLO_REFLEJO;
}

/**
 * Agrupa los píxeles marcados que se tocan (componentes conexas).
 *
 * @return las manchas con su caja { x0, y0, x1, y1, w, h }, su cantidad de píxeles n y las
 *         etiquetas de cada píxel (-1 si no está marcado)
 */
function buscarManchas(marcado, width, height) {
  const etiquetas = new Int32Array(width * height).fill(-1);
  const manchas = [];
  const pila = [];
  for (let inicio = 0; inicio < width * height; inicio++) {
    if (!marcado[inicio] || etiquetas[inicio] >= 0) continue;
    const mancha = { id: manchas.length, x0: width, y0: height, x1: 0, y1: 0, n: 0 };
    etiquetas[inicio] = mancha.id;
    pila.push(inicio);
    while (pila.length) {
      const p = pila.pop();
      const x = p % width;
      const y = (p - x) / width;
      mancha.n++;
      mancha.x0 = Math.min(mancha.x0, x);
      mancha.x1 = Math.max(mancha.x1, x);
      mancha.y0 = Math.min(mancha.y0, y);
      mancha.y1 = Math.max(mancha.y1, y);
      const vecinos = [
        x > 0 && p - 1,
        x < width - 1 && p + 1,
        y > 0 && p - width,
        y < height - 1 && p + width,
      ];
      for (const q of vecinos) {
        if (q !== false && marcado[q] && etiquetas[q] < 0) {
          etiquetas[q] = mancha.id;
          pila.push(q);
        }
      }
    }
    mancha.w = mancha.x1 - mancha.x0 + 1;
    mancha.h = mancha.y1 - mancha.y0 + 1;
    manchas.push(mancha);
  }
  return { manchas, etiquetas };
}

function mediana(valores) {
  const ordenados = [...valores].sort((a, b) => a - b);
  return ordenados[Math.floor(ordenados.length / 2)];
}

/**
 * Busca la placa: la zona amarilla (o blanca, si el flash la aclaró) más grande con forma de
 * rectángulo apaisado. Conviene pasarle la foto reducida (unos 800 px de ancho), es más rápido.
 *
 * @return la caja { x, y, w, h } en píxeles de esa imagen, o null si no hay placa
 */
export function localizarPlaca({ data, width, height }) {
  const marcado = new Uint8Array(width * height);
  for (let i = 0; i < width * height; i++) {
    const [r, g, b] = [data[i * 4], data[i * 4 + 1], data[i * 4 + 2]];
    marcado[i] = esAmarillo(r, g, b) || esBlanco(r, g, b) ? 1 : 0;
  }
  const { manchas } = buscarManchas(marcado, width, height);
  const placas = manchas.filter((m) => {
    const proporcion = m.w / m.h;
    return proporcion > 1.3 && proporcion < 4.5 && m.w > width * 0.05 && m.n > m.w * m.h * 0.45;
  });
  if (placas.length === 0) return null;
  const placa = placas.reduce((mayor, m) => (m.n > mayor.n ? m : mayor));
  return { x: placa.x0, y: placa.y0, w: placa.w, h: placa.h };
}

/**
 * Con flash la placa refleja la luz: el fondo amarillo se ve blanco y las letras se borran.
 *
 * @return true si dentro de la caja hay más blanco que amarillo
 */
export function tieneReflejo({ data, width }, caja) {
  let amarillos = 0;
  let blancos = 0;
  for (let y = caja.y; y < caja.y + caja.h; y++) {
    for (let x = caja.x; x < caja.x + caja.w; x++) {
      const i = (y * width + x) * 4;
      if (esAmarillo(data[i], data[i + 1], data[i + 2])) amarillos++;
      else if (esBlanco(data[i], data[i + 1], data[i + 2])) blancos++;
    }
  }
  return blancos > amarillos;
}

/** Marca lo oscuro comparando cada píxel con el promedio de su vecindad (umbral de Bradley). */
function marcarOscuros(brillo, width, height) {
  const ancho = width + 1;
  const integral = new Float64Array(ancho * (height + 1));
  for (let y = 0; y < height; y++) {
    let fila = 0;
    for (let x = 0; x < width; x++) {
      fila += brillo[y * width + x];
      integral[(y + 1) * ancho + x + 1] = integral[y * ancho + x + 1] + fila;
    }
  }
  const radio = Math.max(8, Math.round(height / 4));
  const oscuro = new Uint8Array(width * height);
  for (let y = 0; y < height; y++) {
    const ya = Math.max(0, y - radio);
    const yb = Math.min(height, y + radio + 1);
    for (let x = 0; x < width; x++) {
      const xa = Math.max(0, x - radio);
      const xb = Math.min(width, x + radio + 1);
      const suma =
        integral[yb * ancho + xb] - integral[ya * ancho + xb] - integral[yb * ancho + xa] + integral[ya * ancho + xa];
      const promedio = suma / ((xb - xa) * (yb - ya));
      oscuro[y * width + x] = brillo[y * width + x] < promedio * SENSIBILIDAD ? 1 : 0;
    }
  }
  return oscuro;
}

/** Borra las líneas oscuras largas (el borde de la placa) para que no se peguen a las letras. */
function borrarBordes(oscuro, width, height) {
  const borrar = new Uint8Array(width * height);
  const recorrer = (largo, total, indice, limite) => {
    for (let fijo = 0; fijo < total; fijo++) {
      let inicio = 0;
      for (let k = 0; k <= largo; k++) {
        if (k < largo && oscuro[indice(fijo, k)]) continue;
        if (k - inicio > limite) for (let j = inicio; j < k; j++) borrar[indice(fijo, j)] = 1;
        inicio = k + 1;
      }
    }
  };
  recorrer(width, height, (y, x) => y * width + x, width * 0.3);
  recorrer(height, width, (x, y) => y * width + x, height * 0.75);
  for (let i = 0; i < oscuro.length; i++) if (borrar[i]) oscuro[i] = 0;
}

/**
 * Deja solo los caracteres grandes de la placa, en negro sobre blanco: quita la ciudad, los
 * tornillos, el borde y el sello del centro. Recibe el recorte de la placa.
 *
 * @return { data, width, height, alturaCaracter }, o null si no encontró al menos 4 caracteres
 */
export function aislarCaracteres({ data, width, height }) {
  // El canal más claro de cada píxel: el amarillo queda claro aunque esté en sombra
  const brillo = new Uint8Array(width * height);
  for (let i = 0; i < width * height; i++) {
    brillo[i] = Math.max(data[i * 4], data[i * 4 + 1], data[i * 4 + 2]);
  }
  const oscuro = marcarOscuros(brillo, width, height);
  borrarBordes(oscuro, width, height);
  const { manchas, etiquetas } = buscarManchas(oscuro, width, height);

  let caracteres = manchas.filter(
    (m) => m.h > height * 0.2 && m.h < height * 0.95 && m.w < width * 0.3 && m.w < m.h * 1.5
      && m.n > m.w * m.h * 0.1 && m.n < m.w * m.h * 0.85,
  );
  if (caracteres.length < 4) return null;
  // Los caracteres de la placa tienen la misma altura y están en la misma línea
  const altura = mediana(caracteres.map((m) => m.h));
  caracteres = caracteres.filter((m) => Math.abs(m.h - altura) < altura * 0.3);
  const centro = mediana(caracteres.map((m) => (m.y0 + m.y1) / 2));
  caracteres = caracteres.filter((m) => Math.abs((m.y0 + m.y1) / 2 - centro) < altura * 0.35);
  if (caracteres.length < 4) return null;

  const conservar = new Set(caracteres.map((m) => m.id));
  const x0 = Math.min(...caracteres.map((m) => m.x0));
  const y0 = Math.min(...caracteres.map((m) => m.y0));
  const margen = Math.round(altura * 0.3);
  const ancho = Math.max(...caracteres.map((m) => m.x1)) - x0 + 1 + 2 * margen;
  const alto = Math.max(...caracteres.map((m) => m.y1)) - y0 + 1 + 2 * margen;
  const salida = new Uint8ClampedArray(ancho * alto * 4).fill(255);
  for (let y = 0; y < alto - 2 * margen; y++) {
    for (let x = 0; x < ancho - 2 * margen; x++) {
      if (!conservar.has(etiquetas[(y + y0) * width + x + x0])) continue;
      const o = ((y + margen) * ancho + x + margen) * 4;
      salida[o] = 0;
      salida[o + 1] = 0;
      salida[o + 2] = 0;
    }
  }
  return { data: salida, width: ancho, height: alto, alturaCaracter: altura };
}
