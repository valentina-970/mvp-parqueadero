import { Row, Col, Card } from 'react-bootstrap';
import CarrilSalida from '../components/CarrilSalida.jsx';

/**
 * Salida y caja. A la izquierda el carril de salida (HU-04); a la derecha irá la caja
 * para registrar el pago (HU-03).
 */
export default function Salida() {
  return (
    <>
      <h1 className="h3 fw-bold mb-0">Salida y caja</h1>
      <p className="text-secondary mb-4">Sede Principal · Salida 1</p>
      <Row className="g-4">
        <Col lg={7}>
          <CarrilSalida />
        </Col>
        <Col lg={5}>
          {/* Marcador: la caja la implementa HU-03 */}
          <Card>
            <Card.Body>
              <Card.Title className="h6 fw-bold">Caja · Registrar pago</Card.Title>
              <p className="text-secondary mb-0">Pantalla pendiente de implementar.</p>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </>
  );
}
