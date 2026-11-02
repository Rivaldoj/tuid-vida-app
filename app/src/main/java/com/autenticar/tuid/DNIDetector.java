package com.autenticar.tuid;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.autenticar.tuid.camera.CameraSourcePreview;
import com.autenticar.tuid.camera.DocumentOverlay;
import com.autenticar.tuid.camera.GraphicOverlay;
import com.autenticar.tuid.model.Persona;
import com.autenticar.tuid.three.DNIBack_OCR;
import com.autenticar.tuid.three.DNIBack_PDF;
import com.autenticar.tuid.three.DNIFront_OCR;
import com.autenticar.tuid.three.DNIFront_PDF;
import com.autenticar.tuid.utils.HelperFunctions;
import com.autenticar.tuid.utils.cropimage.CropperDetector;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiDetector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import pl.droidsonroids.gif.GifDrawable;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.autenticar.tuid.utils.HelperFunctions.setSound;
import static com.autenticar.tuid.utils.HelperFunctions.sound;

public class DNIDetector extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
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
    private ProgressBar progressBar;
    final Activity activity = this;
    private boolean BIENILUMINADO = true;
    private boolean ENFOCARFRENTE = true;
    private boolean FLASH = false;
    private Camera camera;
    private Camera.Parameters parameters;
    private TextView scan_guide_info;
    public static MenuItem item;
    public RectF rect;
    private ArrayList<DNIFront_OCR> iten_DNIFront_OCR;
    private ArrayList<DNIFront_PDF> iten_DNIFront_PDF;
    private ArrayList<DNIBack_OCR> iten_DNIBack_OCR;
    private ArrayList<DNIBack_PDF> item_DNIBack_PDF;
    private TextView nav_salir;

    private CropperDetector frontCropDetector = null;
    private CropperDetector backCropDetector = null;
    public static boolean FLIPISOVER = false;

    public static Bitmap tempBitmap;

    public static int RESULT_TIMEOUT = 10012;

    class MyTextDetector extends Detector<TextBlock> {
        private Detector<TextBlock> mDelegate;

        MyTextDetector(Detector<TextBlock> delegate) {
            mDelegate = delegate;
        }

        public SparseArray<TextBlock> detect(final Frame frame) {


//            Log.e("FrameSize:", frame.getMetadata().getWidth() + " " + frame.getMetadata().getHeight());
            YuvImage yuvImage = new YuvImage(frame.getGrayscaleImageData().array(), ImageFormat.NV21, frame.getMetadata().getWidth(), frame.getMetadata().getHeight(), null);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, frame.getMetadata().getWidth(), frame.getMetadata().getHeight()), 100, byteArrayOutputStream);
            byte[] jpegArray = byteArrayOutputStream.toByteArray();
            tempBitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);
