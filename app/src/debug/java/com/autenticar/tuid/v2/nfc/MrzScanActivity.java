package com.autenticar.tuid.v2.nfc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SOLO DEBUG. Apunta la camara a la zona MRZ del pasaporte o del dorso del nuevo DNI.
 * Cuando la MRZ se lee y TODOS sus digitos de control coinciden, devuelve los datos a la
 * pantalla NFC. El texto leido no se guarda ni se escribe en el log.
 */
public class MrzScanActivity extends AppCompatActivity {

    public static final String EXTRA_DOC = "DOC";
    public static final String EXTRA_BIRTH = "BIRTH";
    public static final String EXTRA_EXPIRY = "EXPIRY";
    private static final int REQ_CAMERA = 2001;

    private PreviewView preview;
    private TextView estado;
    private ProcessCameraProvider provider;
    private TextRecognizer recognizer;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean terminado = false;
    // Consenso: la MRZ solo se acepta si dos cuadros consecutivos leen exactamente lo mismo.
    // Un digito de control puede dar el mismo valor por casualidad (aprox. 1 de cada 10) con una letra mal leida.
    private static final int LECTURAS_IGUALES = 2;
    private String ultimaClave = null;
    private int repeticiones = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        FrameLayout root = new FrameLayout(this);
        preview = new PreviewView(this);
        root.addView(preview);
        estado = new TextView(this);
        estado.setTextColor(0xFFFFFFFF);
        estado.setBackgroundColor(0x99000000);
        int p = (int) (12 * getResources().getDisplayMetrics().density);
        estado.setPadding(p, p, p, p);
        estado.setText("Apuntá a la zona MRZ (las líneas con <<<) y mantené el documento quieto.");
        root.addView(estado, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        setContentView(root);
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            iniciar();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] perms, @NonNull int[] res) {
        super.onRequestPermissionsResult(code, perms, res);
        if (code == REQ_CAMERA && res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED) iniciar();
        else estado.setText("Sin permiso de cámara: no se puede escanear.");
    }

    @androidx.camera.core.ExperimentalGetImage
    private void iniciar() {
        final ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    provider = future.get();
                    Preview pv = new Preview.Builder().build();
                    pv.setSurfaceProvider(preview.getSurfaceProvider());
                    ImageAnalysis analysis = new ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build();
                    analysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
                        @Override
                        public void analyze(@NonNull ImageProxy image) {
                            procesar(image);
                        }
                    });
                    provider.unbindAll();
                    provider.bindToLifecycle(MrzScanActivity.this, CameraSelector.DEFAULT_BACK_CAMERA, pv, analysis);
                } catch (Exception e) {
                    Log.e("MrzScan", "No se pudo iniciar la camara: " + e.getClass().getSimpleName());
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @androidx.camera.core.ExperimentalGetImage
    private void procesar(final ImageProxy proxy) {
        if (terminado || proxy.getImage() == null) {
            proxy.close();
            return;
        }
        InputImage img = InputImage.fromMediaImage(proxy.getImage(), proxy.getImageInfo().getRotationDegrees());
        recognizer.process(img).addOnSuccessListener(ContextCompat.getMainExecutor(this), new com.google.android.gms.tasks.OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                List<String> lineas = new ArrayList<>();
                for (Text.TextBlock bl : text.getTextBlocks()) {
                    for (Text.Line l : bl.getLines()) lineas.add(l.getText());
                }
                final MrzParser.Result r = MrzParser.parse(lineas);
                if (r != null && !terminado) {
                    String clave = r.numeroDocumento + "|" + r.fechaNacimiento + "|" + r.fechaVencimiento;
                    if (clave.equals(ultimaClave)) repeticiones++;
                    else { ultimaClave = clave; repeticiones = 1; }
                    if (repeticiones < LECTURAS_IGUALES) return;
                    terminado = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent data = new Intent()
                                    .putExtra(EXTRA_DOC, r.numeroDocumento)
                                    .putExtra(EXTRA_BIRTH, r.fechaNacimiento)
                                    .putExtra(EXTRA_EXPIRY, r.fechaVencimiento);
                            setResult(RESULT_OK, data);
                            finish();
                        }
                    });
                }
            }
        }).addOnCompleteListener(ContextCompat.getMainExecutor(this), new com.google.android.gms.tasks.OnCompleteListener<Text>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Text> t) {
                proxy.close();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        terminado = true;
        if (provider != null) provider.unbindAll();
        executor.shutdown();
        try { recognizer.close(); } catch (Exception ignored) { }
    }
}
