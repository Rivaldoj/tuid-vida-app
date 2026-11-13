package com.autenticar.tuid.v2.nfc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.autenticar.tuid.R;
import com.autenticar.tuid.usuarios.Audio;
import com.autenticar.tuid.utils.EdgeInsets;

/**
 * SOLO DEBUG. Pantalla 2 del registro con DNI nuevo (chip NFC): claves de acceso.
 *  - Via camara: escanea la MRZ del dorso (MrzScanActivity).
 *  - Via manual: escribe el CAN de 6 digitos del frente (abajo a la derecha).
 * Cualquiera de las dos lleva a la pantalla de verificacion (apoyar el DNI al celular).
 */
public class ClavesNfcActivity extends AppCompatActivity {

    private static final int REQ_MRZ = 3101;
    private EditText campoCan;
    private TextView errorCan;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_claves_nfc);
        EdgeInsets.aplicar(findViewById(R.id.raiz));
        NfcClaves.borrar();
        campoCan = findViewById(R.id.can_campo);
        errorCan = findViewById(R.id.can_error);

        findViewById(R.id.card_via_camara).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(ClavesNfcActivity.this, MrzScanActivity.class), REQ_MRZ);
            }
        });
        findViewById(R.id.can_continuar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String can = campoCan.getText().toString().trim();
                if (!can.matches("\\d{6}")) {
                    errorCan.setVisibility(View.VISIBLE);
                    return;
                }
                errorCan.setVisibility(View.GONE);
                NfcClaves.borrar();
                NfcClaves.can = can;
                irAVerificacion();
            }
        });
        Audio.play(this, R.raw.a28_2);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQ_MRZ && res == RESULT_OK && data != null) {
            NfcClaves.borrar();
            NfcClaves.doc = String.valueOf(data.getStringExtra(MrzScanActivity.EXTRA_DOC)).trim().toUpperCase();
            NfcClaves.nacimiento = String.valueOf(data.getStringExtra(MrzScanActivity.EXTRA_BIRTH)).trim();
            NfcClaves.vencimiento = String.valueOf(data.getStringExtra(MrzScanActivity.EXTRA_EXPIRY)).trim();
            if (NfcClaves.hayMrz()) irAVerificacion();
        }
    }

    private void irAVerificacion() {
        startActivity(new Intent(this, VerificacionNfcActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Audio.detener();
    }
}
