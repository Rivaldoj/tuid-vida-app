package com.autenticar.tuid.v2.services;

import com.google.gson.annotations.SerializedName;

public class CheckDocumentRequest {

    @SerializedName("dni")
    public String dni;

    @SerializedName("sexo")
    public String sexo;

    @SerializedName("birth")
    public String birth;
}
