package com.uam.parqueadero.pago;

import java.math.BigDecimal;
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
    private final RegistroPagos pagos;
    private final OperadoresCaja operadores;
    private final SedeActual sede;
    private final Clock reloj;

    public CajaServicio(BuscadorDeCobros buscador, RegistroPagos pagos, OperadoresCaja operadores,
                        SedeActual sede, Clock reloj) {
        this.buscador = buscador;
        this.pagos = pagos;
        this.operadores = operadores;
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
        return buscarCobro(placaLeida);
    }

    /**
     * Registra el pago en efectivo del cobro de la placa. El estado del cobro decide si se puede
     * pagar; el pago guarda el valor, la hora y el operador que lo recibió (CA-3).
     *
     * @param valorRecibido  efectivo que entregó el cliente
     * @param nombreOperador usuario que inició sesión y recibe el dinero
     * @throws CobroNoEncontradoException   si la placa no tiene cobro
     * @throws ValorInsuficienteException   si el efectivo no alcanza (CA-2)
     * @throws CobroNoPagableException      si el cobro no está pendiente
     */
    @Transactional
    public PagoDeCaja pagar(String placaLeida, BigDecimal valorRecibido, String nombreOperador) {
        // Se bloquea el cobro: así repetir o simultanear el pago nunca crea un segundo pago (CA-6)
        CobroPorPagar cobro = buscador.buscarParaPagar(sede.id(), Placa.normalizar(placaLeida), diaOperativoActual())
                .orElseThrow(CobroNoEncontradoException::new);
        DatosPago datos = new DatosPago(
                valorRecibido, operadores.idDe(nombreOperador), nombreOperador, reloj.instant(), reloj.getZone());

        ResultadoPago resultado = cobro.pagar(datos);

        if (resultado.nuevo()) {
            pagos.registrar(resultado.pago());
        }
        return new PagoDeCaja(cobro.placa(), resultado);
    }

    private CobroPorPagar buscarCobro(String placaLeida) {
        return buscador.buscar(sede.id(), Placa.normalizar(placaLeida), diaOperativoActual())
                .orElseThrow(CobroNoEncontradoException::new);
    }

    private LocalDate diaOperativoActual() {
        return DiaOperativo.de(reloj.instant(), reloj.getZone());
    }
}
