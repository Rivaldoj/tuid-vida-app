package com.autenticar.tuid.v2.services;

import com.google.gson.annotations.SerializedName;

public class Entidad {

    @SerializedName("nombre")
    public String nombre = "";

    @SerializedName("img")
    public String img = "";

    @Override
    public String toString() {
        return nombre + " " + img;
    }
}
