package com.uam.parqueadero.cobro;

/**
 * Eslabón de la cadena que decide si un ingreso genera cobro (patrón Chain of
 * Responsibility, igual que en la entrada y la salida). Cada paso aplica una sola regla y,
 * si el cobro no se descartó, entrega el contexto al siguiente.
 */

public abstract class PasoCobro {

    private PasoCobro siguiente;

    /**
     * Enlaza el paso que sigue a este.
     *
     * @return el paso recibido, para poder encadenar varias llamadas
     */

    public PasoCobro enlazar(PasoCobro siguiente){
        this.siguiente = siguiente;
        return siguiente;
    }

    /** Aplica la regla de este paso y continúa con el siguiente si el cobro sigue en pie. */

    public final void ejecutar(ContextoCobro contexto){
        aplicar(contexto);
        if(!contexto.omitido() && siguiente != null){
            siguiente.ejecutar(contexto);
        }
    }

    protected abstract void aplicar(ContextoCobro contexto);

}
