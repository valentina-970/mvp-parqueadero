import { useState } from 'react';
import { Card, Form, Button, Alert } from 'react-bootstrap';
import { get, post } from '../services/api.js';
import { formatearDia, formatearFechaHora, formatearPesos, soloDigitos } from '../utils/formato.js';
import BannerResultado from './BannerResultado.jsx';
import EstadoBadge from './EstadoBadge.jsx';

const AVISOS = {
  PAGADO: 'Este cobro ya está pagado.',
  ANULADO: 'El cobro está anulado y no se puede pagar.',
  VENCIDO: 'El cobro está vencido: terminó su día operativo sin pagarse.',
};

function Fila({ etiqueta, children, destacado = false }) {
  return (
    <div className="d-flex justify-content-between align-items-center py-1">
      <span className="text-secondary">{etiqueta}</span>
      <span className={destacado ? 'fw-bold' : 'fw-semibold'}>{children}</span>
    </div>
  );
}

/**
 * Caja del operador (HU-03): busca por placa el cobro del día operativo, recibe el efectivo,
 * muestra el cambio y registra el pago. Si el cobro no se puede pagar, explica el motivo.
 */
export default function CajaPago() {
  const [placa, setPlaca] = useState('');
  const [cobro, setCobro] = useState(null);
  const [recibido, setRecibido] = useState('');
  const [pago, setPago] = useState(null);
  const [error, setError] = useState('');
  const [ocupado, setOcupado] = useState(false);

  const valorRecibido = soloDigitos(recibido);
  const cambio = cobro ? valorRecibido - Number(cobro.valor) : 0;
  const alcanza = cobro && cambio >= 0;

  const nuevoPago = () => {
    setPlaca('');
    setCobro(null);
    setRecibido('');
    setPago(null);
    setError('');
  };

  const buscar = async (evento) => {
    evento.preventDefault();
    setError('');
    setCobro(null);
    setPago(null);
    setOcupado(true);
    try {
      setCobro(await get(`/api/caja/cobros/${encodeURIComponent(placa.trim())}`));
    } catch (e) {
      setError(e.message);
    } finally {
      setOcupado(false);
    }
  };

  const registrar = async () => {
    setError('');
    setOcupado(true);
    try {
      setPago(await post('/api/caja/pagos', { placa: cobro.placa, valorRecibido }));
    } catch (e) {
      setError(e.message);
    } finally {
      setOcupado(false);
    }
  };

  return (
    <Card>
      <Card.Body>
        <Card.Title className="h6 fw-bold mb-3">Caja · Registrar pago</Card.Title>

        {pago ? (
          <>
            <BannerResultado tipo="exito">Pago registrado correctamente</BannerResultado>
            <div className="mt-3">
              <Fila etiqueta="Placa">{pago.placa}</Fila>
              <Fila etiqueta="Valor del cobro">{formatearPesos(pago.valorCobro)}</Fila>
              <Fila etiqueta="Valor recibido">{formatearPesos(pago.valorRecibido)}</Fila>
              <Fila etiqueta="Cambio" destacado>
                <span className="text-success">{formatearPesos(pago.cambio)}</span>
              </Fila>
              <Fila etiqueta="Estado"><EstadoBadge estado={pago.estadoCobro} /></Fila>
              <Fila etiqueta="Pase vigente hasta">{formatearFechaHora(pago.paseVigenteHasta)}</Fila>
              <Fila etiqueta="Recibido por">{pago.recibidoPor}</Fila>
            </div>
            <Button variant="outline-dark" className="w-100 mt-3" onClick={nuevoPago}>Nuevo pago</Button>
          </>
        ) : (
          <>
            <Form onSubmit={buscar} className="d-flex gap-2 align-items-end">
              <Form.Group controlId="caja-placa" className="flex-grow-1">
                <Form.Label className="small mb-1">Placa</Form.Label>
                <Form.Control
                  value={placa}
                  onChange={(e) => setPlaca(e.target.value.toUpperCase())}
                  placeholder="Ej.: ABC123"
                  autoComplete="off"
                  required
                />
              </Form.Group>
              <Button type="submit" variant="outline-dark" disabled={ocupado}>Buscar</Button>
            </Form>

            {error && <Alert variant="warning" className="py-2 small mt-3 mb-0">{error}</Alert>}

            {cobro && (
              <>
                <hr />
                <Fila etiqueta="Día operativo">{formatearDia(cobro.diaOperativo)}</Fila>
                <Fila etiqueta="Franja">
                  {cobro.franja.charAt(0) + cobro.franja.slice(1).toLowerCase()}
                </Fila>
                <Fila etiqueta="Tarifa aplicada">{cobro.tarifaAplicada}</Fila>
                <Fila etiqueta="Valor a pagar">{formatearPesos(cobro.valor)}</Fila>
                <Fila etiqueta="Estado"><EstadoBadge estado={cobro.estado} /></Fila>

                {cobro.estado !== 'PENDIENTE' ? (
                  <Alert variant="secondary" className="py-2 small mt-3 mb-0">{AVISOS[cobro.estado]}</Alert>
                ) : (
                  <>
                    <Form.Group controlId="caja-recibido" className="mt-3">
                      <Form.Label className="small mb-1">Valor recibido</Form.Label>
                      <Form.Control
                        inputMode="numeric"
                        value={recibido === '' ? '' : formatearPesos(valorRecibido)}
                        onChange={(e) => setRecibido(e.target.value)}
                        placeholder="$0"
                        autoComplete="off"
                      />
                    </Form.Group>
                    <div className="d-flex justify-content-between mt-3">
                      <span className="text-secondary">Cambio</span>
                      {alcanza ? (
                        <span className="fw-bold text-success">{formatearPesos(cambio)}</span>
                      ) : (
                        <span className="text-secondary">
                          {valorRecibido > 0 ? `Faltan ${formatearPesos(-cambio)}` : '—'}
                        </span>
                      )}
                    </div>
                    <Button
                      variant="success"
                      className="w-100 mt-3"
                      disabled={!alcanza || ocupado}
                      onClick={registrar}
                    >
                      Registrar pago
                    </Button>
                    <p className="text-secondary small mt-2 mb-0">
                      El pase queda vigente hasta las 5:59 a. m. del día operativo.
                    </p>
                  </>
                )}
              </>
            )}
          </>
        )}
      </Card.Body>
    </Card>
  );
}
