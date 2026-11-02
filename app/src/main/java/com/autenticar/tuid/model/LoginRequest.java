package com.autenticar.tuid.model;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.google.gson.annotations.SerializedName;

//import android.support.v4.app.ActivityCompat;


public class LoginRequest implements LocationListener {

    @SerializedName("file")
    public String FileEncode64;

    @SerializedName("idDevice")
    public String Token;

    @SerializedName("longitud")
    public String longitud = "-0.000001";

    @SerializedName("latitud")
    public String latitud = "-0.000001";

    @SerializedName("idValidation")
    public int idValidation = 0;

    // SEND APP VERSION
    @SerializedName("appVersion")
    public String appVersion ="";

    @SerializedName("os")
    public String os;

    @SerializedName("version")
    public String version;
    //  ------

    public Context mContext;

    @Override
    public void onLocationChanged(Location location) {
        this.longitud = String.valueOf(location.getLatitude());
        this.latitud = String.valueOf(location.getLongitude());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {


    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

}

