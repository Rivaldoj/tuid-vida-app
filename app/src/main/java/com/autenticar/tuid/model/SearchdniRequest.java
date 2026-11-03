package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class SearchdniRequest {
    @SerializedName("dni")
    public String dni;
    @SerializedName("sexo")
    public String sexo;

    // solo los usa el backend v2 (auditoria); el v1 los ignora
    @SerializedName("os")
    public String os;
    @SerializedName("version")
    public String version;
    @SerializedName("appversion")
    public String appversion;
}
