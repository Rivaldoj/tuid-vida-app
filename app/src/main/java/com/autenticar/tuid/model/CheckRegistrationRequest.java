package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class CheckRegistrationRequest {
    @SerializedName("cuil")
    public String cuil;

    public CheckRegistrationRequest(String cuil) {
        this.cuil = cuil;
    }
}
