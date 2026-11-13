package com.autenticar.tuid.v2;

/**
 * Datos del DNI nuevo leidos del chip NFC, para el registro (POST /vida/v2/signup).
 * Los llena la pantalla de verificacion NFC (solo version de desarrollo) y los usa PreGestureActivity.
 * Viven solo en memoria y se borran al terminar el registro. Nunca se guardan ni se escriben en el log.
 *
 * La foto del chip se envia como "photo" (la que compara el servidor contra la prueba de vida) y tambien
 * ocupa el lugar del frente del DNI (archivo NUMEROSEXO_F); no se fotografian el frente ni el dorso.
 */
public final class RegistroNfcDatos {
    public static boolean activo = false;
    public static String numero;
    public static String nombre;
    public static String apellido;
    public static String sexo;          // M | F | X
    public static String nacimiento;    // yyyy-MM-dd
    public static String vencimiento;   // yyyy-MM-dd
    public static String fotoBase64;    // JPEG del chip
    public static String mrz;           // lineas de la MRZ separadas por \n (solo si entro por camara)
    public static String can;           // CAN de 6 digitos (solo si entro por CAN)
    public static String tramite;       // numero de tramite, si el chip lo trae
    public static String pais;          // nacionalidad del chip (ARG)

    private RegistroNfcDatos() {
    }

    public static void borrar() {
        activo = false;
        numero = null;
        nombre = null;
        apellido = null;
        sexo = null;
        nacimiento = null;
        vencimiento = null;
        fotoBase64 = null;
        mrz = null;
        can = null;
        tramite = null;
        pais = null;
    }
}