//            CropperDetector.saveImage(TempBitmap, "ASDASD" + System.currentTimeMillis());

            return mDelegate.detect(frame);
        }

        public boolean isOperational() {
            return mDelegate.isOperational();
        }

        public boolean setFocus(int id) {
            return mDelegate.setFocus(id);
        }
    }

    @Override
    public void onCreate (Bundle icicle){
        super.onCreate(icicle);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.scan_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

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

//        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        RectF rf= new RectF();
        rf.set(40, 200,40,200);
        mPreview.SetSize(rf);

        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        mVideo = (pl.droidsonroids.gif.GifImageView) findViewById(R.id.videoView);
        scan_guide_info = (TextView) findViewById(R.id.scan_guide_info);
        scan_guide_info.setText("");
        mediaController = new MediaController(this);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setIndeterminate(true);
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
        FLIPISOVER = false;

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

        final Persona persona = ((MainApp) getApplication()).GetPersonaToRegister();
        persona.setProgressBar(progressBar);
        persona.setActivity(activity);
        persona.setScan_guide(((TextView) findViewById(R.id.scan_guide)));
        //recibir los eventos de PDF


        ITrackerProcess<TextBlock> iTrackerOCR = new ITrackerProcess<TextBlock>(){
            @Override
            public void ValueReaded(Detector.Detections<TextBlock> detectionResults) {

                if(FLIPISOVER && backCropDetector!=null && backCropDetector.isCompleted()){
                    persona.dniFront = CropperDetector.frontResult;
                    persona.dniBack = CropperDetector.backResult;
                    persona.SetFaceDetected(CropperDetector.faceResult);
                    persona.NombreQR = CropperDetector.nombre;
                    persona.ApellidoQR = CropperDetector.apellido;
                    persona.DocumentoQR = CropperDetector.dni;
                    persona.FechaNacQR = CropperDetector.fechaNacimiento;
                    persona.EsMasculinoQR = CropperDetector.sexo.equalsIgnoreCase("M");
                    setResult(RESULT_OK);
                    finish();
                }


                
                if(frontCropDetector!=null && frontCropDetector.isCompleted()){
//                    Log.e("CropperD", "Completed front detection");
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            FlipDNI(persona);
                        }
                    });

                }

                //TIMEOUT LOGIC
                if(backCropDetector!=null){
                    if(backCropDetector.startTime+backCropDetector.maxScanTime<System.currentTimeMillis()) {
                        Intent i = getIntent();
                        i.putExtra("timeoutResult", backCropDetector.getTimeoutDetails());
                        setResult(RESULT_TIMEOUT,i);
                        finish();
                    }
                }
                else{
                    if(frontCropDetector!=null &&frontCropDetector.startTime+frontCropDetector.maxScanTime<System.currentTimeMillis()){
                        Intent i = getIntent();
                        i.putExtra("timeoutResult", frontCropDetector.getTimeoutDetails());
                        setResult(RESULT_TIMEOUT,i);
                        finish();
                    }
                }
                // -------------

            }
        };

        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        MyTextDetector mtd =  new MyTextDetector(textRecognizer);
        OCRTrackerFactory ocrTrackerFactory = new OCRTrackerFactory(mGraphicOverlay, iTrackerOCR);
        mtd.setProcessor(new MultiProcessor.Builder<>(ocrTrackerFactory).build());

        MultiDetector multiDetector = new MultiDetector.Builder()
                .add(mtd)
                .build();


        int finalWidth = 1280;
        int finalHeight = 720;

        try {
            Camera c = Camera.open();

            c.enableShutterSound(false);
            List<Camera.Size> cameraSizes = c.getParameters().getSupportedPictureSizes();
            for (Camera.Size s : cameraSizes) {
//                Log.e("CropperD", s.width + " : " + s.height);
                if (s.width > finalWidth && s.width < 130000000) {
                    finalWidth = s.width;
                    finalHeight = s.height;
                }

            }
        }
        catch (Exception e){

        }

//        Log.e("FinalCameraSize", finalWidth + " " + finalHeight);

        mCameraSource = new CameraSource.Builder(getApplicationContext(), multiDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
//                .setRequestedPreviewSize(1600, 1024)
                .setRequestedPreviewSize(finalWidth, finalHeight)
                .setRequestedFps(15.0f)
                .setAutoFocusEnabled(true)
                .build();

//        mPreview.setPreviewSize(finalWidth,finalHeight);
//        mGraphicOverlay.setCameraInfo(finalWidth,finalHeight, CameraSource.CAMERA_FACING_BACK);



    Handler h = new Handler();
    h.postDelayed(new Runnable() {
        @Override
        public void run() {
            if(frontCropDetector == null){
                frontCropDetector = new CropperDetector(activity,mCameraSource, true, new String[]{"<<<<"});
            }
        }
    }, 3000);




    }


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

    public static Camera getCameraInstance () {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


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
//                getCameraInstance().autoFocus(new Camera.AutoFocusCallback() {
//                    @Override
//                    public void onAutoFocus(boolean success, Camera camera) {
//
//                        if(frontCropDetector == null){
//                            frontCropDetector = new CropperDetector(activity,mCameraSource, true, new String[]{"<<<<"});
//                        }
//                    }
//                });
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
        if(backCropDetector == null){
            if(CropperDetector.model == 1) backCropDetector = new CropperDetector(activity,mCameraSource, false, new String[]{CropperDetector.dni+"<"+CropperDetector.getVerificationDigit(CropperDetector.dni)});
            else  backCropDetector = new CropperDetector(activity,mCameraSource, false, new String[]{""});
        }
        frontCropDetector = null;
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
                                            DNIDetector.FLIPISOVER = true;
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
                Intent intent = new Intent(DNIDetector.this, LoginActivity.class);
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
            Intent intent = new Intent(DNIDetector.this, HistoryActivity.class);
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
