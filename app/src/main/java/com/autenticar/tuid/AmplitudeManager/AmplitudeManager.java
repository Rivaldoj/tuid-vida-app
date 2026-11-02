package com.autenticar.tuid.AmplitudeManager;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.amplitude.api.Amplitude;
import com.amplitude.api.AmplitudeClient;
import com.amplitude.api.Revenue;

import org.json.JSONObject;

public class AmplitudeManager {

    private static AmplitudeClient client;

    public void init(Activity context){
        Log.e("AmplitudeManager", "Initializing...");
        client = Amplitude.getInstance()
                .initialize(context.getApplicationContext(), "d96c61f02b46c1d23a02ee331d12c2bd")
                .enableForegroundTracking(context.getApplication());
    }

    public void log(Activity context, String title, JSONObject data){
        Log.e("AmplitudeManager", "Logging..." + title);
        if(client == null) init(context);
        try{
            data.put("os", "Android");
            data.put("version", Build.VERSION.RELEASE);
        }
        catch (Exception e){

        }
        client.logEvent(title, data);
    }

    public void logRevenue(Activity context, String title, JSONObject data){
        Log.e("AmplitudeManager", "Logging revenue..." + title);
        if(client == null) init(context);
        Revenue rev = new Revenue().setProductId(title).setPrice(1).setQuantity(1);
        client.logRevenueV2(rev);
    }
}
