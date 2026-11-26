import { Row, Col } from 'react-bootstrap';
import CarrilSalida from '../components/CarrilSalida.jsx';
import CajaPago from '../components/CajaPago.jsx';
import ValidacionManual from '../components/ValidacionManual.jsx';

/**
 * Salida y caja. A la izquierda el carril de salida (HU-04) y las lecturas por validar (HU-06);
 * a la derecha la caja para registrar el pago en efectivo (HU-03).
 */
export default function Salida() {
  return (
    <>
      <h1 className="h3 fw-bold mb-0">Salida y caja</h1>
      <p className="text-secondary mb-4">Sede Principal · Salida 1</p>
      <Row className="g-4">
        <Col lg={7} className="d-flex flex-column gap-4">
          <CarrilSalida />
          <ValidacionManual carril="SALIDA" />
        </Col>
        <Col lg={5}>
          <CajaPago />
        </Col>
      </Row>
    </>
  );
}
