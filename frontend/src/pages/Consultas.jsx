import { useEffect, useState } from 'react';
import { Card, Form, Row, Col, Button, Alert, Table, Nav } from 'react-bootstrap';
import { get } from '../services/api.js';
import { formatearPesos, formatearDia, formatearFechaHora } from '../utils/formato.js';
import PlacaVisual from '../components/PlacaVisual.jsx';
import EstadoBadge from '../components/EstadoBadge.jsx';
import Paginador from '../components/Paginador.jsx';

const SIN_FILTROS = { placa: '', desde: '', hasta: '', estado: '' };
const RANGO_INVALIDO = 'La fecha final no puede ser anterior a la inicial';

const RESULTADOS = {
  PERMITIDO: 'Permitido',
  RECHAZADO: 'Rechazado',
  PENDIENTE_VALIDACION: 'Pendiente de validación',
};
const LECTURAS = { AUTOMATICA: 'Automática', MANUAL: 'Manual' };
const FRANJAS = { DIURNA: 'Diurna', NOCTURNA: 'Nocturna' };

/** Arma la ruta de la API con los filtros que tienen valor. */
function rutaConsulta(vista, filtros, pagina) {
  const parametros = new URLSearchParams();
  if (filtros.placa) parametros.set('placa', filtros.placa);
  if (filtros.desde) parametros.set('desde', filtros.desde);
  if (filtros.hasta) parametros.set('hasta', filtros.hasta);
  if (vista === 'cobros' && filtros.estado) parametros.set('estado', filtros.estado);
  parametros.set('pagina', pagina);
  return `/api/consultas/${vista}?${parametros}`;
}

function TablaEventos({ eventos }) {
  return (
    <Table responsive hover className="align-middle mb-0">
      <thead>
        <tr>
          <th>Placa</th>
          <th>Tipo</th>
          <th>Carril</th>
          <th>Fecha y hora</th>
          <th>Sede</th>
          <th>Lectura</th>
          <th>Resultado</th>
        </tr>
      </thead>
      <tbody>
        {eventos.map((evento, i) => (
          <tr key={`${evento.ocurridoEn}-${i}`}>
            <td><PlacaVisual placa={evento.placa} pequena /></td>
            <td>{evento.tipoVehiculo}</td>
            <td>{evento.carril === 'ENTRADA' ? 'Entrada' : 'Salida'}</td>
            <td>{formatearFechaHora(evento.ocurridoEn)}</td>
            <td>{evento.sede}</td>
            <td>{LECTURAS[evento.tipoLectura] ?? evento.tipoLectura}</td>
            <td title={evento.motivo ?? ''}>{RESULTADOS[evento.resultado] ?? evento.resultado}</td>
          </tr>
        ))}
      </tbody>
    </Table>
  );
}

function TablaCobros({ cobros }) {
  return (
    <Table responsive hover className="align-middle mb-0">
      <thead>
        <tr>
          <th>Placa</th>
          <th>Día operativo</th>
          <th>Franja</th>
          <th>Tarifa aplicada</th>
          <th>Valor</th>
          <th>Estado</th>
        </tr>
      </thead>
      <tbody>
        {cobros.map((cobro) => (
          <tr key={`${cobro.placa}-${cobro.diaOperativo}`}>
            <td><PlacaVisual placa={cobro.placa} pequena /></td>
            <td>{formatearDia(cobro.diaOperativo)}</td>
            <td>{FRANJAS[cobro.franja] ?? cobro.franja}</td>
            <td>{formatearPesos(cobro.tarifaAplicada)}</td>
            <td className="fw-semibold">{formatearPesos(cobro.valor)}</td>
            <td><EstadoBadge estado={cobro.estado} /></td>
          </tr>
        ))}
      </tbody>
    </Table>
  );
}

/**
 * Consultas de eventos y cobros (HU-09). Los filtros se combinan; los resultados llegan
 * en páginas de 20, del más reciente al más antiguo. Si la fecha final es anterior a la
 * inicial, se avisa y no se consulta (CA-7).
 */
