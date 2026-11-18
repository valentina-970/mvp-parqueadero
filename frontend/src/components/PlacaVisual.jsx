/** Muestra una placa como las colombianas (amarilla). Con `invalida`, borde rojo punteado. */
export default function PlacaVisual({ placa, invalida = false, pequena = false }) {
  const clases = ['placa-visual'];
  if (invalida) clases.push('placa-invalida');
  if (pequena) clases.push('placa-pequena');
  return <span className={clases.join(' ')}>{placa}</span>;
}
