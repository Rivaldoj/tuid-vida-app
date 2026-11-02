package com.autenticar.tuid.v2.services;

import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;

public class FormSubmitRequest {

    @SerializedName("id")
    public int id;

    @SerializedName("cuestionario")
    public JsonArray cuestionario;
}