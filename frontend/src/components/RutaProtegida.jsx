import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/useAuth.js';

/**
 * Protege una pantalla: sin sesión lleva al login (y recuerda a dónde quería ir);
 * si se indican `roles` y el usuario no tiene uno de ellos, lo devuelve al carril de entrada.
 */
export default function RutaProtegida({ roles, children }) {
  const { usuario, cargando } = useAuth();
  const ubicacion = useLocation();

  if (cargando) {
    return null;
  }
  if (!usuario) {
    return <Navigate to="/login" replace state={{ desde: ubicacion.pathname }} />;
  }
  if (roles && !roles.includes(usuario.rol)) {
    return <Navigate to="/entrada" replace />;
  }
  return children;
}
