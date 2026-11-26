import { useEffect, useState } from 'react';
import { Card } from 'react-bootstrap';
import { get } from '../services/api.js';
import PlacaVisual from './PlacaVisual.jsx';
import BannerResultado from './BannerResultado.jsx';
import IndicadorBarrera from './IndicadorBarrera.jsx';
import CobroGenerado from './CobroGenerado.jsx';

const INTERVALO_MS = 2000;

/** Texto y color del banner según el resultado de la lectura. */
const BANNERS = {
  PERMITIDO: { tipo: 'exito', texto: 'INGRESO REGISTRADO' },
  PENDIENTE_VALIDACION: { tipo: 'aviso', texto: 'VALIDACIÓN MANUAL' },
  RECHAZADO: { tipo: 'error', texto: 'INGRESO RECHAZADO' },
};

/** Un ingreso rechazado por falta de cupo se muestra como LLENO (HU-07). */
const BANNER_LLENO = { tipo: 'error', texto: 'LLENO · Barrera cerrada' };

function Fila({ etiqueta, valor }) {
  return (
    <div className="d-flex justify-content-between py-1">
      <span className="text-secondary">{etiqueta}</span>
      <span className="fw-semibold">{valor}</span>
    </div>
  );
}

/**
 * Carril de entrada (HU-01). Muestra la última lectura, que llega desde el simulador:
 * la placa, el tipo de vehículo y si el ingreso quedó registrado. `version` cambia
 * cada vez que se envía una lectura, para consultar de inmediato sin esperar.
 */
export default function CarrilEntrada({ version }) {
  const [lectura, setLectura] = useState(null);

  useEffect(() => {
    let activo = true;
    const consultar = () =>
      get('/api/entrada/ultimo')
        .then((resultado) => activo && setLectura(resultado))
        .catch(() => {});
    consultar();
    const temporizador = setInterval(consultar, INTERVALO_MS);
    return () => {
      activo = false;
      clearInterval(temporizador);
    };
  }, [version]);

  const banner = lectura && (lectura.lleno ? BANNER_LLENO : BANNERS[lectura.resultado]);

  return (
    <Card>
      <Card.Body>
        <Card.Title className="h6 fw-bold mb-3">Última lectura</Card.Title>
        {!lectura ? (
          <p className="text-secondary mb-0">Esperando una lectura de placa…</p>
        ) : (
          <>
            <div className="mb-3">
              <PlacaVisual placa={lectura.placa || '—'} invalida={!lectura.tipoVehiculo} />
            </div>
            <Fila etiqueta="Tipo de vehículo" valor={lectura.tipoVehiculo ?? '—'} />
            <Fila etiqueta="Confianza" valor={`${Math.round(lectura.confianza * 100)} %`} />
            <Fila etiqueta="Lectura" valor={lectura.manual ? 'Manual (operador)' : 'Automática'} />
            <Fila
              etiqueta="Hora"
              valor={new Date(lectura.ocurridoEn).toLocaleTimeString('es-CO', {
                hour: 'numeric',
                minute: '2-digit',
              })}
            />
            {lectura.resultado === 'PERMITIDO' && (
              <CobroGenerado key={lectura.ocurridoEn} placa={lectura.placa} />
            )}
            <div className="my-3">
              <BannerResultado tipo={banner.tipo}>{banner.texto}</BannerResultado>
            </div>
            <p className="fw-semibold">Motivo: {lectura.lleno ? lectura.motivo.replace(/^LLENO:\s*/, '') : lectura.motivo}</p>
            {lectura.lleno && (
              <p className="text-secondary small">
                No se registró el ingreso ni se generó cobro. El intento quedó en el registro de eventos.
              </p>
            )}
            <IndicadorBarrera abierta={lectura.resultado === 'PERMITIDO'} />
          </>
        )}
      </Card.Body>
    </Card>
  );
}