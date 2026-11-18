import { Routes, Route, Navigate } from 'react-router-dom';
import { Container } from 'react-bootstrap';
import Navegacion from './components/Navegacion.jsx';
import Pagina from './components/Pagina.jsx';
import RutaProtegida from './components/RutaProtegida.jsx';
import Login from './pages/Login.jsx';

/** Pantalla con la barra superior; el login se muestra sin ella. */
function ConBarra({ roles, children }) {
  return (
    <RutaProtegida roles={roles}>
      <Navegacion />
      <Container fluid className="px-4 mt-4">{children}</Container>
    </RutaProtegida>
  );
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/entrada" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/entrada" element={<ConBarra><Pagina titulo="Carril de entrada" /></ConBarra>} />
      <Route path="/salida" element={<ConBarra><Pagina titulo="Salida y caja" /></ConBarra>} />
      <Route path="/consultas" element={<ConBarra><Pagina titulo="Consultas" /></ConBarra>} />
      <Route
        path="/administracion"
        element={<ConBarra roles={['ADMINISTRADOR']}><Pagina titulo="Administración" /></ConBarra>}
      />
    </Routes>
  );
}
