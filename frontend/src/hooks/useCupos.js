import { useEffect, useState } from 'react';
import { get } from '../services/api.js';

const INTERVALO_MS = 3000;

/**
 * Cupos por tipo de vehículo (HU-07, CA-6). Se consultan al abrir la pantalla, cada 3 segundos
 * y cada vez que cambia `version` (por ejemplo, después de enviar una lectura).
 */
export function useCupos(version = 0) {
  const [cupos, setCupos] = useState([]);

  useEffect(() => {
    let activo = true;
    const cargar = () =>
      get('/api/cupos')
        .then((datos) => activo && setCupos(datos))
        .catch(() => {});
    cargar();
    const temporizador = setInterval(cargar, INTERVALO_MS);
    return () => {
      activo = false;
      clearInterval(temporizador);
    };
  }, [version]);

  return cupos;
}
