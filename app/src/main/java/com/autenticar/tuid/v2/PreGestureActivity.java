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
    private static final int REQUEST_LIVENESS_REGISTRO = 7010;
    /** Registro con AWS Face Liveness (POST /vida/v2/signup); false = flujo anterior de los 3 pestaneos. */
    private static final boolean USAR_LIVENESS_AWS = true;
    private String livenessSessionId;
    private static final int CHANCE_ACTIVITY = 701;
    String token = "";

    private ProgressDialog mProgressDialog;
    private Question question;

    @Override
    public void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.v2_pre_gesture_activity);
        playGuideSounds(new int[]{R.raw.a31_7, R.raw.a31_6, R.raw.a31_5}, 0, null);   // instrucciones y orden de tocar Comenzar
        findViewById(R.id.btnCaptureGestures).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFirebaseToken();
                if (USAR_LIVENESS_AWS) {
                    iniciarLivenessAws();
                    return;
                }
                Intent i = new Intent(context, FeDeVidaActivity.class);
                i.putExtra(GESTURE_TO_DETECT, FeDeVidaActivity.DETECTAR_PESTANEO_CLOSED_EYES);
                startActivityForResult(i, PRE_GESTURE_ACTIVITY);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LIVENESS_REGISTRO) {
            if (resultCode == RESULT_OK) {
                mostrarIdentificacionSatisfactoria();
            } else {
                com.autenticar.tuid.usuarios.Cuadro.mostrar(this, "No se pudo completar la prueba de vida", "Inténtelo nuevamente en un lugar con buena luz y buena conexión a Internet.", "Entendido", null, null, R.drawable.ic_alerta_prueba_vida);
            }
            return;
        }
        if(requestCode == PRE_GESTURE_ACTIVITY){
            if(resultCode == RESULT_OK){
                mostrarIdentificacionSatisfactoria();
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



    private void mostrarIdentificacionSatisfactoria() {
        setContentView(R.layout.v2_fe_de_vida_satisfactoria);
        playGuideSounds(new int[]{R.raw.a19}, 0, null);
        findViewById(R.id.btnConfirmar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (USAR_LIVENESS_AWS) registrarConLiveness();
                else registrar();
            }
        });
    }

    /** Crea la sesion de Face Liveness en el servidor y abre la prueba de vida de AWS. */
    private void iniciarLivenessAws() {
        showProgressDialog("Aguarde un momento, preparando la prueba de vida");
        ServiceGenerator.createV2Service(com.autenticar.tuid.services.TuidServiceV2.class).session()
                .enqueue(new Callback<com.autenticar.tuid.model.LivenessSessionResponse>() {
                    @Override
                    public void onResponse(Call<com.autenticar.tuid.model.LivenessSessionResponse> call,
                                           Response<com.autenticar.tuid.model.LivenessSessionResponse> response) {
                        hideProgressDialog();
                        com.autenticar.tuid.model.LivenessSessionResponse r = response.body();
                        if (!response.isSuccessful() || r == null || r.sessionId == null || r.sessionId.isEmpty()) {
                            HelperFunctions.ShowAlertWithContact(PreGestureActivity.this,
                                    "No pudimos iniciar la prueba de vida. Inténtelo nuevamente en unos minutos.", 1, "0");
                            return;
                        }
                        livenessSessionId = r.sessionId;
                        startActivityForResult(com.autenticar.tuid.v2.aws.LivenessActivity.intent(
                                PreGestureActivity.this, livenessSessionId, getString(R.string.aws_liveness_region), false),
                                REQUEST_LIVENESS_REGISTRO);
                    }

                    @Override
                    public void onFailure(Call<com.autenticar.tuid.model.LivenessSessionResponse> call, Throwable t) {
                        hideProgressDialog();
                        HelperFunctions.ShowAlertWithContact(PreGestureActivity.this,
                                getString(R.string.no_hay_conexion_a_internet), 1, "0");
                    }
                });
    }

    private static String fecha(java.util.Date d) {
        return d == null ? null : new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(d);
    }

    /** Envia el DNI escaneado y la sesion de Face Liveness al servidor (POST /vida/v2/signup). */
    private void registrarConLiveness() {
        findViewById(R.id.btnConfirmar).setEnabled(false);
        if (RegistroNfcDatos.activo) {
            registrarConChip();
            return;
        }
        try {
            Persona persona = ((MainApp) getApplication()).GetPersonaToRegister();
            persona.dniFront = Scanner.frontResult;
            persona.dniBack = Scanner.backResult;
            persona.SetFaceDetected(Scanner.faceResult);

            final com.autenticar.tuid.model.SignupRequest request = new com.autenticar.tuid.model.SignupRequest();
            com.autenticar.tuid.model.SignupRequest.Documento doc = new com.autenticar.tuid.model.SignupRequest.Documento();
            doc.number = Scanner.dni;
            doc.name = Scanner.nombre;
            doc.lastname = Scanner.apellido;
            doc.gender = Scanner.sexo == null ? null : Scanner.sexo.toUpperCase();
            doc.birth = fecha(Scanner.fechaNacimiento);
            doc.expires = fecha(Scanner.expiry);
            doc.tramit = Scanner.tramite;
            doc.front = HelperFunctions.encodeTobase64(Scanner.frontResult);
            doc.back = HelperFunctions.encodeTobase64(Scanner.backResult);
            doc.photo = HelperFunctions.encodeTobase64(persona.bitmapFace);
            request.idDocument = doc;
            request.sessionId = livenessSessionId;
            request.idDevice = token;
            request.appversion = BuildConfig.VERSION_NAME;
            request.os = "Android " + Build.VERSION.RELEASE + " (" + Build.MANUFACTURER + " " + Build.MODEL + ")";
            request.version = String.valueOf(Build.VERSION.SDK_INT);
            try {
                com.autenticar.tuid.utils.GPSLocation gps = new com.autenticar.tuid.utils.GPSLocation(this, getApplicationContext());
                request.latitud = String.valueOf(gps.getLatitud());
                request.longitud = String.valueOf(gps.getLongitud());
            } catch (Exception e) {
                request.latitud = "0.0";
                request.longitud = "0.0";
            }

            showProgressDialog("Aguarde un momento, procesando el registro");
            ServiceGenerator.createV2Service(com.autenticar.tuid.services.TuidServiceV2.class).signup(request)
                    .enqueue(new Callback<RegisterResponse>() {
                        @Override
                        public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                            hideProgressDialog();
                            procesarRespuestaRegistro(response.body(), request.idDocument.number, request.idDocument.gender, request.idDocument.name);
                        }

                        @Override
                        public void onFailure(Call<RegisterResponse> call, Throwable t) {
                            hideProgressDialog();
                            notSuccessRegister("Se ha producido un error en la registración. Verifique que su conexión a internet sea estable y pruebe nuevamente. Si el problema persiste, contacte a soporte utilizando el formulario de contacto.", "0");
                        }
                    });
        } catch (Exception ex) {
            Log.e("EX", String.valueOf(ex.getMessage()), ex);
            notSuccessRegister("Se ha producido un error en la registración. Verifique que su conexión a internet sea estable y pruebe nuevamente. Si el problema persiste, contacte a soporte utilizando el formulario de contacto.", "0");
        }
    }

    /**
     * DNI nuevo leido por NFC: los datos y la foto salen del chip (no hay fotos del frente ni del dorso).
     * La foto del chip va como foto del DNI (la que compara el servidor) y en el lugar del frente; el servidor
     * exige tambien el dorso, que se completa con la misma imagen.
     */
    private void registrarConChip() {
        try {
            final com.autenticar.tuid.model.SignupRequest request = new com.autenticar.tuid.model.SignupRequest();
            com.autenticar.tuid.model.SignupRequest.Documento doc = new com.autenticar.tuid.model.SignupRequest.Documento();
            doc.type = "L";
            doc.nfc = Boolean.TRUE;
            doc.number = RegistroNfcDatos.numero;
            doc.name = RegistroNfcDatos.nombre;
            doc.lastname = RegistroNfcDatos.apellido;
            doc.gender = RegistroNfcDatos.sexo;
            doc.birth = RegistroNfcDatos.nacimiento;
            doc.expires = RegistroNfcDatos.vencimiento;
            doc.tramit = RegistroNfcDatos.tramite;
            doc.pais = RegistroNfcDatos.pais;
            doc.photo = RegistroNfcDatos.fotoBase64;
            doc.front = RegistroNfcDatos.fotoBase64;
            doc.back = RegistroNfcDatos.fotoBase64;
            if (RegistroNfcDatos.can != null && !RegistroNfcDatos.can.isEmpty()) doc.nfcCode = RegistroNfcDatos.can;
            if (RegistroNfcDatos.mrz != null && !RegistroNfcDatos.mrz.isEmpty()) doc.mrz = RegistroNfcDatos.mrz;
            // Al terminar el registro se compara el CUIL escrito con el DNI y se guarda a la persona en Usuarios:
            // esa comparacion lee estos datos, que en el flujo del chip salen del chip (y no de un escaneo anterior).
            com.autenticar.tuid.v2.utils.Scanner.dni = RegistroNfcDatos.numero;
            com.autenticar.tuid.v2.utils.Scanner.nombre = RegistroNfcDatos.nombre;
            com.autenticar.tuid.v2.utils.Scanner.apellido = RegistroNfcDatos.apellido;
            request.idDocument = doc;
            request.sessionId = livenessSessionId;
            request.idDevice = token;
            request.appversion = BuildConfig.VERSION_NAME;
            request.os = "Android " + Build.VERSION.RELEASE + " (" + Build.MANUFACTURER + " " + Build.MODEL + ")";
            request.version = String.valueOf(Build.VERSION.SDK_INT);
            try {
                com.autenticar.tuid.utils.GPSLocation gps = new com.autenticar.tuid.utils.GPSLocation(this, getApplicationContext());
                request.latitud = String.valueOf(gps.getLatitud());
                request.longitud = String.valueOf(gps.getLongitud());
            } catch (Exception e) {
                request.latitud = "0.0";
                request.longitud = "0.0";
            }
            showProgressDialog("Aguarde un momento, procesando el registro");
            ServiceGenerator.createV2Service(com.autenticar.tuid.services.TuidServiceV2.class).signup(request)
                    .enqueue(new Callback<RegisterResponse>() {
                        @Override
                        public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                            hideProgressDialog();
                            procesarRespuestaRegistro(response.body(), request.idDocument.number, request.idDocument.gender, request.idDocument.name);
                        }

                        @Override
                        public void onFailure(Call<RegisterResponse> call, Throwable t) {
                            hideProgressDialog();
                            notSuccessRegister("Se ha producido un error en la registración. Verifique que su conexión a internet sea estable y pruebe nuevamente. Si el problema persiste, contacte a soporte utilizando el formulario de contacto.", "0");
                        }
                    });
        } catch (Exception ex) {
            Log.e("EX", String.valueOf(ex.getMessage()));
            notSuccessRegister("Se ha producido un error en la registración. Verifique que su conexión a internet sea estable y pruebe nuevamente. Si el problema persiste, contacte a soporte utilizando el formulario de contacto.", "0");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RegistroNfcDatos.borrar();   // los datos del chip no quedan en memoria despues del registro
    }

    /** Aprobado: sigue con correo y celular. Revancha: pregunta de conocimiento. Error: aviso. */
    private void procesarRespuestaRegistro(RegisterResponse r, String dni, String sexo, String nombre) {
        if (r == null) {
            notSuccessRegister("Se ha producido un error en la registración. Inténtelo nuevamente en unos minutos. Si el problema persiste, contacte a soporte utilizando el formulario de contacto.", "0");
            return;
        }
        if (r.IsOk() && r.userID != null) {
            ((MainApp) getApplication()).SetNewUserSession(r.userID, dni, sexo, nombre);
            try {
                JSONObject data = new JSONObject();
                data.put("chance", false);
                new AmplitudeManager().log(activity, "Registration Success", data);
            } catch (Exception ignored) {
            }
            validarTelefonoMail();
        } else if (r.IsChance() && r.userID != null && r.Questions != null) {
            ((MainApp) getApplication()).SetNewUserSession(r.userID, dni, sexo, nombre);
            question = r.Questions;
            Intent i = new Intent(context, ChanceActivity.class);
            i.putExtra("question", question.question);
            i.putExtra("a0", question.chance0);
            i.putExtra("a1", question.chance1);
            i.putExtra("a2", question.chance2);
            i.putExtra("a3", question.chance3);
            startActivityForResult(i, CHANCE_ACTIVITY);
        } else {
            notSuccessRegister(r.Message != null ? r.Message : "Se ha producido un error con los datos suministrados", "0");
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
                            if (registerResponse == null) {
                                // El servidor no respondio con los datos esperados (error, mantenimiento): se avisa en vez de cerrar la app
                                notSuccessRegister("Se ha producido un error en la registración. Inténtelo nuevamente en unos minutos. Si el problema persiste, contacte a soporte utilizando el formulario de contacto.", "0");
                                return;
                            }
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
