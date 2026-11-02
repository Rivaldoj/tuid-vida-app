package com.autenticar.tuid;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.autenticar.tuid.AmplitudeManager.AmplitudeManager;
import com.autenticar.tuid.facedetection.FaceDetectionProcessor;
import com.autenticar.tuid.model.LoginRequest;
import com.autenticar.tuid.model.ReconfirmationRequest;
import com.autenticar.tuid.model.ReconfirmationResponse;
import com.autenticar.tuid.model.RevalidationRequest;
import com.autenticar.tuid.model.RevalidationResponse;
import com.autenticar.tuid.model.itemError;
import com.autenticar.tuid.services.TuidService;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.autenticar.tuid.utils.HelperFunctions;
import com.autenticar.tuid.v2.HomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.autenticar.tuid.LivePreviewActivity.GESTURE_TO_DETECT;
import static com.autenticar.tuid.utils.HelperFunctions.CreateAlert;
import static com.autenticar.tuid.utils.HelperFunctions.hideProgressDialog;
import static com.autenticar.tuid.utils.HelperFunctions.mProgressDialog;
import static com.autenticar.tuid.utils.HelperFunctions.setSound;
import static com.autenticar.tuid.utils.HelperFunctions.sound;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public final Activity activity = this;
    public static final int REQUEST_LIVE_PREVIEW = 7002;
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final String TAG = "MainActivity";
    final AppCompatActivity compatActivity = this;
    private FloatingActionButton fab, fab1;//, fab2;
    private LinearLayout fabLayout1;//, fabLayout2;
    private View fabBGLayout;
    private CircleImageView profileImage;
    private boolean isFABOpen = false;
    private Bitmap imagen_ico = null;
    private String token;
    public boolean p_location = false;
    static final int PERMISON_LOCATION = 2;
    private static double longitud = 0;
    private static double latitud = 0;
    private static TextView main_cuil;
    private static TextView main_email;
    private static TextView main_telefono;
    private static LinearLayout lay_fe_vida;
    private static TextView fe_vida;
    private static ImageView img_fe_vida;
    private static ListView list_error;
    public ArrayList<itemError> item;
    private static MainListValidationAdapter MainListValidationAdapter;
    String email_1 = "";
    String email_2 = "";
    String telefono_1 = "";
    String telefono_2 = "";
    private boolean newDialog = false;
    private int mode = -1;

    private Dialog dialog = null;
    private TextView nav_salir;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        try {
            FirebaseMessaging.getInstance().setAutoInitEnabled(true);
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "getInstanceId failed", task.getException());
                                return;
                            }
                            // Get new Instance ID token
                            token = task.getResult();
                            Log.d(TAG, token);
                        }
                    });

        } catch (Exception e) {
            Log.i(TAG, "FirebaseInstanceId error: " + e);
            token = "0";
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /*Donde se asigna el nombre de usuario en el menu*/
        View hView = navigationView.getHeaderView(0);
        TextView nav_username = (TextView) hView.findViewById(R.id.nav_username);
        nav_username.setText(getIntent().getStringExtra("UserName"));
        inicializacionHome();

//        fabLayout1 = (LinearLayout) findViewById(R.id.fabLayout1);
//        //fabLayout2 = (LinearLayout) findViewById(R.id.fabLayout2);
//        fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
//        //fab2 = (FloatingActionButton) findViewById(R.id.fab2);
//        fabBGLayout = findViewById(R.id.fabBGLayout);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (isFABOpen) {
//                    closeFABMenu();
//                } else {
//                    showFABMenu();
//                }
//            }
//        });
//        fab1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                actualizarData();
//                //showDialogActualizarDatosContacto();
//            }
//        });
        nav_salir=(TextView) findViewById(R.id.nav_salir);
        nav_salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogo = new AlertDialog.Builder(activity
                );
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

    /*findViewById(R.id.btnBanco).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Snackbar.make(view, "Información bancaria", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show();
      }
    });*/
        // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);

        final String banco = getIntent().getStringExtra("Banco").toUpperCase();
        if (banco != null) {
            findViewById(R.id.fab2).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    closeFABMenu();
                    HelperFunctions.ShowAlert(activity, banco);

                }
            });
            ((TextView) findViewById(R.id.main_aviso_instituto)).setText(banco);
        } else {
            findViewById(R.id.fab2).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    closeFABMenu();
                    HelperFunctions.ShowAlert(activity, "Su entidad bancaria no está adherida aún al sistema. Si le pareció útil solicite a su entidad que se adhiera al sistema de TuID Vida.");
                }
            });
            ((TextView) findViewById(R.id.main_aviso_instituto)).setText("Por Definir");
        }
