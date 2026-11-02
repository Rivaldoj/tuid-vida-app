package com.autenticar.tuid.v2.services;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Certificado {

    @SerializedName("empresas")
    @Expose
    public List<String> empresas;

    @SerializedName("cuil")
    @Expose
    public String cuil;

    @SerializedName("ultima")
    @Expose
    public String ultima;

    @SerializedName("nombre")
    @Expose
    public String nombre;

    @SerializedName("fecha")
    @Expose
    public String fecha;

    @SerializedName("token")
    @Expose
    public String token;

    @SerializedName("url")
    @Expose
    public String url;

    @SerializedName("nacimiento")
    @Expose
    public String nacimiento;

    @SerializedName("validez")
    @Expose
    public String validez;



}
