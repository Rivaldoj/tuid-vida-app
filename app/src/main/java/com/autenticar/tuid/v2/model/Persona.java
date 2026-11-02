package com.autenticar.tuid.v2.model;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.Serializable;
import java.util.Date;

public class Persona implements Serializable {

    public static Bitmap dniFront;
    public static Bitmap dniBack;
    public static Bitmap face;

    public Persona(){
        dniFront = null;
        dniBack = null;
        face = null;
    }

    public String nombre;
    public String apellido;
    public String dni;
    public Date nacimiento;
    public String sexo;
    public String tramite;
}
