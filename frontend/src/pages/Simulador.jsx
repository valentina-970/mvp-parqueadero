import { useRef, useState } from 'react';
import { Navigate, useParams } from 'react-router-dom';
import { Form, Button, Alert } from 'react-bootstrap';
import { post } from '../services/api.js';
import BannerResultado from '../components/BannerResultado.jsx';
import EscenaSede from '../components/EscenaSede.jsx';
import { useCupos } from '../hooks/useCupos.js';
import { leerPlaca } from '../services/lectorCamara.js';

const CARRILES = {
  salida: { titulo: 'Salida', ruta: '/api/salida' },
  entrada: { titulo: 'Entrada', ruta: '/api/entrada' },
};

const EJEMPLOS = { CARRO: 'ABC123', MOTO: 'XYZ12D' };

const MENSAJES_LECTURA = {
  SIN_PLACA: 'No se encontró una placa en la foto. Acércate más o escríbela abajo.',
  REFLEJO: 'La placa tiene reflejo. Toma la foto sin flash y de frente.',
};

/**
 * Convierte la respuesta de cada carril en lo que muestra la pantalla. La salida responde
 * `abre`; la entrada responde `resultado` (PERMITIDO, PENDIENTE_VALIDACION o RECHAZADO).
 */
function interpretar(carril, respuesta) {
  if (carril === 'salida') {
    return { abre: respuesta.abre, aviso: false, lleno: false, motivo: respuesta.motivo };
  }
  return {
    abre: respuesta.resultado === 'PERMITIDO',
    aviso: respuesta.resultado === 'PENDIENTE_VALIDACION',
    lleno: respuesta.lleno === true,
    motivo: respuesta.lleno ? respuesta.motivo.replace(/^LLENO:\s*/, '') : respuesta.motivo,
  };
}

/**
 * Vista de la sede para el celular (TT-09): simula la cámara de un carril. Se elige el tipo
 * de vehículo, la placa y la confianza, y se ve si la barrera abre. Rutas:
 * `/simulador/salida` y `/simulador/entrada`.
 */
