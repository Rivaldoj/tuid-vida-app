package com.autenticar.tuid.v2.captura;

import java.nio.ByteBuffer;

/**
 * Luminancia media (0..255) de una zona de la imagen a partir del plano Y de un cuadro YUV_420_888.
 * Las coordenadas de la zona estan en la imagen "derecha" (ya rotada segun rotationDegrees), como las
 * que devuelve ML Kit; aca se las traslada al buffer original. Se muestrea cada {@code paso} pixeles.
 */
public final class LumaSampler {
    private LumaSampler() {
    }

    public static double mean(ByteBuffer y, int rowStride, int pixelStride, int width, int height,
                              int rotationDegrees, int left, int top, int right, int bottom, int paso) {
        int upW = (rotationDegrees % 180 == 0) ? width : height;
        int upH = (rotationDegrees % 180 == 0) ? height : width;
        left = Math.max(0, left);
        top = Math.max(0, top);
        right = Math.min(upW - 1, right);
        bottom = Math.min(upH - 1, bottom);
        if (right < left || bottom < top) return 0;
        paso = Math.max(1, paso);

        long suma = 0;
        long n = 0;
        for (int yy = top; yy <= bottom; yy += paso) {
            for (int xx = left; xx <= right; xx += paso) {
                int ox, oy;
                switch (rotationDegrees) {
                    case 90:
                        ox = yy;
                        oy = height - 1 - xx;
                        break;
                    case 180:
                        ox = width - 1 - xx;
                        oy = height - 1 - yy;
                        break;
                    case 270:
                        ox = width - 1 - yy;
                        oy = xx;
                        break;
                    default:
                        ox = xx;
                        oy = yy;
                }
                int idx = oy * rowStride + ox * pixelStride;
                if (idx < 0 || idx >= y.limit()) continue;
                suma += (y.get(idx) & 0xFF);
                n++;
            }
        }
        return n == 0 ? 0 : (double) suma / n;
    }
}
