// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.autenticar.tuid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.autenticar.tuid.facedetection.FaceDetectionProcessor;
import com.autenticar.tuid.facedetection.IGestureResult;
import com.autenticar.tuid.ui.vision.CameraSource;
import com.autenticar.tuid.ui.vision.CameraSourcePreview;
import com.autenticar.tuid.ui.vision.GraphicOverlay;
import com.autenticar.tuid.utils.HelperFunctions;
import com.google.android.gms.common.annotation.KeepName;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.autenticar.tuid.facedetection.FaceDetectionProcessor.CANTIDAD_DETECCIONES_NECESARIAS;
import static com.autenticar.tuid.facedetection.FaceDetectionProcessor.DETECTAR_OJOSCERRADOS;
import static com.autenticar.tuid.facedetection.FaceDetectionProcessor.DETECTAR_OJOSCERRADOS_SONRIENDO;
import static com.autenticar.tuid.facedetection.FaceDetectionProcessor.DETECTAR_OJO_DIESTRO_CERRADO_SONRIENDO;
import static com.autenticar.tuid.facedetection.FaceDetectionProcessor.DETECTAR_OJO_IZQ_CERRADO_SONRIENDO;
import static com.autenticar.tuid.facedetection.FaceDetectionProcessor.DETECTAR_PESTANEO_CLOSED_EYES;
import static com.autenticar.tuid.facedetection.FaceDetectionProcessor.DETECTAR_PESTANEO_NEUTRAL;
import static com.autenticar.tuid.facedetection.FaceDetectionProcessor.DETECTAR_SIN_GESTOS;
import static com.autenticar.tuid.facedetection.FaceDetectionProcessor.DETECTAR_SONRISA;
import static com.autenticar.tuid.utils.HelperFunctions.setSound;
import static com.autenticar.tuid.utils.HelperFunctions.sound;

/**
 * Demo app showing the various features of ML Kit for Firebase. This class is used to
 * set up continuous frame processing on frames from a camera source.
 */
@KeepName
public final class LivePreviewActivity extends AppCompatActivity
        implements IGestureResult, ActivityCompat.OnRequestPermissionsResultCallback, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "LivePreviewActivity";
    public static final String GESTURE_TO_DETECT = "GESTURE_TO_DETECT";
    private static final int PERMISSION_REQUESTS = 1;

    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private int gestureToDetect;
    private ProgressBar progressBar;
    final Activity activity = this;
    boolean ban1 = true;
    boolean ban2 = true;
    boolean ban3 = true;
    private boolean FONDOCLARO = true;
    private boolean SINANTEOJOS = true;
    public static FrameLayout fireTopLayout;
    public  TextView alertMessager;
    private TextView nav_salir;


    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }

    private String gestureToDetectText() {
        switch (gestureToDetect) {
            case DETECTAR_SONRISA:
                return "Sonrie";
            case DETECTAR_OJO_IZQ_CERRADO_SONRIENDO:
                return "Cierra tu ojo izquierdo y sonrie";
            case DETECTAR_OJO_DIESTRO_CERRADO_SONRIENDO:
                return "Cierra tu ojo derecho y sonrie";
            case DETECTAR_OJOSCERRADOS_SONRIENDO:
                return "Cierra tus ojos y sonrie. El teléfono vibrará cuando terminemos";
            case DETECTAR_OJOSCERRADOS:
                return "Cierra tus ojos. El teléfono vibrará cuando terminemos";
            case DETECTAR_PESTANEO_CLOSED_EYES:
                return "Por favor pestañea 3 veces";
            case DETECTAR_SIN_GESTOS:
                return "No hagas ningún gesto";
        }
        return "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v2_activity_live_preview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if (sound) {
            final int duration = setSound("bieniluminado", activity);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        synchronized (this) {
                            wait(duration + 1000);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (sound && FONDOCLARO) {
                                        FONDOCLARO = false;
                                        final int duraction_1 = setSound("fondoclaro", activity);
                                        Thread thread_1 = new Thread() {
                                            @Override
                                            public void run() {
                                                try {
                                                    synchronized (this) {
                                                        wait(duraction_1 + 1000);
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                if (sound && SINANTEOJOS) {
                                                                    SINANTEOJOS = false;
                                                                    setSound("sinanteojos", activity);
                                                                }
                                                            }
                                                        });
                                                    }
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            ;
                                        };
                                        thread_1.start();
                                    }

                                }
                            });
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                ;
            };
            thread.start();
        }
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
        View hView = navigationView.getHeaderView(0);
        TextView nav_username = (TextView) hView.findViewById(R.id.nav_username);
        alertMessager= (TextView) findViewById(R.id.alertMessager);
        alertMessager.setText("Verificando tu fe de vida...");
        TextView nav_bien = (TextView) hView.findViewById(R.id.nav_bien);
        ImageView nav_ico_username = (ImageView) hView.findViewById(R.id.nav_ico_username);
        nav_username.setText("");
        nav_bien.setTextSize(20);
        fireTopLayout = findViewById(R.id.fireTopLayout);
        preview = findViewById(R.id.firePreview);

        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = findViewById(R.id.fireFaceOverlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        gestureToDetect = getIntent().getIntExtra(GESTURE_TO_DETECT, DETECTAR_SONRISA);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setIndeterminate(false);
        progressBar.setMax(CANTIDAD_DETECCIONES_NECESARIAS);
        progressBar.setProgress(0);
        progressBar.getIndeterminateTintList();

        ((TextView) findViewById(R.id.lblGestosNecesarios)).setText(gestureToDetectText());

        if (allPermissionsGranted()) {
            createCameraSource();
        } else {
            getRuntimePermissions();
        }
    }

    @Override
    public void GestureProgress(int i) {
        progressBar.setProgress(i);

        if(sound){
            Log.e("Progress", i+"");
            switch (i){
                case 1: setSound("uno", activity);break;
                case 2: setSound("segundo",activity);break;
                case 3: setSound("tres",activity);break;

            }
        }

;
       // progressBar.setVisibility(i > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void GestureDetected(FirebaseVisionFace face, Bitmap bitmap, int gestureType) {
        setResult(RESULT_OK);
        HelperFunctions.Vibrate(this);
        // if (gestureType == DETECTAR_SIN_GESTOS || gestureType == DETECTAR_PESTANEO_NEUTRAL) {
        ((MainApp) getApplication()).SetSelfie(bitmap);
        finish();
    }

    private void createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }

        cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
        try {
            Log.i(TAG, "Using Face Detector Processor");
            cameraSource.setMachineLearningFrameProcessor(new FaceDetectionProcessor(this.gestureToDetect, this, activity, getApplicationContext(),  alertMessager));
        } catch (Exception e) {
            Log.e(TAG, "can not create camera source: " + e);
        }
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);

            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (sound) {
            setSound("play", activity);
            SINANTEOJOS = true;
            FONDOCLARO = true;
        }
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (sound) {
            setSound("pause", activity);
            SINANTEOJOS = false;
            FONDOCLARO = false;
        }
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
            createCameraSource();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
                sound = false;
                setSound("pause", activity);
            } else {
                item.setIcon(R.drawable.on_sound_3);
                item.setTitle(R.string.on);
                sound = true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

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
            Intent intent = new Intent(LivePreviewActivity.this, HistoryActivity.class);
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
        getMenuInflater().inflate(R.menu.main_, menu);
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
}
