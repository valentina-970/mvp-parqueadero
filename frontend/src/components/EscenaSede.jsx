/**
 * Escena de la sede para el simulador del celular: una vía, la cámara, un vehículo con su
 * placa y la barrera. `abierta` puede ser true (barrera levantada, luz verde), false
 * (barrera horizontal, luz roja) o null (sin lectura todavía: barrera cerrada, luz apagada).
 * Es solo una ilustración de interfaz; la decisión real la toma la API.
 */
export default function EscenaSede({ placa, abierta }) {
  const levantada = abierta === true;
  const luz = abierta === true ? '#22c55e' : abierta === false ? '#ef4444' : '#64748b';
  const franjas = levantada ? '#22c55e' : '#ef4444';

  return (
    <svg
      className="escena-sede"
      viewBox="0 0 360 200"
      role="img"
      aria-label={levantada ? 'Barrera abierta' : 'Barrera cerrada'}
    >
      <rect width="360" height="200" fill="#1e3a5f" />
      <rect y="120" width="360" height="80" fill="#374151" />
      <rect y="112" width="360" height="12" fill="#4b5563" />
      {[20, 100, 180, 260, 330].map((x) => (
        <rect key={x} x={x} y="170" width="30" height="4" fill="#e5e7eb" />
      ))}

      {/* Cámara */}
      <rect x="296" y="34" width="46" height="22" rx="4" fill="#e5e7eb" />
      <circle cx="308" cy="45" r="5" fill="#0ea5e9" />
      <rect x="316" y="56" width="5" height="62" fill="#9ca3af" />

      {/* Vehículo: avanza cuando la barrera abre */}
      <g className="escena-vehiculo" style={{ transform: levantada ? 'translateX(70px)' : 'translateX(0)' }}>
        <rect x="30" y="118" width="120" height="34" rx="8" fill="#3b82f6" />
        <rect x="50" y="98" width="72" height="26" rx="8" fill="#2563eb" />
        <rect x="58" y="104" width="56" height="14" rx="4" fill="#bfdbfe" />
        <circle cx="62" cy="154" r="13" fill="#111827" />
        <circle cx="120" cy="154" r="13" fill="#111827" />
        {placa && (
          <>
            <rect x="82" y="134" width="64" height="18" rx="3" fill="#fcd34d" stroke="#111827" strokeWidth="1.5" />
            <text x="114" y="148" textAnchor="middle" fontSize="12" fontWeight="800" fill="#111827">
              {placa}
            </text>
          </>
        )}
      </g>

      {/* Barrera: caseta, brazo y luz */}
      <rect x="236" y="104" width="18" height="48" rx="3" fill="#f59e0b" />
      <circle cx="245" cy="94" r="7" fill={luz} />
      <g
        className="escena-brazo"
        style={{ transform: `rotate(${levantada ? 80 : 0}deg)`, transformOrigin: '245px 112px' }}
      >
        <rect x="100" y="108" width="145" height="8" rx="2" fill="#f9fafb" />
        {[110, 140, 170, 200].map((x) => (
          <rect key={x} x={x} y="108" width="15" height="8" fill={franjas} />
        ))}
      </g>
    </svg>
  );
}
