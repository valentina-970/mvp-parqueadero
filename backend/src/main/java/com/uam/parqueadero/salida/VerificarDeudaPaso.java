package com.uam.parqueadero.salida;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import com.uam.parqueadero.comun.DiaOperativo;
import com.uam.parqueadero.comun.Dinero;

/**
 * Tercer paso (RN-06): la barrera abre solo si el cobro del día está pagado o no existe.
 * Un cobro pendiente o vencido la mantiene cerrada. Si el vehículo cruzó las 06:00 y el nuevo
 * día no tiene cobro, se revisa el del día en que ingresó, para que nadie salga sin pagar.
 */
public class VerificarDeudaPaso extends PasoSalida {

    static final String SIN_COBRO = "Sin cobro del día";
    static final String PAGADO = "Cobro del día pagado";

    private final CobrosDelDia cobros;
    private final Clock reloj;

    public VerificarDeudaPaso(CobrosDelDia cobros, Clock reloj) {
        this.cobros = cobros;
        this.reloj = reloj;
    }

    @Override
    protected void aplicar(ContextoSalida contexto) {
        Optional<CobroDia> cobro = buscarCobro(contexto);

        if (cobro.isEmpty()) {
            contexto.autorizar(SIN_COBRO);
            return;
        }
        CobroDia debido = cobro.get();
        if (debeSerPagado(debido.estado())) {
            contexto.valorPendiente(debido.valor());
            String estado = debido.estado() == EstadoCobro.VENCIDO ? "vencido" : "pendiente";
            contexto.rechazar("Cobro " + estado + " por " + Dinero.formatear(debido.valor()));
        } else {
            contexto.autorizar(PAGADO);
        }
    }

    private Optional<CobroDia> buscarCobro(ContextoSalida contexto) {
        LocalDate hoy = DiaOperativo.de(contexto.ahora(), reloj.getZone());
        Optional<CobroDia> cobro = cobros.buscar(contexto.sedeId(), contexto.placa(), hoy);
        if (cobro.isEmpty() && contexto.estancia() != null) {
            LocalDate diaIngreso = DiaOperativo.de(contexto.estancia().ingresoEn(), reloj.getZone());
            if (!diaIngreso.equals(hoy)) {
                cobro = cobros.buscar(contexto.sedeId(), contexto.placa(), diaIngreso);
            }
        }
        return cobro;
    }

    /** Pendiente y vencido son deuda; pagado y anulado no. */
    private static boolean debeSerPagado(EstadoCobro estado) {
        return estado == EstadoCobro.PENDIENTE || estado == EstadoCobro.VENCIDO;
    }
}
