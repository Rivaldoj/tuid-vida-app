package com.autenticar.tuid.v2.captura;

import static org.junit.Assert.assertEquals;

import com.autenticar.tuid.v2.captura.CaptureQualityEvaluator.Hint;

import org.junit.Test;

public class CaptureQualityTest {

    private final CaptureQualityEvaluator ev = new CaptureQualityEvaluator(new CaptureQualityEvaluator.Thresholds());

    @Test
    public void rostro_centrado_y_bien_iluminado_es_ok() {
        assertEquals(Hint.OK, ev.evaluate(1, 0.5, 0.5, 0.5, 130, 120));
    }

    @Test
    public void varios_rostros_tiene_prioridad_sobre_todo() {
        assertEquals(Hint.MULTIPLE_FACES, ev.evaluate(2, 0.1, 0.1, 0.1, 10, 10));
    }

    @Test
    public void sin_rostro_con_luz_normal_pide_ubicar_el_rostro() {
        assertEquals(Hint.NO_FACE, ev.evaluate(0, 0, 0, 0, 0, 120));
    }

    @Test
    public void sin_rostro_y_todo_oscuro_avisa_poca_luz() {
        assertEquals(Hint.TOO_DARK, ev.evaluate(0, 0, 0, 0, 0, 20));
    }

    @Test
    public void rostro_corrido_o_lejos_o_cerca_pide_ajustar_posicion() {
        assertEquals(Hint.ADJUST_POSITION, ev.evaluate(1, 0.9, 0.5, 0.5, 130, 120));
        assertEquals(Hint.ADJUST_POSITION, ev.evaluate(1, 0.5, 0.5, 0.15, 130, 120));
        assertEquals(Hint.ADJUST_POSITION, ev.evaluate(1, 0.5, 0.5, 0.95, 130, 120));
    }

    @Test
    public void iluminacion_del_rostro() {
        assertEquals(Hint.TOO_DARK, ev.evaluate(1, 0.5, 0.5, 0.5, 40, 120));
        assertEquals(Hint.TOO_BRIGHT, ev.evaluate(1, 0.5, 0.5, 0.5, 230, 120));
    }

    @Test
    public void la_posicion_se_avisa_antes_que_la_luz() {
        assertEquals(Hint.ADJUST_POSITION, ev.evaluate(1, 0.9, 0.5, 0.5, 40, 120));
    }

    @Test
    public void estabilizador_no_cambia_por_un_cuadro_suelto() {
        HintStabilizer s = new HintStabilizer(3);
        assertEquals(Hint.NO_FACE, s.actual());
        // llegan 3 OK seguidos -> recien ahi cambia
        assertEquals(Hint.NO_FACE, s.aceptar(Hint.OK));
        assertEquals(Hint.NO_FACE, s.aceptar(Hint.OK));
        assertEquals(Hint.OK, s.aceptar(Hint.OK));
        // un cuadro de TOO_DARK aislado no hace parpadear el aviso
        assertEquals(Hint.OK, s.aceptar(Hint.TOO_DARK));
        assertEquals(Hint.OK, s.aceptar(Hint.OK));
        // lecturas distintas intercaladas reinician la cuenta
        assertEquals(Hint.OK, s.aceptar(Hint.TOO_DARK));
        assertEquals(Hint.OK, s.aceptar(Hint.TOO_BRIGHT));
        assertEquals(Hint.OK, s.aceptar(Hint.TOO_DARK));
        assertEquals(Hint.OK, s.aceptar(Hint.TOO_DARK));
        assertEquals(Hint.TOO_DARK, s.aceptar(Hint.TOO_DARK));
    }
}
