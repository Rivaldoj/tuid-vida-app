package com.autenticar.tuid.v2.services;

import com.autenticar.tuid.model.ResponseBase;
import com.google.gson.annotations.SerializedName;

public class ManualRegisterSendCodeResponse extends ResponseBase {

    @SerializedName("ticketId")
    public String ticketId;

    @SerializedName("id")
    public int id;

    @SerializedName("url")
    public String url;
}
