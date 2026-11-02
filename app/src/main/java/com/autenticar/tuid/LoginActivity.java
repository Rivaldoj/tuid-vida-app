package com.autenticar.tuid;

import android.Manifest;
import com.google.firebase.messaging.FirebaseMessaging;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;

import com.autenticar.tuid.AmplitudeManager.AmplitudeManager;
import com.autenticar.tuid.model.SearchdniRequest;
import com.autenticar.tuid.model.SearchdniResponse;
import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import com.autenticar.tuid.facedetection.FaceDetectionProcessor;
import com.autenticar.tuid.model.CUIL;
import com.autenticar.tuid.model.LoginRequest;
import com.autenticar.tuid.model.LoginResponse;
import com.autenticar.tuid.model.Session;
import com.autenticar.tuid.services.TuidService;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.autenticar.tuid.utils.GPSLocation;
import com.autenticar.tuid.utils.HelperFunctions;
import com.autenticar.tuid.utils.SQLHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.autenticar.tuid.LivePreviewActivity.GESTURE_TO_DETECT;
import static com.autenticar.tuid.utils.HelperFunctions.CreateAlert;
import static com.autenticar.tuid.utils.HelperFunctions.setSound;
import static com.autenticar.tuid.utils.HelperFunctions.sound;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final int REQUEST_LIVE_PREVIEW = 7002;
    private static final String TAG = "LoginActivity";
    private static final int RC_HANDLE_CAMERA_PERM = 100;
    private static final int REQUEST_LOCATION = 101;
    private ProgressDialog mProgressDialog;
    final Activity activity = this;
    private static String token = "";
    private SQLHelper sql;
    private Dialog dialog;

    //variable de control de audio
    private static boolean FEDEVIDA = true;
    private static boolean REGISTRATE = true;
    private String latitud = "0";
    private String longitud = "0";
    private GPSLocation gpsLocation;
    private TextView nav_salir;
    private int idValidacion=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // ATTENTION: This was auto-generated to handle app links.
        /*Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //buildGoogleApiClient();
        View hView = navigationView.getHeaderView(0);
        TextView nav_username = (TextView) hView.findViewById(R.id.nav_username);
        TextView nav_bien = (TextView) hView.findViewById(R.id.nav_bien);
        ImageView nav_ico_username = (ImageView) hView.findViewById(R.id.nav_ico_username);
        nav_username.setText(getIntent().getStringExtra(""));
        nav_bien.setTextSize(16);
        nav_salir=(TextView) findViewById(R.id.nav_salir);
        nav_salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogo = new AlertDialog.Builder(activity);
                dialogo.setTitle("Alerta");
                dialogo.setMessage("¿ Esta seguro que desea salir de la aplicación ?");
                dialogo.setCancelable(true);
                dialogo.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo, int id) {
                        dialogo.cancel();
                    }
                });
                dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo, int id) {
                        finish();
                    }
                });
                //Cambio de color los botones del alert
                AlertDialog alert = dialogo.create();
                alert.show();
                Button btn_negativo = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                btn_negativo.setTextColor(Color.BLACK);
                Button btn_positivo = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                btn_positivo.setTextColor(Color.BLACK);
            }
        });
        Button btnRegistrarUser = findViewById(R.id.btnRegistrarUser);
        btnRegistrarUser.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(getBaseContext(), RegistrarUserActivity.class);
                if (sound) {
                    setSound("stop", activity);
                    FEDEVIDA = false;
                    REGISTRATE = false;
                }
                Intent intent = new Intent(getBaseContext(), RegistrarScanUserActivity.class);
                startActivity(intent);
            }
        });

        Button btnHistorial = findViewById(R.id.btnHistorial);
        btnHistorial.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), HistoryActivity.class);
                startActivity(intent);
            }
        });

        final AppCompatActivity compatActivity = this;
        Button btnLoginDBiom = findViewById(R.id.btnLoginDBiom);
        btnLoginDBiom.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestPermissions(compatActivity);

            }
        });
        TextView versionName = findViewById(R.id.txtVersionName);
        try {
            versionName.setText("Versión " + activity.getPackageManager().getPackageInfo(getPackageName(), 0).versionName);// + BuildConfig.VERSION_NAME);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        soun_ini_login();
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
            Log.i(TAG, "FirebaseInstanceId.getInstance error: " + e);
        }
    }
    public void RequestPermissions(AppCompatActivity compatActivity) {
        if (checkGPSStatus()) {
            String[] permisions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
            if (!hasPermissions(this, permisions)) {
                ActivityCompat.requestPermissions(this, permisions, 1501);
            } else {
                gpsLocation = new GPSLocation(activity, getApplicationContext());
                latitud = gpsLocation.getLatitud();
                longitud = gpsLocation.getLongitud();
                autenticarPaso1();
            }
        }else{
            if(servGPS()) {
                RequestPermissions(compatActivity);
            }
        }
    }
   /* private void iniciarLogin(AppCompatActivity compatActivity) {
        if (checkGPSStatus()) {
            String[] permisions = new String[]{android.Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION};
            if (!hasPermissions(this, permisions)) {
                ActivityCompat.requestPermissions(this, permisions, requestCode);
            } else {
                ContinueTakingPicture(requestCode);
            }
            int camara = ActivityCompat.checkSelfPermission(compatActivity, Manifest.permission.CAMERA);
            if (camara == PackageManager.PERMISSION_GRANTED) {
                int gps = ActivityCompat.checkSelfPermission(compatActivity, Manifest.permission.ACCESS_FINE_LOCATION);
                if (gps == PackageManager.PERMISSION_GRANTED) {
                    gpsLocation = new GPSLocation(activity, getApplicationContext());
                    latitud = gpsLocation.getLatitud();
                    longitud = gpsLocation.getLongitud();
                    takePicture();

                } else {
                    requestCameraPermission(REQUEST_LOCATION);
                }
            } else {
                requestCameraPermission(RC_HANDLE_CAMERA_PERM);
            }

        } else {
            if(servGPS()) {
                iniciarLogin(compatActivity);
            }
        }
    }*/

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

    @Override
    protected void onStart() {
        if (gpsLocation != null) {
            gpsLocation.onStart();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (gpsLocation != null) {
            gpsLocation.onStop();
        }
        super.onStop();
    }

    protected void takePicture() {
        if (sound) {
            setSound("stop", activity);
            FEDEVIDA = false;
            REGISTRATE = false;
        }
        Intent intent = new Intent(getBaseContext(), LivePreviewActivity.class);
        intent.putExtra(GESTURE_TO_DETECT, FaceDetectionProcessor.DETECTAR_PESTANEO_CLOSED_EYES);
        new AmplitudeManager().log(this, "Login Process Started", null);
        startActivityForResult(intent, REQUEST_LIVE_PREVIEW);
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission(int RequestCode) {
        String[] permisions = new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION};
        if (!hasPermissions(this, permisions)) {
            ActivityCompat.requestPermissions(activity, permisions, RequestCode);
        }
    }
/*
    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }*/


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
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
            CreateAlert(this, getString(R.string.alert_default_title), getString(R.string.es_necesario_otorgar_los_permisos_solicitados_para_continuar)).show();
        } else {
            gpsLocation = new GPSLocation(activity, getApplicationContext());
            latitud = gpsLocation.getLatitud();
            longitud = gpsLocation.getLongitud();
            autenticarPaso1();
            //takePicture();
        }
        return;
    }

