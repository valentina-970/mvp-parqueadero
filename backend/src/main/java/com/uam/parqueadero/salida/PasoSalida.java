package com.uam.parqueadero.salida;

/**
 * Eslabón de la cadena de responsabilidad que decide si un vehículo puede salir
 * (patrón Chain of Responsibility). Cada paso aplica una sola regla y, si la salida
 * no fue rechazada, entrega el contexto al siguiente. Agregar una regla nueva
 * (por ejemplo el cupo o la confianza de HU-06) es crear un paso y enlazarlo.
 */
public abstract class PasoSalida {

    private PasoSalida siguiente;

    /**
     * Enlaza el paso que sigue a este.
     *
     * @return el paso recibido, para poder encadenar varias llamadas
     */
    public PasoSalida enlazar(PasoSalida siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    /** Aplica la regla de este paso y continúa con el siguiente si no hubo rechazo. */
    public final void ejecutar(ContextoSalida contexto) {
        aplicar(contexto);
        if (!contexto.rechazada() && siguiente != null) {
            siguiente.ejecutar(contexto);
        }
    }

    /** Regla propia del paso. Para rechazar, llama a {@link ContextoSalida#rechazar(String)}. */
    protected abstract void aplicar(ContextoSalida contexto);
}
