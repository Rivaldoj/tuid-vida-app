package com.autenticar.tuid.v2.services;

import com.autenticar.tuid.model.ResponseBase;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import androidx.annotation.Nullable;

public class SolicitarCertificadoResponse{

    @SerializedName("data")
    @Expose
    public Certificado certificado;

    @SerializedName("error")
    @Expose
    public int error;

    @SerializedName("code")
    @Expose
    public String code;

    @SerializedName("message")
    @Expose
    public String message;


    public boolean isOk(){
        return error == 0;
    }
}
