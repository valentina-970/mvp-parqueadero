import { useState } from 'react';
import { Row, Col } from 'react-bootstrap';
import CarrilEntrada from '../components/CarrilEntrada.jsx';
import SimuladorLectura from '../components/SimuladorLectura.jsx';

/** Carril de entrada (HU-01): a la izquierda la última lectura; a la derecha el simulador. */
export default function Entrada() {
  const [version, setVersion] = useState(0);

  return (
    <>
      <h1 className="h3 fw-bold mb-0">Carril de entrada</h1>
      <p className="text-secondary mb-4">Sede Principal · Entrada 1</p>
      <Row className="g-4">
        <Col lg={7}>
          <CarrilEntrada version={version} />
        </Col>
        <Col lg={5}>
          <SimuladorLectura ruta="/api/entrada" alEnviar={() => setVersion((v) => v + 1)} />
        </Col>
      </Row>
    </>
  );
}