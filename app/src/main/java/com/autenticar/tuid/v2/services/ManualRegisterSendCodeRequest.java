package com.autenticar.tuid.v2.services;

import com.google.gson.annotations.SerializedName;

public class ManualRegisterSendCodeRequest {

    @SerializedName("code")
    public int code;

    @SerializedName("usuario_id")
    public int usuario_id;
}
