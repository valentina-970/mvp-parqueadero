package com.uam.parqueadero.salida.infraestructura;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import com.uam.parqueadero.salida.Barrera;
import com.uam.parqueadero.salida.EstadoBarrera;

/** Barrera simulada: solo recuerda su posición, que la interfaz muestra al operador. */
@Component
public class BarreraSimulada implements Barrera {

    private final AtomicReference<EstadoBarrera> estado = new AtomicReference<>(EstadoBarrera.CERRADA);

    @Override
    public void abrir() {
        estado.set(EstadoBarrera.ABIERTA);
    }

    @Override
    public void cerrar() {
        estado.set(EstadoBarrera.CERRADA);
    }

    @Override
    public EstadoBarrera estado() {
        return estado.get();
    }
}
