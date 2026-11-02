package com.autenticar.tuid.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

public class LoginResponse extends ResponseBase {

    @SerializedName("name")
    public String Name;

    @SerializedName("idUser")
    public Integer userID;

    @SerializedName("banco")
    public String Banco;

    @SerializedName("cuil")
    public String Cuil;

    @SerializedName("fecha_valida")
    public String Fecha_valida;

    @SerializedName("email")
    public String Email;

    @SerializedName("telefono")
    public String Telefono;

    @SerializedName("token")
    public String token;

    @SerializedName("pendingTasks")
    public JsonArray pendingTasks;

    @SerializedName("img")
    public String novedad_image_url;

    @SerializedName("link")
    public String novedad_link;

}
