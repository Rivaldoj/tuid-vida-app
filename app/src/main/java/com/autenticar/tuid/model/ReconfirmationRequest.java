package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class ReconfirmationRequest {
    @SerializedName("idUser")
    public String userID;

    @SerializedName("codeTel")
    public String CodeTel;

    @SerializedName("codeEmail")
    public String CodeEmail;
}
