package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class ReconfirmationResponse extends ResponseBase {
    @SerializedName("email")
    public String Email;

    @SerializedName("telefono")
    public String Telefono;

}
