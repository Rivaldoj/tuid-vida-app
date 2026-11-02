package com.autenticar.tuid.v2.model;

import androidx.annotation.NonNull;

public class TelefonoCodigoPais {

    public String pais;
    public String codigo;

    public TelefonoCodigoPais(String pais, String codigo){
        this.pais = pais;
        this.codigo = codigo;
    }

    @NonNull
    @Override
    public String toString() {
        return pais + " " + codigo;
    }
}
