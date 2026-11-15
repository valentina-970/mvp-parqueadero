import { Navbar, Nav, Container } from 'react-bootstrap';
import { Link } from 'react-router-dom';

export default function Navegacion() {
  return (
    <Navbar bg="dark" variant="dark" expand="md">
      <Container>
        <Navbar.Brand as={Link} to="/">Parqueadero</Navbar.Brand>
        <Navbar.Toggle aria-controls="menu" />
        <Navbar.Collapse id="menu">
          <Nav className="me-auto">
            <Nav.Link as={Link} to="/entrada">Entrada</Nav.Link>
            <Nav.Link as={Link} to="/salida">Salida y caja</Nav.Link>
            <Nav.Link as={Link} to="/consultas">Consultas</Nav.Link>
            <Nav.Link as={Link} to="/administracion">Administración</Nav.Link>
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
}