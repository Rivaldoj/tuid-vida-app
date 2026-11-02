package com.autenticar.tuid.v2;

import android.app.ProgressDialog;
import com.google.firebase.messaging.FirebaseMessaging;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.autenticar.tuid.AmplitudeManager.AmplitudeManager;
import com.autenticar.tuid.BuildConfig;
import com.autenticar.tuid.MainApp;
import com.autenticar.tuid.R;
import com.autenticar.tuid.model.Answer;
import com.autenticar.tuid.model.ChanceRequest;
import com.autenticar.tuid.model.ChanceResponse;
import com.autenticar.tuid.model.Persona;
import com.autenticar.tuid.model.Question;
import com.autenticar.tuid.model.RegisterRequest;
import com.autenticar.tuid.model.RegisterResponse;
import com.autenticar.tuid.services.TuidService;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.autenticar.tuid.utils.HelperFunctions;
import com.autenticar.tuid.v2.utils.Scanner;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.autenticar.tuid.v2.FeDeVidaActivity.GESTURE_TO_DETECT;
import static com.autenticar.tuid.utils.HelperFunctions.setSound;
import static com.autenticar.tuid.utils.HelperFunctions.sound;


public class PreGestureActivity extends TuIdActivity{

    private static final int PRE_GESTURE_ACTIVITY = 9;
    private static final int CHANCE_ACTIVITY = 701;
    String token = "";

    private ProgressDialog mProgressDialog;
    private Question question;

