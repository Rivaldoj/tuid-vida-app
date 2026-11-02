package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class SearchdniRequest {
    @SerializedName("dni")
    public String dni;
    @SerializedName("sexo")
    public String sexo;
}
