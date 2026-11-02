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
    private int idValidacion=0;
    private SQLHelper sql;

    @Override
    public void onResume(){
        super.onResume();
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

        // Android 13+: permiso en tiempo de ejecucion para mostrar notificaciones push
        if (Build.VERSION.SDK_INT >= 33
                && androidx.core.content.ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 9001);
        }

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

        findViewById(R.id.v2_home_cuando_cobro).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchDniForCobro();
            }
        });

        findViewById(R.id.home_solicitar_certifiado).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    public void requestPermissions() {
        if (checkGPSStatus()) {
            String[] permisions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA};
            if (!hasPermissions(this, permisions)) {
                ActivityCompat.requestPermissions(this, permisions, 1501);
            } else {
                gpsLocation = new GPSLocation(activity, getApplicationContext());
                latitud = gpsLocation.getLatitud();
                longitud = gpsLocation.getLongitud();
                searchDniForAuthentication();
            }
        }else{
            if(isGpsEnabled()) {
                requestPermissions();
            }
        }
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
                TuidService service = ServiceGenerator.createService(TuidService.class);
                request.dni=String.valueOf(text_content_info_cuil_text.getText());
                if(rdb_genero.getCheckedRadioButtonId()==R.id.rdb_masculino){
                    request.sexo="m";
                }else if(rdb_genero.getCheckedRadioButtonId()==R.id.rdb_femenino){
                    request.sexo="f";
                }

                if (request.dni.trim().equals("")) {
                    text_content_info_cuil_text.requestFocus();
                    CreateAlert(self, "Alerta", activity.getString(R.string.contact_validar_dni)).show();
                    return;
                }
                try {
                    Call<SearchdniResponse> callUser = service.searchdni(request);
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



    protected void irAFeDeVida(String dni, String sexo) {
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
        startActivityForResult(intent,REQUEST_LIVE_PREVIEW);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        } else if (requestCode == REQUEST_LIVE_PREVIEW) {
            login();
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        request.FileEncode64 = HelperFunctions.encodeTobase64(bitmap);
        request.Token = token;
        request.idValidation=idValidacion;
        try {
            request.appVersion = activity.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        request.os = "Android " + Build.VERSION.RELEASE + " (" + Build.MANUFACTURER + " " + Build.MODEL + ")";
        request.version = BuildConfig.VERSION_CODE + "(" + BuildConfig.VERSION_NAME + ")";
        Call<LoginResponse> callUser = apiServiceUser.login(request);
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
