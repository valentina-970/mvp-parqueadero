/**
 * Banner grande con el resultado de una lectura. `tipo` puede ser
 * 'exito' (verde: ABRE, INGRESO REGISTRADO) o 'error' (rojo: NO ABRE, LLENO).
 */
export default function BannerResultado({ tipo, children }) {
  return <div className={`banner-resultado banner-${tipo}`} role="status">{children}</div>;
}
