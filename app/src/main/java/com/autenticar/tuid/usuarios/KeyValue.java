package com.autenticar.tuid.usuarios;

/** Almacen minimo clave/valor (SharedPreferences en la app, memoria en los tests). */
public interface KeyValue {
    String get(String key);

    void put(String key, String value);

    void remove(String key);
}
