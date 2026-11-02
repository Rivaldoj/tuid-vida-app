package com.autenticar.tuid.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Obtiene la ultima ubicacion conocida. Todas las llamadas a Play Services son asincronas:
 * la version anterior (GoogleApiClient + FusedLocationApi) bloqueaba el hilo principal con un
 * CountDownLatch y provocaba ANR ("la app no responde") con play-services-location 21.x.
 */
public class GPSLocation {

    private static final String TAG = "GPSLocation";
    private final int REQUEST_LOCATION = 200;
    private final int REQUEST_CHECK_SETTINGS = 300;
    private final FusedLocationProviderClient fusedClient;
    private final Activity activity;
    private final Context context;
    private volatile String latitud = "0";
    private volatile String longitud = "0";
    private static String mAddress = "";
    private static String mCity = "";
    private static String mState = "";
    private static String mCountry = "";
    private static String mPostalCode = "";

    public GPSLocation(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
        this.fusedClient = LocationServices.getFusedLocationProviderClient(context);
        checkSettingsAndLoad();
    }

    public String getLatitud() {
        return latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public static String getmAddress() {
        return mAddress;
    }

    public static String getmCity() {
        return mCity;
    }

    public static String getmState() {
        return mState;
    }

    public static String getmCountry() {
        return mCountry;
    }

    public static String getmPostalCode() {
        return mPostalCode;
    }

    public void onStart() {
    }

    public void onStop() {
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void checkSettingsAndLoad() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(createLocationRequest()).build();
        LocationServices.getSettingsClient(context).checkLocationSettings(request)
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse response) {
                        loadLastLocation();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ResolvableApiException) {
                            try {
                                ((ResolvableApiException) e).startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException ex) {
                                Log.i(TAG, "startResolutionForResult: " + ex);
                            }
                        } else {
                            Log.i(TAG, "checkLocationSettings: " + e);
                        }
                    }
                });
    }

    private void loadLastLocation() {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            return;
        }
        try {
            fusedClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        latitud = String.valueOf(location.getLatitude());
                        longitud = String.valueOf(location.getLongitude());
                        getAddressFromLocation(location, context);
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "Sin permiso de ubicacion", e);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_OK) {
            loadLastLocation();
        }
    }

    private static void getAddressFromLocation(final Location location, final Context context) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                try {
                    List<Address> list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (list != null && list.size() > 0) {
                        Address address = list.get(0);
                        mAddress = address.getAddressLine(0);
                        mCity = address.getLocality();
                        mState = address.getAdminArea();
                        mCountry = address.getCountryName();
                        mPostalCode = address.getPostalCode();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Impossible to connect to Geocoder", e);
                }
            }
        };
        thread.start();
    }

    public LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }
}
