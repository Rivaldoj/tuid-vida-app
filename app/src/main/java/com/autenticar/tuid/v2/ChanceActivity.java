package com.autenticar.tuid.v2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.autenticar.tuid.LoginActivity;
import com.autenticar.tuid.MainApp;
import com.autenticar.tuid.R;
import com.autenticar.tuid.RegistrarScanUserActivity;
import com.autenticar.tuid.model.Answer;
import com.autenticar.tuid.model.ChanceRequest;
import com.autenticar.tuid.model.ChanceResponse;
import com.autenticar.tuid.services.TuidService;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.autenticar.tuid.utils.HelperFunctions;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.autenticar.tuid.utils.HelperFunctions.setSound;
import static com.autenticar.tuid.utils.HelperFunctions.sound;

public class ChanceActivity extends TuIdActivity{

    String question;
    String a0;
    String a1;
    String a2;
    String a3;
    String a4;
    @Override
    public void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.v2_chance_activity);
        question = getIntent().getStringExtra("question");
        a0 = getIntent().getStringExtra("a0");
        a1 = getIntent().getStringExtra("a1");
        a2 = getIntent().getStringExtra("a2");
        a3 = getIntent().getStringExtra("a3");
        a4 = getIntent().getStringExtra("a4");
        ((TextView) findViewById(R.id.txtPreguntaChance)).setText(question);
        ((RadioButton) findViewById(R.id.rdChance0)).setText(a0);
        ((RadioButton) findViewById(R.id.rdChance1)).setText(a1);
        ((RadioButton) findViewById(R.id.rdChance2)).setText(a2);
        ((RadioButton) findViewById(R.id.rdChance3)).setText(a3);

        findViewById(R.id.btnSendChance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedOption = -1;
                selectedOption = ((RadioButton) findViewById(R.id.rdChance0)).isChecked() ? 0 : selectedOption;
                selectedOption = ((RadioButton) findViewById(R.id.rdChance1)).isChecked() ? 1 : selectedOption;
                selectedOption = ((RadioButton) findViewById(R.id.rdChance2)).isChecked() ? 2 : selectedOption;
                selectedOption = ((RadioButton) findViewById(R.id.rdChance3)).isChecked() ? 3 : selectedOption;
                if (selectedOption < 0) {
                    HelperFunctions.ShowAlert(activity, getString(R.string.alert_elige_respuesta));
                } else {
                    sendChanceAnswer(String.valueOf(selectedOption));
                }
            }
        });
    }



    private void sendChanceAnswer(String id) {
        Intent data = new Intent();
        data.putExtra("selectedOption", id);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }
}

