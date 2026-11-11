package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

/** Registro con AWS Face Liveness: POST {base}/vida/v2/signup (fechas como texto yyyy-MM-dd). */
public class SignupRequest {

    public static class Documento {
        /** O = DNI tarjeta (codigo de barras PDF417), N = DNI anterior, L = ultimo DNI (con chip) */
        @SerializedName("type")
        public String type = "O";
        @SerializedName("number")
        public String number;
        @SerializedName("name")
        public String name;
        @SerializedName("lastname")
        public String lastname;
        @SerializedName("gender")
        public String gender;
        @SerializedName("birth")
        public String birth;
        @SerializedName("expires")
        public String expires;
        @SerializedName("tramit")
        public String tramit;
        @SerializedName("front")
        public String front;
        @SerializedName("back")
        public String back;
        @SerializedName("photo")
        public String photo;
    }

    @SerializedName("sessionId")
    public String sessionId;

    @SerializedName("idDevice")
    public String idDevice;

    @SerializedName("os")
    public String os;

    @SerializedName("version")
    public String version;

    @SerializedName("appversion")
    public String appversion;

    @SerializedName("latitud")
    public String latitud;

    @SerializedName("longitud")
    public String longitud;

    @SerializedName("idDocument")
    public Documento idDocument;
}
