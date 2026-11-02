package com.autenticar.tuid.v2.services;

import com.autenticar.tuid.v2.model.Phone;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ManualRegisterRequest {

    @SerializedName("front")
    public String front;

    @SerializedName("back")
    public String back;

    @SerializedName("selfie")
    public String selfie;

    @SerializedName("phone")
    @Expose
    public Phone phone = new Phone();

    @SerializedName("os")
    public String os;

    @SerializedName("version")
    public String version;

    @SerializedName("id_device")
    public String id_device;
}
