package com.autenticar.tuid.v2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.autenticar.tuid.AmplitudeManager.AmplitudeManager;
import com.autenticar.tuid.MainApp;
import com.autenticar.tuid.R;
import com.autenticar.tuid.utils.HelperFunctions;

import androidx.core.app.ActivityCompat;
public class TerminosYCondicionesActivity extends TuIdActivity {

    @Override
    public void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.v2_terminos_y_condiciones_activity);

        findViewById(R.id.checkbox_terminos_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelperFunctions.showDialog(R.string.nav_titulo_termino_condiciones, R.string.nav_content_termino_condiciones, 1, view.getContext(), activity);
            }
        });

        findViewById(R.id.checkbox_politicas_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelperFunctions.showDialog(R.string.nav_titulo_politicas, R.string.nav_content_politicas, 1, view.getContext(), activity);
            }
        });

        findViewById(R.id.btnIniciarScan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean terms_accepted = ((CheckBox)findViewById(R.id.checkbox_terminos)).isChecked();
                boolean politicas_accepted = ((CheckBox)findViewById(R.id.checkbox_politicas)).isChecked();

                if(!terms_accepted || !politicas_accepted){
                    Toast.makeText(activity, "Debes aceptar los términos y condiciones y las políticas de privacidad", Toast.LENGTH_SHORT).show();
                    return;
                }
                requestPermissions();
            }
        });

        playGuideSounds(new int[]{ R.raw.scan, R.raw.terminosycondiciones}, 0, null);
    }

    private void iniciarScan() {
        ((MainApp) getApplication()).InitPersonaToRegister();
        new AmplitudeManager().log(this, "Registration Process Started", null);
        // Primero las instrucciones (luz, codigo nitido, flash); su boton "Continuar" abre la camara
        startActivity(new Intent(getBaseContext(), InstruccionesDniActivity.class));
//        startActivity(new Intent(getBaseContext(), DNIDetector.class));
        finish();
    }

    public void requestPermissions() {
        String[] permisions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA};
        if (!hasPermissions(this, permisions)) {
            ActivityCompat.requestPermissions(this, permisions, 1501);
        } else {
            iniciarScan();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) { super.onRequestPermissionsResult(requestCode,permissions, grantResults);
        boolean needPermission = false;
        if (grantResults.length == permissions.length) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    needPermission = true;
                }
            }
        } else {
            needPermission = true;
        }
        if (needPermission) {
            HelperFunctions.CreateAlert(this, getString(R.string.alert_default_title), getString(R.string.es_necesario_otorgar_los_permisos_solicitados_para_continuar)).show();
        } else {
            iniciarScan();
        }
        return;
    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

}
