package com.autenticar.tuid.v2.services;

import com.autenticar.tuid.v2.model.Phone;
import com.google.gson.annotations.SerializedName;

public class ValidationRequest2 {

    @SerializedName("idUser")
    public int userID;

    @SerializedName("email")
    public String EMail;

    @SerializedName("phone")
    public Phone phone;

    @Override
    public String toString() {
        return "ValidationRequest{" +
                "userID=" + userID +
                ", EMail='" + EMail + '\'' +
                ", CellPhone='" + phone.country + " " + phone.area + " " + phone.phone  + '\'' +
                '}';
    }
}
