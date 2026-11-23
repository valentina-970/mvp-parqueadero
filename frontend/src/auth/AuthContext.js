import { createContext } from 'react';

/** Contexto con la sesión: { usuario, cargando, iniciarSesion, cerrarSesion }. */
export const AuthContext = createContext(null);
