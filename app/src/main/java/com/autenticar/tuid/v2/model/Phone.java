package com.autenticar.tuid.v2.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Phone {

    @SerializedName("country")
    @Expose
    public String country;

    @SerializedName("area")
    @Expose
    public String area;

    @SerializedName("phone")
    @Expose
    public String phone;

    public boolean isValid(){
        return country != null && country.length()>0 && area != null && area.length()>0 && phone != null && phone.length() >= 0;
    }
}