export default function Simulador() {
  const { carril } = useParams();
  const [tipo, setTipo] = useState('CARRO');
  const [placa, setPlaca] = useState(EJEMPLOS.CARRO);
  const [confianza, setConfianza] = useState(0.95);
  const [resultado, setResultado] = useState(null);
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [version, setVersion] = useState(0);
  const [leyendo, setLeyendo] = useState(false);
  const camara = useRef(null);
  const cupos = useCupos(version);

  const configuracion = CARRILES[carril];
  if (!configuracion) {
    return <Navigate to="/simulador/salida" replace />;
  }

  const elegirTipo = (nuevo) => {
    setTipo(nuevo);
    // Si la placa es el ejemplo del otro tipo, se cambia por el ejemplo del nuevo
    if (placa === '' || Object.values(EJEMPLOS).includes(placa)) {
      setPlaca(EJEMPLOS[nuevo]);
    }
  };

  /** Envía la lectura al carril, venga del formulario o de la cámara. */
  const enviarLectura = async (placaLeida, confianzaLeida) => {
    setError('');
    setEnviando(true);
    try {
      const respuesta = await post(configuracion.ruta, { placa: placaLeida, confianza: confianzaLeida });
      setResultado({ ...interpretar(carril, respuesta), placa: respuesta.placa });
      setVersion((v) => v + 1); // actualiza los cupos de inmediato
    } catch (e) {
      setResultado(null);
      setError(e.estado === 400 ? 'Revisa la placa y el nivel de confianza' : e.message);
    } finally {
      setEnviando(false);
    }
  };

  const enviar = (evento) => {
    evento.preventDefault();
    enviarLectura(placa, confianza);
  };

  /** TT-11: lee la placa de la foto y la envía como lo haría la cámara del carril. */
  const leerConCamara = async (evento) => {
    const foto = evento.target.files?.[0];
    evento.target.value = '';
    if (!foto) return;
    setError('');
    setLeyendo(true);
    try {
      const lectura = await leerPlaca(foto, tipo);
      if (lectura.motivo) {
        setError(MENSAJES_LECTURA[lectura.motivo]);
        return;
      }
      // Las placas de moto terminan en letra (AAA12A); las de carro, en número
      setTipo(/[A-Z]$/.test(lectura.placa) ? 'MOTO' : 'CARRO');
      setPlaca(lectura.placa);
      setConfianza(lectura.confianza);
      await enviarLectura(lectura.placa, lectura.confianza);
    } catch {
      setError('No se pudo leer la foto. Intenta de nuevo o escribe la placa.');
    } finally {
      setLeyendo(false);
    }
  };

  const abierta = resultado ? resultado.abre : null;

  return (
    <main className="sede-pantalla">
      <header className="d-flex justify-content-between align-items-start mb-3">
        <div>
          <div className="sede-etiqueta">Vista de la sede</div>
          <h1 className="h5 fw-bold mb-0">Sede Principal · {configuracion.titulo}</h1>
        </div>
        <span className="badge-simulador">SIMULADOR</span>
      </header>

      <EscenaSede placa={resultado?.placa ?? placa} abierta={abierta} />

      <div className="my-3" aria-live="polite">
        {resultado && (
          <BannerResultado tipo={resultado.aviso ? 'aviso' : resultado.abre ? 'exito' : 'error'}>
            {resultado.aviso ? 'VALIDACIÓN MANUAL' : resultado.lleno ? 'LLENO' : resultado.abre ? 'ABRE' : 'NO ABRE'} ·{' '}
            {resultado.motivo}
          </BannerResultado>
        )}
      </div>

      <input
        ref={camara}
        type="file"
        accept="image/*"
        capture="environment"
        className="d-none"
        onChange={leerConCamara}
      />
      <Button
        type="button"
        variant="light"
        className="w-100 mb-1 fw-semibold"
        disabled={leyendo || enviando}
        onClick={() => camara.current?.click()}
      >
        {leyendo ? 'Leyendo la placa…' : '📷 Leer con la cámara'}
      </Button>
      <p className="sede-etiqueta text-center mb-3">
        Elige primero si es carro o moto. Foto de frente y sin flash.
      </p>

      <Form onSubmit={enviar} className="sede-formulario">
        <Form.Label className="small mb-1">Tipo de vehículo</Form.Label>
        <div className="d-flex gap-2 mb-3">
          {[['CARRO', 'Carro'], ['MOTO', 'Moto']].map(([valor, etiqueta]) => (
            <Button
              key={valor}
              type="button"
              variant={tipo === valor ? 'primary' : 'light'}
              className="rounded-pill px-3"
              aria-pressed={tipo === valor}
              onClick={() => elegirTipo(valor)}
            >
              {etiqueta}
            </Button>
          ))}
        </div>

        <Form.Group className="mb-3" controlId="sede-placa">
          <Form.Label className="small mb-1">Placa</Form.Label>
          <Form.Control
            value={placa}
            onChange={(e) => setPlaca(e.target.value.toUpperCase())}
            placeholder={tipo === 'MOTO' ? 'Ej.: XYZ12D' : 'Ej.: ABC123'}
            autoCapitalize="characters"
            autoComplete="off"
            required
          />
        </Form.Group>

        <Form.Group className="mb-3" controlId="sede-confianza">
          <Form.Label className="small d-flex justify-content-between mb-1">
            <span>Nivel de confianza</span>
            <strong className="text-primary">{Math.round(confianza * 100)} %</strong>
          </Form.Label>
          <Form.Range
            min={0}
            max={1}
            step={0.01}
            value={confianza}
            onChange={(e) => setConfianza(Number(e.target.value))}
          />
        </Form.Group>

        {error && <Alert variant="danger" className="py-2 small">{error}</Alert>}
        <Button type="submit" className="w-100" disabled={enviando}>
          {enviando ? 'Enviando…' : 'Enviar lectura'}
        </Button>
      </Form>

      <div className="d-flex flex-wrap gap-2 mt-3" aria-label="Cupos disponibles">
        {cupos.map((cupo) => (
          <span key={cupo.codigo} className={`chip-cupo ${cupo.lleno ? 'chip-lleno' : ''}`}>
            {cupo.nombre}s {cupo.lleno ? 'LLENO' : `${cupo.disponibles} libres`}
          </span>
        ))}
      </div>
    </main>
  );
}
