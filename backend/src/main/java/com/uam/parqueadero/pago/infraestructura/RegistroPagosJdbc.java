package com.uam.parqueadero.pago.infraestructura;

import java.time.Clock;
import java.time.OffsetDateTime;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.uam.parqueadero.pago.CobroNoPagableException;
import com.uam.parqueadero.pago.Pago;
import com.uam.parqueadero.pago.RegistroPagos;

/** Implementación del puerto {@link RegistroPagos} sobre las tablas {@code pago} y {@code cobro}. */
@Repository
public class RegistroPagosJdbc implements RegistroPagos {

    private final JdbcTemplate jdbc;
    private final Clock reloj;

    public RegistroPagosJdbc(JdbcTemplate jdbc, Clock reloj) {
        this.jdbc = jdbc;
        this.reloj = reloj;
    }

    @Override
    public void registrar(Pago pago) {
        // Solo un cobro PENDIENTE pasa a PAGADO: si otra petición ya lo pagó, no se actualiza nada
        int actualizados = jdbc.update(
                "UPDATE cobro SET estado = 'PAGADO' WHERE id = ? AND estado = 'PENDIENTE'", pago.cobroId());
        if (actualizados != 1) {
            throw new CobroNoPagableException("El cobro ya no está pendiente");
        }
        jdbc.update("""
                INSERT INTO pago (id, cobro_id, valor, valor_recibido, cambio, medio,
                    recibido_por_id, pagado_en, pase_vigente_hasta)
                VALUES (?, ?, ?, ?, ?, 'EFECTIVO', ?, ?, ?)
                """,
                pago.id(), pago.cobroId(), pago.valor(), pago.valorRecibido(), pago.cambio(),
                pago.operadorId(),
                OffsetDateTime.ofInstant(pago.pagadoEn(), reloj.getZone()),
                OffsetDateTime.ofInstant(pago.paseVigenteHasta(), reloj.getZone()));
    }
}