//        if(banco.toUpperCase().equals("CAJA BAPRO")){
//            ((TextView) findViewById(R.id.main_aviso_aler)).setText("Si no es residente en la zona desfavorable, el próximo periodo de supervicion es: \n Desde el 15/04/2020\n Hasta el 15/07/2020");
//        }
        TextView main_aviso_certificado = (TextView) findViewById(R.id.main_aviso_certificado);
        if (getIntent().getStringExtra("Fecha_valida").equals("")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd LLL yyyy", Locale.getDefault());
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_YEAR, 30);
            String fecha = String.valueOf(dateFormat.format(calendar.getTime()));
            System.out.println(" calendar.getTime() " + fecha);
            main_aviso_certificado.setText(fecha);

            //ARREGLO ESTEBAN PARA EFECTIVO SI SI NO VIENE FECHA VALIDA, SE OCULTA EL CAMPO Y TAMBIEN EL LABEL DE ARRIBA
            main_aviso_certificado.setVisibility(View.GONE);
            findViewById(R.id.main_aviso_certificado_label).setVisibility(View.GONE);


            //FIN DE ARRGEO

        } else {
            main_aviso_certificado.setText(getIntent().getStringExtra("Fecha_valida"));
        }
       /* TextView main_aviso_fecha_cobro = (TextView) findViewById(R.id.main_aviso_fecha_cobro);
        if(getIntent().getStringExtra("Fecha_cobro")==null && getIntent().getStringExtra("Fecha_cobro").equals("")){
            main_aviso_fecha_cobro.setText("Sin Información");
        }else{
            main_aviso_fecha_cobro.setText(getIntent().getStringExtra("Sin Información"));
        }*/


    }

    private void inicializacionHome() {
        lay_fe_vida = (LinearLayout) findViewById(R.id.lay_fe_vida);
        fe_vida = (TextView) findViewById(R.id.fe_vida);
        img_fe_vida = (ImageView) findViewById(R.id.img_fe_vida);
        item = new ArrayList();
        

        if (getIntent().getStringExtra("UserName") != null) {
            ((TextView) findViewById(R.id.main_name)).setText(getIntent().getStringExtra("UserName"));
        } else {
            ((TextView) findViewById(R.id.main_name)).setVisibility(View.GONE);
        }
        main_cuil = (TextView) findViewById(R.id.main_cuil);
        if (getIntent().getStringExtra("Cuil") != null) {
            main_cuil.setText(getIntent().getStringExtra("Cuil"));
        } else {
            main_cuil.setText("No disponible");
            img_fe_vida.setBackground(getDrawable(R.drawable.ic_alert));
            fe_vida.setTextColor(getColor(R.color.colorRed));
            fe_vida.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            item.add(new itemError("Su fe de vida no ha sido registrada correctamente", "CUIL"));
        }
        main_email = (TextView) findViewById(R.id.main_email);
        if (getIntent().getStringExtra("Email") != null) {
            main_email.setText(getIntent().getStringExtra("Email"));
        } else {
            main_email.setText("No disponible");
            img_fe_vida.setBackground(getDrawable(R.drawable.ic_alert));
            fe_vida.setTextColor(getColor(R.color.colorRed));
            fe_vida.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            item.add(new itemError("Se debe validar su email", "EMAIL"));
        }
        main_telefono = (TextView) findViewById(R.id.main_telefono);
        if (getIntent().getStringExtra("Telefono") != null) {
            main_telefono.setText(getIntent().getStringExtra("Telefono"));
        } else {
            main_telefono.setText("No disponible");
            img_fe_vida.setBackground(getDrawable(R.drawable.ic_alert));
            fe_vida.setTextColor(getColor(R.color.colorRed));
            fe_vida.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            item.add(new itemError("Se debe validar su teléfono", "TELEFONO"));
        }
        if (item.size() > 0) {
            list_error = findViewById(R.id.lista_error);
            MainListValidationAdapter = new MainListValidationAdapter(item, activity, String.valueOf(getIntent().getIntExtra("userID", -1)), activity);
            list_error.setAdapter(MainListValidationAdapter);
        }
        else{
            rateApp();
        }


        profileImage = (CircleImageView) findViewById(R.id.ico_perfil);
        imagen_ico = ((MainApp) getApplication()).GetSelfie();
        if (imagen_ico != null) {
            profileImage.setImageBitmap(imagen_ico);
        }
    }

    private void closeFABMenu() {
        isFABOpen = false;
        fabBGLayout.setVisibility(View.GONE);
        fab.animate().rotationBy(-225);
        fabLayout1.animate().translationY(0).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (!isFABOpen) {
                    fabLayout1.setVisibility(View.GONE);
                    //fabLayout2.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        /*fabLayout2.animate().translationY(0).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (!isFABOpen) {
                    fabLayout1.setVisibility(View.GONE);
                    fabLayout2.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });*/
    }

    private void showFABMenu() {
        isFABOpen = true;
        fabLayout1.setVisibility(View.VISIBLE);
        //fabLayout2.setVisibility(View.VISIBLE);
        fabBGLayout.setVisibility(View.VISIBLE);
        fab.animate().rotationBy(225);
        fabLayout1.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        //fabLayout2.animate().translationY(-getResources().getDimension(R.dimen.standard_100));
    }

    protected void takePicture() {
        Intent intent = new Intent(getBaseContext(), LivePreviewActivity.class);
        intent.putExtra(GESTURE_TO_DETECT, FaceDetectionProcessor.DETECTAR_PESTANEO_CLOSED_EYES);
        startActivityForResult(intent, REQUEST_LIVE_PREVIEW);
    }

    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // We have permission, so create the camerasource
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Multitracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder dialogo = new AlertDialog.Builder(this);
        dialogo.setTitle(R.string.alert_titulo_alerta);
        dialogo.setMessage(R.string.main_alert_back);
        dialogo.setCancelable(true);
        dialogo.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo, int id) {
                dialogo.cancel();
            }
        });
        dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo, int id) {
                setSound("stop", activity);
//                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
//                startActivity(intent);
                finish();
            }
        });
        /*Cambio de color los botones del alert*/
        AlertDialog alert = dialogo.create();
        alert.show();
        Button btn_negativo = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        btn_negativo.setTextColor(Color.BLACK);
        Button btn_positivo = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        btn_positivo.setTextColor(Color.BLACK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode) {
            case REQUEST_LIVE_PREVIEW:
                login();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void consultarPermiso(String permission) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, PERMISON_LOCATION);
            } else {
                p_location = false;
            }
        } else {
            p_location = true;
        }
    }

    private void login() {
        consultarPermiso(Manifest.permission.ACCESS_FINE_LOCATION);
        if (p_location) {
            LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            final LoginRequest request = new LoginRequest();
            mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, request);
            TuidService apiServiceUser = ServiceGenerator.createService(TuidService.class);

            Bitmap bitmap = null;
            if (((MainApp) getApplication()).isDebugSession()) { //cambio las imagenes a subir
                bitmap = HelperFunctions.getBitmapFromAsset("images/selfie.png", this);
            } else {
                bitmap = ((MainApp) getApplication()).GetSelfie();

            }
            request.latitud = String.valueOf(latitud);
            request.longitud = String.valueOf(longitud);
            try {
                request.appVersion = activity.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            request.os = "Android (" + Build.MANUFACTURER + " " + Build.MODEL +")";
            request.version = Build.VERSION.RELEASE;
            profileImage.setImageBitmap(bitmap);
            try {
                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (task.isSuccessful()) {
                                    request.Token = task.getResult();
                                } else {
                                    request.Token = "";
                                }
                            }
                        });
            } catch (Exception e) {
                Log.i(TAG, "FirebaseInstanceId Error: " + e);
            }
        }
    }

    public void otraValidacion() {
        AlertDialog.Builder dialogo = new AlertDialog.Builder(activity);
        dialogo.setTitle(R.string.alert_titulo_alerta);
        dialogo.setMessage("¿Desea cerrar sesión y realizar otra validación de fe de vida?");
        dialogo.setCancelable(true);
        dialogo.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo, int id) {
                dialogo.cancel();
            }
        });
        dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo, int id) {

                int rc = ActivityCompat.checkSelfPermission(compatActivity, Manifest.permission.CAMERA);
                if (rc == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                } else {
                    requestCameraPermission();
                }

            }
        });
        /*Cambio de color los botones del alert*/
        AlertDialog alert = dialogo.create();
        alert.show();
        Button btn_negativo = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        btn_negativo.setTextColor(Color.BLACK);
        Button btn_positivo = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        btn_positivo.setTextColor(Color.BLACK);
    }

    /*Funcionalidades del menu*/
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
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_notificationes) {
            HelperFunctions.ShowAlert(activity, "No posee notificación");
            //Intent intent = new Intent(MainActivity.this, HistoryNotificationActivity.class);
            //intent.putExtra("Token",token);
            //startActivity(intent);
        } else if (id == R.id.nav_salir) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sing_out) {
            AlertDialog.Builder dialogo = new AlertDialog.Builder(this);
            dialogo.setTitle("Alerta");
            dialogo.setMessage("¿ Esta seguro que desea salir de la sesión ?");
            dialogo.setCancelable(true);
            dialogo.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo, int id) {
                    dialogo.cancel();
                }
            });
            dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo, int id) {
//                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
//                    startActivity(i);
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
        return super.onOptionsItemSelected(item);
    }

    /*Funcionalidades del menu fin */
    @Override
    protected void onPause() {
        super.onPause();
        if (sound) {
            setSound("pause", activity);
        }
        System.out.println("Entro en pausa");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void actualizarData(final int mode) {
        this.mode = mode;
        if (!newDialog) {
            newDialog=true;
            dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.dialog_actualizar);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            TextView text_title_info_empresarialdialog = (TextView) dialog.findViewById(R.id.text_title_info_empresarial);
            ImageView btn_cancel = (ImageView) dialog.findViewById(R.id.btn_cancel);
            ImageView btn_return = (ImageView) dialog.findViewById(R.id.btn_return);
            final EditText text_email_1 = (EditText) dialog.findViewById(R.id.text_email_1);
            final EditText text_email_2 = (EditText) dialog.findViewById(R.id.text_email_2);
            final EditText text_telefono_1 = (EditText) dialog.findViewById(R.id.text_telefono_1);
            final EditText text_telefono_2 = (EditText) dialog.findViewById(R.id.text_telefono_2);
            final EditText edit_validacion_email = (EditText) dialog.findViewById(R.id.edit_validacion_email);
            final EditText edit_validacion_telefono = (EditText) dialog.findViewById(R.id.edit_validacion_telefono);

            if(mode==1){
                text_telefono_1.setVisibility(View.GONE);
                text_telefono_2.setVisibility(View.GONE);
                dialog.findViewById(R.id.text_info_telefono_1_lo).setVisibility(View.GONE);
                dialog.findViewById(R.id.text_info_telefono_2_lo).setVisibility(View.GONE);
                edit_validacion_telefono.setVisibility(View.GONE);
            }
            if(mode==2){
                text_email_1.setVisibility(View.GONE);
                text_email_2.setVisibility(View.GONE);
                dialog.findViewById(R.id.text_info_email_1_lo).setVisibility(View.GONE);
                dialog.findViewById(R.id.text_info_email_2_lo).setVisibility(View.GONE);
                edit_validacion_email.setVisibility(View.GONE);
            }

            final Button btn_update_code = (Button) dialog.findViewById(R.id.btn_update_code);

            text_title_info_empresarialdialog.setText("Actualizar " + (mode==1?"Email":"Telefono"));
            btn_return.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.findViewById(R.id.fragment_actualizar).setVisibility(View.VISIBLE);
                    dialog.findViewById(R.id.fragment_validate).setVisibility(View.GONE);
                }
            });
            Button btn_update = (Button) dialog.findViewById(R.id.btn_update);
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            btn_update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mode==2) ((TextView)dialog.findViewById(R.id.info_envio)).setText("Se envió un SMS con el código de validación.");
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    email_1 = String.valueOf(text_email_1.getText());
                    email_2 = String.valueOf(text_email_2.getText());
                    telefono_1 = String.valueOf(text_telefono_1.getText());
                    telefono_2 = String.valueOf(text_telefono_2.getText());
                    if(!email_1.equals("") && telefono_1.equals("")){
                        if(!HelperFunctions.isValidEmail(email_1)){
                            CreateAlert(activity, "Alerta", getString(R.string.corre_invalido)).show();
                        }else if (!email_1.trim().equals(email_2.trim())) {
                            CreateAlert(activity, "Alerta", getString(R.string.validacion_conirmacion_email)).show();
                        }else{
                            sendUpdate();
                            if(email_1.equals("")){
                                edit_validacion_email.setVisibility(View.GONE);
                                ((TextView)dialog.findViewById(R.id.text_code_email)).setVisibility(View.GONE);
                            }
                            if(telefono_1.equals("")){
                                edit_validacion_telefono.setVisibility(View.GONE);
                                ((TextView)dialog.findViewById(R.id.text_code_sms)).setVisibility(View.GONE);
                            }
                        }
                    }else if(email_1.equals("") && !telefono_1.equals("")){
                        if(telefono_1.length() < 8){
                            CreateAlert(activity, "Alerta", getString(R.string.telefono_invalido)).show();
                        }else if (!telefono_1.trim().equals(telefono_2.trim())) {
                            CreateAlert(activity, "Alerta", getString(R.string.validacion_conirmacion_telefono)).show();
                        }else{
                            sendUpdate();
                            if(email_1.equals("")){
                                edit_validacion_email.setVisibility(View.GONE);
                                ((TextView)dialog.findViewById(R.id.text_code_email)).setVisibility(View.GONE);
                            }
                            if(telefono_1.equals("")){
                                edit_validacion_telefono.setVisibility(View.GONE);
                                ((TextView)dialog.findViewById(R.id.text_code_sms)).setVisibility(View.GONE);
                            }
                        }
                    }else if(!email_1.equals("") && !telefono_1.equals("")){
                        if (!HelperFunctions.isValidEmail(email_1)) {
                            CreateAlert(activity, "Alerta", getString(R.string.corre_invalido)).show();
                        } else if (!email_1.trim().equals(email_2.trim())) {
                            CreateAlert(activity, "Alerta", getString(R.string.validacion_conirmacion_email)).show();
                        } else if ( (telefono_1.length() < 8)) {
                            CreateAlert(activity, "Alerta", getString(R.string.telefono_invalido)).show();
                        } else if (!telefono_1.trim().equals(telefono_2.trim())) {
                            CreateAlert(activity, "Alerta", getString(R.string.validacion_conirmacion_telefono)).show();
                        } else {
                            sendUpdate();
                            if(email_1.equals("")){
                                edit_validacion_email.setVisibility(View.GONE);
                                ((TextView)dialog.findViewById(R.id.text_code_email)).setVisibility(View.GONE);
                            }
                            if(telefono_1.equals("")){
                                edit_validacion_telefono.setVisibility(View.GONE);
                                ((TextView)dialog.findViewById(R.id.text_code_sms)).setVisibility(View.GONE);
                            }
                        }
                    }else if(email_1.equals("") && telefono_1.equals("")){
                        CreateAlert(activity, "Alerta", "Debe validar al menos el email o teléfono, para continuar la actualización").show();
                    }
                }
            });


            btn_update_code.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (edit_validacion_telefono.getText().length() == 4 || edit_validacion_email.getText().length() == 4) {
                        showProgressDialog("Enviando código de validación...");
                        final TuidService service = ServiceGenerator.createService(TuidService.class);
                        final ReconfirmationRequest request_reconfirmation = new ReconfirmationRequest();
                        request_reconfirmation.userID = String.valueOf(getIntent().getIntExtra("userID", -1));
                        request_reconfirmation.CodeTel = String.valueOf(edit_validacion_telefono.getText());
                        request_reconfirmation.CodeEmail = String.valueOf(edit_validacion_email.getText());
                        final Call<ReconfirmationResponse> callUserReconfirmation = service.reconfirmation(request_reconfirmation);
                        callUserReconfirmation.enqueue(new Callback<ReconfirmationResponse>() {
                            @Override
                            public void onResponse(Call<ReconfirmationResponse> call, Response<ReconfirmationResponse> response) {
                                hideProgressDialog();
                                if (response.isSuccessful()) {
                                    if (response.body().IsOk()) {
                                        ReconfirmationResponse reconfirmationResponse = response.body();

                                        //TELEFONO Y MAIL
                                        if(!telefono_1.equals("") && !email_1.equals("")) {
                                            if(reconfirmationResponse.Email.equals("true") && reconfirmationResponse.Telefono.equals("true")) {
                                                ((TextView) activity.findViewById(R.id.main_telefono)).setText(telefono_1);
                                                ((TextView) activity.findViewById(R.id.main_email)).setText(email_1);
                                                newDialog = false;
                                                HelperFunctions.ShowAlert(activity, "El cambió de Email y Número teléfonico se realizó correctamente. ");
                                                MainListValidationAdapter.removeErrorFromList("EMAIL");
                                                MainListValidationAdapter.removeErrorFromList("TELEFONO");
                                                dialog.dismiss();
                                            }else if(!reconfirmationResponse.Email.equals("true") && reconfirmationResponse.Telefono.equals("true")){
                                                newDialog=true;
                                                ((TextView) activity.findViewById(R.id.main_telefono)).setText(telefono_1);
                                                edit_validacion_telefono.setText("");
                                                edit_validacion_telefono.setVisibility(View.GONE);
                                                ((TextView)dialog.findViewById(R.id.text_code_sms)).setText("Su código SMS fue validado correctamente");
                                                MainListValidationAdapter.removeErrorFromList("TELEFONO");
                                            }else if(reconfirmationResponse.Email.equals("true") || !reconfirmationResponse.Telefono.equals("true")){
                                                newDialog=true;
                                                ((TextView) activity.findViewById(R.id.main_email)).setText(email_1);
                                                edit_validacion_email.setText("");
                                                ((TextView)dialog.findViewById(R.id.text_code_email)).setText("Su Email fue validado correctamente");
                                                edit_validacion_email.setVisibility(View.GONE);
                                                MainListValidationAdapter.removeErrorFromList("EMAIL");
                                            }

                                            //NI TELEFONO NI MAIL
                                        }else if(telefono_1.equals("") && email_1.equals("")) {
                                            HelperFunctions.ShowAlert(activity, "Ocurrio un problema con la verificación");
                                            // TELEFONO
                                        }else if(!telefono_1.equals("")  && email_1.equals("")) {
                                            if(reconfirmationResponse.Telefono.equals("true")){
                                                newDialog=false;
                                                ((TextView) activity.findViewById(R.id.main_telefono)).setText(telefono_1);
                                                edit_validacion_telefono.setText("");
                                                edit_validacion_telefono.setVisibility(View.GONE);
                                                dialog.dismiss();
                                                ((TextView)dialog.findViewById(R.id.text_code_sms)).setText("Su código SMS fue validado correctamente");
                                                MainListValidationAdapter.removeErrorFromList("TELEFONO");
                                            }else{
                                                HelperFunctions.ShowAlert(activity, "Código suministrado no es valido ");
                                            }

                                            //EMAIL
                                        }else if(telefono_1.equals("") && !email_1.equals("")) {
                                            if(reconfirmationResponse.Email.equals("true")){
                                                newDialog=false;
                                                ((TextView) activity.findViewById(R.id.main_email)).setText(email_1);
                                                edit_validacion_email.setText("");
                                                ((TextView)dialog.findViewById(R.id.text_code_email)).setText("Su Email fue validado correctamente");
                                                edit_validacion_email.setVisibility(View.GONE);
                                                MainListValidationAdapter.removeErrorFromList("EMAIL");
                                                dialog.dismiss();
                                            }else{
                                                HelperFunctions.ShowAlert(activity, "Código suministrado no es valido ");
                                            }
                                        }
                                    } else {
                                        newDialog=true;
                                        HelperFunctions.ShowAlert(activity, response.body().Message);
                                    }
                                } else {
                                    newDialog=true;
                                    HelperFunctions.ShowAlert(activity, "Ocurrio un error con la validación ");
                                }
                            }

                            @Override
                            public void onFailure(Call<ReconfirmationResponse> call, Throwable t) {
                                hideProgressDialog();
                            }
                        });
                    }
                }
            });
            dialog.show();
        }else {
            try {
                dialog.show();
            }catch (Exception e){
                Log.i(TAG, " dialog.show(): "+e);
            }
        }
    }
    private void sendUpdate() {
        final TuidService service = ServiceGenerator.createService(TuidService.class);
        final TextView info_envio = (TextView) dialog.findViewById(R.id.info_envio);
        final RevalidationRequest request_revalidation = new RevalidationRequest();
        request_revalidation.userID = String.valueOf(getIntent().getIntExtra("userID", -1));
        request_revalidation.email = email_1;
        request_revalidation.telefono = telefono_1;
        final Call<RevalidationResponse> callUser = service.revalidation(request_revalidation);
        showProgressDialog("Enviando Actualización de datos...");
        callUser.enqueue(new Callback<RevalidationResponse>() {
            @Override
            public void onResponse(Call<RevalidationResponse> call, Response<RevalidationResponse> response) {
                hideProgressDialog();
                RevalidationResponse revalidationResponse = response.body();
                if (response.isSuccessful()) {
                    if (revalidationResponse.IsOk()) {
                        dialog.findViewById(R.id.fragment_actualizar).setVisibility(View.GONE);
                        dialog.findViewById(R.id.fragment_validate).setVisibility(View.VISIBLE);

                    }
                }
            }

            @Override
            public void onFailure(Call<RevalidationResponse> call, Throwable t) {
                hideProgressDialog();
            }
        });
    }
    protected void showProgressDialog(String message) {
        if (mProgressDialog == null || true) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(message);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();

    }

    public void editmail(View v){
        if(mode==2)newDialog=false;
        actualizarData(1);
    }

    public void editphone(View v){
        if(mode==1)newDialog=false;
        actualizarData(2);
    }


    public void rateApp(){
        final Context context = this;
        try{
            SharedPreferences sp = this.getSharedPreferences("TuIDVidaSP", MODE_PRIVATE);
            if(!sp.contains("AppRated")){
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            new RateAppDialog(context);
                        }
                        catch (Exception e){

                        }
                    }
                }, 5000);

            }
        }
        catch (Exception e){

        }
    }
}
