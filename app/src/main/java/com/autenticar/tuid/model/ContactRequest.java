package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class ContactRequest {
    @SerializedName("cuil")
    public String cuil;

    @SerializedName("nombre")
    public String nombre;

    @SerializedName("telefono")
    public String telefono;

    @SerializedName("email")
    public String email;

    @SerializedName("asunto")
    public String asunto;

    @SerializedName("mensaje")
    public String mensaje;

    // SEND APP VERSION
    @SerializedName("appVersion")
    public String appVersion = "";

    @SerializedName("os")
    public String os;

    @SerializedName("version")
    public String version;

    @SerializedName("code")
    public String code;
    //  ------
}
