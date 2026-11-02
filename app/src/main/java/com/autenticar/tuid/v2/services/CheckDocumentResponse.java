package com.autenticar.tuid.v2.services;

import com.autenticar.tuid.model.ResponseBase;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CheckDocumentResponse extends ResponseBase {

    @SerializedName("proceed")
    @Expose
    public boolean proceed;

    @SerializedName("code")
    @Expose
    public String resultCode;

}
