import { useCallback, useEffect, useMemo, useState } from 'react';
import { get, post, registrarSesionExpirada } from '../services/api.js';
import { AuthContext } from './AuthContext.js';

/**
 * Guarda el usuario de la sesión ({ usuario, rol }) y ofrece iniciar y cerrar sesión.
 * Al abrir la aplicación consulta /api/auth/yo para recuperar una sesión existente.
 */
export default function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    registrarSesionExpirada(() => setUsuario(null));
    get('/api/auth/yo')
      .then(setUsuario)
      .catch(() => setUsuario(null))
      .finally(() => setCargando(false));
  }, []);

  const iniciarSesion = useCallback(async (nombre, contrasena) => {
    const sesion = await post('/api/auth/login', { usuario: nombre, contrasena });
    setUsuario(sesion);
    return sesion;
  }, []);

  const cerrarSesion = useCallback(async () => {
    try {
      await post('/api/auth/logout');
    } finally {
      // Aunque falle la llamada, la interfaz deja de mostrar la sesión
      setUsuario(null);
    }
  }, []);

  const valor = useMemo(
    () => ({ usuario, cargando, iniciarSesion, cerrarSesion }),
    [usuario, cargando, iniciarSesion, cerrarSesion],
  );

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}
