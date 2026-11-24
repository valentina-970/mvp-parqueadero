import { useEffect, useState } from 'react';
import { get } from '../services/api.js';
import { formatearPesos } from '../utils/formato.js';
import EstadoBadge from './EstadoBadge.jsx';

/**
 * Muestra el cobro del día que generó un ingreso (HU-02), como en el mockup de la entrada.
 * Lo consulta en la caja (HU-03): el carril de entrada no conoce los cobros, así que el
 * patrón Observer entre HU-01 y HU-02 queda intacto. Un 404 significa que no hubo cobro,
 * por ejemplo una moto con tarifa $0.
 */
export default function CobroGenerado({ placa }) {
  // undefined: consultando; null: sin cobro; objeto: el cobro del día
  const [cobro, setCobro] = useState(undefined);
  const [error, setError] = useState(false);

  useEffect(() => {
    let activo = true;
    get(`/api/caja/cobros/${encodeURIComponent(placa)}`)
      .then((respuesta) => activo && setCobro(respuesta))
      .catch((e) => {
        if (!activo) return;
        if (e.estado === 404) setCobro(null);
        else setError(true);
      });
    return () => {
      activo = false;
    };
  }, [placa]);

  let valor;
  if (error) valor = 'No se pudo consultar';
  else if (cobro === undefined) valor = 'Consultando…';
  else if (cobro === null) valor = 'Sin cobro (tarifa $0)';
  else
    valor = (
      <>
        {formatearPesos(cobro.valor)} · <EstadoBadge estado={cobro.estado} />
      </>
    );

  return (
    <div className="d-flex justify-content-between align-items-center py-1">
      <span className="text-secondary">Cobro generado</span>
      <span className="fw-semibold">{valor}</span>
    </div>
  );
}