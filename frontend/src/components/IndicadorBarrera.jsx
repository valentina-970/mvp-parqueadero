/** Barra que representa la barrera simulada: verde y "Abierta", o roja y "Cerrada". */
export default function IndicadorBarrera({ abierta }) {
  return (
    <div className="d-flex align-items-center gap-3">
      <span className="text-secondary">Barrera</span>
      <span className={`barrera-barra ${abierta ? 'barrera-abierta' : 'barrera-cerrada'}`} />
      <span className={`fw-semibold ${abierta ? 'text-success' : 'text-danger'}`}>
        {abierta ? 'Abierta' : 'Cerrada'}
      </span>
    </div>
  );
}
