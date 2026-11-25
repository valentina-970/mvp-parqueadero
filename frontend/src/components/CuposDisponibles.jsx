import { Card } from 'react-bootstrap';
import { useCupos } from '../hooks/useCupos.js';

/** Cupos disponibles por tipo de vehículo en la sede; marca LLENO cuando un tipo no tiene lugar. */
export default function CuposDisponibles({ version }) {
  const cupos = useCupos(version);

  return (
    <Card>
      <Card.Body>
        <Card.Title className="h6 fw-bold mb-3">Cupos disponibles</Card.Title>
        {cupos.length === 0 && <p className="text-secondary mb-0">Consultando los cupos…</p>}
        {cupos.map((cupo) => (
          <div key={cupo.codigo} className="d-flex justify-content-between align-items-center py-1">
            <span className="text-secondary">{cupo.nombre}s</span>
            <span className="d-flex align-items-center gap-2">
              <span className={`fw-bold ${cupo.lleno ? 'text-danger' : ''}`}>
                {cupo.disponibles} de {cupo.capacidad}
              </span>
              {cupo.lleno && <span className="badge-lleno">LLENO</span>}
            </span>
          </div>
        ))}
      </Card.Body>
    </Card>
  );
}
