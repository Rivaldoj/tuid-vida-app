package com.autenticar.tuid.v2;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.autenticar.tuid.v2.components.FeDeVidaOscuraDialog;
import com.autenticar.tuid.v2.utils.BitmapsHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;



public class FeDeVidaActivity extends TuIdActivity  implements ActivityCompat.OnRequestPermissionsResultCallback, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "FEDEVIDAACTIVITY";
    public static final String GESTURE_TO_DETECT = "GESTURE_TO_DETECT";
    public static final int DETECTAR_PESTANEO_CLOSED_EYES = 7;
    public static final int CANTIDAD_DETECCIONES_NECESARIAS = 3; // pestaneos necesarios
    private static final int PERMISSION_REQUESTS = 1;

    public static FeDeVidaActivity self;
    boolean sono_el_3 = false;

    private PreviewView preview;
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

    // --- Camara: CameraX + ML Kit (reemplaza a Mobile Vision CameraSource) ---
    private ProcessCameraProvider cameraProvider;
    private final ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
    private com.google.mlkit.vision.face.FaceDetector faceDetector;
    private volatile boolean getImage = false;

    // estado del conteo de pestaneos (antes vivia en el Detector.Processor anonimo)
    private boolean errorDisplayed = false;
    private boolean ojoEstanCerrados = false;
    private int pestaneos = 0;

    private void createCameraSource() {
        if (faceDetector == null) {
            FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .setMinFaceSize(0.7f)
                    .build();
            faceDetector = FaceDetection.getClient(options);
        }
    }

    private void onFaces(final List<Face> faces) {
        synchronized (this) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (stop) return;
                    if (faces.size() == 1 && faceIsCentered(faces)) {
                        oval.setVisibility(View.VISIBLE);
                        Face face = faces.get(0);
                        Float leftProb = face.getLeftEyeOpenProbability();
                        Float rightProb = face.getRightEyeOpenProbability();
                        if (leftProb == null || rightProb == null) return;
                        float left_open_percentage = leftProb;
                        float right_open_percentage = rightProb;
                        if (errorDisplayed) {
                            alertMessager.setText("Capturando movimientos");
                            alertMessager.setBackgroundResource(R.color.colorPrimary);
                            errorDisplayed = false;
                        }

                        if (ojoEstanCerrados) {
                            if (left_open_percentage > 0.8 && right_open_percentage > 0.8) {
                                if (pestaneos < 3) {
                                    setProgresoEnPestaneos(++pestaneos);
                                    ojoEstanCerrados = false;
                                } else {
                                    if (resultBitmap == null) getImage = true;
                                    else if (sono_el_3) {
                                        gestureDetected();
                                        ojoEstanCerrados = false;
                                    }
                                }
                            }
                        } else {
                            if (pestaneos == 3) {
                                ojoEstanCerrados = true;
                                return;
                            }
                            if (left_open_percentage < 0.2 && right_open_percentage < 0.2) {
                                ojoEstanCerrados = true;
                            }
                        }
                    } else {
                        oval.setVisibility(View.INVISIBLE);
                        setProgresoEnPestaneos(pestaneos = 0);
                        if (!errorDisplayed) {
                            errorDisplayed = true;
                            String error = "Alerta: NO se detectan rostros....";
                            if (faces.size() == 1) error = "Alerta: Posicione su cara en el centro del circulo.";
                            if (faces.size() > 1) error = "Alerta: Se detectan más de dos rostros.";
                            alertMessager.setText(error);
                            alertMessager.setBackgroundResource(R.color.colorAccentDark);
                        }
                    }
                }
            });
        }
    }

    boolean faceIsCentered(List<Face> faces) {
        return true; // validacion desactivada (igual que en la version anterior)
    }

    @androidx.camera.core.ExperimentalGetImage
    private void analyze(final ImageProxy imageProxy) {
        if (imageProxy.getImage() == null || faceDetector == null) {
            imageProxy.close();
            return;
        }
        final int rotation = imageProxy.getImageInfo().getRotationDegrees();
        if (getImage && resultBitmap == null) {
            try {
                Bitmap raw = imageProxy.toBitmap();
                resultBitmap = BitmapsHelper.RotateBitmap(raw, rotation);
            } catch (Exception e) {
                Log.e(TAG, "No se pudo capturar la imagen", e);
            }
            getImage = false;
        }
        InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), rotation);
        faceDetector.process(image)
                .addOnSuccessListener(cameraExecutor, new OnSuccessListener<List<Face>>() {
                    @Override
                    public void onSuccess(List<Face> faces) {
                        onFaces(faces);
                    }
                })
                .addOnCompleteListener(cameraExecutor, new OnCompleteListener<List<Face>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Face>> task) {
                        imageProxy.close();
                    }
                });
    }

    @androidx.camera.core.ExperimentalGetImage
    private void startCameraSource() {
        if (faceDetector == null) createCameraSource();
        if (preview == null) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) return;
        final ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    cameraProvider = future.get();
                    bindCamera();
                } catch (Exception e) {
                    Log.e(TAG, "No se pudo iniciar la camara", e);
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @androidx.camera.core.ExperimentalGetImage
    private void bindCamera() {
        if (cameraProvider == null) return;
        cameraProvider.unbindAll();
        Preview previewUseCase = new Preview.Builder().build();
        previewUseCase.setSurfaceProvider(preview.getSurfaceProvider());
        ImageAnalysis analysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        analysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                FeDeVidaActivity.this.analyze(image);
            }
        });
        CameraSelector selector = cameraIsFront ? CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;
        try {
            cameraProvider.bindToLifecycle(this, selector, previewUseCase, analysis);
        } catch (Exception e) {
            Log.e(TAG, "bindToLifecycle fallo", e);
        }
    }

    boolean cameraIsFront = true;

    @androidx.camera.core.ExperimentalGetImage
    void rotateCamera() {
        cameraIsFront = !cameraIsFront;
        bindCamera();
    }

    @Override
    @androidx.camera.core.ExperimentalGetImage
    public void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraProvider != null) cameraProvider.unbindAll();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) cameraProvider.unbindAll();
        cameraExecutor.shutdown();
        if (faceDetector != null) faceDetector.close();
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