export default function Consultas() {
  const [vista, setVista] = useState('eventos');
  const [filtros, setFiltros] = useState(SIN_FILTROS);
  const [consulta, setConsulta] = useState({ vista: 'eventos', filtros: SIN_FILTROS, pagina: 0 });
  const [resultado, setResultado] = useState(null);
  const [error, setError] = useState('');

  // Cada vez que cambia la consulta aplicada (pestaña, filtros o página), se pide a la API
  useEffect(() => {
    let activo = true;
    get(rutaConsulta(consulta.vista, consulta.filtros, consulta.pagina))
      .then((pagina) => activo && setResultado({ vista: consulta.vista, ...pagina }))
      .catch((e) => activo && setError(e.message));
    return () => {
      activo = false;
    };
  }, [consulta]);

  const cambiarFiltro = (campo) => (evento) =>
    setFiltros((actuales) => ({ ...actuales, [campo]: evento.target.value }));

  const buscar = (evento) => {
    evento.preventDefault();
    if (filtros.desde && filtros.hasta && filtros.hasta < filtros.desde) {
      setError(RANGO_INVALIDO);
      return;
    }
    setError('');
    setConsulta({ vista, filtros, pagina: 0 });
  };

  const limpiar = () => {
    setFiltros(SIN_FILTROS);
    setError('');
    setConsulta({ vista, filtros: SIN_FILTROS, pagina: 0 });
  };

  const cambiarVista = (nueva) => {
    setVista(nueva);
    setError('');
    setConsulta({ vista: nueva, filtros, pagina: 0 });
  };

  const mostrado = resultado && resultado.vista === consulta.vista ? resultado : null;

  return (
    <>
      <h1 className="h3 fw-bold mb-0">Consultas</h1>
      <p className="text-secondary mb-4">Ingresos, salidas y cobros de la sede</p>

      <Nav variant="tabs" activeKey={vista} onSelect={cambiarVista} className="mb-3">
        <Nav.Item>
          <Nav.Link eventKey="eventos">Eventos</Nav.Link>
        </Nav.Item>
        <Nav.Item>
          <Nav.Link eventKey="cobros">Cobros</Nav.Link>
        </Nav.Item>
      </Nav>

      <Card className="mb-3">
        <Card.Body>
          <Form onSubmit={buscar}>
            <Row className="g-3 align-items-end">
              <Col md={3}>
                <Form.Group controlId="consulta-placa">
                  <Form.Label className="small">Placa</Form.Label>
                  <Form.Control
                    value={filtros.placa}
                    onChange={cambiarFiltro('placa')}
                    placeholder="Ej.: ABC123"
                    autoComplete="off"
                  />
                </Form.Group>
              </Col>
              <Col md={2}>
                <Form.Group controlId="consulta-desde">
                  <Form.Label className="small">Desde</Form.Label>
                  <Form.Control type="date" value={filtros.desde} onChange={cambiarFiltro('desde')} />
                </Form.Group>
              </Col>
              <Col md={2}>
                <Form.Group controlId="consulta-hasta">
                  <Form.Label className="small">Hasta</Form.Label>
                  <Form.Control type="date" value={filtros.hasta} onChange={cambiarFiltro('hasta')} />
                </Form.Group>
              </Col>
              {vista === 'cobros' && (
                <Col md={2}>
                  <Form.Group controlId="consulta-estado">
                    <Form.Label className="small">Estado</Form.Label>
                    <Form.Select value={filtros.estado} onChange={cambiarFiltro('estado')}>
                      <option value="">Todos</option>
                      <option value="PENDIENTE">Pendiente</option>
                      <option value="PAGADO">Pagado</option>
                      <option value="VENCIDO">Vencido</option>
                      <option value="ANULADO">Anulado</option>
                    </Form.Select>
                  </Form.Group>
                </Col>
              )}
              <Col md="auto" className="d-flex gap-2">
                <Button type="submit">Buscar</Button>
                <Button type="button" variant="outline-secondary" onClick={limpiar}>
                  Limpiar
                </Button>
              </Col>
            </Row>
          </Form>
        </Card.Body>
      </Card>

      {error && <Alert variant="danger">{error}</Alert>}

      <Card>
        <Card.Body>
          {!mostrado ? (
            <p className="text-secondary mb-0">Cargando…</p>
          ) : mostrado.contenido.length === 0 ? (
            <p className="text-secondary mb-0">No hay registros</p>
          ) : (
            <>
              {consulta.vista === 'eventos' ? (
                <TablaEventos eventos={mostrado.contenido} />
              ) : (
                <TablaCobros cobros={mostrado.contenido} />
              )}
              <div className="mt-3">
                <Paginador
                  pagina={mostrado.pagina}
                  totalPaginas={mostrado.totalPaginas}
                  totalElementos={mostrado.totalElementos}
                  alCambiar={(pagina) => setConsulta({ ...consulta, pagina })}
                />
              </div>
            </>
          )}
        </Card.Body>
      </Card>
    </>
  );
}