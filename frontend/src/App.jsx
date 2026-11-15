import { Routes, Route, Navigate } from 'react-router-dom';
import { Container } from 'react-bootstrap';
import Navegacion from './components/Navegacion.jsx';
import Pagina from './components/Pagina.jsx';
import Login from './pages/Login.jsx';

export default function App() {
  return (
    <>
      <Navegacion />
      <Container className="mt-4">
        <Routes>
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/login" element={<Login />} />
          <Route path="/entrada" element={<Pagina titulo="Carril de entrada" />} />
          <Route path="/salida" element={<Pagina titulo="Carril de salida y caja" />} />
          <Route path="/administracion" element={<Pagina titulo="Administración" />} />
          <Route path="/consultas" element={<Pagina titulo="Consultas" />} />
        </Routes>
      </Container>
    </>
  );
}