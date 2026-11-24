import { Pagination } from 'react-bootstrap';

/** Paginación simple: anterior, "Página X de Y · N registros" y siguiente (HU-09, CA-5). */
export default function Paginador({ pagina, totalPaginas, totalElementos, alCambiar }) {
  if (totalPaginas <= 1) {
    return <p className="small text-secondary mb-0">{totalElementos} registro(s)</p>;
  }
  return (
    <div className="d-flex align-items-center gap-3">
      <Pagination className="mb-0">
        <Pagination.Prev disabled={pagina === 0} onClick={() => alCambiar(pagina - 1)} />
        <Pagination.Next disabled={pagina >= totalPaginas - 1} onClick={() => alCambiar(pagina + 1)} />
      </Pagination>
      <span className="small text-secondary">
        Página {pagina + 1} de {totalPaginas} · {totalElementos} registros
      </span>
    </div>
  );
}