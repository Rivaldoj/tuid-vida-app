package com.autenticar.tuid.v2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.autenticar.tuid.R;

/**
 * Instrucciones antes de escanear el DNI antiguo (con codigo de barras): luz, codigo nitido y flash.
 * "Continuar" abre la camara (ScanActivity), que lee solo el frente y el dorso.
 */
public class InstruccionesDniActivity extends TuIdActivity {

    @Override
    public void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.v2_instrucciones_dni);
        playGuideSounds(new int[]{R.raw.a28, R.raw.a31_2}, 0, null);

        findViewById(R.id.btnContinuarScan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), ScanActivity.class));
                finish();
            }
        });
    }
}
