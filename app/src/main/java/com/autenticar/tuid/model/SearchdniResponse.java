package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class SearchdniResponse extends ResponseBase {

    @SerializedName("idValidation")
    public int idValidacion;

    // v2: sesion de AWS Face Liveness para esta validacion (null en el backend v1)
    @SerializedName("sessionId")
    public String sessionId;

    // v2 por CUIL: "NOT_REGISTERED" cuando no existe un registro para ese CUIL
    @SerializedName("registration")
    public String registration;

    @SerializedName("url")
    public String url;

    @SerializedName("code")
    public int code;

}
