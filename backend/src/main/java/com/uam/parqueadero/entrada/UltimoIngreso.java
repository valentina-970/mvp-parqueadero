package com.uam.parqueadero.entrada;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

/**
 * Recuerda la última lectura del carril de entrada para que la pantalla del operador la
 * muestre aunque haya llegado desde otro cliente, como el simulador del celular.
 */
@Component
public class UltimoIngreso {

    private final AtomicReference<ResultadoIngreso> ultimo = new AtomicReference<>();

    public void guardar(ResultadoIngreso resultado) {
        ultimo.set(resultado);
    }

    public Optional<ResultadoIngreso> ultimo() {
        return Optional.ofNullable(ultimo.get());
    }
}