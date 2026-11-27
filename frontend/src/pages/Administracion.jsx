import { useEffect, useState } from 'react';
import { Alert, Button, Card, Col, Form, Row } from 'react-bootstrap';
import { get, post } from '../services/api.js';

const CONFIGURACION = '/api/admin/configuracion';

function Campo({ etiqueta, nombre, valor, tipo = 'number', ...restricciones }) {
  return (
    <Col sm={6} lg={3}>
      <Form.Group>
        <Form.Label className="small w-100 mb-0">
          {etiqueta}
          <Form.Control
            className="mt-1"
            name={nombre}
            type={tipo}
            defaultValue={valor ?? ''}
            required
            {...restricciones}
          />
        </Form.Label>
      </Form.Group>
    </Col>
  );
}

function FormularioConfiguracion({ titulo, ruta, convertir, alGuardar, children }) {
  const [enviando, setEnviando] = useState(false);
  const [aviso, setAviso] = useState(null);

  const guardar = async (evento) => {
    evento.preventDefault();
    const valores = Object.fromEntries(new FormData(evento.currentTarget));
    setAviso(null);
    setEnviando(true);
    try {
      await post(ruta, convertir(valores));
      setAviso({ tipo: 'success', mensaje: 'Cambios guardados.' });
      alGuardar?.();
    } catch (error) {
      setAviso({ tipo: 'danger', mensaje: error.message });
    } finally {
      setEnviando(false);
    }
  };

  return (
    <Form onSubmit={guardar} className="border rounded p-3">
      {titulo && <h3 className="h6 fw-semibold">{titulo}</h3>}
      <fieldset disabled={enviando}>
        <Row className="g-3 align-items-end">
          {children}
          <Col xs="auto">
            <Button type="submit">{enviando ? 'Guardando…' : 'Guardar'}</Button>
          </Col>
        </Row>
      </fieldset>
      {aviso && <Alert variant={aviso.tipo} className="py-2 small mt-3 mb-0">{aviso.mensaje}</Alert>}
    </Form>
  );
}

function Seccion({ titulo, descripcion, children }) {
  return (
    <Card className="mb-3">
      <Card.Body>
        <h2 className="h5 fw-bold">{titulo}</h2>
        <p className="text-secondary small">{descripcion}</p>
        <div className="d-grid gap-3">{children}</div>
      </Card.Body>
    </Card>
  );
}

function Tarifas({ tarifas }) {
  return (
    <Seccion titulo="Tarifas" descripcion="Los cambios solo aplican a cobros nuevos.">
      {tarifas.map(({ tipoId, nombre, diurna, nocturna }) => (
        <FormularioConfiguracion
          key={tipoId}
          titulo={nombre}
          ruta={`/api/admin/tarifas/${tipoId}`}
          convertir={(valores) => ({ diurna: Number(valores.diurna), nocturna: Number(valores.nocturna) })}
        >
          <Campo etiqueta="Diurna ($)" nombre="diurna" valor={diurna} min="0" max="9999999999.99" step="0.01" />
          <Campo etiqueta="Nocturna ($)" nombre="nocturna" valor={nocturna} min="0" max="9999999999.99" step="0.01" />
        </FormularioConfiguracion>
      ))}
    </Seccion>
  );
}

function Horarios({ horarios }) {
  return (
    <Seccion titulo="Horarios de las franjas" descripcion="El minuto de fin está incluido. Las franjas deben cubrir las 24 horas.">
      <FormularioConfiguracion ruta="/api/admin/horarios" convertir={(valores) => valores}>
        <Campo etiqueta="Inicio diurno" nombre="inicioDiurno" tipo="time" valor={horarios.inicioDiurno.slice(0, 5)} />
        <Campo etiqueta="Fin diurno" nombre="finDiurno" tipo="time" valor={horarios.finDiurno.slice(0, 5)} />
        <Campo etiqueta="Inicio nocturno" nombre="inicioNocturno" tipo="time" valor={horarios.inicioNocturno.slice(0, 5)} />
        <Campo etiqueta="Fin nocturno" nombre="finNocturno" tipo="time" valor={horarios.finNocturno.slice(0, 5)} />
      </FormularioConfiguracion>
    </Seccion>
  );
}

/** Configuración de la sede, disponible para el rol administrador (HU-08). */
export default function Administracion() {
  const [configuracion, setConfiguracion] = useState(null);
  const [error, setError] = useState('');
  const [version, setVersion] = useState(0);

  useEffect(() => {
    let activo = true;
    get(CONFIGURACION)
      .then((datos) => {
        if (activo) {
          setConfiguracion(datos);
          setError('');
        }
      })
      .catch((fallo) => activo && setError(fallo.message));
    return () => { activo = false; };
  }, [version]);

  const recargar = () => {
    setError('');
    setVersion((actual) => actual + 1);
  };

  return (
    <>
      <h1 className="h3 fw-bold mb-0">Administración</h1>
      <p className="text-secondary mb-4">Configuración del parqueadero</p>
      {error && (
        <Alert variant="danger">
          {error}{' '}
          <Button variant="outline-danger" size="sm" onClick={recargar}>Reintentar</Button>
        </Alert>
      )}
      {configuracion ? (
        <>
          <Tarifas tarifas={configuracion.tarifas} />
          <Horarios horarios={configuracion.horarios} />
        </>
      ) : !error && <p className="text-secondary">Cargando…</p>}
    </>
  );
}