    @Override
    public void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.v2_pre_gesture_activity);
        playGuideSounds(new int[]{R.raw.gestodevida}, 0, null);
        findViewById(R.id.btnCaptureGestures).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, FeDeVidaActivity.class);
                i.putExtra(GESTURE_TO_DETECT, FeDeVidaActivity.DETECTAR_PESTANEO_CLOSED_EYES);
                startActivityForResult(i, PRE_GESTURE_ACTIVITY);
                getFirebaseToken();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PRE_GESTURE_ACTIVITY){
            if(resultCode == RESULT_OK){
                setContentView(R.layout.v2_fe_de_vida_satisfactoria);
                playGuideSounds(new int[]{R.raw.continuar}, 0, null);
                findViewById(R.id.btnConfirmar).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        registrar();
                    }
                });
            }

        }
        if(requestCode == CHANCE_ACTIVITY){
            if(resultCode == RESULT_OK){
                sendChanceAnswer(data.getStringExtra("selectedOption"));
            }
            else{
                notSuccessRegister(getString(R.string.imposible_validar_identidad),  "0");
            }
        }
    }



    private void registrar() {
        final RegisterRequest request = new RegisterRequest();
        TuidService service = ServiceGenerator.createService(TuidService.class);
        findViewById(R.id.btnConfirmar).setEnabled(false);
        try {
            request.DeviceID = token;
                try {
                    Persona persona = ((MainApp) this.getApplication()).GetPersonaToRegister();
                    persona.dniFront = Scanner.frontResult;
                    persona.dniBack = Scanner.backResult;
                    persona.SetFaceDetected(Scanner.faceResult);
                    persona.NombreQR = Scanner.nombre;
                    persona.ApellidoQR = Scanner.apellido;
                    persona.DocumentoQR = Scanner.dni;
                    persona.FechaNacQR = Scanner.fechaNacimiento;
                    persona.EsMasculinoQR = Scanner.sexo.equalsIgnoreCase("M");
                    persona.TramiteQR = Scanner.tramite;
                    persona.FechaExpiracionDni = Scanner.expiry;
//                    persona.Domicilio = Scanner.domicilio;

                    request.DataDocument = ((MainApp) getApplication()).GetPersonaToRegister().GetRequest();
                    request.selfie = HelperFunctions.encodeTobase64(((MainApp) getApplication()).GetSelfie());
                    request.DataDocument.front = HelperFunctions.encodeTobase64(Scanner.frontResult);
                    request.DataDocument.back = HelperFunctions.encodeTobase64(Scanner.backResult);
                    persona = ((MainApp) this.getApplication()).GetPersonaToRegister();
                    request.DataDocument.photo = HelperFunctions.encodeTobase64(persona.bitmapFace);
                    request.DeviceID = token;
                    request.appVersion = BuildConfig.VERSION_CODE + "(" + BuildConfig.VERSION_NAME + ")";
                    request.os = "Android " + Build.VERSION.RELEASE + " (" + Build.MANUFACTURER + " " + Build.MODEL + ")";
                    request.version = BuildConfig.VERSION_CODE + "(" + BuildConfig.VERSION_NAME + ")";

                    Call<RegisterResponse> callUser = service.register(request);
                    showProgressDialog("Aguarde un momento, procesando el registro");
                    callUser.enqueue(new Callback<RegisterResponse>() {
                        @Override
                        public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                            hideProgressDialog();
//                            Log.e("ASD", response.code() + " : " + response.message() + " : "  + response.raw().toString() + ":" + response.body().Response);
                            RegisterResponse registerResponse = response.body();
                            if (response.isSuccessful()) {
                                if (registerResponse.IsOk()) {
                                    if (registerResponse.userID != null) {
                                        (((MainApp) getApplication())).SetNewUserSession(registerResponse.userID, request.DataDocument.number, request.DataDocument.gender, request.DataDocument.name);
//                                        Log.e("ASD", "GO TO Validar mail/telefono");
                                        // SwitchStep(4);//ir a validacion
                                        try {
                                            JSONObject data = new JSONObject();
                                            data.put("chance", false);
                                            new AmplitudeManager().log(activity, "Registration Success", data);
                                            new AmplitudeManager().logRevenue(activity, "Registration", null);
                                        }
                                        catch (Exception e){

                                        }
                                        validarTelefonoMail();
                                        
                                        return;

                                    } else {
                                        notSuccessRegister("Se ha producido un error con los datos suministrados","0");
                                        return;
                                    }
                                } else if (registerResponse.IsChance()) {
                                    try {
                                        JSONObject data = new JSONObject();
                                        data.put("chance", true);
                                        new AmplitudeManager().log(activity, "Registration Success", data);
                                        new AmplitudeManager().logRevenue(activity, "Registration", null);
                                    }
                                    catch (Exception e){

                                    }
//                                    Log.e("ASD", "GO TO CHANCE");
                                    //go to chances
                                    Intent i = new Intent(context, ChanceActivity.class);
                                    question = registerResponse.Questions;
                                    (((MainApp) getApplication())).SetNewUserSession(registerResponse.userID, request.DataDocument.number, request.DataDocument.gender, request.DataDocument.name);
                                    i.putExtra("question", question.question);
                                    i.putExtra("a0", question.chance0);
                                    i.putExtra("a1", question.chance1);
                                    i.putExtra("a2", question.chance2);
                                    i.putExtra("a3", question.chance3);
                                    startActivityForResult(i, CHANCE_ACTIVITY);
                                    return;

                                } else {
                                    notSuccessRegister(registerResponse.Message,"0");
                                }
                                notSuccessRegister(registerResponse.Message,"0");
                            } else {
                                notSuccessRegister(registerResponse.Message, "0");
                            }
                        }

                        @Override
                        public void onFailure(Call<RegisterResponse> call, Throwable t) {
                            hideProgressDialog();
                            notSuccessRegister("Se ha producido un error en la registración. Verifique que su conexión a internet sea estable y pruebe nuevamente. Si el problema persiste, contacte a soporte utilizando el formulario de contacto.","0");
                        }
                    });

                } catch (Exception ex) {
                    Log.e("EX", ex.getMessage(), ex);
                    notSuccessRegister("Se ha producido un error en la registración. Verifique que su conexión a internet sea estable y pruebe nuevamente. Si el problema persiste, contacte a soporte utilizando el formulario de contacto.", "0");
                }
        } catch (Exception ex) {
            String msg = ex.getMessage();
            if(msg == null || msg.length()<1) msg="Se ha producido un error en la registración. Verifique que su conexión a internet sea estable y pruebe nuevamente. Si el problema persiste, contacte a soporte utilizando el formulario de contacto.";
            Log.e("EX", msg);
            notSuccessRegister(msg, "0");
        }
    }


    public void getFirebaseToken(){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful()) {
                            token = task.getResult();
                        }
                    }
                });
    }


    protected void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(message);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    protected void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public void notSuccessRegister(String message, String responseCode){
        logErrorAmplitude(message);
        if(responseCode.startsWith("http") && responseCode.length()>5){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(responseCode));
            startActivity(browserIntent);
        }
        else{
            HelperFunctions.ShowAlertWithContact(activity, message, 1, responseCode);
        }
        findViewById(R.id.btnConfirmar).setEnabled(true);
        ((Button)findViewById(R.id.btnConfirmar)).setText("Finalizar");
        findViewById(R.id.btnConfirmar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    public void validarTelefonoMail(){
        startActivity(new Intent(context, ValidarTelefonoMail.class));
        finish();
    }


    void logErrorAmplitude(String message){
        try {
            JSONObject error = new JSONObject();
            error.put("error", message);
            if(Scanner.dni!=null)error.put("document", Scanner.dni);
            if(Scanner.sexo!=null)error.put("gender", Scanner.sexo);
            new AmplitudeManager().log(activity, "Registration Failed", error);
        }
        catch (Exception e){

        }
    }


    private void sendChanceAnswer(String id) {
        final ChanceRequest request = new ChanceRequest();
        TuidService service = ServiceGenerator.createService(TuidService.class);
        request.DeviceID = token;
        //request.DeviceID = HelperFunctions.GetDeviceID(this);
        request.userID = ((MainApp) getApplication()).GetUserSession().userID;
        Answer answer = new Answer(question.ID, id);
        request.Answers = new ArrayList<>();
        request.Answers.add(answer);

        Call<ChanceResponse> callUser = service.chance(request);
        if (!sound) {
            setSound("stop", activity);
        }

        callUser.enqueue(new Callback<ChanceResponse>() {
            @Override
            public void onResponse(Call<ChanceResponse> call, Response<ChanceResponse> response) {
                hideProgressDialog();
                ChanceResponse chanceResponse = response.body();
                if (response.isSuccessful()) {
                    Log.e("ASD", "1");
                    if (chanceResponse.IsOk()) {
                        Log.e("ASD", "2");
                        //go to validacion
                        validarTelefonoMail();
                    } else {
                        Log.e("ASD", "3" );
                        notSuccessRegister(getString(R.string.imposible_validar_identidad), chanceResponse.url.length()>5?chanceResponse.url:"0");
                    }
                } else {
                    Log.e("ASD", "4");
                    notSuccessRegister(response.message(), "0");
                }
            }

            @Override
            public void onFailure(Call<ChanceResponse> call, Throwable t) {
                hideProgressDialog();
                notSuccessRegister("Se ha producido un error en la registración. Verifique que su conexión a internet sea estable y pruebe nuevamente. Si el problema persiste, contacte a soporte utilizando el formulario de contacto.", "0");
            }
        });
    }
}
