package com.uam.parqueadero.salida;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

/**
 * Recuerda el último intento de salida para que la pantalla del operador lo muestre
 * aunque la lectura haya llegado desde otro cliente, como el simulador del celular.
 */
@Component
public class UltimaSalida {

    private final AtomicReference<ResultadoSalida> ultima = new AtomicReference<>();

    public void guardar(ResultadoSalida resultado) {
        ultima.set(resultado);
    }

    public Optional<ResultadoSalida> ultima() {
        return Optional.ofNullable(ultima.get());
    }
}
