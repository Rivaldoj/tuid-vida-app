package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class SearchdniResponse extends ResponseBase {

    @SerializedName("idValidation")
    public int idValidacion;

    @SerializedName("url")
    public String url;

    @SerializedName("code")
    public int code;

}
