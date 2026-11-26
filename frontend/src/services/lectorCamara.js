import { createWorker, PSM } from 'tesseract.js';

/**
 * Lector de placas con la cámara del celular (TT-11). Es un adaptador (patrón Adapter): recibe
 * la foto, la lee con Tesseract.js en el propio navegador y la convierte al formato que
 * esperan los carriles, { placa, confianza } con la confianza entre 0 y 1. Para el backend es
 * un lector más, igual que el simulador (principio L).
 */

const ANCHO_MAXIMO = 1600;
const PATRON_PLACA = /^[A-Z]{3}[0-9]{2}[A-Z0-9]$/;

// Confusiones típicas del OCR: en las tres primeras posiciones solo van letras y en la
// cuarta y la quinta solo números
const A_LETRA = { 0: 'O', 1: 'I', 2: 'Z', 5: 'S', 6: 'G', 8: 'B' };
const A_NUMERO = { O: '0', Q: '0', D: '0', I: '1', L: '1', Z: '2', S: '5', G: '6', B: '8' };

let trabajador = null;

/**
 * Crea el lector de Tesseract una sola vez; la primera vez descarga sus archivos. No se le
 * pone lista de caracteres permitidos: con ella Tesseract deja la confianza en 0. Los
 * caracteres que sobran los limpia interpretarTexto.
 */
function obtenerTrabajador() {
    if (!trabajador) {
        trabajador = createWorker('eng').then(async (nuevo) => {
            await nuevo.setParameters({ tessedit_pageseg_mode: PSM.SINGLE_BLOCK });
            return nuevo;
        });
    }
    return trabajador;
}

/** Reduce la foto: una de 12 megapíxeles tarda mucho en leerse en un celular. */
async function reducirImagen(archivo) {
    const imagen = await createImageBitmap(archivo);
    const escala = Math.min(1, ANCHO_MAXIMO / imagen.width);
    const lienzo = document.createElement('canvas');
    lienzo.width = Math.round(imagen.width * escala);
    lienzo.height = Math.round(imagen.height * escala);
    lienzo.getContext('2d').drawImage(imagen, 0, 0, lienzo.width, lienzo.height);
    return lienzo;
}

function corregir(candidato) {
    const letras = [...candidato.slice(0, 3)].map((c) => A_LETRA[c] ?? c).join('');
    const numeros = [...candidato.slice(3, 5)].map((c) => A_NUMERO[c] ?? c).join('');
    return letras + numeros + candidato[5];
}

/**
 * Busca una placa (AAA123 o AAA12A) en el texto que leyó el OCR. Primero intenta sin
 * corregir nada; si no encuentra, corrige las confusiones típicas, pero solo en trozos
 * que tengan algún número, para no convertir el nombre de la ciudad en una placa.
 *
 * @return la placa, o null si no hay ninguna
 */
export function interpretarTexto(texto) {
    const lineas = texto
        .toUpperCase()
        .split('\n')
        .map((linea) => linea.replace(/[^A-Z0-9]/g, ''));
    const trozos = lineas.flatMap((linea) =>
        Array.from({ length: Math.max(0, linea.length - 5) }, (_, i) => linea.slice(i, i + 6)),
    );
    const exacto = trozos.find((trozo) => PATRON_PLACA.test(trozo));
    if (exacto) return exacto;
    const corregido = trozos
        .filter((trozo) => /[0-9]/.test(trozo))
        .map(corregir)
        .find((trozo) => PATRON_PLACA.test(trozo));
    return corregido ?? null;
}

/**
 * Confianza de la línea donde está la placa, entre 0 y 1. Si no la encuentra, usa la
 * confianza promedio de toda la foto.
 */
export function calcularConfianza(data, placa) {
    const lineas = (data.blocks ?? []).flatMap((bloque) =>
        bloque.paragraphs.flatMap((parrafo) => parrafo.lines),
    );
    const linea = lineas.find((candidata) => interpretarTexto(candidata.text) === placa);
    const valor = linea ? linea.confidence : data.confidence;
    return Math.round(Math.min(100, Math.max(0, valor))) / 100;
}

/**
 * Lee la placa de una foto.
 *
 * @return { placa, confianza } con la confianza entre 0 y 1, o null si no se encontró placa
 */
export async function leerPlaca(archivo) {
    const [imagen, lector] = await Promise.all([reducirImagen(archivo), obtenerTrabajador()]);
    const { data } = await lector.recognize(imagen, {}, { text: true, blocks: true });
    const placa = interpretarTexto(data.text);
    if (!placa) return null;
    return { placa, confianza: calcularConfianza(data, placa) };
}