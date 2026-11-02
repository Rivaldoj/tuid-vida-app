package com.autenticar.tuid.v2.services;

import com.autenticar.tuid.v2.model.Phone;
import com.google.gson.annotations.SerializedName;

public class ContactRequest2 {
    @SerializedName("cuil")
    public String cuil;

    @SerializedName("nombre")
    public String nombre;

    @SerializedName("phone")
    public Phone phone = new Phone();

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
