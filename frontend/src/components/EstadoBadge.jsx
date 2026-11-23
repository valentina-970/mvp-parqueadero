const ETIQUETAS = {
  PENDIENTE: 'Pendiente',
  PAGADO: 'Pagado',
  VENCIDO: 'Vencido',
  ANULADO: 'Anulado',
};

/** Etiqueta con el color del estado de un cobro: Pendiente, Pagado, Vencido o Anulado. */
export default function EstadoBadge({ estado }) {
  return (
    <span className={`estado-badge estado-${estado.toLowerCase()}`}>
      {ETIQUETAS[estado] ?? estado}
    </span>
  );
}
