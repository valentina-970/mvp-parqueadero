package com.uam.parqueadero.consulta;

/** Puerto de solo lectura para consultar los eventos de acceso. */
public interface ConsultaEventos {

    /** @return los eventos que cumplen el filtro, del más reciente al más antiguo */
    Pagina<EventoConsultado> buscar(FiltroConsulta filtro);
}