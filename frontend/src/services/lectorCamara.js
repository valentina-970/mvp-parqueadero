import { createWorker, PSM } from 'tesseract.js';
import { aislarCaracteres, localizarPlaca, tieneReflejo } from './procesadorPlaca.js';

/**
 * Lector de placas con la cámara del celular (TT-11). Es un adaptador (patrón Adapter): recibe
 * la foto, la lee con Tesseract.js en el propio navegador y la convierte al formato que
 * esperan los carriles, { placa, confianza } con la confianza entre 0 y 1. Para el backend es
 * un lector más, igual que el simulador (principio L).
 */

const ANCHO_MAXIMO = 1600;
const ANCHO_BUSQUEDA = 800;
const ALTURA_LETRA_OCR = 70;
const PATRON_PLACA = /^[A-Z]{3}[0-9]{2}[A-Z0-9]$/;

// Confusiones típicas del OCR: en las tres primeras posiciones solo van letras y en la
// cuarta y la quinta solo números
const A_LETRA = { 0: 'O', 1: 'I', 2: 'Z', 5: 'S', 6: 'G', 8: 'B' };
const A_NUMERO = { O: '0', Q: '0', D: '0', I: '1', L: '1', Z: '2', S: '5', G: '6', B: '8' };
// En el último carácter el OCR no distingue estos pares; los demás sí los lee bien
const AMBIGUOS = { 1: 'I', I: '1', 0: 'O', O: '0' };

let trabajador = null;

/**
 * Crea el lector de Tesseract una sola vez; la primera vez descarga sus archivos. Lee una
 * sola línea, porque le llegan solo los caracteres de la placa. No se le pone lista de
 * caracteres permitidos: con ella Tesseract deja la confianza en 0.
 */
function obtenerTrabajador() {
  if (!trabajador) {
    trabajador = createWorker('eng').then(async (nuevo) => {
      await nuevo.setParameters({ tessedit_pageseg_mode: PSM.SINGLE_LINE });
      return nuevo;
    });
  }
  return trabajador;
}

function crearLienzo(ancho, alto) {
  const lienzo = document.createElement('canvas');
  lienzo.width = Math.round(ancho);
  lienzo.height = Math.round(alto);
  return lienzo;
}

/** Dibuja la imagen en un lienzo del ancho pedido, conservando la proporción. */
function aAncho(imagen, ancho) {
  const lienzo = crearLienzo(ancho, (imagen.height * ancho) / imagen.width);
  lienzo.getContext('2d').drawImage(imagen, 0, 0, lienzo.width, lienzo.height);
  return lienzo;
}

function pixeles(lienzo, x = 0, y = 0, ancho = lienzo.width, alto = lienzo.height) {
  return lienzo.getContext('2d', { willReadFrequently: true }).getImageData(x, y, ancho, alto);
}

/** Pasa los caracteres aislados a un lienzo, escalados al tamaño de letra que mejor lee Tesseract. */
function aLienzo(caracteres) {
  const original = crearLienzo(caracteres.width, caracteres.height);
  const imagen = new ImageData(caracteres.data, caracteres.width, caracteres.height);
  original.getContext('2d').putImageData(imagen, 0, 0);
  const escala = ALTURA_LETRA_OCR / caracteres.alturaCaracter;
  return aAncho(original, caracteres.width * escala);
}

function corregir(candidato) {
  const letras = [...candidato.slice(0, 3)].map((c) => A_LETRA[c] ?? c).join('');
  const numeros = [...candidato.slice(3, 5)].map((c) => A_NUMERO[c] ?? c).join('');
  return letras + numeros + candidato[5];
}

/**
 * El último carácter es número en los carros (AAA123) y letra en las motos (AAA12A). Si el OCR
 * leyó uno de los pares que confunde (1 e I, 0 y O), decide el tipo elegido en el carril.
 */
function ajustarUltimo(placa, tipo) {
  const ultimo = placa[5];
  const cambio = AMBIGUOS[ultimo];
  const debeSerLetra = tipo === 'MOTO';
  if (!tipo || !cambio || /[A-Z]/.test(ultimo) === debeSerLetra) return placa;
  return placa.slice(0, 5) + cambio;
}

/**
 * Busca una placa (AAA123 o AAA12A) en el texto que leyó el OCR. Primero intenta sin
 * corregir nada; si no encuentra, corrige las confusiones típicas, pero solo en trozos
 * que tengan algún número, para no convertir el nombre de la ciudad en una placa.
 *
 * @param tipo CARRO o MOTO; resuelve el último carácter cuando es ambiguo (opcional)
 * @return la placa, o null si no hay ninguna
 */
export function interpretarTexto(texto, tipo) {
  const lineas = texto
    .toUpperCase()
    .split('\n')
    .map((linea) => linea.replace(/[^A-Z0-9]/g, ''));
  const trozos = lineas.flatMap((linea) =>
    Array.from({ length: Math.max(0, linea.length - 5) }, (_, i) => linea.slice(i, i + 6)),
  );
  const placa =
    trozos.find((trozo) => PATRON_PLACA.test(trozo)) ??
    trozos
      .filter((trozo) => /[0-9]/.test(trozo))
      .map(corregir)
      .find((trozo) => PATRON_PLACA.test(trozo));
  return placa ? ajustarUltimo(placa, tipo) : null;
}

/**
 * Confianza de la línea donde está la placa, entre 0 y 1. Si no la encuentra, usa la
 * confianza promedio de toda la foto.
 */
export function calcularConfianza(data, placa, tipo) {
  const lineas = (data.blocks ?? []).flatMap((bloque) =>
    bloque.paragraphs.flatMap((parrafo) => parrafo.lines),
  );
  const linea = lineas.find((candidata) => interpretarTexto(candidata.text, tipo) === placa);
  const valor = linea ? linea.confidence : data.confidence;
  return Math.round(Math.min(100, Math.max(0, valor))) / 100;
}

/**
 * Lee la placa de una foto: la busca, revisa que no tenga reflejo, aísla sus caracteres y los
 * pasa por el OCR.
 *
 * @param tipo CARRO o MOTO, el tipo de vehículo elegido en el carril
 * @return { placa, confianza } con la confianza entre 0 y 1, o { motivo } si no se pudo leer:
 *         SIN_PLACA o REFLEJO
 */
export async function leerPlaca(archivo, tipo) {
  const [mapa, lector] = await Promise.all([createImageBitmap(archivo), obtenerTrabajador()]);
  const foto = aAncho(mapa, Math.min(ANCHO_MAXIMO, mapa.width));
  const pequena = pixeles(aAncho(foto, ANCHO_BUSQUEDA));
  const caja = localizarPlaca(pequena);
  if (!caja) return { motivo: 'SIN_PLACA' };
  if (tieneReflejo(pequena, caja)) return { motivo: 'REFLEJO' };

  const escala = foto.width / ANCHO_BUSQUEDA;
  const x = Math.round(caja.x * escala);
  const y = Math.round(caja.y * escala);
  const ancho = Math.min(foto.width - x, Math.round(caja.w * escala));
  const alto = Math.min(foto.height - y, Math.round(caja.h * escala));
  const caracteres = aislarCaracteres(pixeles(foto, x, y, ancho, alto));
  if (!caracteres) return { motivo: 'SIN_PLACA' };

  const { data } = await lector.recognize(aLienzo(caracteres), {}, { text: true, blocks: true });
  const placa = interpretarTexto(data.text, tipo);
  if (!placa) return { motivo: 'SIN_PLACA' };
  return { placa, confianza: calcularConfianza(data, placa, tipo) };
}
