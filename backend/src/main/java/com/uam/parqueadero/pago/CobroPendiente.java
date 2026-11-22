package com.uam.parqueadero.pago;

import java.math.BigDecimal;
import java.util.UUID;

import com.uam.parqueadero.comun.DiaOperativo;

/**
 * Estado PENDIENTE: el único desde el que se puede pagar. Si el efectivo alcanza, crea el pago
 * con su cambio y el pase vigente hasta el fin del día operativo, y el cobro pasa a PAGADO.
 */
public class CobroPendiente implements EstadoDelCobro {

    @Override
    public String nombre() {
        return "PENDIENTE";
    }

    @Override
    public ResultadoPago pagar(CobroPorPagar cobro, DatosPago datos) {
        BigDecimal faltante = cobro.valor().subtract(datos.valorRecibido());
        if (faltante.signum() > 0) {
            throw new ValorInsuficienteException(faltante);
        }
        BigDecimal cambio = datos.valorRecibido().subtract(cobro.valor());
        Pago pago = new Pago(
                UUID.randomUUID(), cobro.id(), cobro.valor(), datos.valorRecibido(), cambio,
                datos.operadorId(), datos.operadorNombre(), datos.ahora(),
                DiaOperativo.fin(cobro.diaOperativo(), datos.zona()));
        cobro.cambiarEstado(EstadoDelCobro.desde("PAGADO"));
        return ResultadoPago.creado(pago);
    }
}
