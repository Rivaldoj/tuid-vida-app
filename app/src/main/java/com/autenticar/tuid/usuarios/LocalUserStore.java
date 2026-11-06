package com.autenticar.tuid.usuarios;

import android.content.Context;
import android.content.SharedPreferences;

import com.autenticar.tuid.utils.CuilValidator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Lista de personas que usan este telefono. Es una facilidad de navegacion: NO prueba identidad ni
 * reemplaza ninguna verificacion (cada Fe de Vida repite la prueba de vida y la comparacion facial).
 *
 * Se guarda cifrada. Si no se puede descifrar (por ejemplo cambio de clave del Keystore) se trata como
 * lista vacia: el usuario vuelve a aparecer tras su proxima Fe de Vida, nunca se rompe la app.
 */
public class LocalUserStore {
    private static final String KEY = "usuarios_v1";
    private static final Gson GSON = new Gson();

    private final KeyValue storage;
    private final Crypto crypto;

    public LocalUserStore(KeyValue storage, Crypto crypto) {
        this.storage = storage;
        this.crypto = crypto;
    }

    public static LocalUserStore get(Context context) {
        final SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences("tuid_usuarios", Context.MODE_PRIVATE);
        KeyValue kv = new KeyValue() {
            @Override public String get(String key) { return prefs.getString(key, null); }
            @Override public void put(String key, String value) { prefs.edit().putString(key, value).apply(); }
            @Override public void remove(String key) { prefs.edit().remove(key).apply(); }
        };
        return new LocalUserStore(kv, new KeystoreCrypto("tuid_usuarios_key"));
    }

    public synchronized List<LocalUser> list() {
        String enc = storage.get(KEY);
        if (enc == null || enc.isEmpty()) return new ArrayList<>();
        try {
            String json = crypto.decrypt(enc);
            List<LocalUser> l = GSON.fromJson(json, new TypeToken<List<LocalUser>>() {}.getType());
            return l == null ? new ArrayList<LocalUser>() : l;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public synchronized boolean contains(String cuil) {
        String c = CuilValidator.normalizar(cuil);
        for (LocalUser u : list()) if (u.cuil.equals(c)) return true;
        return false;
    }

    /**
     * Agrega o actualiza el nombre de la persona. Se llama SOLO despues de una Fe de Vida
     * confirmada por el servidor. Devuelve false si el CUIL no es valido o no se pudo guardar.
     */
    public synchronized boolean addOrUpdate(String cuil, String nombre) {
        String c = CuilValidator.normalizar(cuil);
        if (!CuilValidator.esValido(c)) return false;
        String n = nombre == null ? "" : nombre.trim();
        List<LocalUser> l = list();
        String nombreFinal = n;
        for (int i = l.size() - 1; i >= 0; i--) {
            if (l.get(i).cuil.equals(c)) {
                if (n.isEmpty()) nombreFinal = l.get(i).nombre;
                l.remove(i);   // se vuelve a agregar al final: el ultimo de la lista es el que hizo la Fe de Vida mas reciente
            }
        }
        l.add(new LocalUser(c, nombreFinal));
        return save(l);
    }

    /** Quita la persona solo de este telefono; no toca nada en el servidor. */
    public synchronized boolean remove(String cuil) {
        String c = CuilValidator.normalizar(cuil);
        List<LocalUser> l = list();
        boolean changed = false;
        for (int i = l.size() - 1; i >= 0; i--) {
            if (l.get(i).cuil.equals(c)) { l.remove(i); changed = true; }
        }
        return !changed || save(l);
    }

    private boolean save(List<LocalUser> l) {
        try {
            storage.put(KEY, crypto.encrypt(GSON.toJson(l)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
