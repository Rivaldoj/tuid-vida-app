package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {

    @SerializedName("idDevice")
    public String DeviceID;

    @SerializedName("selfie")
    public String selfie;

    @SerializedName("idDocument")
    public Document DataDocument;

    @Override
    public String toString() {
        return "RegisterRequest{" +
                "idDevice='" + DeviceID + '\'' +
                '}';
    }

    // SEND APP VERSION
    @SerializedName("appVersion")
    public String appVersion ="";

    @SerializedName("os")
    public String os;

    @SerializedName("version")
    public String version;
    //  ------

}

