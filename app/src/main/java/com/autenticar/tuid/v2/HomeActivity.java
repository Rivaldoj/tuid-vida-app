package com.autenticar.tuid.v2;

import android.Manifest;
import com.google.firebase.messaging.FirebaseMessaging;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.autenticar.tuid.AmplitudeManager.AmplitudeManager;
import com.autenticar.tuid.BuildConfig;
import com.autenticar.tuid.HistoryActivity;
import com.autenticar.tuid.MainApp;
import com.autenticar.tuid.R;
import com.autenticar.tuid.model.CUIL;
import com.autenticar.tuid.model.LoginRequest;
import com.autenticar.tuid.model.LoginResponse;
import com.autenticar.tuid.model.SearchdniRequest;
import com.autenticar.tuid.model.SearchdniResponse;
import com.autenticar.tuid.services.TuidService;
import com.autenticar.tuid.services.TuidServiceV2;
import com.autenticar.tuid.v2.aws.LivenessActivity;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.autenticar.tuid.utils.GPSLocation;
import com.autenticar.tuid.utils.HelperFunctions;
import com.autenticar.tuid.utils.SQLHelper;
import com.autenticar.tuid.v2.utils.LoadingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.autenticar.tuid.v2.FeDeVidaActivity.GESTURE_TO_DETECT;
import static com.autenticar.tuid.utils.HelperFunctions.CreateAlert;
import static com.autenticar.tuid.utils.HelperFunctions.mProgressDialog;

public class HomeActivity extends TuIdActivity {

    public static HomeActivity self;
    public static boolean irAFeDeVidaOnResume = false;
    public static boolean irARegistroOnResume = false;
    private static String token = "";
    private String latitud = "0";
    private String longitud = "0";
    private GPSLocation gpsLocation;
    private Dialog dialog;

    private String from_notification_dni;
    private String from_notification_gender;

    public static final int REQUEST_LIVE_PREVIEW = 7002;
    public static final int REQUEST_AWS_LIVENESS = 7003;
    public static final int REQUEST_PRE_CAPTURA = 7004;
    public static final int REQUEST_USUARIOS = 7005;
    // true: searchdni/login contra /vida/v2 con AWS Face Liveness; false: flujo anterior (3 pestaneos + selfie)
    private static final boolean USE_LIVENESS_V2 = true;
    private String livenessSessionId;
    private int idValidacion=0;
    private SQLHelper sql;

