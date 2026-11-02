package com.autenticar.tuid.v2;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.autenticar.tuid.R;
import com.autenticar.tuid.services.TuidService;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.autenticar.tuid.v2.services.FormSubmitRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.autenticar.tuid.utils.HelperFunctions.mProgressDialog;

public class FormActivity extends AppCompatActivity {


    public String token = "";
    JSONObject formJson;
    boolean cancelable = false;
    LinearLayout fields_lo;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_activity);
        String formtext = "";
        try {
            formtext = getIntent().getExtras().getString("form");
            token = getIntent().getExtras().getString("token");
            if(formtext.length()<1 || token.length()<1) throw new Exception("Not token or not form string");
            try {
                formJson = new JSONObject(formtext);
                initForm();
            }
            catch (Exception e){
                throw new Exception("Form string is not a valid json object");
            }
        }
        catch (Exception e){
            Log.e("EX", e.getMessage());
            finish();
        }
    }

    @Override
    public void onBackPressed(){
        if(cancelable) super.onBackPressed();
    }

    void initForm(){
        getSupportActionBar().show();
        getSupportActionBar().setTitle("Tareas pendientes");
        ((TextView)findViewById(R.id.form_title)).setText(formJson.optString("nombre", ""));
        ((TextView)findViewById(R.id.form_desc)).setText(formJson.optString("descripcion", ""));
        fields_lo = findViewById(R.id.form_controls);
        cancelable = formJson.optInt("cancelable")==1;
        findViewById(R.id.form_cancel).setVisibility(cancelable?View.VISIBLE:View.INVISIBLE);
        findViewById(R.id.form_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        try {
            JSONObject cuestionario_json = new JSONObject(formJson.getString("cuestionario").replace("\n",""));
            formJson.put("cuestionario", cuestionario_json);
            JSONArray cuestionario_ar = cuestionario_json.getJSONArray("cuestionario");
            for(int i = 0; i<cuestionario_ar.length();i++) addField(cuestionario_ar.getJSONObject(i));
        }
        catch (Exception e){
            Log.e("Cuestionario-EX", e.getMessage());
        }

        findViewById(R.id.form_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateForm()){
                    submitForm();
                }
            }
        });
    }

    void addField(JSONObject field){
        LinearLayout field_lo = new LinearLayout(this);
        field_lo.setOrientation(LinearLayout.VERTICAL);
        TextView text = new TextView(this);
        text.setText(field.optString("texto"));
        field_lo.addView(text);

        if(field.optString("objeto","").equalsIgnoreCase("checkbox")){
            //deberia ser radio button

            RadioGroup rg = new RadioGroup(this);
            JSONArray options_ar = field.optJSONArray("options");
            for(int i = 0; i<options_ar.length();i++){
                String value = options_ar.optString(i);
                RadioButton rb = new RadioButton(this);
                rb.setText(value);
                rb.setTag(value);
                rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        try {
                            if (b) field.put("value", value);
                        }
                        catch (Exception e){

                        }
                    }
                });
                rg.addView(rb);
            }
            field_lo.addView(rg);
            try {
                field.put("value", "");
            }
            catch (Exception e){

            }
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0,10,10,10);
        fields_lo.addView(field_lo,lp);
    }

    boolean validateForm(){
        JSONArray ar = formJson.optJSONObject("cuestionario").optJSONArray("cuestionario");
        for(int i =  0; i<ar.length(); i++){
            if(ar.optJSONObject(i).optString("value").length()==0){
                Toast.makeText(this,"Complete todos los campos para continuar", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    void submitForm(){
        try {
            JSONObject body = new JSONObject();
            body.put("id", formJson.getInt("id"));
            body.put("cuestionario", formJson.getJSONObject("cuestionario").getJSONArray("cuestionario") );

            FormSubmitRequest request = new FormSubmitRequest();
            request.id = formJson.getInt("id");
            JsonParser parser = new JsonParser();
            JsonElement tradeElement = parser.parse(formJson.getJSONObject("cuestionario").getJSONArray("cuestionario").toString());
            JsonArray cuestionario = tradeElement.getAsJsonArray();
//            request.cuestionario =   formJson.getJSONObject("cuestionario").getJSONArray("cuestionario");
            request.cuestionario = cuestionario;
            Log.e("RESULT", request.toString());
            TuidService apiServiceUser = ServiceGenerator.createFormService(TuidService.class, token);
            Call<JsonObject> callUser = apiServiceUser.submitForm(request);
            showProgressDialog(getString(R.string.alert_validando_identidad));
            callUser.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    hideProgressDialog();
                    if (response.isSuccessful()){
                        if(response.body().get("res").getAsString().equalsIgnoreCase("ok")) {
                            Log.e("Response", response.body().toString());
                            finish();
                        }
                        else{
                            Log.e("ASD-response error", response.body().get("message").getAsString());
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    hideProgressDialog();
                    Log.e("Response", "ERROR");
                }
            });

        }
        catch (Exception e){
            
        }

    }



    protected void showProgressDialog(String message) {
        try {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage(message);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setIndeterminate(true);
            }
            mProgressDialog.show();
        }
        catch (Exception e){

        }
    }

    protected void hideProgressDialog() {
        try {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
        catch (Exception e){

        }
    }

}
