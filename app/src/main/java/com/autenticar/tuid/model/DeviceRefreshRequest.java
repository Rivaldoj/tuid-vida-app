package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

/**
 * Aviso de que Firebase renovo el token de notificaciones del telefono. El servidor solo lo cambia si
 * {@code idDevice} (el token anterior) coincide con el que tiene guardado: esa es la prueba de que es el mismo telefono.
 */
public class DeviceRefreshRequest {
    @SerializedName("cuil")
    public String cuil;

    /** Token anterior: el que el servidor tiene guardado para esta persona. */
    @SerializedName("idDevice")
    public String idDevice;

    /** Token nuevo, el vigente en este telefono. */
    @SerializedName("newIdDevice")
    public String newIdDevice;

    public DeviceRefreshRequest(String cuil, String idDevice, String newIdDevice) {
        this.cuil = cuil;
        this.idDevice = idDevice;
        this.newIdDevice = newIdDevice;
    }
}
