import { useEffect, useState } from 'react';
import { Card, Form, Button, Badge } from 'react-bootstrap';
import { get } from '../services/api.js';

export default function Login() {
  const [usuario, setUsuario] = useState('');
  const [contrasena, setContrasena] = useState('');
  const [api, setApi] = useState('verificando');

  useEffect(() => {
    get('/api/health')
      .then(() => setApi('conectada'))
      .catch(() => setApi('sin conexión'));
  }, []);

  const enviar = (evento) => {
    evento.preventDefault();
    // La autenticación real se implementa en HU-05.
    console.log('Pendiente HU-05', usuario);
  };

  return (
    <Card style={{ maxWidth: 400 }} className="mx-auto">
      <Card.Body>
        <Card.Title>Iniciar sesión</Card.Title>
        <Form onSubmit={enviar}>
          <Form.Group className="mb-3" controlId="usuario">
            <Form.Label>Usuario</Form.Label>
            <Form.Control value={usuario} onChange={(e) => setUsuario(e.target.value)} />
          </Form.Group>
          <Form.Group className="mb-3" controlId="contrasena">
            <Form.Label>Contraseña</Form.Label>
            <Form.Control
              type="password"
              value={contrasena}
              onChange={(e) => setContrasena(e.target.value)}
            />
          </Form.Group>
          <Button type="submit" className="w-100">Entrar</Button>
        </Form>
        <div className="mt-3 text-end">
          <Badge bg={api === 'conectada' ? 'success' : 'secondary'}>API: {api}</Badge>
        </div>
      </Card.Body>
    </Card>
  );
}