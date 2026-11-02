package com.autenticar.tuid.v2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.autenticar.tuid.MainApp;
import com.autenticar.tuid.R;
import com.autenticar.tuid.utils.HelperFunctions;
import com.autenticar.tuid.v2.components.CameraSourcePreview;
import com.autenticar.tuid.v2.components.FeDeVidaOscuraDialog;
import com.autenticar.tuid.v2.utils.FaceDetector;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.face.Face;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import static com.autenticar.tuid.facedetection.FaceDetectionProcessor.CANTIDAD_DETECCIONES_NECESARIAS;

public class FeDeVidaActivity extends TuIdActivity  implements ActivityCompat.OnRequestPermissionsResultCallback, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "FEDEVIDAACTIVITY";
    public static final String GESTURE_TO_DETECT = "GESTURE_TO_DETECT";
    private static final int PERMISSION_REQUESTS = 1;

    public static FeDeVidaActivity self;
    boolean sono_el_3 = false;

    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private ProgressBar progressBar;
    public static FrameLayout fireTopLayout;
    public TextView alertMessager;
    ImageView oval;
    boolean stop = false;

    boolean playSoundsAux = true;
    public static Bitmap resultBitmap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        self = this;
        super.onCreate(savedInstanceState);
        resultBitmap = null;
        setContentView(R.layout.v2_fe_de_vida_scan);

        try{
            //OCULTO DRAWER Y ACTION BAR SI EL INTENT LO INDICAR (REGISTRO MANUAL)
            if(getIntent().getExtras().getBoolean("manual", false)){
                getSupportActionBar().hide();
                playSoundsAux = false;
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }
        }
        catch (Exception e){

        }
        playGuideSounds(new int[]{R.raw.iluminacion, R.raw.fondoclaro, R.raw.pestania}, 0, null);

        ((TextView) findViewById(R.id.lblGestosNecesarios)).setText("Por favor pestañea 3 veces");
        alertMessager= findViewById(R.id.alertMessager);
        alertMessager.setText("Verificando tu fe de vida...");
        oval = findViewById(R.id.ovalok);
        fireTopLayout = findViewById(R.id.fireTopLayout);
        preview = findViewById(R.id.preview);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setIndeterminate(false);
        progressBar.setMax(CANTIDAD_DETECCIONES_NECESARIAS);
        progressBar.setProgress(0);
        progressBar.getIndeterminateTintList();

        if (!allPermissionsGranted()) createCameraSource(); else getRuntimePermissions();
        createCameraSource();

        findViewById(R.id.fe_de_vida__rotate_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotateCamera();
            }
        });
    }

    FeDeVidaOscuraDialog dlg;
    public void gestureDetected() {
        stop = true;
        float oscuridad = pictureisDark(resultBitmap);
        if(oscuridad>=40){
            FeDeVidaOscuraDialog dlg = new FeDeVidaOscuraDialog(this, oscuridad);
            dlg.setListener( new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("ASD","LaALA");
                    setProgresoEnPestaneos(0);
                    stop = false;
                    dlg.dismiss();
                }
            });
            dlg.show();
        }
        else{
            setResult(RESULT_OK);
            HelperFunctions.Vibrate(this);
            ((MainApp) getApplication()).SetSelfie(resultBitmap);
            if(MainApp.isTesting)((ImageView)HomeActivity.self.findViewById(R.id.lblLogo)).setImageBitmap(resultBitmap);
            finish();
        }
