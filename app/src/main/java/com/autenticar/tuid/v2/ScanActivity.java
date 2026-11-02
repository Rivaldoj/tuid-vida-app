package com.autenticar.tuid.v2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.autenticar.tuid.AmplitudeManager.AmplitudeManager;
import com.autenticar.tuid.ITrackerProcess;
import com.autenticar.tuid.OCRTrackerFactory;
import com.autenticar.tuid.R;
import com.autenticar.tuid.camera.CameraSourcePreview;
import com.autenticar.tuid.camera.DocumentOverlay;
import com.autenticar.tuid.camera.GraphicOverlay;
import com.autenticar.tuid.services.TuidService;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.autenticar.tuid.utils.HelperFunctions;
import com.autenticar.tuid.v2.model.Persona;
import com.autenticar.tuid.v2.services.CheckDocumentRequest;
import com.autenticar.tuid.v2.services.CheckDocumentResponse;
import com.autenticar.tuid.v2.utils.LoadingDialog;
import com.autenticar.tuid.v2.utils.Scanner;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiDetector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import pl.droidsonroids.gif.GifDrawable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ScanActivity extends TuIdActivity {

    public static int scanAttemps = 0;
    private static final int RC_HANDLE_GMS = 9001;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private DocumentOverlay documentOverlay;
    public pl.droidsonroids.gif.GifImageView mVideo = null;
    private boolean FLASH = false;
    public CameraSource mCameraSource = null;
    private Camera camera;
    private Camera.Parameters parameters;
    private TextView scan_guide_info;

    private ProgressBar progressBar;

    public static int error_scaninning_attemps = 0;
    public static Bitmap tempBitmap;

    @Override
    public void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.v2_scan_view);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initCamera();
        progressBar = findViewById(R.id.progressbar);



    }

    @Override
    public void onResume(){
        super.onResume();
        startCameraSource();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        try {
            mCameraSource.stop();
            mCameraSource.release();
        }
        catch (Exception e){

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scaner, menu);
        this.menu = menu;
        updateSoundButton();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_flash){
            toggleFlash2();
            item.setIcon(FLASH? R.drawable.ic_on_flash: R.drawable.ic_off_flash);
        }
        return super.onOptionsItemSelected(item);
    }

    public void toggleFlash(){
        if(FLASH){
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
        else{
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
    }

    public void toggleFlash2(){
        if (!getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) return;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,}, 1000);
        } else {

            camera = getCamera(mCameraSource);
            if (camera != null) {
                parameters = camera.getParameters();
                if (camera != null) {
                    FLASH = !FLASH;
                    parameters.setFlashMode(FLASH? Camera.Parameters.FLASH_MODE_TORCH: Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(parameters);
                }
                else{
                    Log.e("FLASH", "NO CAMERA");
                }
            }
        }
    }
    public void initCamera(){
        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        RectF rf= new RectF();
        rf.set(40, 200,40,200);
        mPreview.SetSize(rf);

        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        mVideo = (pl.droidsonroids.gif.GifImageView) findViewById(R.id.videoView);
        scan_guide_info = (TextView) findViewById(R.id.scan_guide_info);
        scan_guide_info.setText("");

        progressBar = findViewById(R.id.progressbar);
        progressBar.setIndeterminate(true);

        createCameraSource();

        playGuideSounds(new int[]{R.raw.enfocafrente, R.raw.iluminacion}, 0, null);



    }

    private void createCameraSource () {
//        final Persona persona = ((MainApp) getApplication()).GetPersonaToRegister();
//        persona.setProgressBar(progressBar);
//        persona.setActivity(activity);
//        persona.setScan_guide((TextView) findViewById(R.id.scan_guide));

        ITrackerProcess<TextBlock> iTrackerOCR = new ITrackerProcess<TextBlock>(){

            @Override
            public void ValueReaded(Detector.Detections<TextBlock> detectionResults) {

            }
        };

        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        MyTextDetector mtd =  new MyTextDetector(textRecognizer);
        OCRTrackerFactory ocrTrackerFactory = new OCRTrackerFactory(mGraphicOverlay, iTrackerOCR);
        mtd.setProcessor(new MultiProcessor.Builder<>(ocrTrackerFactory).build());

        MultiDetector multiDetector = new MultiDetector.Builder().add(mtd).build();

        int finalWidth = 1280;
        int finalHeight = 720;

        try {
            Camera c = Camera.open();

            c.enableShutterSound(false);
            List<Camera.Size> cameraSizes = c.getParameters().getSupportedPictureSizes();
            for (Camera.Size s : cameraSizes) {
                if (s.width > finalWidth && s.width < 130000000) {
                    finalWidth = s.width;
                    finalHeight = s.height;
                }

            }
        }
        catch (Exception e){

        }

        mCameraSource = new CameraSource.Builder(getApplicationContext(), multiDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
//                .setRequestedPreviewSize(1600, 1024)
                .setRequestedPreviewSize(finalWidth, finalHeight)
                .setRequestedFps(15.0f)
                .setAutoFocusEnabled(true)
                .build();
    }

    public void flipDNI (){
        ((TextView) findViewById(R.id.scan_guide)).setText("Por favor, enfoca el dorso del DNI");
        playGuideSounds(new int[]{R.raw.giradni, R.raw.fondoclaro}, 0, null);
        try{
            GifDrawable gifDrawable = new GifDrawable(getResources(), R.drawable.dni_transparente);
            mVideo.setImageDrawable(gifDrawable);
            mVideo.setVisibility(VISIBLE);
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mVideo.setVisibility(GONE);
                    s.isFrontDetection = false;
                    Scanner.busy = false;
                }
            }, 3000);
        }
        catch (Exception e){
            Log.e("EX", e.getMessage());
        }
    }


    class MyTextDetector extends Detector<TextBlock> {
        private Detector<TextBlock> mDelegate;

        MyTextDetector(Detector<TextBlock> delegate) {
            mDelegate = delegate;
        }

        public SparseArray<TextBlock> detect(final Frame frame) {
            YuvImage yuvImage = new YuvImage(frame.getGrayscaleImageData().array(), ImageFormat.NV21, frame.getMetadata().getWidth(), frame.getMetadata().getHeight(), null);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, frame.getMetadata().getWidth(), frame.getMetadata().getHeight()), 100, byteArrayOutputStream);
            byte[] jpegArray = byteArrayOutputStream.toByteArray();
            tempBitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);

            processImage();
            return mDelegate.detect(frame);
        }

        public boolean isOperational() {
            return mDelegate.isOperational();
        }

        public boolean setFocus(int id) {
            return mDelegate.setFocus(id);
        }
    }

    public void informTimeout(boolean isFront){
        // TODO:
        // INFORMAR TIMEOUT
//        Log.e("TIMEOUT", "Front: " + isFront);
//        Log.e("TIMEOUT", "Details: " + (isFront?frontCropDetector.getTimeoutDetails():backCropDetector.getTimeoutDetails()));
//        finish();

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

    private void startCameraSource () {
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
//        if (code != ConnectionResult.SUCCESS) {
//            Dialog dlg =
//                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
//            dlg.show();
//        }
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
                Log.e("EX", "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
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


    Scanner s = new Scanner(this);

    synchronized void processImage(){
        s.process(tempBitmap);
    }

    public void stopScanning(Persona persona){
        mCameraSource.stop();
        mCameraSource.release();


//        Log.e("ASD", "DOMICLIO: " + Scanner.domicilio);
        Intent intent = new Intent(this, ScanResultActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("SCAN_DATA_RESULT", persona);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    public void stopScanningTimeout(){
        try{
            mCameraSource.stop();
            mCameraSource.release();
            try {
                if(++error_scaninning_attemps==3){
                    error_scaninning_attemps = 0;
                    JSONObject errors = new JSONObject();
                    errors.put("error", s.getTimeoutDetails());
                    new AmplitudeManager().log(this, "Document Scanning Error Ocurred", errors);
                }
            }
            catch (Exception e){
                
            }

            if(scanAttemps>1){
                Intent i = new Intent(HomeActivity.self, RegistroManualActivity.class);
                i.putExtra("scanFailed",true);
                HomeActivity.self.startActivity(i);
                finish();
                scanAttemps=0;
                return;
            }
//            TuidDialog.show(HomeActivity.self,"Detección del documento", s.getTimeoutDetails());
            HelperFunctions.ShowAlertWithContact(activity, "Detección del documento" + "\n" + s.getTimeoutDetails(), 1, s.getTimeoutDetails());
            scanAttemps++;
        }
        catch (Exception e){

        }

    }


    public void checkDniIsOk(){
        Scanner.busy = true;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        final CheckDocumentRequest request = new CheckDocumentRequest();
        request.dni = Scanner.dni;
        request.sexo = Scanner.sexo;
        request.birth = sdf.format(Scanner.fechaNacimiento);

        TuidService service = ServiceGenerator.createService(TuidService.class);
        Log.e("CheckingDNI","REQUEST");
        Call<CheckDocumentResponse> callUser = service.checkDni(request);
        LoadingDialog.getInstance().show(this,"Validando DNI...");
        Scanner.preventTimeOut=true;
        callUser.enqueue(new Callback<CheckDocumentResponse>() {
            @Override
            public void onResponse(Call<CheckDocumentResponse> call, Response<CheckDocumentResponse> response) {
                Log.e("CheckingDNI", "RESPONSE");
                Scanner.preventTimeOut=false;
                LoadingDialog.getInstance().hide();
//                Log.e("CheckDocumentSuccess", request.dni + " " + request.sexo + " " + request.birth);
//                Log.e("CheckDocumentSuccess", response.body().Message + " " + response.body().proceed + " " + response.body().resultCode);
                if(response.body().proceed){
                    flipDNI();
                }
                else{
//                    DBManager.sharedInstance.logEvent(name: "Registration Process Blocked", data: ["document":checkDoc?.dni,"gender":checkDoc?.sexo, "code": validation.code, "message": validation.message])
                    try{
                        JSONObject error = new JSONObject();
                        error.put("doument", request.dni);
                        error.put("gender", request.sexo);
                        new AmplitudeManager().log(activity, "Registration Process Blocked", error );
                    }
                    catch (Exception e){

                    }
                    try {
                        mCameraSource.stop();
                        mCameraSource.release();
                    }
                    catch (Exception e){

                    }
                    HelperFunctions.ShowAlertWithContact(activity, response.body().Message, 1, response.body().resultCode);
                }
            }

            @Override
            public void onFailure(Call<CheckDocumentResponse> call, Throwable t) {
                Scanner.preventTimeOut=false;
                LoadingDialog.getInstance().hide();
//                Log.e("CheckDocumentFail", request.dni + " " + request.sexo + " " + request.birth);
//                TuidDialog.show(HomeActivity.self,"Detección del documento", "Error con datos del DNI");
//                finish();
                HelperFunctions.ShowAlertWithContact(ScanActivity.this, context.getString(R.string.no_hay_conexion_a_internet), 1, "0");

            }

        });
    }
}

