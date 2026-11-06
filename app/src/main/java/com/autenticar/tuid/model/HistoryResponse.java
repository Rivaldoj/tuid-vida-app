package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** Respuesta de /history: hasta 20 certificaciones, de la mas reciente a la mas antigua. */
public class HistoryResponse extends ResponseBase {

    @SerializedName("certificaciones")
    public List<Certificacion> certificaciones;

    public static class Certificacion {
        @SerializedName("id")
        public long id;
        @SerializedName("fecha")
        public String fecha;           // ISO 8601, hora del servidor
        @SerializedName("empresas")
        public List<String> empresas;
        @SerializedName("direccion")
        public String direccion;
        @SerializedName("latitud")
        public String latitud;
        @SerializedName("longitud")
        public String longitud;
        @SerializedName("estado")
        public String estado;          // OK | OBSERVADA
    }
}