//        new AlertDialog.Builder(HomeActivity.self)
//                .setTitle("Luminosidad de la foto")
//                .setMessage("La foto tiene un "+ ((int)oscuridad) +"% de oscuridad")
//
//                // Specifying a listener allows you to take an action before dismissing the dialog.
//                // The dialog is automatically dismissed when a dialog button is clicked.
//                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                })
//                .setIcon(android.R.drawable.ic_dialog_alert)
//                .show();
    }

    float pictureisDark(Bitmap a){
        int histogram[] = new int[256];
        for (int i=0;i<256;i++) {
            histogram[i] = 0;
        }

        for (int x = 0; x < a.getWidth(); x++) {
            for(int y = 0; y < a.getHeight(); y++) {
                int pixel = a.getPixel(x, y);

                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                int brightness = (int) (0.2126*r + 0.7152*g + 0.0722*b);
                histogram[brightness]++;
            }
        }
        Log.e("ARRAY", Arrays.toString(histogram));

        float allPixelsCount = a.getWidth() * a.getHeight();

        float darkPixelCount = 0;
        for (int i=0;i<18;i++) {
            darkPixelCount += histogram[i];
        }

        return 100* (darkPixelCount/allPixelsCount);
    }


    void setProgresoEnPestaneos(int pestaneos_cant){
        progressBar.setProgress(pestaneos_cant);
        if(pestaneos_cant==0){
            resultBitmap=null;
            sono_el_3 = false;
        }
        switch (pestaneos_cant){
            case 1: playGuideSounds(new int[]{R.raw.uno}, 0, null);break;
            case 2: playGuideSounds(new int[]{R.raw.segundo}, 0, null);break;
            case 3: playGuideSounds(new int[]{R.raw.tres}, 0, new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sono_el_3 = true;
                        }
                    });
                }
            });break;
        }
    }

    private void createCameraSource() {
        if (cameraSource == null) {
            FaceDetector faceRecognizer = new FaceDetector.Builder(getApplicationContext()).setClassificationType(FaceDetector.ALL_CLASSIFICATIONS).setLandmarkType(FaceDetector.ALL_LANDMARKS).setProminentFaceOnly(false).setMode(FaceDetector.ACCURATE_MODE).setMinFaceSize(0.7f).build();
            Detector.Processor<Face> processor = new Detector.Processor<Face>() {
                boolean errorDisplayed = false;
                boolean ojoEstanCerrados = false;
                int pestaneos = 0;

                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<Face> detections) {
                    synchronized (this){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(stop) return;
                                if(detections.getDetectedItems().size()==1  && faceIsCentered(detections)){
                                    oval.setVisibility(View.VISIBLE);
                                    Face face = detections.getDetectedItems().get(detections.getDetectedItems().keyAt(0)) ;
                                    float left_open_percentage = face.getIsLeftEyeOpenProbability();
                                    float right_open_percentage = face.getIsRightEyeOpenProbability();
                                    if(errorDisplayed){
                                        alertMessager.setText("Capturando movimientos");
                                        alertMessager.setBackgroundResource(R.color.colorPrimary);
                                        errorDisplayed=false;
                                    }

                                    if(ojoEstanCerrados){
                                        if( left_open_percentage > 0.8 && right_open_percentage > 0.8){
                                            if(pestaneos<3){
                                                setProgresoEnPestaneos(++pestaneos);
                                                ojoEstanCerrados = false;
                                            }
                                            else{
                                                if(resultBitmap==null) faceRecognizer.getImage = true;
                                                else
                                                 {
                                                     if(sono_el_3) {
                                                         gestureDetected();
                                                         ojoEstanCerrados = false;
                                                     }
                                                 }
                                                return;
                                            }

                                            return;
                                        }
                                    }
                                    else{
                                        if(pestaneos==3){
                                            ojoEstanCerrados=true;
                                            return;
                                        }
                                        if( left_open_percentage < 0.2 && right_open_percentage < 0.2){
                                            ojoEstanCerrados = true;
                                            return;
                                        }
                                    }
                                }
                                else{
                                    oval.setVisibility(View.INVISIBLE);
                                    setProgresoEnPestaneos(pestaneos=0);
                                    if(!errorDisplayed) {
                                        errorDisplayed = true;
                                        String error = "Alerta: NO se detectan rostros....";
                                         if(detections.getDetectedItems().size()==1) error = "Alerta: Posicione su cara en el centro del circulo.";
                                         if(detections.getDetectedItems().size()>1) error = "Alerta: Se detectan más de dos rostros.";
                                        alertMessager.setText(error);
                                        alertMessager.setBackgroundResource(R.color.colorAccentDark);
                                    }
                                }
                            }
                        });
                    }
                }
            };
            faceRecognizer.setProcessor(processor);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;
            Log.e("ASD", width +" :" + height);

            cameraSource = new CameraSource.Builder(getApplicationContext(),faceRecognizer)
                    .setFacing(cameraIsFront?CameraSource.CAMERA_FACING_FRONT:CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(width,height)
//                .setRequestedPreviewSize(1600, 1024)
//                    .setRequestedPreviewSize(finalWidth, finalHeight)
//                    .setRequestedFps(15.0f)

                    .setAutoFocusEnabled(true)
                    .build();

        }
    }

    boolean metadata_calculated = false;
    RectF oval_rectangle;
    boolean faceIsCentered(Detector.Detections<Face> detections){
        if(true) return true; // desactivo la validacion
        if(!metadata_calculated) {
            float offset_percentage = 10f;
            int width = detections.getFrameMetadata().getWidth();
            int height = detections.getFrameMetadata().getHeight();
            oval_rectangle = new RectF(width/offset_percentage,height/offset_percentage, width-(width/offset_percentage),height-(height/offset_percentage));
            metadata_calculated = true;
        }
        Face face = detections.getDetectedItems().get(detections.getDetectedItems().keyAt(0));
        RectF face_rect = new RectF(face.getPosition().x, face.getPosition().y, face.getPosition().x+face.getWidth(),face.getPosition().y+face.getHeight());
        return oval_rectangle.left < face_rect.left && oval_rectangle.right > face_rect.right && oval_rectangle.top<face_rect.top && oval_rectangle.bottom > face_rect.bottom;
    }

    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if(preview!=null) preview.start(cameraSource);
            } catch (IOException e) {
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    boolean cameraIsFront = true;
    void rotateCamera(){
        cameraIsFront = !cameraIsFront;
        if(cameraSource!=null){
            cameraSource.release();
            cameraSource = null;
        }
        createCameraSource();
        startCameraSource();
    }

    @Override
    public void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        Log.e("ASD", "getRuntimePermissions");
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
        if (allPermissionsGranted()) {
            createCameraSource();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }


    @Override
    public void playGuideSounds(int[] raws, int startPos, Runnable afterAllRunnable) {
        if(!playSoundsAux) return;
        super.playGuideSounds(raws, startPos, afterAllRunnable);
    }
}

