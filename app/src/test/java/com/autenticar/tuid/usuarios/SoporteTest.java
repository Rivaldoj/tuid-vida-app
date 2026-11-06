package com.autenticar.tuid.usuarios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.net.URLDecoder;

public class SoporteTest {

    @Test
    public void el_mensaje_dice_quien_es_y_por_que_se_comunica() {
        String m = Soporte.mensaje("ANA MARIA PRUEBA", "27111111117", "Registración incompleta");
        assertTrue(m.contains("Nombre y apellido: ANA MARIA PRUEBA"));
        assertTrue(m.contains("CUIL: 27-11111111-7"));
        assertTrue(m.contains("Motivo: Registración incompleta"));
    }

    @Test
    public void sin_nombre_deja_el_campo_para_completar() {
        String m = Soporte.mensaje(null, "27111111117", "Registración incompleta");
        assertTrue(m.contains("Nombre y apellido: \n"));
    }

    @Test
    public void la_url_es_de_whatsapp_y_codifica_acentos_espacios_y_saltos() throws Exception {
        String texto = Soporte.mensaje("JOSÉ NÚÑEZ", "20222222223", "Registración incompleta");
        String url = Soporte.urlWhatsApp(texto);
        assertTrue(url.startsWith("https://wa.me/5491123443283?text="));
        assertFalse(url.contains(" "));
        assertFalse(url.contains("\n"));
        assertTrue(url.contains("%0A"));
        // al decodificar vuelve exactamente el mismo texto
        String decodificado = URLDecoder.decode(url.substring(url.indexOf("?text=") + 6), "UTF-8");
        assertEquals(texto, decodificado);
    }
}
