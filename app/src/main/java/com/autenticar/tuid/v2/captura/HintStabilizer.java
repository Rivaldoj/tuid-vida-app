package com.autenticar.tuid.v2.captura;

import com.autenticar.tuid.v2.captura.CaptureQualityEvaluator.Hint;

/**
 * Evita que el aviso parpadee: la indicacion mostrada solo cambia cuando la misma indicacion nueva se
 * repite durante {@code framesParaCambiar} lecturas seguidas.
 */
public final class HintStabilizer {
    private final int framesParaCambiar;
    private Hint estable = Hint.NO_FACE;
    private Hint candidato = null;
    private int repeticiones = 0;

    public HintStabilizer(int framesParaCambiar) {
        this.framesParaCambiar = Math.max(1, framesParaCambiar);
    }

    /** Recibe la lectura cruda de este cuadro y devuelve la indicacion estable a mostrar. */
    public Hint aceptar(Hint lectura) {
        if (lectura == estable) {
            candidato = null;
            repeticiones = 0;
            return estable;
        }
        if (lectura == candidato) {
            repeticiones++;
        } else {
            candidato = lectura;
            repeticiones = 1;
        }
        if (repeticiones >= framesParaCambiar) {
            estable = candidato;
            candidato = null;
            repeticiones = 0;
        }
        return estable;
    }

    public Hint actual() {
        return estable;
    }

    public void reiniciar() {
        estable = Hint.NO_FACE;
        candidato = null;
        repeticiones = 0;
    }
}
