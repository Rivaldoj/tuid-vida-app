package com.autenticar.tuid.v2.captura;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.nio.ByteBuffer;

public class LumaSamplerTest {

    /** Imagen 4 (ancho) x 2 (alto): la mitad izquierda (cols 0-1) vale 200 y la derecha (cols 2-3) vale 20. */
    private static ByteBuffer imagen() {
        byte[] d = new byte[8];
        for (int y = 0; y < 2; y++)
            for (int x = 0; x < 4; x++)
                d[y * 4 + x] = (byte) (x < 2 ? 200 : 20);
        return ByteBuffer.wrap(d);
    }

    @Test
    public void sin_rotacion_la_zona_izquierda_es_clara() {
        double izq = LumaSampler.mean(imagen(), 4, 1, 4, 2, 0, 0, 0, 1, 1, 1);
        double der = LumaSampler.mean(imagen(), 4, 1, 4, 2, 0, 2, 0, 3, 1, 1);
        assertEquals(200, izq, 0.01);
        assertEquals(20, der, 0.01);
    }

    @Test
    public void rotada_90_la_mitad_clara_pasa_arriba() {
        // al girar 90 grados en sentido horario la columna izquierda original queda ARRIBA
        // imagen derecha: ancho 2, alto 4; filas 0-1 claras, filas 2-3 oscuras
        double arriba = LumaSampler.mean(imagen(), 4, 1, 4, 2, 90, 0, 0, 1, 1, 1);
        double abajo = LumaSampler.mean(imagen(), 4, 1, 4, 2, 90, 0, 2, 1, 3, 1);
        assertEquals(200, arriba, 0.01);
        assertEquals(20, abajo, 0.01);
    }

    @Test
    public void rotada_180_y_270() {
        double izq180 = LumaSampler.mean(imagen(), 4, 1, 4, 2, 180, 0, 0, 1, 1, 1);
        assertEquals(20, izq180, 0.01);   // lo claro pasa a la derecha
        double arriba270 = LumaSampler.mean(imagen(), 4, 1, 4, 2, 270, 0, 0, 1, 1, 1);
        assertEquals(20, arriba270, 0.01); // 270: lo claro queda abajo
    }

    @Test
    public void zona_fuera_de_la_imagen_devuelve_cero() {
        assertEquals(0, LumaSampler.mean(imagen(), 4, 1, 4, 2, 0, 10, 10, 12, 12, 1), 0.0);
    }
}
