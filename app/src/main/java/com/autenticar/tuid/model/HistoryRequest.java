package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class HistoryRequest {
    @SerializedName("cuil")
    public String cuil;

    /** Token de notificaciones del telefono: el servidor solo responde al telefono vinculado. */
    @SerializedName("idDevice")
    public String idDevice;

    public HistoryRequest(String cuil, String idDevice) {
        this.cuil = cuil;
        this.idDevice = idDevice;
    }
}
