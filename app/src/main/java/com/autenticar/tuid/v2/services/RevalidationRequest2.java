package com.autenticar.tuid.v2.services;

import com.autenticar.tuid.v2.model.Phone;
import com.google.gson.annotations.SerializedName;

public class RevalidationRequest2 {

    @SerializedName("idUser")
    public String userID;

    @SerializedName("email")
    public String email;

    @SerializedName("telefono")
    public Phone phone;
}