    @Override
    public void onResume(){
        super.onResume();
        if (permisosFeDeVida != null) permisosFeDeVida.onResume();
        if(irAFeDeVidaOnResume){
            irAFeDeVidaOnResume = false;
            requestPermissions();
        }

        if(irARegistroOnResume){
            irARegistroOnResume = false;
            Intent intent = new Intent(getBaseContext(), TerminosYCondicionesActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.v2_home_activity);
        self = this;

        // borrar prueba webview
//        Intent i = new Intent(this,WebActivity.class);
//        i.putExtra("url", "https://tuid.ar");
//        startActivity(i);
        // =---=--=--=--=--=--=--=--=
        try{
            if(getIntent().hasExtra("action")){
                String action_from_notification = getIntent().getStringExtra("action");
                from_notification_dni = getIntent().getStringExtra("dni");
                from_notification_gender = getIntent().getStringExtra("sexo");
                irAFeDeVidaOnResume = true;

                Log.e("NOTIFACTIONACTION", action_from_notification + " " + from_notification_dni + " " + from_notification_gender);
            }
        }
        catch (Exception e){
            Log.e("EX", e.getMessage());
        }

        //BORRAR TEST
        findViewById(R.id.v2_home_test).setVisibility(MainApp.isTesting?View.VISIBLE: View.GONE);
        findViewById(R.id.v2_home_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(context, RegistroManualActivity.class));
                startActivity(new Intent(context, FeDeVidaActivity.class));

//                try {
//                    String token = "lalalalalala";
//                    JSONArray ar = new JSONArray("[{\"id\":2,\"empresa_id\":\"TUID\",\"periodicidad\":0,\"cuestionario\":\"{\\\"cuestionario\\\":[\\n{\\n\\\"texto\\\":\\\"¿Desea recibir el recibo digitalmente?\\\",\\n\\\"objeto\\\":\\\"checkbox\\\",\\n\\\"options\\\": [\\\"Si\\\",\\\"No\\\"],\\n\\\"value\\\": \\\"Si\\\"\\n}]}\",\"activo\":1,\"nombre\":\"Aceptación de resumen digital\",\"descripcion\":\"Acepta recibir el resumen de cuenta digitalmente?\",\"cancelable\":1,\"versionMinima\":1},{\"id\":3,\"empresa_id\":\"TUID\",\"periodicidad\":10,\"cuestionario\":\"{\\\"cuestionario\\\":[\\n{\\n\\\"texto\\\":\\\"¿Desea recibir el recibo digitalmente?\\\",\\n\\\"objeto\\\":\\\"checkbox\\\",\\n\\\"options\\\": [\\\"Si\\\",\\\"No\\\"],\\n\\\"value\\\": \\\"Si\\\"\\n}]}\",\"activo\":1,\"nombre\":\"Información Nupcial\",\"descripcion\":\"Necesitamos renovar los datos nupciales. Esta información es fundamental para evitar inconvenientes en el cobro de la pensión futura.\",\"cancelable\":1,\"versionMinima\":1}]");
//                    Intent intent = new Intent(HomeActivity.self, TareasPendientesActivity.class);
//                    intent.putExtra("pendingTasks", ar.toString());
//                    intent.putExtra("token", token);
//                    startActivity(intent);
//
//                }
//                catch (Exception e){
//                    Log.e("EX", e.getMessage());
//                    Log.e("EX", e.getStackTrace()[0].getLineNumber() +" < - Line");
//                }

            }
        });
        // ----------

        findViewById(R.id.btnRegistrarUser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BuildConfig.USE_CUIL_API) {
                    comprobarRegistroPrevio();
                    return;
                }
                Intent intent = new Intent(getBaseContext(), TerminosYCondicionesActivity.class);
                startActivity(intent);
            }
        });
        
        Button btnHistorial = findViewById(R.id.btnHistorial);
        btnHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), HistoryActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.btnUsuarios).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getBaseContext(), com.autenticar.tuid.usuarios.UsuariosActivity.class), REQUEST_USUARIOS);
            }
        });

        // "Cuando cobro": servicio SUSPENDIDO por el momento (el boton esta comentado en v2_home_activity.xml).
        // Para reactivarlo, descomentar el boton del layout y este bloque.
        // findViewById(R.id.v2_home_cuando_cobro).setOnClickListener(new View.OnClickListener() {
        // @Override
        // public void onClick(View view) {
        // if (BuildConfig.USE_CUIL_API) {
        // elegirUsuarioPara("la consulta de cobro", CuandoCobroActivity.class);
        // return;
        // }
        // searchDniForCobro();
        // }
        // });

        findViewById(R.id.home_solicitar_certifiado).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BuildConfig.USE_CUIL_API) {
                    elegirUsuarioPara("la solicitud de certificado", CertificadoActivity.class);
                    return;
                }
                searchDniForCertificate();
            }
        });


        findViewById(R.id.btnLoginDBiom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermissions();

            }
        });
        TextView versionName = findViewById(R.id.txtVersionName);
        try {
            versionName.setText("Versión " + this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName);// + BuildConfig.VERSION_NAME);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        initFirebase();
        playGuideSounds(new int[]{R.raw.fedevida, R.raw.registrate}, 0, null);
    }

    private void initFirebase(){
        try {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (task.isSuccessful()) {
                                token = task.getResult();
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e("EX", "FirebaseInstanceId.getInstance error: " + e);
        }
    }

    private PermisosFeDeVida permisosFeDeVida;

    /** Comprueba camara, ubicacion y GPS (explicando que falta) y recien entonces pide los datos para la Fe de Vida. */
    public void requestPermissions() {
        if (permisosFeDeVida == null) {
            permisosFeDeVida = new PermisosFeDeVida(this, new PermisosFeDeVida.Listener() {
                @Override
                public void onListo() {
                    gpsLocation = new GPSLocation(activity, getApplicationContext());
                    latitud = gpsLocation.getLatitud();
                    longitud = gpsLocation.getLongitud();
                    if (BuildConfig.USE_CUIL_API) {
                        // Primero se elige la persona; los controles de luz y rostro van despues (ver iniciarIdentificacion)
                        iniciarIdentificacion();
                    } else {
                        // Controles de luz y rostro ANTES de pedir los datos: la sesion de prueba de vida que
                        // crea searchdni vence a los 3 minutos, por eso este paso va antes y no despues
                        startActivityForResult(new Intent(HomeActivity.this, com.autenticar.tuid.v2.captura.PreCapturaActivity.class), REQUEST_PRE_CAPTURA);
                    }
                }
            });
        }
        permisosFeDeVida.iniciar();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permisosFeDeVida != null) permisosFeDeVida.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean checkGPSStatus() {
        LocationManager locationManager;
        boolean gps_enabled = false;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!gps_enabled) {
                // if(Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT){
                String providers = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                if (TextUtils.isEmpty(providers)) {
                    return false;
                } else {
                    return true;
                }
            }
        } catch (Exception ex) {
            Log.e("EX", "checkGPSStatus gps_enabled error: " + ex.getMessage());
            return false;
        }
        return gps_enabled;
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

    private boolean isGpsEnabled() {
        final boolean[] ban = {false};
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Se requerie la activacion del GPS ¿Desea activarlo?")
                .setCancelable(false)
                .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        ban[0] = true;
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        Button btn_negativo = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        btn_negativo.setTextColor(Color.BLACK);
        Button btn_positivo = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        btn_positivo.setTextColor(Color.BLACK);
        return ban[0];
    }

    private void searchDniForAuthentication(){
        dialog = new Dialog(self);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_autenticar_dni);

        try {

            if(from_notification_dni!=null && from_notification_gender!=null){
                ((EditText)dialog.findViewById (R.id.text_content_info_CUIL_text)).setText(from_notification_dni);

                if(!from_notification_gender.equalsIgnoreCase("M")){
                    ((RadioButton)dialog.findViewById(R.id.rdb_masculino)).setChecked(false);
                    ((RadioButton)dialog.findViewById(R.id.rdb_femenino)).setChecked(true);
                    from_notification_dni = null;
                    from_notification_gender = null;
                }
            }
            else{
                ((EditText)dialog.findViewById (R.id.text_content_info_CUIL_text)).setText( ((MainApp)getApplication()).GetUserSession().dni);
                if(!((MainApp)getApplication()).GetUserSession().gender.equalsIgnoreCase("M")){
                    ((RadioButton)dialog.findViewById(R.id.rdb_masculino)).setChecked(false);
                    ((RadioButton)dialog.findViewById(R.id.rdb_femenino)).setChecked(true);
                }
            }

        }
        catch (Exception e){

        }
        TextView text_title_info_empresarialdialog = (TextView) dialog.findViewById(R.id.text_title_info_empresarial);
        text_title_info_empresarialdialog.setText("Autenticar DNI");
        ImageView btn_cancel = (ImageView) dialog.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        Button btn_send = (Button) dialog.findViewById(R.id.btn_send);
        final EditText text_content_info_cuil_text = (EditText) dialog.findViewById(R.id.text_content_info_CUIL_text);
        final RadioGroup rdb_genero = (RadioGroup) dialog.findViewById(R.id.rdb_genero);
