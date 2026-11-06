package com.autenticar.tuid.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Los numeros son ficticios; el verificador se calcula con el algoritmo publico modulo 11. */
public class CuilValidatorTest {

    @Test
    public void verificador_ejemplo_calculado_a_mano() {
        // 20 12345678 -> suma = 2*5+0*4+1*3+2*2+3*7+4*6+5*5+6*4+7*3+8*2 = 10+0+3+4+21+24+25+24+21+16 = 148
        // 148 % 11 = 5 -> 11 - 5 = 6
        assertEquals(6, CuilValidator.calcularVerificador("2012345678"));
        assertTrue(CuilValidator.esValido("20123456786"));
    }

    @Test
    public void acepta_guiones_y_espacios() {
        assertTrue(CuilValidator.esValido("20-12345678-6"));
        assertTrue(CuilValidator.esValido(" 20 12345678 6 "));
    }

    @Test
    public void rechaza_verificador_incorrecto() {
        assertFalse(CuilValidator.esValido("20123456787"));
    }

    @Test
    public void rechaza_largo_incorrecto_y_vacios() {
        assertFalse(CuilValidator.esValido("2012345678"));
        assertFalse(CuilValidator.esValido("201234567860"));
        assertFalse(CuilValidator.esValido(""));
        assertFalse(CuilValidator.esValido(null));
        assertFalse(CuilValidator.esValido("abc"));
    }

    @Test
    public void rechaza_prefijo_desconocido() {
        // 99 12345678 ? : aunque el verificador cierre, el prefijo no existe
        int v = CuilValidator.calcularVerificador("9912345678");
        assertFalse(CuilValidator.esValido("9912345678" + Math.max(v, 0)));
    }

    @Test
    public void resto_diez_no_tiene_verificador() {
        // se busca un documento cuyo resto de 10 con prefijo 20
        boolean encontrado = false;
        for (int doc = 10000000; doc < 10000200 && !encontrado; doc++) {
            String diez = "20" + doc;
            if (CuilValidator.calcularVerificador(diez) == -1) {
                assertFalse(CuilValidator.esValido(diez + "0"));
                assertFalse(CuilValidator.esValido(diez + "9"));
                encontrado = true;
            }
        }
        assertTrue(encontrado);
    }

    @Test
    public void mensaje_distingue_largo_de_numero_equivocado() {
        assertEquals(null, CuilValidator.mensajeError("20-12345678-6"));
        assertTrue(CuilValidator.mensajeError("2012345678").contains("usted ingresó 10"));
        assertTrue(CuilValidator.mensajeError("201234567860").contains("usted ingresó 12"));
        // 11 numeros con el verificador equivocado: dice que es INCORRECTO y no habla de la cantidad de numeros
        String m = CuilValidator.mensajeError("27222222229");
        assertTrue(m.startsWith("El CUIL ingresado es incorrecto."));
        assertTrue(!m.contains("11 números"));
        assertEquals("Ingrese el CUIL.", CuilValidator.mensajeError(""));
    }

    @Test
    public void formatear_y_enmascarar() {
        assertEquals("20-12345678-6", CuilValidator.formatear("20123456786"));
        assertEquals("XX-XXXX5678-6", CuilValidator.enmascarar("20123456786"));
        assertEquals("", CuilValidator.enmascarar("123"));
    }
}
