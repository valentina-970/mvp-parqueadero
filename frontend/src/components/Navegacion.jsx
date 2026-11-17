import { Navbar, Nav, Container, Button } from 'react-bootstrap';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/useAuth.js';

const ETIQUETA_ROL = { OPERADOR: 'Operador', ADMINISTRADOR: 'Administrador' };

/** Barra superior. La opción Administración solo la ve el administrador (HU-05, CA-4 y CA-5). */
export default function Navegacion() {
  const { usuario, cerrarSesion } = useAuth();
  const navegar = useNavigate();
  const esAdministrador = usuario?.rol === 'ADMINISTRADOR';

  const salir = async () => {
    await cerrarSesion();
    navegar('/login', { replace: true });
  };

  return (
    <Navbar variant="dark" expand="md" className="barra-principal">
      <Container fluid className="px-4">
        <Navbar.Brand as={NavLink} to="/entrada" className="fw-bold">Parqueadero MVP</Navbar.Brand>
        <Navbar.Toggle aria-controls="menu" />
        <Navbar.Collapse id="menu">
          <Nav className="me-auto">
            <Nav.Link as={NavLink} to="/entrada">Carril de entrada</Nav.Link>
            <Nav.Link as={NavLink} to="/salida">Salida y caja</Nav.Link>
            <Nav.Link as={NavLink} to="/consultas">Consultas</Nav.Link>
            {esAdministrador && (
              <Nav.Link as={NavLink} to="/administracion">Administración</Nav.Link>
            )}
          </Nav>
          <Navbar.Text className="text-light me-3">
            {usuario?.usuario} · {ETIQUETA_ROL[usuario?.rol]}
          </Navbar.Text>
          <Button variant="link" className="enlace-sesion p-0" onClick={salir}>
            Cerrar sesión
          </Button>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
}
