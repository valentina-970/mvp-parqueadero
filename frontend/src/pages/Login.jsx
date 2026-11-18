import { useState } from 'react';
import { Card, Form, Button, Alert } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/useAuth.js';

/** Pantalla de inicio de sesión (HU-05). */
export default function Login() {
  const [usuario, setUsuario] = useState('');
  const [contrasena, setContrasena] = useState('');
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);
  const { iniciarSesion } = useAuth();
  const navegar = useNavigate();

  const enviar = async (evento) => {
    evento.preventDefault();
    setError('');
    setEnviando(true);
    try {
      await iniciarSesion(usuario.trim(), contrasena);
      navegar('/entrada', { replace: true });
    } catch (e) {
      // El servidor responde un mensaje genérico para no revelar qué dato falló
      setError(e.estado === 401 ? e.message : 'No se pudo conectar con el servidor');
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="login-pantalla">
      <Card className="login-tarjeta">
        <Card.Body>
          <h1 className="h4 fw-bold mb-1">Parqueadero MVP</h1>
          <p className="text-secondary mb-3">Inicia sesión para continuar</p>
          <Form onSubmit={enviar}>
            <Form.Group className="mb-3" controlId="usuario">
              <Form.Label className="small">Usuario</Form.Label>
              <Form.Control
                value={usuario}
                onChange={(e) => setUsuario(e.target.value)}
                autoComplete="username"
                required
              />
            </Form.Group>
            <Form.Group className="mb-3" controlId="contrasena">
              <Form.Label className="small">Contraseña</Form.Label>
              <Form.Control
                type="password"
                value={contrasena}
                onChange={(e) => setContrasena(e.target.value)}
                autoComplete="current-password"
                required
              />
            </Form.Group>
            {error && <Alert variant="danger" className="py-2 small">{error}</Alert>}
            <Button type="submit" className="w-100" disabled={enviando}>
              {enviando ? 'Ingresando…' : 'Ingresar'}
            </Button>
          </Form>
          <p className="text-secondary small mt-3 mb-0">Acceso para operador y administrador</p>
        </Card.Body>
      </Card>
    </div>
  );
}
