import { useEffect, useState } from 'react';
import { Card } from 'react-bootstrap';
import { get } from '../services/api.js';
import PlacaVisual from './PlacaVisual.jsx';
import BannerResultado from './BannerResultado.jsx';
import IndicadorBarrera from './IndicadorBarrera.jsx';

const INTERVALO_MS = 2000;

function Fila({ etiqueta, valor }) {
  return (
    <div className="d-flex justify-content-between py-1">
      <span className="text-secondary">{etiqueta}</span>
      <span className="fw-semibold">{valor}</span>
    </div>
  );
}

/**
 * Carril de salida (HU-04). Muestra el último intento de salida, que llega desde el
 * simulador o la lectura de una imagen: ABRE o NO ABRE, el motivo y la barrera.
 */
export default function CarrilSalida() {
  const [lectura, setLectura] = useState(null);

  useEffect(() => {
    let activo = true;
    const consultar = () =>
      get('/api/salida/ultima')
        .then((resultado) => activo && setLectura(resultado))
        .catch(() => {});
    consultar();
    const temporizador = setInterval(consultar, INTERVALO_MS);
    return () => {
      activo = false;
      clearInterval(temporizador);
    };
  }, []);

  return (
    <Card>
      <Card.Body>
        <Card.Title className="h6 fw-bold mb-3">Carril de salida</Card.Title>
        {!lectura ? (
          <p className="text-secondary mb-0">Esperando una lectura de placa…</p>
        ) : (
          <>
            <div className="mb-3">
              <PlacaVisual placa={lectura.placa} invalida={!lectura.tipoVehiculo} />
            </div>
            <Fila etiqueta="Tipo de vehículo" valor={lectura.tipoVehiculo ?? '—'} />
            <Fila etiqueta="Confianza" valor={`${Math.round(lectura.confianza * 100)} %`} />
            <Fila
              etiqueta="Hora"
              valor={new Date(lectura.ocurridoEn).toLocaleTimeString('es-CO', {
                hour: 'numeric',
                minute: '2-digit',
              })}
            />
            <div className="my-3">
              <BannerResultado tipo={lectura.abre ? 'exito' : 'error'}>
                {lectura.abre ? 'ABRE' : 'NO ABRE'}
              </BannerResultado>
            </div>
            <p className={`fw-semibold ${lectura.abre ? 'text-success' : 'text-danger'}`}>
              Motivo: {lectura.motivo.charAt(0).toLowerCase() + lectura.motivo.slice(1)}
            </p>
            <IndicadorBarrera abierta={lectura.barrera === 'ABIERTA'} />
          </>
        )}
      </Card.Body>
    </Card>
  );
}
