import { Routes, Route, Navigate } from "react-router-dom";
import { Container } from "react-bootstrap";
import Navegacion from "./components/Navegacion.jsx";
import RutaProtegida from "./components/RutaProtegida.jsx";
import Login from "./pages/Login.jsx";
import Salida from "./pages/Salida.jsx";
import Entrada from "./pages/Entrada.jsx";
import Simulador from "./pages/Simulador.jsx";
import Consultas from "./pages/Consultas.jsx";
import Administracion from "./pages/Administracion.jsx";

/** Pantalla con la barra superior; el login se muestra sin ella. */
function ConBarra({ roles, children }) {
  return (
    <RutaProtegida roles={roles}>
      <Navegacion />
      <Container fluid className="px-4 mt-4">
        {children}
      </Container>
    </RutaProtegida>
  );
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/entrada" replace />} />
      <Route path="/login" element={<Login />} />
      {/* Vista de la sede para el celular: requiere sesión, pero no muestra la barra superior */}
      <Route path="/simulador" element={<Navigate to="/simulador/salida" replace />} />
      <Route
        path="/simulador/:carril"
        element={
          <RutaProtegida>
            <Simulador />
          </RutaProtegida>
        }
      />
      <Route
        path="/entrada"
        element={
          <ConBarra>
            <Entrada />
          </ConBarra>
        }
      />
      <Route
        path="/salida"
        element={
          <ConBarra>
            <Salida />
          </ConBarra>
        }
      />
      <Route
        path="/consultas"
        element={
          <ConBarra>
            <Consultas />
          </ConBarra>
        }
      />
      <Route
        path="/administracion"
        element={
          <ConBarra roles={["ADMINISTRADOR"]}>
            <Administracion />
          </ConBarra>
        }
      />
    </Routes>
  );
}
