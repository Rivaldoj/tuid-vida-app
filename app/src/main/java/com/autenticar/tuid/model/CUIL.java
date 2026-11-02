package com.autenticar.tuid.model;

public class CUIL {
    private String id_cuil;
    private String num_cuil;
    private String date_cuil;
    private String longitude_cuil;
    private String latitude_cuil;
    private String diretion_cuil;
    private String entidad_pagadora_cuil;

    public CUIL(String id_cuil, String num_cuil, String date_cuil, String longitude_cuil, String latitude_cuil, String diretion_cuil, String entidad_pagadora_cuil) {
        this.id_cuil = id_cuil;
        this.num_cuil = num_cuil;
        this.date_cuil = date_cuil;
        this.longitude_cuil = longitude_cuil;
        this.latitude_cuil = latitude_cuil;
        this.diretion_cuil = diretion_cuil;
        this.entidad_pagadora_cuil = entidad_pagadora_cuil;
    }

    public String getId_cuil() {
        return id_cuil;
    }

    public void setId_cuil(String id_cuil) {
        this.id_cuil = id_cuil;
    }

    public String getNum_cuil() {
        return num_cuil;
    }

    public void setNum_cuil(String num_cuil) {
        this.num_cuil = num_cuil;
    }

    public String getDate_cuil() {
        return date_cuil;
    }

    public void setDate_cuil(String date_cuil) {
        this.date_cuil = date_cuil;
    }

    public String getLongitude_cuil() {
        return longitude_cuil;
    }

    public void setLongitude_cuil(String longitude_cuil) {
        this.longitude_cuil = longitude_cuil;
    }

    public String getLatitude_cuil() {
        return latitude_cuil;
    }

    public void setLatitude_cuil(String latitude_cuil) {
        this.latitude_cuil = latitude_cuil;
    }

    public String getDiretion_cuil() {
        return diretion_cuil;
    }

    public void setDiretion_cuil(String diretion_cuil) {
        this.diretion_cuil = diretion_cuil;
    }

    public String getEntidad_pagadora_cuil() {
        return entidad_pagadora_cuil;
    }

    public void setEntidad_pagadora_cuil(String entidad_pagadora_cuil) {
        this.entidad_pagadora_cuil = entidad_pagadora_cuil;
    }
}
