package com.autenticar.tuid.usuarios;

/** Persona guardada en este telefono: solo apellido y nombres + CUIL (sin fotos ni datos biometricos). */
public final class LocalUser {
    public final String cuil;      // 11 digitos, sin guiones
    public final String nombre;    // tal cual lo informa el servidor, ej. "PEREZ, JUAN CARLOS"

    public LocalUser(String cuil, String nombre) {
        this.cuil = cuil;
        this.nombre = nombre;
    }
}
