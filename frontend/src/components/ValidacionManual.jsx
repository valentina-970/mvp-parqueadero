import { useEffect, useState } from 'react';
import { Card, Form, Button, Alert, Badge } from 'react-bootstrap';
import { get, post } from '../services/api.js';
import PlacaVisual from './PlacaVisual.jsx';

const INTERVALO_MS = 2000;

/**
 * Una lectura pendiente: muestra la placa leída y su confianza (CA-3) y deja confirmarla o
 * corregirla (CA-4). Si el operador no cambia la placa, la confirma; si la cambia, la corrige.
 */
function LecturaPorValidar({ pendiente, alValidar }) {
  const [placa, setPlaca] = useState(pendiente.placa);
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

  const corrige = placa.trim().toUpperCase() !== pendiente.placa;

  const validar = async (evento) => {
    evento.preventDefault();
    setError('');
    setEnviando(true);
    try {
      const resultado = await post(`/api/validaciones/${pendiente.eventoId}`, { placa });
      alValidar(resultado);
    } catch (e) {
      // 422: la placa digitada no tiene formato válido (CA-5); 404: otro operador ya la validó
      setError(e.message);
    } finally {
      setEnviando(false);
    }
  };

  return (
    <Form onSubmit={validar} className="border rounded p-3 mb-3">
      <div className="d-flex align-items-center gap-3 mb-2">
        <PlacaVisual placa={pendiente.placa} pequena />
        <Badge bg="warning" text="dark">
          Confianza {Math.round(pendiente.confianza * 100)} %
        </Badge>
        <span className="small text-secondary ms-auto">
          {new Date(pendiente.ocurridoEn).toLocaleTimeString('es-CO', { hour: 'numeric', minute: '2-digit' })}
        </span>
      </div>
      <p className="small text-secondary mb-2">
        Leída como «{pendiente.placaLeida}». Sin imagen de la cámara.
      </p>
      <div className="d-flex gap-2">
        <Form.Control
          value={placa}
          onChange={(e) => setPlaca(e.target.value)}
          aria-label="Placa validada"
          maxLength={20}
          required
        />
        <Button type="submit" variant={corrige ? 'warning' : 'primary'} disabled={enviando} className="text-nowrap">
          {enviando ? 'Validando…' : corrige ? 'Corregir y validar' : 'Confirmar'}
        </Button>
      </div>
      {error && <Alert variant="danger" className="py-2 small mt-2 mb-0">{error}</Alert>}
    </Form>
  );
}

/**
 * Validación manual de lecturas dudosas (HU-06) para un carril (`ENTRADA` o `SALIDA`, CA-7).
 * Consulta las lecturas pendientes cada 2 segundos; si no hay ninguna, no muestra nada.
 * `alValidar` avisa a la página para que el carril muestre de inmediato el resultado.
 */
export default function ValidacionManual({ carril, version, alValidar }) {
  const [pendientes, setPendientes] = useState([]);
  const [mensaje, setMensaje] = useState('');
  const [consultas, setConsultas] = useState(0);

  useEffect(() => {
    let activo = true;
    const consultar = () =>
      get(`/api/validaciones?carril=${carril}`)
        .then((lista) => activo && setPendientes(lista))
        .catch(() => {});
    consultar();
    const temporizador = setInterval(consultar, INTERVALO_MS);
    return () => {
      activo = false;
      clearInterval(temporizador);
    };
  }, [carril, version, consultas]);

  const validada = (resultado) => {
    setMensaje(`${resultado.placa} · ${resultado.motivo}`);
    setConsultas((c) => c + 1);
    alValidar?.();
  };

  if (pendientes.length === 0 && !mensaje) return null;

  return (
    <Card className="border-warning">
      <Card.Body>
        <Card.Title className="h6 fw-bold mb-3">
          Validación manual {pendientes.length > 0 && <Badge bg="warning" text="dark">{pendientes.length}</Badge>}
        </Card.Title>
        {mensaje && (
          <Alert variant="success" className="py-2 small" dismissible onClose={() => setMensaje('')}>
            Validada: {mensaje}
          </Alert>
        )}
        {pendientes.length === 0 ? (
          <p className="text-secondary small mb-0">No hay lecturas pendientes.</p>
        ) : (
          pendientes.map((pendiente) => (
            <LecturaPorValidar key={pendiente.eventoId} pendiente={pendiente} alValidar={validada} />
          ))
        )}
      </Card.Body>
    </Card>
  );
}