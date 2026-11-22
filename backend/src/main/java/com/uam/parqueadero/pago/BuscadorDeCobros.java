package com.uam.parqueadero.pago;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto con lo único que la caja necesita para encontrar un cobro (principios I y D):
 * no expone cómo se crean los cobros ni cómo se guardan.
 */
public interface BuscadorDeCobros {

    /**
     * Busca el cobro de la placa en el día operativo. Si ese día no tiene cobro, devuelve el
     * del día anterior solo si sigue pendiente: así el vehículo que entró antes de las 06:00
     * puede pagar después de que cambie el día operativo. Si el cobro ya está pagado, trae
     * también su pago.
     */
    Optional<CobroPorPagar> buscar(UUID sedeId, String placa, LocalDate diaOperativo);

    /**
     * Igual que {@link #buscar}, pero bloquea el cobro hasta que termine la transacción. Se usa
     * al pagar: dos pagos simultáneos del mismo cobro se atienden uno tras otro, y el segundo
     * encuentra el cobro ya pagado y recibe el pago existente.
     */
    Optional<CobroPorPagar> buscarParaPagar(UUID sedeId, String placa, LocalDate diaOperativo);
}
