package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class RevalidationRequest {
    @SerializedName("idUser")
    public String userID;

    @SerializedName("email")
    public String email;

    @SerializedName("telefono")
    public String telefono;
}
