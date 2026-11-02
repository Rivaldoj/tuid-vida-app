/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.autenticar.tuid;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
//import android.support.annotation.NonNull;
//import android.support.design.widget.NavigationView;
//import android.support.design.widget.Snackbar;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.view.GravityCompat;
//import android.support.v4.widget.DrawerLayout;
//import android.support.v7.app.ActionBarDrawerToggle;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.autenticar.tuid.camera.CameraSourcePreview;
import com.autenticar.tuid.camera.DocumentOverlay;
import com.autenticar.tuid.camera.GraphicOverlay;
import com.autenticar.tuid.model.Persona;
import com.autenticar.tuid.three.DNIBack_OCR;
import com.autenticar.tuid.three.DNIBack_PDF;
import com.autenticar.tuid.three.DNIFront_OCR;
import com.autenticar.tuid.three.DNIFront_PDF;
import com.autenticar.tuid.utils.DNIArgentinaPDF417ParserHelper;
import com.autenticar.tuid.utils.HelperFunctions;
import com.autenticar.tuid.utils.cropimage.CropperDetector;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiDetector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;


import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import pl.droidsonroids.gif.GifDrawable;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.autenticar.tuid.utils.HelperFunctions.chararterSpece;
import static com.autenticar.tuid.utils.HelperFunctions.setSound;
import static com.autenticar.tuid.utils.HelperFunctions.sound;


/**
 * Activity for the multi-tracker app.  This app detects faces and barcodes with the rear facing
 * camera, and draws overlay graphics to indicate the position, size, and ID of each face and
 * barcode.
 */
public final class MultiTrackerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MultiTracker";

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private DocumentOverlay documentOverlay;
    public static pl.droidsonroids.gif.GifImageView mVideo = null;
    private MediaController mediaController;
    private boolean readingFront = false;
    private boolean takingFace;
    private boolean check_front = false;
    private boolean check_back = false;
    private int forma = 0;
    private String fron_pdf_value = "";
    private String fron_gcr_value = "";
    private ProgressBar progressBar;
    final Activity activity = this;
    private boolean BIENILUMINADO = true;
    private boolean ENFOCARFRENTE = true;
    private boolean FLASH = false;
    private Camera camera;
    private Camera.Parameters parameters;
    private TextView scan_guide_info;
    public double avr_front = 0.0;
    public double avr_back = 0.0;
    public int menor = 0;
    public int porcentaje = 0;
    public static int count = 0;
    public static MenuItem item;
    public RectF rect;
    private RectF rect_dni_frontal = new RectF();
    private RectF rect_dni_frontal_ocr= new RectF();
    private RectF rect_dni_frontal_pdf= new RectF();
    private RectF rect_dni_dorso= new RectF();
    private RectF rect_dni_dorso_ocr= new RectF();
    private RectF rect_dni_dorso_pdf= new RectF();
    private ArrayList<DNIFront_OCR> iten_DNIFront_OCR;
    private ArrayList<DNIFront_PDF> iten_DNIFront_PDF;
    private ArrayList<DNIBack_OCR> iten_DNIBack_OCR;
    private ArrayList<DNIBack_PDF> item_DNIBack_PDF;
    private TextView nav_salir;



    private CropperDetector frontCropDetector = null;
    private CropperDetector backCropDetector = null;


