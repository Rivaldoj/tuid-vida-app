package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Document {

    @SerializedName("type")
    public String type;

    @SerializedName("number")
    public String number;

    @SerializedName("name")
    public String name;

    @SerializedName("lastname")
    public String lastname;

    @SerializedName("expires")
    public Date expires;

    @SerializedName("tramit")
    public String tramit;

    @SerializedName("gender")
    public String gender;

    @SerializedName("birth")
    public Date birth;

    @SerializedName("address")
    public String address;

    @SerializedName("back")
    public String back;

    @SerializedName("front")
    public String front;

    @SerializedName("photo")
    public String photo;

    @SerializedName("valfront")
    public String valFront;

    @SerializedName("valback")
    public String valBack;

    public Document() {
        type = "DNI";
    }

    @Override
    public String toString() {
        return "{" +
                "type='" + type + '\'' +
                ", number='" + number + '\'' +
                ", name='" + name + '\'' +
                ", lastname='" + lastname + '\'' +
                ", expires='" + expires + '\'' +
                ", tramit='" + tramit + '\'' +
                ", gender='" + gender + '\'' +
                ", birth=" + birth +
                ", valFront=" + valFront +
                ", valBack=" + valBack +
                ", address='" + address + '\'' +
                '}';
    }
}
/*
 "type":"DNI",
                "number":"36755088",
                "name":"GUIDO",
                "lastname":"RIVALDO",
                "expires":"2020-01-30",
                "tramit":"2343243",
                "gender":"M",
                "birth": "1999-01-30",
                "address":"gxcxcvxcvx"
 */