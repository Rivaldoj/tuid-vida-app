package com.autenticar.tuid;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.autenticar.tuid.model.Persona;
import com.autenticar.tuid.model.Session;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.autenticar.tuid.services.utils.UserExclusionStrategy;
import com.autenticar.tuid.utils.FontsOverride;
import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainApp extends Application {

    public static boolean isTesting = false;

    private static Session userSession;
    Bitmap selfie;
    public int userId;

    public void SetSelfie(Bitmap bm) {
        selfie = null;
        SharedPreferences prefs = getSharedPreferences("selfie_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("selfie", BitMapToString(bm));
        editor.commit();
        editor.apply();
        selfie = bm;
    }

    public Bitmap GetSelfie() {
        if(selfie!=null) return selfie;
        SharedPreferences prefs = getSharedPreferences("selfie_preferences", Context.MODE_PRIVATE);
        String sSelfie = prefs.getString("selfie", "");
//        Log.e("SelfieData", sSelfie);
        return StringToBitMap(sSelfie);
    }

    public String BitMapToString(Bitmap bitmap) {
        try {
            ByteArrayOutputStream ByteStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, ByteStream);
            byte[] b = ByteStream.toByteArray();
            String temp = Base64.encodeToString(b, Base64.DEFAULT);
            return temp;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    private Persona personaToRegister;

    public Persona GetPersonaToRegister() {
        return personaToRegister;
    }

    public Session GetUserSession() {
        return userSession;
    }

    public void SetNewUserSession(int userID, String dni, String gender, String description) {
        userSession = Session.build(userID, dni, gender, description);
    }


    public void InitPersonaToRegister() {
        personaToRegister = new Persona();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            // Diagnostico: registra en logcat el trabajo pesado (disco/red) hecho en el hilo principal
            android.os.StrictMode.setThreadPolicy(new android.os.StrictMode.ThreadPolicy.Builder()
                    .detectAll().penaltyLog().build());
        }

        //sobre escribo los fonts
        FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/MuseoSans-500.otf");
        FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/MuseoSans-500.otf");
        FontsOverride.setDefaultFont(this, "SERIF", "fonts/MuseoSans-500.otf");
        FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/MuseoSans-500.otf");

        //     PrintConfig.initDefault(getAssets(), "fonts/MaterialIcons-Regular.ttf");

        Gson gson = new GsonBuilder()
                //.setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .setDateFormat("yyyy-MM-dd")
                .setExclusionStrategies(new UserExclusionStrategy())
                .create();
        //instancion la conexion
        String stringConnection;

        stringConnection = getURLServicios();

        ServiceGenerator.builder = new Retrofit.Builder()
                .baseUrl(stringConnection)
                //  .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson));
        ServiceGenerator.context = getBaseContext();
        ServiceGenerator.v2BaseUrl = stringConnection + "vida/v2/";
        new Thread(() -> com.autenticar.tuid.services.utils.IntegrityInterceptor.warmUp(this)).start();

        InitPersonaToRegister();
        FirebaseApp.initializeApp(this);
        LoadLoginInfo();
    }


    public String getURLServicios() {
        return getString(isTesting?R.string.urlConnectionQA:R.string.urlConnectionProd);
    }

    public boolean isDebugSession() {
        return false; // (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
    }

    public void SaveLogin() {
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (this.GetUserSession().dni != null) {
            editor.putString("dni", this.GetUserSession().dni);
        } else {
            editor.putString("dni", "");
        }
        if (this.GetUserSession().gender != null) {
            editor.putString("gender", this.GetUserSession().gender);
        } else {
            editor.putString("gender", "");
        }
        if (this.GetUserSession().description != null) {
            editor.putString("description", this.GetUserSession().description);
        } else {
            editor.putString("description", "");
        }
        editor.putInt("userId", this.GetUserSession().userID);
        editor.commit();
    }

    private boolean LoadLoginInfo() {
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        userId = sharedPref.getInt("userId", 0);
        String dni = sharedPref.getString("dni", null);
        String gender = sharedPref.getString("gender", null);
        String description = sharedPref.getString("description", null);
        if (dni != null && gender != null && userId > 0) {
            this.SetNewUserSession(userId, dni, gender, description);
            return true;
        } else {
            return false;
        }

    }

}