// if (AppContext.bindService(new Intent("org.opencv.engine.BIND"),
// helper.mServiceConnection, Context.BIND_AUTO_CREATE))
// {
// return true;
// }
        /**
         * Initializes the UI and creates the detector pipeline.
         */
        @Override
        public void onCreate (Bundle icicle){
            super.onCreate(icicle);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            setContentView(R.layout.scan_view);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

            CropperDetector.model = 1; // ****ESTEBAN
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
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
            mPreview = (CameraSourcePreview) findViewById(R.id.preview);
            RectF rf= new RectF();
            rf.set(40, 200,40,200);
            mPreview.SetSize(rf);

            mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

            mVideo = (pl.droidsonroids.gif.GifImageView) findViewById(R.id.videoView);
            scan_guide_info = (TextView) findViewById(R.id.scan_guide_info);
            scan_guide_info.setText("");
            readingFront = true;
            mediaController = new MediaController(this);
            progressBar = findViewById(R.id.progressbar);
            //progressBar.setMax(100);
            progressBar.setIndeterminate(true);
            //progressBar.setVisibility(View.INVISIBLE);
            //progressBar.setProgress(0);
            //imgSource = new Mat();
            //imgCircleOut = new Mat();
            // Check for the camera permission before accessing the camera.  If the
            // permission is not granted yet, request permission.
            int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

            if (rc == PackageManager.PERMISSION_GRANTED) {
                createCameraSource();
            } else {
                requestCameraPermission();
            }

            if (sound) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            synchronized (this) {
                                wait(1000);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (ENFOCARFRENTE) {
                                            setSound("enfocafrente", activity);
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
                Thread thread_1 = new Thread() {
                    @Override
                    public void run() {
                        try {
                            synchronized (this) {
                                wait(7000);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (BIENILUMINADO) {
                                            setSound("bieniluminado", activity);
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

        /**
         * Handles the requesting of the camera permission.  This includes
         * showing a "Snackbar" message of why the permission is needed then
         * sending the request.
         */
        private void requestCameraPermission () {
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
            Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, listener)
                    .show();
        }

        /**
         * Creates and starts the camera.  Note that this uses a higher resolution in comparison
         * to other detection examples to enable the barcode detector to detect small barcodes
         * at long distances.
         */
        private void createCameraSource () {

            int left = 0, top = 0, right = 0, bottom = 0;

            Context context = getApplicationContext();
            // A face detector is created to track faces.  An associated multi-processor instance
            // is set to receive the face detection results, track the faces, and maintain graphics for
            // each face on screen.  The factory is used by the multi-processor to create a separate
            // tracker instance for each face.
            final Persona persona = ((MainApp) getApplication()).GetPersonaToRegister();
            persona.setProgressBar(progressBar);
            persona.setActivity(activity);
            persona.setScan_guide(((TextView) findViewById(R.id.scan_guide)));
            //recibir los eventos de PDF
            ITrackerProcess<Face> iTrackerFace = new ITrackerProcess<Face>() {
                @Override
                public void ValueReaded(Detector.Detections<Face> detectionResults) {
                    //solo capturo la imagen del frente
                    if ( !persona.IsFaceDetected() && persona.dniFront != null && readingFront && !takingFace) {
                        takingFace = true;
                        Face face = detectionResults.getDetectedItems().valueAt(0);
                        rect = new RectF();
                        rect.set(face.getPosition().x - 30, face.getPosition().y - 30, face.getWidth() + 30, face.getHeight() + 30);
                        takingFace = true;
                        mPreview.takePicture(persona, 1, rect);
                    }
                    if(!persona.IsFaceDetected() && !persona.IsPDFFrontReaded() && readingFront  ) {
                        Face face = detectionResults.getDetectedItems().valueAt(0);
                        Display display= getWindowManager().getDefaultDisplay();
                        rect_dni_frontal_pdf.set(
                                (float) (face.getWidth()*(0.27)),
                                face.getPosition().y-(face.getPosition().y/4),
                                face.getPosition().x /2,
                                (float) (face.getHeight()*2.2));
                        rect_dni_dorso_pdf.set(
                                (float) (face.getWidth()*(0.3)),
                                face.getPosition().y-face.getPosition().y/4,
                                face.getPosition().x /2,
                                (float) (face.getHeight()*2.2));
                        rect_dni_frontal_ocr.set(
                                face.getPosition().x /4,
                                (face.getPosition().y-face.getPosition().y/4),
                                face.getWidth()*2,
                                (face.getPosition().y/2+30));
                        rect_dni_dorso_ocr=rect_dni_frontal_ocr;

                     }
                }
            };
            FaceDetector faceDetector = new FaceDetector.Builder(context).build();
            FaceTrackerFactory faceFactory = new FaceTrackerFactory(mGraphicOverlay, iTrackerFace);
            faceDetector.setProcessor(
                    new MultiProcessor.Builder<>(faceFactory).build());

            ((TextView) findViewById(R.id.scan_guide)).setText("Por favor, enfoca el frente del DNI");


            // A barcode detector is created to track barcodes.  An associated multi-processor instance
            // is set to receive the barcode detection results, track the barcodes, and maintain
            // graphics for each barcode on screen.  The factory is used by the multi-processor to
            // create a separate tracker instance for each barcode.
            BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();

            final Activity activity = this;
            //recibir los eventos de PDF
            ITrackerProcess<Barcode> iTrackerBarcode = new ITrackerProcess<Barcode>() {
                @Override
                public void ValueReaded(Detector.Detections<Barcode> detectionResults) {
                    if (!persona.IsBarcodeReaded() && !persona.IsOCRFrontReaded() && !check_front && detectionResults.getDetectedItems().size() > 0) {
                        DNIArgentinaPDF417ParserHelper.Parse(detectionResults.getDetectedItems().valueAt(0).rawValue, persona);
                        forma = 1;
                    } else if (!persona.IsPDFBackReaded() && persona.IsPDFFrontReaded() && check_front && detectionResults.getDetectedItems().size() > 0) {
                        DNIArgentinaPDF417ParserHelper.Parse(detectionResults.getDetectedItems().valueAt(0).rawValue, persona);
                    }
                }
            };
            BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay, iTrackerBarcode);
            barcodeDetector.setProcessor(
                    new MultiProcessor.Builder<>(barcodeFactory).build());

            //recibir los eventos de OCR
            ITrackerProcess<TextBlock> iTrackerOCR = new ITrackerProcess<TextBlock>() {

                @Override
                public void ValueReaded(Detector.Detections<TextBlock> detectionResults) {

                    if(CropperDetector.frontResult == null && frontCropDetector!=null && !frontCropDetector.isCompleted()){ Log.e("F","Front");}
                    if(CropperDetector.backResult == null && backCropDetector!=null && !backCropDetector.isCompleted()){ Log.e("B","Back");}

                    if (detectionResults.getDetectedItems().size() > 0) {
                        if (!persona.IsOCRFrontReaded() && readingFront && !check_front && !check_back) {
                            check_back = true;
                            StringBuilder stringBuilder = new StringBuilder();
                            for (int i = 0; i < detectionResults.getDetectedItems().size(); i++) {
                                stringBuilder.append(detectionResults.getDetectedItems().valueAt(i).getValue());
                            }
                            String valor = chararterSpece(stringBuilder.toString());
                            fron_gcr_value = valor;
                            avr_front = persona.ProcessFrontOCRReaded(valor);
                            System.out.println("persona.IsOCRFrontReaded(): "+persona.IsOCRFrontReaded());
                            if (persona.IsOCRFrontReaded()) {
                                menor = 0;
                                rect_dni_frontal=rect_dni_frontal_ocr;
                                mPreview.takePicture(persona, 2, rect_dni_frontal_ocr);
                                forma = 1;
                                check_front = true;
                            }
                        } else if (!persona.IsPDFFrontReaded() && readingFront && !check_front && check_back) {
                            check_back = false;
                            StringBuilder stringBuilder = new StringBuilder();
                            for (int i = 0; i < detectionResults.getDetectedItems().size(); i++) {
                                stringBuilder.append(detectionResults.getDetectedItems().valueAt(i).getValue());
                            }
                            String valor = chararterSpece(stringBuilder.toString());
                            fron_pdf_value = valor;
                            persona.ProcessFrontPDFReaded(valor);
                            System.out.println("persona.IsPDFFrontReaded(): "+persona.IsPDFFrontReaded());
                            if (persona.IsPDFFrontReaded()) {
                                mPreview.takePicture(persona, 2, rect_dni_frontal_pdf);
                                forma = 2;
                                CropperDetector.model = 2;
                                check_front = true;
                            }
                        } else if (persona.IsOCRFrontReaded() && !persona.IsOCRBackReaded() && !readingFront && forma == 1) {

                           StringBuilder stringBuilder = new StringBuilder();
                            int value = 0;
                            for (int i = 0; i < detectionResults.getDetectedItems().size(); i++) {
                            /* value=i+1;
                            if(detectionResults.getDetectedItems().valueAt(value).getValue().indexOf("LUGAR")>0 ){
                                stringBuilder.append("DOMICILIO "+detectionResults.getDetectedItems().valueAt(i).getValue());
                                System.out.println("entro en domicilio");
                            }else {*/
                                stringBuilder.append(detectionResults.getDetectedItems().valueAt(i).getValue());
                                //}
                            }
                            String valor = chararterSpece(stringBuilder.toString());
                            persona.setMaxProgressBar(5);
                            persona.prog = 0;
                            avr_back = persona.ProcessBackOCRReaded(valor);
                            System.out.println("");
                            if (persona.IsOCRBackReaded() && !valor.equals(fron_gcr_value)) {
                                mPreview.takePicture(persona, 3, rect_dni_dorso_ocr);
                            }
                        } else if (persona.IsPDFFrontReaded() && !persona.IsPDFBackReaded() && !readingFront && forma == 2) {
                            StringBuilder stringBuilder = new StringBuilder();
                            for (int i = 0; i < detectionResults.getDetectedItems().size(); i++) {
                                stringBuilder.append(detectionResults.getDetectedItems().valueAt(i).getValue());
                            }
                            String valor = chararterSpece(stringBuilder.toString());
                            persona.ProcessBackPDFReaded(fron_pdf_value, valor);
                            if (persona.IsPDFBackReaded() && !valor.equals(fron_pdf_value)) {
                                mPreview.takePicture(persona, 3, rect_dni_dorso_pdf);
                            }
                        } else if (persona.IsFilled()  && CropperDetector.backResult!=null) {
                            persona.dniFront = CropperDetector.frontResult;
                            persona.dniBack = CropperDetector.backResult;
                            setResult(RESULT_OK);
                            finish();
                        }
                        if ((persona.IsOCRFrontReaded() || persona.IsPDFFrontReaded()) && readingFront && persona.IsFaceDetected() && CropperDetector.frontResult!=null) {
                            readingFront = false;
                            if(backCropDetector==null) backCropDetector = new CropperDetector(activity,mCameraSource, false, new String[]{"<<<", frontCropDetector.apellido});
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    FlipDNI(persona);
                                }
                            });
                        }
                    }
                }
            };

            TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
            OCRTrackerFactory ocrTrackerFactory = new OCRTrackerFactory(mGraphicOverlay, iTrackerOCR);
            textRecognizer.setProcessor(new MultiProcessor.Builder<>(ocrTrackerFactory).build());

            // A multi-detector groups the two detectors together as one detector.  All images received
            // by this detector from the camera will be sent to each of the underlying detectors, which
            // will each do face and barcode detection, respectively.  The detection results from each
            // are then sent to associated tracker instances which maintain per-item graphics on the
            // screen.
            MultiDetector multiDetector = new MultiDetector.Builder()
                    .add(faceDetector)
                    .add(barcodeDetector)
                    .add(textRecognizer)
                    .build();

            if (!multiDetector.isOperational()) {
                // Note: The first time that an app using the barcode or face API is installed on a
                // device, GMS will download a native libraries to the device in order to do detection.
                // Usually this completes before the app is run for the first time.  But if that
                // download has not yet completed, then the above call will not detect any barcodes
                // and/or faces.
                //
                // isOperational() can be used to check if the required native libraries are currently
                // available.  The detectors will automatically become operational once the library
                // downloads complete on device.
                Log.w(TAG, "Detector dependencies are not yet available.");

                // Check for low storage.  If there is low storage, the native library will not be
                // downloaded, so detection will not become operational.
                IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
                boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

                if (hasLowStorage) {
                    Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                    Log.w(TAG, getString(R.string.low_storage_error));
                }
            }

            // Creates and starts the camera.  Note that this uses a higher resolution in comparison
            // to other detection examples to enable the barcode detector to detect small barcodes
            // at long distances.
            mCameraSource = new CameraSource.Builder(getApplicationContext(), multiDetector)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1600, 1024)
                    .setRequestedFps(15.0f)
                    //.setAutoFocusEnabled(true)
                    .build();


            if(frontCropDetector == null){
                frontCropDetector = new CropperDetector(activity,mCameraSource, true, new String[]{""});
            }

        /*mCameraSource =
                new CameraSource.Builder(getApplicationContext(),multiDetector)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setRequestedPreviewSize(1280, 1024)
                        .setRequestedFps(2.0f)
                        .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                        .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null)
                        .setPersonaInfo(persona)
                        .setStatusChanged(this)
                        .build();*/
        }

        /**
         * Restarts the camera.
         */
        @Override
        protected void onResume () {
            super.onResume();
            startCameraSource();
            if (sound) {
                setSound("play", activity);
                BIENILUMINADO = true;
                ENFOCARFRENTE = true;
            }
        }


        /**
         * Releases the resources associated with the camera source, the associated detectors, and the
         * rest of the processing pipeline.
         */
        @Override
        protected void onDestroy () {
            super.onDestroy();
            if (mCameraSource != null) {
                mCameraSource.release();
            }
        }

        @Override
        public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions,
                                                @NonNull int[] grantResults){
            if (requestCode != RC_HANDLE_CAMERA_PERM) {
                Log.d(TAG, "Got unexpected permission result: " + requestCode);
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                return;
            }

            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted - initialize the camera source");
                // we have permission, so create the camerasource
                createCameraSource();
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

        /**
         * A safe way to get an instance of the Camera object.
         */
        public static Camera getCameraInstance () {
            Camera c = null;
            try {
                c = Camera.open(); // attempt to get a Camera instance
            } catch (Exception e) {
                // Camera is not available (in use or does not exist)
            }
            return c; // returns null if camera is unavailable
        }

        /**
         * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
         * (e.g., because onResume was called before the camera source was created), this will be called
         * again when the camera source is created.
         */
        private void startCameraSource () {
            // check that the device has play services available.
            int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                    getApplicationContext());
            if (code != ConnectionResult.SUCCESS) {
                Dialog dlg =
                        GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
                dlg.show();
            }
            if (mCameraSource != null) {
                try {
                    documentOverlay = findViewById(R.id.dniOverlay);
                    mPreview.setCamera(getCameraInstance());
                    mPreview.start(mCameraSource, mGraphicOverlay);
                    final Handler handler = new Handler();
                    documentOverlay.init(false, mPreview);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            documentOverlay.init(true, mPreview);
                        }
                    }, 3000);

                } catch (IOException e) {
                    Log.e(TAG, "Unable to start camera source.", e);
                    mCameraSource.release();
                    mCameraSource = null;
                }
            }
        }

        private void FlipDNI ( final Persona persona){
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.scan_guide)).setText("Por favor, enfoca el dorso del DNI");
                    persona.iniProgressBar();
                    if (sound) {
                        setSound("giradni", activity);
                        Thread thread_1 = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    synchronized (this) {
                                        wait(7000);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                setSound("fondoclaro", activity);
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
                    try {
                        GifDrawable gifDrawable = new GifDrawable(getResources(), R.drawable.dni_transparente);
                        mVideo.setImageDrawable(gifDrawable);
                        mVideo.setVisibility(VISIBLE);
                        Thread thread = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    synchronized (this) {
                                        wait(6000);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mVideo.setVisibility(GONE);
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
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 2000);
        }

        @Override
        public void onBackPressed () {
            if (sound) {
                setSound("cancelarregistro", this);
            }
            AlertDialog.Builder dialogo = new AlertDialog.Builder(this);
            dialogo.setTitle(R.string.alert_titulo_alerta);
            dialogo.setMessage(R.string.alert_msn_cancelar_registro);
            dialogo.setCancelable(true);
            dialogo.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo, int id) {
                    dialogo.cancel();
                }
            });
            dialogo.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo, int id) {
                    forma = 0;
                    readingFront = false;
                    Intent intent = new Intent(MultiTrackerActivity.this, LoginActivity.class);
                    startActivity(intent);
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

        /*Funcionalidades del menu*/
        @Override
        public boolean onOptionsItemSelected (MenuItem item){
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();
            this.item = item;
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
            } else if (id == R.id.action_flash) {
                if (FLASH) {
                    item.setIcon(R.drawable.ic_off_flash);
                    item.setTitle(R.string.off);
                    FLASH = false;
                    offFlash();
                } else {
                    item.setIcon(R.drawable.ic_on_flash);
                    item.setTitle(R.string.on);
                    FLASH = true;
                    try {
                        onFlash();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }/*else if(id == R.id.action_debug){
                Intent intent = new Intent(MultiTrackerActivity.this, ActivityListDebugDNIOcrPdf.class);
                Bundle args=new Bundle();
                args.putSerializable("front_ocr_array", (Serializable) iten_DNIFront_OCR);
                intent.putExtra("front_ocr", args);
                startActivity(intent);
            }*/
            return super.onOptionsItemSelected(item);
        }

        @Override
        public boolean onNavigationItemSelected (@NonNull MenuItem menuItem){
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
                Intent intent = new Intent(MultiTrackerActivity.this, HistoryActivity.class);
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
        public boolean onCreateOptionsMenu (Menu menu){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_scaner, menu);
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
        protected void onPause () {
            super.onPause();
            mPreview.stop();
            offFlash();
            if (sound) {
                setSound("pause", activity);
                BIENILUMINADO = false;
                ENFOCARFRENTE = false;
            }
        }

        public void onFlash () throws Exception {
            FLASH = true;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,}, 1000);
            } else {
                if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    camera = getCamera(mCameraSource);
                    parameters = camera.getParameters();
                }
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameters);
            }
        }

        public void offFlash () {
            FLASH = false;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,}, 1000);
            } else {
                if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    camera = getCamera(mCameraSource);
                    if (camera != null) {
                        parameters = camera.getParameters();
                    }
                }

                if (camera != null) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(parameters);
                }
            }
        }

        @Override
        protected void onStop () {
            super.onStop();
        }

        private static Camera getCamera (@NonNull CameraSource cameraSource){
            Field[] declaredFields = CameraSource.class.getDeclaredFields();

            for (Field field : declaredFields) {
                if (field.getType() == Camera.class) {
                    field.setAccessible(true);
                    try {
                        Camera camera = (Camera) field.get(cameraSource);
                        if (camera != null) {
                            return camera;
                        }
                        return null;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            return null;
        }
    }
