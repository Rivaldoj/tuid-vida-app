package com.autenticar.tuid.v2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.autenticar.tuid.AmplitudeManager.AmplitudeManager;
import com.autenticar.tuid.R;
import com.autenticar.tuid.camera.DocumentOverlay;
import com.autenticar.tuid.services.TuidService;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.autenticar.tuid.utils.HelperFunctions;
import com.autenticar.tuid.v2.model.Persona;
import com.autenticar.tuid.v2.services.CheckDocumentRequest;
import com.autenticar.tuid.v2.services.CheckDocumentResponse;
import com.autenticar.tuid.v2.utils.LoadingDialog;
import com.autenticar.tuid.v2.utils.Scanner;
import com.google.common.util.concurrent.ListenableFuture;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;

import java.text.SimpleDateFormat;

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
    private PreviewView mPreview;
    private DocumentOverlay documentOverlay;
    public pl.droidsonroids.gif.GifImageView mVideo = null;
    private boolean FLASH = false;
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private final ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
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
    @androidx.camera.core.ExperimentalGetImage
    public void onResume(){
        super.onResume();
        startCameraSource();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        stopCamera();
        cameraExecutor.shutdown();
    }

    private void stopCamera() {
        try {
            if (cameraProvider != null) cameraProvider.unbindAll();
        } catch (Exception e) {
            Log.e("SCAN", "stopCamera", e);
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
        toggleFlash2();
    }

    public void toggleFlash2(){
        if (!getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) return;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,}, 1000);
        } else if (camera != null) {
            FLASH = !FLASH;
            camera.getCameraControl().enableTorch(FLASH);
        } else {
            Log.e("FLASH", "NO CAMERA");
        }
    }
    public void initCamera(){
        mPreview = (PreviewView) findViewById(R.id.preview);

        mVideo = (pl.droidsonroids.gif.GifImageView) findViewById(R.id.videoView);
        scan_guide_info = (TextView) findViewById(R.id.scan_guide_info);
        scan_guide_info.setText("");

        progressBar = findViewById(R.id.progressbar);
        progressBar.setIndeterminate(true);

        playGuideSounds(new int[]{R.raw.enfocafrente, R.raw.iluminacion}, 0, null);



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


    public void informTimeout(boolean isFront){
        // TODO:
        // INFORMAR TIMEOUT
//        Log.e("TIMEOUT", "Front: " + isFront);
//        Log.e("TIMEOUT", "Details: " + (isFront?frontCropDetector.getTimeoutDetails():backCropDetector.getTimeoutDetails()));
//        finish();

    }

    @androidx.camera.core.ExperimentalGetImage
    private void startCameraSource () {
        documentOverlay = findViewById(R.id.dniOverlay);
        documentOverlay.init(false, null);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                documentOverlay.init(true, null);
            }
        }, 3000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1000);
            return;
        }
        final ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    cameraProvider = future.get();
                    bindCamera();
                } catch (Exception e) {
                    Log.e("EX", "Unable to start camera.", e);
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    @androidx.camera.core.ExperimentalGetImage
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCameraSource();
        }
    }

    private void bindCamera() {
        if (cameraProvider == null) return;
        cameraProvider.unbindAll();
        Preview previewUseCase = new Preview.Builder().build();
        previewUseCase.setSurfaceProvider(mPreview.getSurfaceProvider());
        // Resolucion alta: el OCR del DNI necesita detalle (antes se pedia el mayor tamano soportado)
        androidx.camera.core.resolutionselector.ResolutionSelector selector =
                new androidx.camera.core.resolutionselector.ResolutionSelector.Builder()
                        .setAspectRatioStrategy(androidx.camera.core.resolutionselector.AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                        .setResolutionStrategy(new androidx.camera.core.resolutionselector.ResolutionStrategy(
                                new android.util.Size(1920, 1080),
                                androidx.camera.core.resolutionselector.ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER))
                        .build();
        ImageAnalysis analysis = new ImageAnalysis.Builder()
                .setResolutionSelector(selector)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        analysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                try {
                    // Si el Scanner esta ocupado o vencio, no vale la pena convertir el cuadro
                    if (!Scanner.busy) {
                        // Bitmap sin rotar (orientacion del sensor): el Scanner lo rota si es apaisado
                        tempBitmap = image.toBitmap();
                        processImage();
                    }
                } catch (Exception e) {
                    Log.e("SCAN", "analyze", e);
                } finally {
                    image.close();
                }
            }
        });
        try {
            camera = cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, previewUseCase, analysis);
            if (FLASH) camera.getCameraControl().enableTorch(true);
        } catch (Exception e) {
            Log.e("EX", "bindToLifecycle fallo", e);
        }
    }

    Scanner s = new Scanner(this);

    synchronized void processImage(){
        s.process(tempBitmap);
    }

    public void stopScanning(Persona persona){
        stopCamera();


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
            stopCamera();
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
                    stopCamera();
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