//        InputMethodManager inputMethodManager=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        inputMethodManager.showSoftInput(text_content_info_cuil_text, InputMethodManager.SHOW_IMPLICIT);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SearchdniRequest request = new SearchdniRequest();
                request.dni=String.valueOf(text_content_info_cuil_text.getText());
                if(rdb_genero.getCheckedRadioButtonId()==R.id.rdb_masculino){
                    request.sexo="m";
                }else if(rdb_genero.getCheckedRadioButtonId()==R.id.rdb_femenino){
                    request.sexo="f";
                }
                if (USE_LIVENESS_V2 && request.sexo != null) request.sexo = request.sexo.toUpperCase();
                request.os = "Android " + Build.VERSION.RELEASE + " (" + Build.MANUFACTURER + " " + Build.MODEL + ")";
                request.version = String.valueOf(Build.VERSION.SDK_INT);
                request.appversion = BuildConfig.VERSION_NAME;

                if (request.dni.trim().equals("")) {
                    text_content_info_cuil_text.requestFocus();
                    CreateAlert(self, "Alerta", activity.getString(R.string.contact_validar_dni)).show();
                    return;
                }
                try {
                    Call<SearchdniResponse> callUser = USE_LIVENESS_V2
                            ? ServiceGenerator.createV2Service(TuidServiceV2.class).searchdni(request)
                            : ServiceGenerator.createService(TuidService.class).searchdni(request);
                    try{
                        LoadingDialog.getInstance().show(self,"Aguarde un momento, procesando la información");
                    }
                    catch (Exception e){
                    }
                    callUser.enqueue(new Callback<SearchdniResponse>() {
                        @Override
                        public void onResponse(Call<SearchdniResponse> call, Response<SearchdniResponse> response) {
                            LoadingDialog.getInstance().hide();
                            SearchdniResponse Response = response.body();
                            if (response.isSuccessful()) {
                                if (Response.IsOk()) {
                                    if (Response.idValidacion>0) {
                                        idValidacion=Response.idValidacion;
                                        livenessSessionId=Response.sessionId;
                                        dialog.dismiss();
                                        irAFeDeVida(request.dni, request.sexo);

                                    } else {
                                        HelperFunctions.ShowAlert(self, "Tu DNI no se encuentra registrado, te recomendamos realiza el debido registro");
                                    }
                                } else {
                                    try {
                                        JSONObject error = new JSONObject();
                                        error.put("error", response.body().Message);
                                        error.put("dni", request.dni);
                                        error.put("sexo", request.sexo);
                                        new AmplitudeManager().log(self, "Search DNI Failed", error);
                                    }
                                    catch (Exception e){

                                    }
                                    dialog.dismiss();
                                    Log.e("ERRORSEARCHDNI", Response.Message + ">" + Response.url + ">" + Response.code);
                                    HelperFunctions.ShowAlertWithContact(self, Response.Message, 0, "DONTFINISHACTIVITY");
                                }
                            } else {
                                dialog.dismiss();
                                Log.e("ERRORSEARCHDNI", Response.Message + ">>" + Response.url + ">>" + Response.code);
                                HelperFunctions.ShowAlert(self, response.message());
                            }
                        }

                        @Override
                        public void onFailure(Call<SearchdniResponse> call, Throwable t) {
                            LoadingDialog.getInstance().hide();

                            try{
                                findViewById(R.id.btnConfirmar).setEnabled(true);
                            }
                            catch (Exception e){

                            }
                            dialog.dismiss();
                            HelperFunctions.ShowAlertWithContact(self, context.getString(R.string.no_hay_conexion_a_internet), 1, "DONTFINISHACTIVITY");
                        }
                    });

                } catch (Exception ex) {
                    Log.e("EX", ex.getMessage(),ex);
                    HelperFunctions.ShowAlert(self, "Se ah producido un error en la autenticación del DNI ");
                    dialog.dismiss();
//                    findViewById(R.id.btnConfirmar).setEnabled(true);
                }
            }
        });
        dialog.show();
        if(MainApp.isTesting) ((EditText)dialog.findViewById (R.id.text_content_info_CUIL_text)).setText("35325041");
    }



    // ------------------------------------------------------------------
    // Identificacion por CUIL (BuildConfig.USE_CUIL_API)
    // ------------------------------------------------------------------

    private String cuilPrecargado;   // viene del aviso "ya registrado": se salta el selector
    private String cuilPendiente;    // persona elegida; se consulta al servidor despues de los controles de camara

    /** Persona ya elegida: controles de luz y rostro y, si salen bien, searchdni (la sesion de prueba de vida dura 3 minutos). */
    private void irAControlesPrevios(final String cuil) {
        // Primero se comprueba si esta registrado: si no lo esta, el aviso sale ANTES de abrir la camara
        LoadingDialog.getInstance().show(self, "Aguarde un momento, comprobando su registro");
        ServiceGenerator.createV2Service(TuidServiceV2.class)
                .checkRegistration(new com.autenticar.tuid.model.CheckRegistrationRequest(cuil))
                .enqueue(new Callback<com.autenticar.tuid.model.CheckRegistrationResponse>() {
                    @Override
                    public void onResponse(Call<com.autenticar.tuid.model.CheckRegistrationResponse> call,
                                           Response<com.autenticar.tuid.model.CheckRegistrationResponse> response) {
                        LoadingDialog.getInstance().hide();
                        com.autenticar.tuid.model.CheckRegistrationResponse r = response.body();
                        if (!response.isSuccessful() || r == null) {
                            falloConsultaRegistro(cuil);
                        } else if (!r.IsOk()) {
                            com.autenticar.tuid.usuarios.Cuadro.mostrar(self, "No pudimos continuar",
                                    r.Message != null ? r.Message : "Inténtelo nuevamente en unos minutos.", "Entendido", null, null);
                        } else if (com.autenticar.tuid.model.CheckRegistrationResponse.NOT_REGISTERED.equals(r.registration)) {
                            ofrecerRegistro();
                        } else if (com.autenticar.tuid.model.CheckRegistrationResponse.UP_TO_DATE.equals(r.registration)) {
                            // Al dia: no hace falta Fe de Vida. Se informa la proxima fecha y se vuelve al menu principal, sin abrir la camara
                            com.autenticar.tuid.usuarios.Cuadro.mostrar(HomeActivity.this, "Fe de Vida al día",
                                    com.autenticar.tuid.usuarios.AlDia.mensaje(r.proximas), "Volver al menú", null, null);
                        } else if (com.autenticar.tuid.model.CheckRegistrationResponse.INCOMPLETE.equals(r.registration)) {
                            // registro desaprobado o sin completar: antes de la camara, se lo manda con Soporte
                            com.autenticar.tuid.usuarios.Soporte.mostrarRegistroIncompleto(HomeActivity.this, cuil);
                        } else {
                            cuilPendiente = cuil;
                            startActivityForResult(new Intent(HomeActivity.this, com.autenticar.tuid.v2.captura.PreCapturaActivity.class), REQUEST_PRE_CAPTURA);
                        }
                    }

                    @Override
                    public void onFailure(Call<com.autenticar.tuid.model.CheckRegistrationResponse> call, Throwable t) {
                        LoadingDialog.getInstance().hide();
                        falloConsultaRegistro(cuil);
                    }
                });
    }

    /** Sin conexion NO se asume que no esta registrado: se avisa y se permite reintentar. */
    private void falloConsultaRegistro(final String cuil) {
        com.autenticar.tuid.usuarios.Cuadro.mostrar(this, "Sin conexión",
                "No pudimos comprobar su registro. Verifique su conexión a Internet e inténtelo nuevamente.",
                "Reintentar", new Runnable() {
                    @Override
                    public void run() {
                        irAControlesPrevios(cuil);
                    }
                }, "Cancelar");
    }

    private void irARegistro() {
        startActivity(new Intent(getBaseContext(), TerminosYCondicionesActivity.class));
    }

    /** Despues de los controles previos: con CUIL se elige la persona; si no, el formulario DNI + sexo de siempre. */
    private void iniciarIdentificacion() {
        if (!BuildConfig.USE_CUIL_API) {
            searchDniForAuthentication();
            return;
        }
        if (cuilPrecargado != null) {
            String c = cuilPrecargado;
            cuilPrecargado = null;
            irAControlesPrevios(c);
            return;
        }
        new com.autenticar.tuid.usuarios.SelectorUsuario(this, com.autenticar.tuid.usuarios.LocalUserStore.get(this),
                "su Fe de Vida", new com.autenticar.tuid.usuarios.SelectorUsuario.Callback() {
            @Override
            public void onCuil(String cuil) {
                irAControlesPrevios(cuil);
            }

            @Override
            public void onRegistrar() {
                comprobarRegistroPrevio();
            }
        }).mostrar();
    }

    /** Cobro / certificado: elige la persona y abre la pantalla con su CUIL. */
    private void elegirUsuarioPara(String accion, final Class<?> destino) {
        new com.autenticar.tuid.usuarios.SelectorUsuario(this, com.autenticar.tuid.usuarios.LocalUserStore.get(this),
                accion, new com.autenticar.tuid.usuarios.SelectorUsuario.Callback() {
            @Override
            public void onCuil(String cuil) {
                Intent i = new Intent(context, destino);
                i.putExtra("cuil", cuil);
                startActivity(i);
            }

            @Override
            public void onRegistrar() {
                comprobarRegistroPrevio();
            }
        }).mostrar();
    }

    /** "Registrarse": primero se comprueba si la persona ya esta registrada. */
    private void comprobarRegistroPrevio() {
        new com.autenticar.tuid.usuarios.ComprobarRegistro(this, new com.autenticar.tuid.usuarios.ComprobarRegistro.Destino() {
            @Override
            public void onContinuarRegistro() {
                irARegistro();
            }

            @Override
            public void onRealizarFeDeVida(String cuil) {
                cuilPrecargado = cuil;   // el formulario ya entra con el CUIL cargado
                requestPermissions();
            }
        }).mostrar();
    }

    private void buscarPorCuil(final String cuil) {
        final SearchdniRequest request = new SearchdniRequest();
        request.cuil = cuil;
        request.os = "Android " + Build.VERSION.RELEASE + " (" + Build.MANUFACTURER + " " + Build.MODEL + ")";
        request.version = String.valueOf(Build.VERSION.SDK_INT);
        request.appversion = BuildConfig.VERSION_NAME;
        try {
            LoadingDialog.getInstance().show(self, "Aguarde un momento, procesando la información");
        } catch (Exception e) {
        }
        ServiceGenerator.createV2Service(TuidServiceV2.class).searchdni(request).enqueue(new Callback<SearchdniResponse>() {
            @Override
            public void onResponse(Call<SearchdniResponse> call, Response<SearchdniResponse> response) {
                LoadingDialog.getInstance().hide();
                SearchdniResponse r = response.body();
                if (!response.isSuccessful() || r == null) {
                    HelperFunctions.ShowAlert(self, "No pudimos iniciar su Fe de Vida. Inténtelo nuevamente en unos minutos.");
                    return;
                }
                if (r.IsOk() && r.idValidacion > 0 && r.sessionId != null && !r.sessionId.isEmpty()) {
                    idValidacion = r.idValidacion;
                    livenessSessionId = r.sessionId;
                    irAFeDeVidaCuil(cuil);
                } else if (r.IsOk() || noRegistrado(r)) {
                    ofrecerRegistro();
                } else {
                    HelperFunctions.ShowAlertWithContact(self, r.Message, 0, "DONTFINISHACTIVITY");
                }
            }

            @Override
            public void onFailure(Call<SearchdniResponse> call, Throwable t) {
                LoadingDialog.getInstance().hide();
                HelperFunctions.ShowAlertWithContact(self, context.getString(R.string.no_hay_conexion_a_internet), 1, "DONTFINISHACTIVITY");
            }
        });
    }

    /** El servidor avisa con el codigo NOT_REGISTERED; se acepta tambien el texto del mensaje por compatibilidad. */
    private boolean noRegistrado(SearchdniResponse r) {
        return "NOT_REGISTERED".equals(r.registration)
                || (r.Message != null && r.Message.contains("Previamente debe registrarse"));
    }

    /** No esta registrado: en lugar de un cartel con un error, se lo lleva a registrarse. */
    private void ofrecerRegistro() {
        com.autenticar.tuid.usuarios.Cuadro.mostrar(this, "Usted no está registrado", "Previamente debe registrarse.",
                "Registrarse", new Runnable() {
                    @Override
                    public void run() {
                        irARegistro();
                    }
                }, "Cancelar");
    }

    private void irAFeDeVidaCuil(String cuil) {
        new AmplitudeManager().log(this, "Login Process Started", new JSONObject());
        ResultLoginActivity.dni = "";
        ResultLoginActivity.sexo = "";
        ResultLoginActivity.cuil = cuil;
        if (BuildConfig.DEBUG && ServiceGenerator.v2BaseUrl != null && ServiceGenerator.v2BaseUrl.startsWith("http://127.0.0.1")) {
            // SOLO pruebas locales (servidor de prueba en la PC, sin AWS): se omite la prueba de vida real
            pedirPermisoNotificaciones();
            login();
            return;
        }
        startActivityForResult(LivenessActivity.Companion.intent(this, livenessSessionId, getString(R.string.aws_liveness_region)), REQUEST_AWS_LIVENESS);
    }

    protected void irAFeDeVida(String dni, String sexo) {
        ResultLoginActivity.cuil = "";
        Intent intent = new Intent(getBaseContext(), FeDeVidaActivity.class);
        intent.putExtra(GESTURE_TO_DETECT, FeDeVidaActivity.DETECTAR_PESTANEO_CLOSED_EYES);
        JSONObject data = new JSONObject();
        try{
            data.put("document", dni);
            data.put("gender", sexo.toUpperCase());
        }
        catch (Exception e){

        }
        new AmplitudeManager().log(this, "Login Process Started", data);
        ResultLoginActivity.dni = dni;
        ResultLoginActivity.sexo = sexo;
        if (USE_LIVENESS_V2 && livenessSessionId != null && !livenessSessionId.isEmpty()) {
            startActivityForResult(LivenessActivity.Companion.intent(this, livenessSessionId, getString(R.string.aws_liveness_region)), REQUEST_AWS_LIVENESS);
            return;
        }
        startActivityForResult(intent,REQUEST_LIVE_PREVIEW);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK) {
            if (requestCode == REQUEST_AWS_LIVENESS) {
                HelperFunctions.ShowAlert(self, "No se pudo completar la prueba de vida. Intentá nuevamente en un lugar con buena luz y buena conexión a internet.");
            }
            return;
        } else if (requestCode == REQUEST_USUARIOS) {
            // se eligio una persona en "Usuarios": Fe de Vida con su CUIL ya cargado
            String c = data != null ? data.getStringExtra("cuil") : null;
            if (c != null) {
                cuilPrecargado = c;
                requestPermissions();
            }
        } else if (requestCode == REQUEST_PRE_CAPTURA) {
            if (BuildConfig.USE_CUIL_API) {
                String c = cuilPendiente;
                cuilPendiente = null;
                if (c != null) buscarPorCuil(c);
            } else {
                searchDniForAuthentication();
            }
        } else if (requestCode == REQUEST_AWS_LIVENESS) {
            pedirPermisoNotificaciones();
            login();
        } else if (requestCode == REQUEST_LIVE_PREVIEW) {
            pedirPermisoNotificaciones();
            login();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    // Android 13+: se pide recien al terminar la fe de vida (no al abrir la app, para no
    // superponer un dialogo del sistema sobre la pantalla principal)
    private void pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= 33
                && androidx.core.content.ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 9001);
        }
    }

    private void login() {
        final LoginRequest request = new LoginRequest();
        TuidService apiServiceUser = ServiceGenerator.createService(TuidService.class);

        Bitmap bitmap = ((MainApp) getApplication()).GetSelfie();
        if(gpsLocation!=null){
            request.longitud = gpsLocation.getLongitud();
            request.latitud = gpsLocation.getLatitud();
        }else{
            request.longitud = "0.0";
            request.latitud = "0.0";
        }
        if (USE_LIVENESS_V2) {
            request.sessionId = livenessSessionId;   // el servidor consulta el resultado en AWS; no se envia selfie
        } else {
            request.FileEncode64 = HelperFunctions.encodeTobase64(bitmap);
        }
        request.Token = token;
        request.idValidation=idValidacion;
        try {
            request.appVersion = activity.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        request.os = "Android " + Build.VERSION.RELEASE + " (" + Build.MANUFACTURER + " " + Build.MODEL + ")";
        request.version = BuildConfig.VERSION_CODE + "(" + BuildConfig.VERSION_NAME + ")";
        Call<LoginResponse> callUser = USE_LIVENESS_V2
                ? ServiceGenerator.createV2Service(TuidServiceV2.class).login(request)
                : apiServiceUser.login(request);
        showProgressDialog(getString(R.string.alert_validando_identidad));
        callUser.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                hideProgressDialog();
                if (response.isSuccessful()) {

                    if (response.body().IsOk()) {
                        LoginResponse loginResponse = response.body();

                        try {
                            new AmplitudeManager().log(self, "Login Success", null);
                            new AmplitudeManager().logRevenue(self, "Validation", null);
                        }
                        catch (Exception e){

                        }
                        Intent intent = new Intent(getBaseContext(), ResultLoginActivity.class);
                        intent.putExtra("UserName", loginResponse.Name);
                        intent.putExtra("token", loginResponse.token);
//                        Log.e("ASD-tkn", loginResponse.token);
//                        Log.e("ASD-pndtasks", loginResponse.pendingTasks.toString());
                        intent.putExtra("pendingTasks", loginResponse.pendingTasks.toString());
                        intent.putExtra("novedad_img", loginResponse.novedad_image_url);
                        intent.putExtra("novedad_link", loginResponse.novedad_link);
                        if(loginResponse.Banco!=null){
                            intent.putExtra("Banco", loginResponse.Banco);
                        }else{
                            intent.putExtra("Banco", "SIN INFORMACIÓN");
                        }
                        intent.putExtra("Cuil", loginResponse.Cuil);
                        intent.putExtra("Longitud", longitud);
                        intent.putExtra("Latitud", latitud);
                        intent.putExtra("Email", loginResponse.Email);
                        intent.putExtra("Telefono", loginResponse.Telefono);
                        intent.putExtra("userID", loginResponse.userID);

                        if (loginResponse.Fecha_valida != null) {
                            intent.putExtra("Fecha_valida", loginResponse.Fecha_valida);
                        } else {
                            intent.putExtra("Fecha_valida", "");
                        }
                        intent.putExtra("Fecha_cobro", "");//loginResponse.Fecha_cobro);
                        sql = new SQLHelper(self);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("E dd LLL yyyy. h:m a", Locale.getDefault());
                        Date date = new Date();
                        String Diretion = GPSLocation.getmAddress() + " '\n'" + GPSLocation.getmCity() + " '\n'" + GPSLocation.getmState() + " '\n'" + GPSLocation.getmCountry() + " '\n'" + GPSLocation.getmPostalCode();
                        CUIL cuil = new CUIL(null, loginResponse.Cuil.toUpperCase(), dateFormat.format(date), String.valueOf(gpsLocation.getLatitud()), String.valueOf(gpsLocation.getLongitud()), Diretion,loginResponse.Banco!=null?loginResponse.Banco:"");
                        sql.insertCUIL(cuil);
                        sql.recortarHistorial(cuil.getNum_cuil(), SQLHelper.MAX_HISTORIAL_POR_USUARIO);
                        // La persona se guarda en este telefono solo despues de una Fe de Vida confirmada por el servidor
                        com.autenticar.tuid.usuarios.LocalUserStore.get(self).addOrUpdate(loginResponse.Cuil, loginResponse.Name);
                        startActivity(intent);
                    } else {
                        HelperFunctions.ShowAlert(self, response.body().Message);
                        try {
                            JSONObject error = new JSONObject();
                            error.put("error", response.body().Message);
                            new AmplitudeManager().log(self, "Login Failed", error);
                        }
                        catch (Exception e){

                        }
                    }
                } else {
                    HelperFunctions.ShowAlert(activity, response.message());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                hideProgressDialog();
            }
        });
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



    private void searchDniForCobro(){
        Log.e("ASD","ASD");
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_autenticar_dni);
        TextView text_title_info_empresarialdialog = (TextView) dialog.findViewById(R.id.text_title_info_empresarial);
        text_title_info_empresarialdialog.setText("Cuando cobro");
        ImageView btn_cancel = (ImageView) dialog.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        Button btn_send = (Button) dialog.findViewById(R.id.btn_send);
        final EditText text_content_info_cuil_text = (EditText) dialog.findViewById(R.id.text_content_info_CUIL_text);
        final RadioGroup rdb_genero = (RadioGroup) dialog.findViewById(R.id.rdb_genero);
