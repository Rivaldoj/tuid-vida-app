package com.autenticar.tuid;

//import android.support.design.widget.BottomSheetDialog;
//import android.support.v7.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.autenticar.tuid.v2.HomeActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;

import static com.autenticar.tuid.utils.HelperFunctions.setSound;
import static com.autenticar.tuid.utils.HelperFunctions.sound;


public class SplashActivity extends AppCompatActivity {
    private static int SPLASHTIEMPOEJECUCION = 1000;
    Activity activity = this;
    private Thread thread;

    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);

        if(MainApp.isTesting){
            goToHome();
            return;
        }



        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        findViewById(R.id.splash_testing).setVisibility(MainApp.isTesting?View.VISIBLE:View.GONE);

        if(!sound){
            ((TextView) findViewById(R.id.text_sound)).setText("Habilitar audio");
            ((ImageView) findViewById(R.id.img_sound)).setBackground(activity.getDrawable(R.drawable.ic_sound_white));
        }else{
            ((TextView) findViewById(R.id.text_sound)).setText("Deshabilitar audio");
            ((ImageView) findViewById(R.id.img_sound)).setBackground(activity.getDrawable(R.drawable.ic_sound_off_white));
        }

        cargaPantalla();
        findViewById(R.id.btn_sound).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sound){
                    sound=false;
                    ((TextView) findViewById(R.id.text_sound)).setText("Habilitar audio");
                    ((ImageView) findViewById(R.id.img_sound)).setBackground(activity.getDrawable(R.drawable.ic_sound_white));
                    if(mp!=null) mp.stop();
                    goToHome();
                }else{
                    sound=true;
                    ((TextView) findViewById(R.id.text_sound)).setText("Deshabilitar audio");
                    ((ImageView) findViewById(R.id.img_sound)).setBackground(activity.getDrawable(R.drawable.ic_sound_off_white));
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
    }

    public  void cargaPantalla() {
        if (sound) {
            mp = MediaPlayer.create(this, R.raw.bienvenido_vida);
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    goToHome();
                }
            });
            mp.start();
        } else {
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    goToHome();
                }
            }, 1500);
        }
    }


    public void goToHome(){
        Intent i = new Intent(SplashActivity.this, HomeActivity.class);

        try {
            Bundle bundle  = getIntent().getExtras();
            JSONObject info = new JSONObject(bundle.get("info").toString());
            i.putExtra("dni", info.getString("document"));
            i.putExtra("sexo", info.getString("gender"));
            i.putExtra("action", "life");
        }
        catch (Exception e){
            Log.e("EX", "NO DATA FOR SPLASH");
            Log.e("EX", e.getMessage());
        }
        startActivity(i);
        finish();
    }
}
