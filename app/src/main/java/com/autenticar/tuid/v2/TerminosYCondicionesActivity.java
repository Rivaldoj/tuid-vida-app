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
import com.autenticar.tuid.BuildConfig;
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

        // Los botones LEER abren el mismo texto que la frase de cada casilla
        findViewById(R.id.leer_terminos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelperFunctions.showDialog(R.string.nav_titulo_termino_condiciones, R.string.nav_content_termino_condiciones, 1, view.getContext(), activity);
            }
        });
        findViewById(R.id.leer_politicas).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelperFunctions.showDialog(R.string.nav_titulo_politicas, R.string.nav_content_politicas, 1, view.getContext(), activity);
            }
        });

        // Al aceptar los dos, se indica que ya puede continuar
        final CheckBox cbTerminos = (CheckBox) findViewById(R.id.checkbox_terminos);
        final CheckBox cbPoliticas = (CheckBox) findViewById(R.id.checkbox_politicas);
        android.widget.CompoundButton.OnCheckedChangeListener aceptados = new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton b, boolean marcado) {
                if (marcado && cbTerminos.isChecked() && cbPoliticas.isChecked()) {
                    playGuideSounds(new int[]{R.raw.a31}, 0, null);
                }
            }
        };
        cbTerminos.setOnCheckedChangeListener(aceptados);
        cbPoliticas.setOnCheckedChangeListener(aceptados);

        findViewById(R.id.btnIniciarScan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean terms_accepted = ((CheckBox)findViewById(R.id.checkbox_terminos)).isChecked();
                boolean politicas_accepted = ((CheckBox)findViewById(R.id.checkbox_politicas)).isChecked();

                if(!terms_accepted || !politicas_accepted){
                    playGuideSounds(new int[]{R.raw.a27}, 0, null);
                    Toast.makeText(activity, "Debes aceptar los términos y condiciones y las políticas de privacidad", Toast.LENGTH_SHORT).show();
                    return;
                }
                requestPermissions();
            }
        });

        // Tipo de DNI: solo en la version de desarrollo (las librerias del chip no estan en la de tiendas)
        if (BuildConfig.DEBUG) {
            ((android.widget.TextView) findViewById(R.id.terminos_titulo)).setText("Tipo DNI");
            findViewById(R.id.bloque_tipo_dni).setVisibility(View.VISIBLE);
            findViewById(R.id.card_dni_barras).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    elegirTipo(false);
                }
            });
            findViewById(R.id.card_dni_nfc).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    elegirTipo(true);
                }
            });
            playGuideSounds(new int[]{R.raw.a27_1, R.raw.a27}, 0, null);
        } else {
            playGuideSounds(new int[]{R.raw.a27}, 0, null);
        }
    }

    private boolean tipoNfc = false;

    private void elegirTipo(boolean nfc) {
        tipoNfc = nfc;
        findViewById(R.id.card_dni_barras).setBackgroundResource(nfc ? R.drawable.tarjeta_aviso : R.drawable.tarjeta_tipo_elegida);
        findViewById(R.id.card_dni_nfc).setBackgroundResource(nfc ? R.drawable.tarjeta_tipo_elegida : R.drawable.tarjeta_aviso);
        findViewById(R.id.tilde_dni_barras).setVisibility(nfc ? View.INVISIBLE : View.VISIBLE);
        findViewById(R.id.tilde_dni_nfc).setVisibility(nfc ? View.VISIBLE : View.INVISIBLE);
    }

    private void iniciarScan() {
        RegistroNfcDatos.borrar();   // un registro nuevo nunca arrastra datos de un chip anterior
        ((MainApp) getApplication()).InitPersonaToRegister();
        new AmplitudeManager().log(this, "Registration Process Started", null);
        if (BuildConfig.DEBUG && tipoNfc) {
            // Nuevo DNI con chip: claves de acceso (MRZ / CAN). La pantalla existe solo en la version de desarrollo.
            try {
                startActivity(new Intent(this, Class.forName("com.autenticar.tuid.v2.nfc.ClavesNfcActivity")));
            } catch (ClassNotFoundException e) {
                Toast.makeText(activity, "La lectura por NFC no está disponible en esta versión", Toast.LENGTH_LONG).show();
            }
            return;
        }
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
