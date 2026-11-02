package com.autenticar.tuid.v2.nfc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.Arrays;

/** Usa los ejemplos publicados en el documento OACI 9303 (datos ficticios de "UTOPIA"). */
public class MrzParserTest {

    @Test
    public void td3_pasaporte_ejemplo_oaci() {
        MrzParser.Result r = MrzParser.parse(Arrays.asList(
                "P<UTOERIKSSON<<ANNA<MARIA<<<<<<<<<<<<<<<<<<<",
                "L898902C36UTO7408122F1204159ZE184226B<<<<<10"));
        assertNotNull(r);
        assertEquals("TD3", r.formato);
        assertEquals("L898902C3", r.numeroDocumento);
        assertEquals("740812", r.fechaNacimiento);
        assertEquals("120415", r.fechaVencimiento);
        assertEquals("F", r.sexo);
        assertEquals("ERIKSSON", r.apellidos);
        assertEquals("ANNA MARIA", r.nombres);
    }

    @Test
    public void td1_tarjeta_ejemplo_oaci() {
        MrzParser.Result r = MrzParser.parse(Arrays.asList(
                "I<UTOD231458907<<<<<<<<<<<<<<<",
                "7408122F1204159UTO<<<<<<<<<<<6",
                "ERIKSSON<<ANNA<MARIA<<<<<<<<<<"));
        assertNotNull(r);
        assertEquals("TD1", r.formato);
        assertEquals("D23145890", r.numeroDocumento);
        assertEquals("740812", r.fechaNacimiento);
        assertEquals("120415", r.fechaVencimiento);
        assertEquals("ANNA MARIA", r.nombres);
    }

    @Test
    public void tolera_espacios_y_minusculas_del_ocr() {
        MrzParser.Result r = MrzParser.parse(Arrays.asList(
                "i<uto d231458907<<<<<<<<<<<<<<<",
                "7408122f1204159uto<<<<<<<<<<<6",
                "eriksson<<anna<maria<<<<<<<<<<"));
        assertNotNull(r);
        assertEquals("D23145890", r.numeroDocumento);
    }

    @Test
    public void corrige_letra_O_en_fecha_numerica() {
        MrzParser.Result r = MrzParser.parse(Arrays.asList(
                "I<UTOD231458907<<<<<<<<<<<<<<<",
                "74O8122F1204159UTO<<<<<<<<<<<6",
                "ERIKSSON<<ANNA<MARIA<<<<<<<<<<"));
        assertNotNull(r);
        assertEquals("740812", r.fechaNacimiento);
    }

    @Test
    public void rechaza_si_falla_un_digito_de_control() {
        MrzParser.Result r = MrzParser.parse(Arrays.asList(
                "I<UTOD231458907<<<<<<<<<<<<<<<",
                "7408123F1204159UTO<<<<<<<<<<<6", // fecha alterada
                "ERIKSSON<<ANNA<MARIA<<<<<<<<<<"));
        assertNull(r);
    }

    @Test
    public void ignora_texto_que_no_es_mrz() {
        assertNull(MrzParser.parse(Arrays.asList("REPUBLICA ARGENTINA", "Domicilio: CALLE 123")));
    }

    @Test
    public void digito_de_control_ejemplo_oaci() {
        assertEquals(7, MrzParser.digitoControl("D23145890"));
        assertEquals(2, MrzParser.digitoControl("740812"));
        assertEquals(9, MrzParser.digitoControl("120415"));
    }
}
