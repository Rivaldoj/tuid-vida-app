package com.autenticar.tuid.v2.captura;

/**
 * Decide que indicacion mostrar antes de la prueba de vida. Logica pura (sin Android) para poder
 * probarla. Muestra UNA sola indicacion por vez, la mas prioritaria.
 *
 * Los umbrales son valores iniciales: hay que calibrarlos con pruebas en telefonos reales.
 * Los anteojos NO se evaluan aca: ML Kit no los detecta de forma confiable; se muestra un aviso fijo.
 */
public final class CaptureQualityEvaluator {

    public enum Hint {
        OK,
        NO_FACE,
        MULTIPLE_FACES,
        ADJUST_POSITION,
        TOO_DARK,
        TOO_BRIGHT
    }

    public static final class Thresholds {
        /** Luminancia media (0-255) del rostro por debajo de la cual hay poca luz. */
        public double minFaceLuma = 70;
        /** Luminancia media (0-255) del rostro por encima de la cual hay exceso de luz. */
        public double maxFaceLuma = 200;
        /** Si no hay rostro y la imagen entera esta por debajo de este valor, se avisa poca luz. */
        public double minFrameLuma = 45;
        /** Ancho minimo del rostro como fraccion del ancho de la imagen (muy lejos). */
        public double minFaceWidth = 0.30;
        /** Ancho maximo del rostro como fraccion del ancho de la imagen (muy cerca). */
        public double maxFaceWidth = 0.80;
        /** Desvio maximo del centro del rostro respecto del centro de la imagen (fraccion). */
        public double maxCenterOffset = 0.18;
    }

    private final Thresholds t;

    public CaptureQualityEvaluator(Thresholds thresholds) {
        this.t = thresholds;
    }

    /**
     * @param faceCount  cantidad de rostros detectados
     * @param centerX    centro horizontal del rostro (0..1), solo si faceCount == 1
     * @param centerY    centro vertical del rostro (0..1), solo si faceCount == 1
     * @param widthFrac  ancho del rostro / ancho de la imagen (0..1), solo si faceCount == 1
     * @param faceLuma   luminancia media (0..255) de la zona del rostro, solo si faceCount == 1
     * @param frameLuma  luminancia media (0..255) de toda la imagen
     */
    public Hint evaluate(int faceCount, double centerX, double centerY, double widthFrac,
                         double faceLuma, double frameLuma) {
        if (faceCount > 1) return Hint.MULTIPLE_FACES;
        if (faceCount == 0) {
            return frameLuma < t.minFrameLuma ? Hint.TOO_DARK : Hint.NO_FACE;
        }
        if (Math.abs(centerX - 0.5) > t.maxCenterOffset || Math.abs(centerY - 0.5) > t.maxCenterOffset
                || widthFrac < t.minFaceWidth || widthFrac > t.maxFaceWidth) {
            return Hint.ADJUST_POSITION;
        }
        if (faceLuma < t.minFaceLuma) return Hint.TOO_DARK;
        if (faceLuma > t.maxFaceLuma) return Hint.TOO_BRIGHT;
        return Hint.OK;
    }
}
