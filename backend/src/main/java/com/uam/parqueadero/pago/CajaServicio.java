package com.uam.parqueadero.pago;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uam.parqueadero.comun.DiaOperativo;
import com.uam.parqueadero.comun.Placa;
import com.uam.parqueadero.comun.SedeActual;

/** Operaciones de la caja del operador (HU-03). */
@Service
public class CajaServicio {

    private final BuscadorDeCobros buscador;
    private final SedeActual sede;
    private final Clock reloj;

    public CajaServicio(BuscadorDeCobros buscador, SedeActual sede, Clock reloj) {
        this.buscador = buscador;
        this.sede = sede;
        this.reloj = reloj;
    }

    /**
     * Busca por placa el cobro del día operativo (CA-1).
     *
     * @throws CobroNoEncontradoException si la placa no tiene cobro (CA-8)
     */
    @Transactional(readOnly = true)
    public CobroPorPagar consultar(String placaLeida) {
        return buscador.buscar(sede.id(), Placa.normalizar(placaLeida), diaOperativoActual())
                .orElseThrow(CobroNoEncontradoException::new);
    }

    private LocalDate diaOperativoActual() {
        return DiaOperativo.de(reloj.instant(), reloj.getZone());
    }
}
