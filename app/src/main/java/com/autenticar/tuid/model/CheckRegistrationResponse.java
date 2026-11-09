package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

/** Respuesta de /checkregistration. No trae datos personales. */
public class CheckRegistrationResponse extends ResponseBase {
    public static final String NOT_REGISTERED = "NOT_REGISTERED";
    /** No esta registrada y su CUIL no figura en ninguna entidad adherida (no puede registrarse). */
    public static final String NOT_ADHERED = "NOT_ADHERED";
    public static final String INCOMPLETE = "REGISTRATION_INCOMPLETE";
    public static final String PENDING = "REGISTERED_PENDING";
    public static final String UP_TO_DATE = "REGISTERED_UP_TO_DATE";

    @SerializedName("registered")
    public boolean registered;

    @SerializedName("registration")
    public String registration;

    @SerializedName("pendingFeDeVida")
    public boolean pendingFeDeVida;

    /** Solo cuando esta al dia: para que entidad y en que fecha (dd/mm/aaaa) le toca la proxima certificacion. */
    @SerializedName("proximas")
    public java.util.List<Proxima> proximas;

    public static class Proxima {
        @SerializedName("empresa")
        public String empresa;
        @SerializedName("proxima")
        public String proxima;
    }
}
