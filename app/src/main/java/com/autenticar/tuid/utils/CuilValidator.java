package com.autenticar.tuid.utils;

/**
 * Normaliza y valida CUIL/CUIT argentinos (11 digitos, dos de prefijo, 8 de documento y un digito
 * verificador modulo 11).
 *
 * Desde 2021 el prefijo (20, 23, 24, 27, 30...) ya no indica el sexo de la persona, por eso este
 * validador NO deduce sexo ni calcula un CUIL a partir de DNI + sexo: solo comprueba que el numero
 * ingresado este bien formado.
 */
public final class CuilValidator {

    private static final int[] PESOS = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    private CuilValidator() {
    }

    /** Deja solo los digitos ("20-12345678-6" -> "20123456786"). Devuelve "" si es null. */
    public static String normalizar(String texto) {
        if (texto == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c >= '0' && c <= '9') sb.append(c);
        }
        return sb.toString();
    }

    /** true si tiene 11 digitos, un prefijo conocido y el digito verificador correcto. */
    public static boolean esValido(String texto) {
        String d = normalizar(texto);
        if (d.length() != 11) return false;
        if (!prefijoPermitido(d.substring(0, 2))) return false;
        int verificador = calcularVerificador(d.substring(0, 10));
        return verificador >= 0 && verificador == d.charAt(10) - '0';
    }

    /**
     * Mensaje para la persona cuando el CUIL no es valido, o null si es valido. Distingue "faltan o sobran
     * numeros" de "tiene 11 numeros pero alguno esta mal" (el ultimo se calcula con los anteriores).
     */
    public static String mensajeError(String texto) {
        String d = normalizar(texto);
        if (d.length() == 0) return "Ingrese el CUIL.";
        if (d.length() != 11) return "El CUIL ingresado es incorrecto: debe tener 11 números y usted ingresó " + d.length() + ".";
        if (esValido(d)) return null;
        // 11 numeros pero alguno esta mal: se dice simplemente que es incorrecto
        return "El CUIL ingresado es incorrecto. Revise que todos los números estén bien escritos; lo encuentra en su recibo de haberes o en su constancia de ANSES.";
    }

    /**
     * Digito verificador para los 10 primeros digitos, o -1 si no existe (resto 10: con ese prefijo
     * el CUIL no es valido).
     */
    static int calcularVerificador(String diezDigitos) {
        int suma = 0;
        for (int i = 0; i < 10; i++) suma += (diezDigitos.charAt(i) - '0') * PESOS[i];
        int resto = 11 - (suma % 11);
        if (resto == 11) return 0;
        if (resto == 10) return -1;
        return resto;
    }

    private static boolean prefijoPermitido(String p) {
        return p.equals("20") || p.equals("23") || p.equals("24") || p.equals("25") || p.equals("26")
                || p.equals("27") || p.equals("30") || p.equals("33") || p.equals("34");
    }

    /** "20123456786" -> "20-12345678-6". Si no tiene 11 digitos devuelve el texto tal cual. */
    public static String formatear(String texto) {
        String d = normalizar(texto);
        if (d.length() != 11) return texto == null ? "" : texto;
        return d.substring(0, 2) + "-" + d.substring(2, 10) + "-" + d.substring(10);
    }

    /** "20-12345678-6" -> "XX-XXXX5678-6": oculta parte para mostrarlo en pantallas compartidas. */
    public static String enmascarar(String texto) {
        String d = normalizar(texto);
        if (d.length() != 11) return "";
        return "XX-XXXX" + d.substring(6, 10) + "-" + d.substring(10);
    }
}
