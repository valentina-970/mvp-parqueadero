package com.uam.parqueadero.entrada;

/**
 * Eslabón de la cadena que decide si se registra un ingreso (patrón Chain of Responsibility,
 * el mismo de la salida). Cada paso aplica una sola regla (principio S) y, si nadie ha
 * decidido todavía, entrega el contexto al siguiente.
 */
public abstract class PasoEntrada {

    private PasoEntrada siguiente;

    /**
     * Enlaza el paso que sigue a este.
     *
     * @return el paso recibido, para poder encadenar varias llamadas
     */
    public PasoEntrada enlazar(PasoEntrada siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    /** Aplica la regla de este paso y continúa con el siguiente si el resultado sigue abierto. */
    public final void ejecutar(ContextoEntrada contexto) {
        aplicar(contexto);
        if (!contexto.decidido() && siguiente != null) {
            siguiente.ejecutar(contexto);
        }
    }

    protected abstract void aplicar(ContextoEntrada contexto);
}