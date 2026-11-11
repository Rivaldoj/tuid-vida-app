package com.autenticar.tuid.v2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.autenticar.tuid.R;
import com.autenticar.tuid.v2.model.Persona;
import com.autenticar.tuid.v2.utils.Scanner;

import java.text.SimpleDateFormat;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ScanResultActivity extends TuIdActivity {

    @Override
    public void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.v2_scan_result_activity);
        ScanActivity.scanAttemps=0;
        try {
            Persona persona = (Persona) getIntent().getExtras().getSerializable("SCAN_DATA_RESULT");
//            ((CircleImageView) findViewById(R.id.imgDNIFotoDNI)).setImageBitmap(Persona.face);
//            ((ImageView) findViewById(R.id.imgDNIFrente)).setImageBitmap(Persona.dniFront);
//            ((ImageView) findViewById(R.id.imgDNIDorso)).setImageBitmap(Persona.dniBack);

            ((CircleImageView) findViewById(R.id.imgDNIFotoDNI)).setImageBitmap(Scanner.faceResult);
            ((ImageView) findViewById(R.id.imgDNIFrente)).setImageBitmap(Scanner.frontResult);
            ((ImageView) findViewById(R.id.imgDNIDorso)).setImageBitmap(Scanner.backResult);


            ((TextView) findViewById(R.id.fullname)).setText(persona.nombre + " " + persona.apellido);
            ((TextView) findViewById(R.id.numerodni)).setText(persona.dni);
            ((TextView) findViewById(R.id.sexo)).setText(String.valueOf(persona.sexo));

            ((TextView) findViewById(R.id.fechaNacimiento)).setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(persona.nacimiento));

            playGuideSounds(new int[]{R.raw.a31}, 0, null);

            // Si algun dato no coincide, la persona vuelve a escanear su DNI desde las instrucciones
            findViewById(R.id.btnCorregirDatos).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(context, InstruccionesDniActivity.class));
                    finish();
                }
            });

            findViewById(R.id.btnContinuarAGestos).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(context,PreGestureActivity.class));
                    finish();
                }
            });

        }
        catch (Exception e){
            Log.e("EROR", e.getMessage());
            finish();
        }

    }
}
