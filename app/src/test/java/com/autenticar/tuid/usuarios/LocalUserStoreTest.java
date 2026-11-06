package com.autenticar.tuid.usuarios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.autenticar.tuid.utils.CuilValidator;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalUserStoreTest {

    private static final String CUIL_A = "20123456786";

    /** Busca el digito verificador de un CUIL ficticio para no depender de numeros reales. */
    private static String cuilValido(String diezDigitos) {
        for (int v = 0; v <= 9; v++) {
            String c = diezDigitos + v;
            if (CuilValidator.esValido(c)) return c;
        }
        throw new IllegalStateException("sin verificador para " + diezDigitos);
    }

    static class MemKv implements KeyValue {
        final Map<String, String> m = new HashMap<>();
        public String get(String k) { return m.get(k); }
        public void put(String k, String v) { m.put(k, v); }
        public void remove(String k) { m.remove(k); }
    }

    /** "Cifrado" reversible de prueba: invierte el texto. */
    static class FakeCrypto implements Crypto {
        boolean romper = false;
        public String encrypt(String p) { return "ENC:" + new StringBuilder(p).reverse(); }
        public String decrypt(String e) throws Exception {
            if (romper || !e.startsWith("ENC:")) throw new Exception("no se puede descifrar");
            return new StringBuilder(e.substring(4)).reverse().toString();
        }
    }

    @Test
    public void vacio_al_inicio() {
        assertTrue(new LocalUserStore(new MemKv(), new FakeCrypto()).list().isEmpty());
    }

    @Test
    public void agrega_y_persiste_cifrado() {
        MemKv kv = new MemKv();
        LocalUserStore s = new LocalUserStore(kv, new FakeCrypto());
        assertTrue(s.addOrUpdate("20-12345678-6", "PEREZ, JUAN"));
        String guardado = kv.get("usuarios_v1");
        assertFalse(guardado.contains(CUIL_A));
        assertFalse(guardado.contains("PEREZ"));
        // una instancia nueva (como tras cerrar la app) lo lee
        List<LocalUser> l = new LocalUserStore(kv, new FakeCrypto()).list();
        assertEquals(1, l.size());
        assertEquals(CUIL_A, l.get(0).cuil);
        assertEquals("PEREZ, JUAN", l.get(0).nombre);
    }

    @Test
    public void no_duplica_por_cuil_y_actualiza_nombre() {
        LocalUserStore s = new LocalUserStore(new MemKv(), new FakeCrypto());
        s.addOrUpdate(CUIL_A, "PEREZ, JUAN");
        s.addOrUpdate("20-12345678-6", "PEREZ, JUAN CARLOS");
        assertEquals(1, s.list().size());
        assertEquals("PEREZ, JUAN CARLOS", s.list().get(0).nombre);
    }

    @Test
    public void el_ultimo_en_certificar_queda_al_final() {
        LocalUserStore s = new LocalUserStore(new MemKv(), new FakeCrypto());
        String otro = cuilValido("2798765432");
        s.addOrUpdate(CUIL_A, "A");
        s.addOrUpdate(otro, "B");
        s.addOrUpdate(CUIL_A, "A");   // A vuelve a certificar
        assertEquals(2, s.list().size());
        assertEquals(otro, s.list().get(0).cuil);
        assertEquals(CUIL_A, s.list().get(1).cuil);
    }

    @Test
    public void rechaza_cuil_invalido() {
        LocalUserStore s = new LocalUserStore(new MemKv(), new FakeCrypto());
        assertFalse(s.addOrUpdate("20123456787", "X"));
        assertTrue(s.list().isEmpty());
    }

    @Test
    public void varios_usuarios_y_quitar() {
        LocalUserStore s = new LocalUserStore(new MemKv(), new FakeCrypto());
        String otro = cuilValido("2798765432");
        s.addOrUpdate(CUIL_A, "A");
        s.addOrUpdate(otro, "B");
        assertEquals(2, s.list().size());
        assertTrue(s.remove(CUIL_A));
        assertEquals(1, s.list().size());
        assertEquals(otro, s.list().get(0).cuil);
        assertFalse(s.contains(CUIL_A));
    }

    @Test
    public void si_no_se_puede_descifrar_queda_vacio_sin_romper() {
        MemKv kv = new MemKv();
        FakeCrypto c = new FakeCrypto();
        LocalUserStore s = new LocalUserStore(kv, c);
        s.addOrUpdate(CUIL_A, "A");
        c.romper = true;
        assertTrue(s.list().isEmpty());
    }
}
