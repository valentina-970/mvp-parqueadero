package com.uam.parqueadero.consulta;

/** Puerto de solo lectura para consultar los cobros. */
public interface ConsultaCobros {

    /** @return los cobros que cumplen el filtro, del día operativo más reciente al más antiguo */
    Pagina<CobroConsultado> buscar(FiltroConsulta filtro);
}