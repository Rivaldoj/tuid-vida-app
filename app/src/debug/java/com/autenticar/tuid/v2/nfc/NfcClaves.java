package com.autenticar.tuid.v2.nfc;

/**
 * SOLO DEBUG. Claves de acceso al chip cargadas en la pantalla 2 (por MRZ o por CAN) y usadas por la pantalla 3.
 * Viven solo en memoria y se borran al terminar la lectura: nunca se guardan ni se escriben en el log.
 */
final class NfcClaves {
    static String can = "";
    static String doc = "";
    static String nacimiento = "";
    static String vencimiento = "";

    private NfcClaves() {
    }

    static void borrar() {
        can = "";
        doc = "";
        nacimiento = "";
        vencimiento = "";
    }

    static boolean hayMrz() {
        return !doc.isEmpty() && nacimiento.length() == 6 && vencimiento.length() == 6;
    }
}