/*
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        System.out.println("Entro en persmiso");
        if (grantResults != null) {
            switch (requestCode) {
                case REQUEST_LOCATION:
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        HelperFunctions.CreateAlert(this, getString(R.string.alert_default_title), getString(R.string.es_necesario_otorgar_los_permisos_solicitados_para_continuar)).show();
                        System.out.println("NO permiso REQUEST_LOCATION: "+REQUEST_LOCATION);
                    } else {
                        System.out.println("SI permiso REQUEST_LOCATION: "+REQUEST_LOCATION);
                        gpsLocation = new GPSLocation(activity, getApplicationContext());
                    }
                case RC_HANDLE_CAMERA_PERM:
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        HelperFunctions.CreateAlert(this, getString(R.string.alert_default_title), getString(R.string.es_necesario_otorgar_los_permisos_solicitados_para_continuar)).show();
                        System.out.println("NO permiso RC_HANDLE_CAMERA_PERM: "+RC_HANDLE_CAMERA_PERM);

                    }else{
                        System.out.println("SI permiso RC_HANDLE_CAMERA_PERM: "+RC_HANDLE_CAMERA_PERM);
                    }
                default:
                    Log.i(TAG, "requestCode: " + requestCode);
            }
        }
    }*/

    @Override
    protected void onResume() {
        Session session = ((MainApp) getApplication()).GetUserSession();
    /* if (session != null && session.description != null) {
          ((TextView) findViewById(R.id.txtCurrentUser)).setText(String.format(getString(R.string.no_sos_usuario), session.description));
      } else {
          findViewById(R.id.txtCurrentUser).setVisibility(View.GONE);
          //findViewById(R.id.btnLoginDBiom).setVisibility(View.GONE);
    }*/
        if (sound) {
            setSound("stop", activity);
            FEDEVIDA = true;
            REGISTRATE = true;
            //} soun_ini_login();
        }
        super.onResume();
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
        if (sound) {
            setSound("stop", activity);
            FEDEVIDA = false;
            REGISTRATE = false;
        }

        final LoginRequest request = new LoginRequest();
        TuidService apiServiceUser = ServiceGenerator.createService(TuidService.class);

        Bitmap bitmap;
        if (((MainApp) getApplication()).isDebugSession()) { //cambio las imagenes a subir
            bitmap = HelperFunctions.getBitmapFromAsset("images/selfie.png", this);
        } else {
            bitmap = ((MainApp) getApplication()).GetSelfie();
        }
        if(gpsLocation!=null){
            request.longitud = gpsLocation.getLongitud();
            request.latitud = gpsLocation.getLatitud();
        }else{
            request.longitud = "0.0";
            request.latitud = "0.0";
        }

        //System.out.println("REQUEST lat: " + request.latitud + " lon: " + request.longitud);


        request.FileEncode64 = HelperFunctions.encodeTobase64(bitmap);
        //Session session = ((MainApp) getApplication()).GetUserSession();
        //request.DNI = session.dni;
        //request.Sexo = session.gender;
        request.Token = token;
        request.idValidation=idValidacion;
        try {
            request.appVersion = activity.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        request.os = "Android (" + Build.MANUFACTURER + " " + Build.MODEL +")";
        request.version = Build.VERSION.RELEASE;
        Call<LoginResponse> callUser = apiServiceUser.login(request);
        showProgressDialog(getString(R.string.alert_validando_identidad));
        final Activity activity = this;
        callUser.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                hideProgressDialog();
                if (response.isSuccessful()) {

                    if (response.body().IsOk()) {
                        LoginResponse loginResponse = response.body();
                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                        intent.putExtra("UserName", loginResponse.Name);
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
                        sql = new SQLHelper(activity);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("E dd LLL yyyy. h:m a", Locale.getDefault());
                        Date date = new Date();
                        String Diretion = GPSLocation.getmAddress() + " '\n'" + GPSLocation.getmCity() + " '\n'" + GPSLocation.getmState() + " '\n'" + GPSLocation.getmCountry() + " '\n'" + GPSLocation.getmPostalCode();
                        CUIL cuil = new CUIL(null, loginResponse.Cuil.toUpperCase(), dateFormat.format(date), String.valueOf(gpsLocation.getLatitud()), String.valueOf(gpsLocation.getLongitud()), Diretion,"");
                        sql.insertCUIL(cuil);
                        startActivity(intent);
                        finish();
                    } else {
                        HelperFunctions.ShowAlert(activity, response.body().Message);
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
            mProgressDialog.hide();
        }
    }

    /*Funcionalidades del menu*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_on) {
            if (sound) {
                item.setIcon(R.drawable.off_sound_3);
                item.setTitle(R.string.off);
                setSound("pause", activity);
                sound = false;
            } else {
                item.setIcon(R.drawable.on_sound_3);
                item.setTitle(R.string.on);
                sound = true;
            }
        } else if (id == R.id.help) {
            HelperFunctions.showDialog(R.string.nav_titulo_contacto, R.string.nav_content_contacto, 4, this, activity);
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("WrongConstant")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_terminos_condiciones) {
            HelperFunctions.showDialog(R.string.nav_titulo_termino_condiciones, R.string.nav_content_termino_condiciones, 1, this, activity);
        } else if (id == R.id.nav_politicas) {
            HelperFunctions.showDialog(R.string.nav_titulo_politicas, R.string.nav_content_politicas, 2, this, activity);
        } else if (id == R.id.nav_empresa) {
            HelperFunctions.showDialog(R.string.nav_titulo_empresa, R.string.nav_content_empresa, 3, this, activity);
        } else if (id == R.id.nav_contacto) {
            HelperFunctions.showDialog(R.string.nav_titulo_contacto, R.string.nav_content_contacto, 4, this, activity);
        } else if (id == R.id.nav_historial) {
            Intent intent = new Intent(LoginActivity.this, HistoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_salir) {
            AlertDialog.Builder dialogo = new AlertDialog.Builder(this);
            dialogo.setTitle("Alerta");
            dialogo.setMessage("¿ Esta seguro que desea salir de la aplicación ?");
            dialogo.setCancelable(true);
            dialogo.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo, int id) {
                    dialogo.cancel();
                }
            });
            dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo, int id) {
                    finish();
                }
            });
            //Cambio de color los botones del alert
            AlertDialog alert = dialogo.create();
            alert.show();
            Button btn_negativo = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
            btn_negativo.setTextColor(Color.BLACK);
            Button btn_positivo = alert.getButton(DialogInterface.BUTTON_POSITIVE);
            btn_positivo.setTextColor(Color.BLACK);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        if (sound) {
            menu.findItem(R.id.action_on).setIcon(R.drawable.on_sound_3);
            menu.findItem(R.id.action_on).setTitle(R.string.on);
            setSound("play", activity);
        } else {
            menu.findItem(R.id.action_on).setIcon(R.drawable.off_sound_3);
            menu.findItem(R.id.action_on).setTitle(R.string.off);
            setSound("pause", activity);
        }
        return true;
    }

    /*Funcionalidades del menu fin */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (sound) {
            setSound("stop", activity);
        }
        finish();
    }

    public void soun_ini_login() {
        if (sound) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        synchronized (this) {
                            wait(1500);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (FEDEVIDA) {
                                        setSound("stop", activity);
                                        final int duration = setSound("fedevida", activity);

                                        Thread thread = new Thread() {
                                            @Override
                                            public void run() {
                                                try {
                                                    synchronized (this) {
                                                        wait(duration + 1000);
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                if (REGISTRATE) {
                                                                    setSound("registrate", activity);
                                                                }
                                                            }
                                                        });
                                                    }
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        };
                                        thread.start();
                                    }
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sound) {
            setSound("pause", activity);
            FEDEVIDA = false;
            REGISTRATE = false;
        }
    }

    private boolean servGPS() {
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
               /* }else{
                    final int locationMode;
                    try {
                        locationMode=Settings.Secure.getInt(getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_MODE);
                    }catch (Settings.SettingNotFoundException e){
                        Log.i(TAG, "SettingNotFoundException error: "+e.getMessage());
                        return  false;
                    }
                    switch (locationMode){
                        case Settings.Secure.LOCATION_MODE_HIGH_ACCURACY:
                        case Settings.Secure.LOCATION_MODE_SENSORS_ONLY:
                            return  true;
                        case Settings.Secure.LOCATION_MODE_BATTERY_SAVING:
                        case Settings.Secure.LOCATION_MODE_OFF:
                            return  false;
                        default:
                            return  false;
                    }
                }*/

            }
        } catch (Exception ex) {
            Log.i(TAG, "checkGPSStatus gps_enabled error: " + ex.getMessage());
            return false;
        }
        return gps_enabled;
    }

    private void autenticarPaso1(){
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_autenticar_dni);
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
                    CreateAlert(activity, "Alerta", activity.getString(R.string.contact_validar_dni)).show();
                }
                try {
                    Call<SearchdniResponse> callUser = service.searchdni(request);
                    showProgressDialog("Aguarde un momento, procesando la información");
                    callUser.enqueue(new Callback<SearchdniResponse>() {
                        @Override
                        public void onResponse(Call<SearchdniResponse> call, Response<SearchdniResponse> response) {
                            hideProgressDialog();
                            SearchdniResponse Response = response.body();
                            if (response.isSuccessful()) {
                                if (Response.IsOk()) {
                                    if (Response.idValidacion>0) {
                                        idValidacion=Response.idValidacion;
                                        System.out.println("idValue "+Response.idValidacion);
                                        dialog.dismiss();
                                        takePicture();
                                    } else {
                                        HelperFunctions.ShowAlert(activity, "Tu DNI no se encuentra registrado, te recomendamos realiza el debido registro");
                                    }
                                } else {
                                    HelperFunctions.ShowAlert(activity, Response.Message);
                                }
                            } else {
                                HelperFunctions.ShowAlert(activity, response.message());
                            }
                        }

                        @Override
                        public void onFailure(Call<SearchdniResponse> call, Throwable t) {
                            hideProgressDialog();

                            try{
                                findViewById(R.id.btnConfirmar).setEnabled(true);
                            }
                            catch (Exception e){

                            }
                            HelperFunctions.ShowAlert(activity, "Requiere una conexión internet estable");
                        }
                    });

                } catch (Exception ex) {
                    HelperFunctions.ShowAlert(activity, "Se ah producido un error en la autenticación del DNI ");
                    findViewById(R.id.btnConfirmar).setEnabled(true);
                }
            }
        });
        dialog.show();
    }
}

