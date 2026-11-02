package com.autenticar.tuid.v2.nfc;

import java.util.ArrayList;
import java.util.List;

/**
 * Lee la zona de lectura mecanica (MRZ) de documentos OACI 9303 a partir del texto del OCR.
 *
 *  - TD1: tarjeta (nuevo DNI), 3 lineas de 30 caracteres.
 *  - TD3: pasaporte, 2 lineas de 44 caracteres.
 *
 * Solo devuelve un resultado cuando TODOS los digitos de control coinciden, de modo que un OCR
 * con errores se descarta y se espera el siguiente cuadro de la camara.
 */
public final class MrzParser {

    private MrzParser() { }

    public static class Result {
        public String formato;          // "TD1" | "TD3"
        public String tipoDocumento;    // "ID", "P"...
        public String paisEmisor;       // "ARG"
        public String numeroDocumento;  // tal como figura en la MRZ, sin '<' (conserva ceros a la izquierda)
        public String fechaNacimiento;  // AAMMDD
        public String fechaVencimiento; // AAMMDD
        public String sexo;             // M | F | X
        public String nacionalidad;
        public String apellidos;
        public String nombres;
    }

    /** Busca una MRZ valida entre las lineas reconocidas por el OCR. Devuelve null si no hay. */
    public static Result parse(List<String> lineasOcr) {
        List<String> l = new ArrayList<>();
        for (String s : lineasOcr) {
            String n = normalizar(s);
            if (n.length() >= 28 && n.indexOf('<') >= 0 || n.length() >= 40) l.add(n);
        }
        // TD3: dos lineas consecutivas de 44
        for (int i = 0; i + 1 < l.size(); i++) {
            if (l.get(i).length() == 44 && l.get(i + 1).length() == 44) {
                Result r = parseTd3(l.get(i), l.get(i + 1));
                if (r != null) return r;
            }
        }
        // TD1: tres lineas consecutivas de 30
        for (int i = 0; i + 2 < l.size(); i++) {
            if (l.get(i).length() == 30 && l.get(i + 1).length() == 30 && l.get(i + 2).length() == 30) {
                Result r = parseTd1(l.get(i), l.get(i + 1), l.get(i + 2));
                if (r != null) return r;
            }
        }
        return null;
    }

    static String normalizar(String s) {
        if (s == null) return "";
        return s.toUpperCase().replace(" ", "").replace('«', '<').replace('‹', '<').replace('〈', '<')
                .replaceAll("[^A-Z0-9<]", "");
    }

    public static Result parseTd3(String l1, String l2) {
        if (l1.length() != 44 || l2.length() != 44) return null;
        String doc = l2.substring(0, 9);
        String nac = l2.substring(10, 13);
        String nac_ = nac;
        String dob = corregirNumero(l2.substring(13, 19));
        String exp = corregirNumero(l2.substring(21, 27));
        if (!ok(doc, l2.charAt(9)) || !ok(dob, l2.charAt(19)) || !ok(exp, l2.charAt(27))) return null;
        String compuesto = l2.substring(0, 10) + dob + l2.charAt(19) + exp + l2.substring(27, 43);
        if (!ok(compuesto, l2.charAt(43))) return null;

        Result r = new Result();
        r.formato = "TD3";
        r.tipoDocumento = l1.substring(0, 2).replace("<", "");
        r.paisEmisor = l1.substring(2, 5);
        r.numeroDocumento = doc.replace("<", "");
        r.fechaNacimiento = dob;
        r.fechaVencimiento = exp;
        r.sexo = String.valueOf(l2.charAt(20)).replace('<', 'X');
        r.nacionalidad = nac_;
        nombres(l1.substring(5), r);
        return r;
    }

    public static Result parseTd1(String l1, String l2, String l3) {
        if (l1.length() != 30 || l2.length() != 30 || l3.length() != 30) return null;
        String doc = l1.substring(5, 14);
        String dob = corregirNumero(l2.substring(0, 6));
        String exp = corregirNumero(l2.substring(8, 14));
        if (!ok(doc, l1.charAt(14)) || !ok(dob, l2.charAt(6)) || !ok(exp, l2.charAt(14))) return null;
        String compuesto = l1.substring(5, 30) + dob + l2.charAt(6) + exp + l2.charAt(14) + l2.substring(18, 29);
        if (!ok(compuesto, l2.charAt(29))) return null;

        Result r = new Result();
        r.formato = "TD1";
        r.tipoDocumento = l1.substring(0, 2).replace("<", "");
        r.paisEmisor = l1.substring(2, 5);
        r.numeroDocumento = doc.replace("<", "");
        r.fechaNacimiento = dob;
        r.fechaVencimiento = exp;
        r.sexo = String.valueOf(l2.charAt(7)).replace('<', 'X');
        r.nacionalidad = l2.substring(15, 18);
        nombres(l3, r);
        return r;
    }

    private static void nombres(String campo, Result r) {
        int sep = campo.indexOf("<<");
        String ape = sep >= 0 ? campo.substring(0, sep) : campo;
        String nom = sep >= 0 ? campo.substring(sep + 2) : "";
        r.apellidos = ape.replace('<', ' ').trim();
        r.nombres = nom.replace('<', ' ').trim();
    }

    /** Confusiones tipicas del OCR en campos que solo pueden ser numericos. */
    static String corregirNumero(String s) {
        StringBuilder b = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case 'O': case 'Q': case 'D': b.append('0'); break;
                case 'I': case 'L': b.append('1'); break;
                case 'Z': b.append('2'); break;
                case 'S': b.append('5'); break;
                case 'G': b.append('6'); break;
                case 'B': b.append('8'); break;
                default: b.append(c);
            }
        }
        return b.toString();
    }

    private static boolean ok(String campo, char digito) {
        if (digito < '0' || digito > '9') return false;
        return digitoControl(campo) == digito - '0';
    }

    /** Digito de control OACI 9303: pesos 7-3-1; digitos = valor, A-Z = 10-35, '<' = 0. */
    public static int digitoControl(String s) {
        int[] pesos = {7, 3, 1};
        int suma = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int v;
            if (c >= '0' && c <= '9') v = c - '0';
            else if (c >= 'A' && c <= 'Z') v = c - 'A' + 10;
            else v = 0;
            suma += v * pesos[i % 3];
        }
        return suma % 10;
    }
}
