import { Navigate } from 'react-router-dom';
import { useAuth } from '../auth/useAuth.js';

/**
 * Protege una pantalla: sin sesión lleva al login; si se indican `roles`
 * y el usuario no tiene uno de ellos, lo devuelve al carril de entrada.
 */
export default function RutaProtegida({ roles, children }) {
  const { usuario, cargando } = useAuth();

  if (cargando) {
    return null;
  }
  if (!usuario) {
    return <Navigate to="/login" replace />;
  }
  if (roles && !roles.includes(usuario.rol)) {
    return <Navigate to="/entrada" replace />;
  }
  return children;
}
