import { useState } from 'react';
import { Card, Form, Button, Alert } from 'react-bootstrap';
import { post } from '../services/api.js';

/**
 * Simula la cámara de un carril: envía una placa con su nivel de confianza a `ruta`.
 * Sirve para la entrada y la salida mientras no exista la vista de la sede (TT-09).
 */
export default function SimuladorLectura({ ruta, alEnviar }) {
  const [placa, setPlaca] = useState('');
  const [confianza, setConfianza] = useState(0.95);
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

  const enviar = async (evento) => {
    evento.preventDefault();
    setError('');
    setEnviando(true);
    try {
      await post(ruta, { placa, confianza });
      alEnviar?.();
    } catch (e) {
      setError(e.estado === 400 ? 'Revisa la placa y el nivel de confianza' : e.message);
    } finally {
      setEnviando(false);
    }
  };

  return (
    <Card>
      <Card.Body>
        <Card.Title className="h6 fw-bold mb-3">Simular lectura</Card.Title>
        <Form onSubmit={enviar}>
          <Form.Group className="mb-3" controlId="placa-simulada">
            <Form.Label className="small">Placa</Form.Label>
            <Form.Control
              value={placa}
              onChange={(e) => setPlaca(e.target.value)}
              placeholder="Ej.: ABC123 o ABC12D"
              required
            />
          </Form.Group>
          <Form.Group className="mb-3" controlId="confianza-simulada">
            <Form.Label className="small d-flex justify-content-between">
              <span>Nivel de confianza</span>
              <strong>{Math.round(confianza * 100)} %</strong>
            </Form.Label>
            <Form.Range
              min={0}
              max={1}
              step={0.01}
              value={confianza}
              onChange={(e) => setConfianza(Number(e.target.value))}
            />
          </Form.Group>
          {error && <Alert variant="danger" className="py-2 small">{error}</Alert>}
          <Button type="submit" className="w-100" disabled={enviando}>
            {enviando ? 'Enviando…' : 'Enviar lectura'}
          </Button>
        </Form>
      </Card.Body>
    </Card>
  );
}