package com.autenticar.tuid.v2.services;

import com.autenticar.tuid.model.ResponseBase;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetEntitiesResponse {

    @SerializedName("data")
    public List<Entidad> entidades;

    public List<Entidad> getEntities() {
        return entidades;
    }



}