//        InputMethodManager inputMethodManager=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        inputMethodManager.showSoftInput(text_content_info_cuil_text, InputMethodManager.SHOW_IMPLICIT);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dni=String.valueOf(text_content_info_cuil_text.getText());
                String sexo = rdb_genero.getCheckedRadioButtonId()==R.id.rdb_masculino?"M":"F";
                if (dni.trim().equals("")) {
                    text_content_info_cuil_text.requestFocus();
                    CreateAlert(activity, "Alerta", activity.getString(R.string.contact_validar_dni)).show();
                    return;
                }
                Intent i = new Intent(context, CuandoCobroActivity.class);
                i.putExtra("sexo", sexo);
                i.putExtra("dni",dni);
                startActivity(i);
                dialog.dismiss();

            }

        });
        dialog.show();
    }




    public void searchDniForCertificate(){
        if (BuildConfig.USE_CUIL_API) {
            super.searchDniForCertificate();   // selector por CUIL (menu lateral)
            return;
        }
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_autenticar_dni);
        TextView text_title_info_empresarialdialog = (TextView) dialog.findViewById(R.id.text_title_info_empresarial);
        text_title_info_empresarialdialog.setText("Certificado");
        ImageView btn_cancel = (ImageView) dialog.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        Button btn_send = (Button) dialog.findViewById(R.id.btn_send);
        final EditText text_content_info_cuil_text = (EditText) dialog.findViewById(R.id.text_content_info_CUIL_text);
        final RadioGroup rdb_genero = (RadioGroup) dialog.findViewById(R.id.rdb_genero);
//        InputMethodManager inputMethodManager=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        inputMethodManager.showSoftInput(text_content_info_cuil_text, InputMethodManager.SHOW_IMPLICIT);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dni=String.valueOf(text_content_info_cuil_text.getText());
                String sexo = rdb_genero.getCheckedRadioButtonId()==R.id.rdb_masculino?"M":"F";
                if (dni.trim().equals("")) {
                    text_content_info_cuil_text.requestFocus();
                    CreateAlert(activity, "Alerta", activity.getString(R.string.contact_validar_dni)).show();
                    return;
                }
                Intent i = new Intent(context, CertificadoActivity.class);
                i.putExtra("sexo", sexo);
                i.putExtra("dni",dni);
                startActivity(i);
                dialog.dismiss();

            }

        });
        dialog.show();
    }


}
