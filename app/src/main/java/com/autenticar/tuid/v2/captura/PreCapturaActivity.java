package com.autenticar.tuid.v2.captura;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.autenticar.tuid.R;
import com.autenticar.tuid.v2.captura.CaptureQualityEvaluator.Hint;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controles previos a la prueba de vida: una sola indicacion prioritaria por vez, sin parpadeos.
 * Devuelve RESULT_OK cuando el rostro esta bien (un solo rostro, centrado y con buena luz) y la persona
 * toca "Comenzar". No reemplaza la prueba de vida ni la verificacion de identidad del servidor.
 */
public class PreCapturaActivity extends AppCompatActivity {
    private static final String TAG = "PreCaptura";

    private PreviewView preview;
    private TextView titulo;
    private TextView mensaje;
    private Button continuar;

    private ProcessCameraProvider cameraProvider;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private FaceDetector detector;
    private final CaptureQualityEvaluator evaluator =
            new CaptureQualityEvaluator(new CaptureQualityEvaluator.Thresholds());
    private final HintStabilizer stabilizer = new HintStabilizer(4);
    private volatile Hint mostrado = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_captura);
        com.autenticar.tuid.utils.EdgeInsets.aplicar(findViewById(R.id.raiz));
        preview = findViewById(R.id.pre_captura_preview);
        titulo = findViewById(R.id.pre_captura_titulo);
        mensaje = findViewById(R.id.pre_captura_mensaje);
        continuar = findViewById(R.id.pre_captura_continuar);

        continuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stabilizer.actual() == Hint.OK) {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
        findViewById(R.id.pre_captura_cancelar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        mostrar(Hint.NO_FACE);

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setMinFaceSize(0.15f)
                .build();
        detector = FaceDetection.getClient(options);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        stabilizer.reiniciar();
        mostrar(Hint.NO_FACE);
        iniciarCamara();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraProvider != null) cameraProvider.unbindAll();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) cameraProvider.unbindAll();
        executor.shutdown();
        if (detector != null) detector.close();
    }

    private void iniciarCamara() {
        final ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    cameraProvider = future.get();
                    enlazar();
                } catch (Exception e) {
                    Log.e(TAG, "No se pudo iniciar la camara", e);
                    mostrarError();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @androidx.camera.core.ExperimentalGetImage
    private void enlazar() {
        cameraProvider.unbindAll();
        Preview previewUseCase = new Preview.Builder().build();
        previewUseCase.setSurfaceProvider(preview.getSurfaceProvider());
        ImageAnalysis analysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        analysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                PreCapturaActivity.this.analizar(image);
            }
        });
        try {
            cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, previewUseCase, analysis);
        } catch (Exception e) {
            Log.e(TAG, "bindToLifecycle fallo", e);
            mostrarError();
        }
    }

    @androidx.camera.core.ExperimentalGetImage
    private void analizar(final ImageProxy proxy) {
        if (proxy.getImage() == null || detector == null) {
            proxy.close();
            return;
        }
        final int rotation = proxy.getImageInfo().getRotationDegrees();
        final int w = proxy.getWidth();
        final int h = proxy.getHeight();
        final int upW = (rotation % 180 == 0) ? w : h;
        final int upH = (rotation % 180 == 0) ? h : w;
        final ImageProxy.PlaneProxy plano = proxy.getPlanes()[0];
        final ByteBuffer y = plano.getBuffer();
        final double frameLuma = LumaSampler.mean(y, plano.getRowStride(), plano.getPixelStride(),
                w, h, rotation, 0, 0, upW - 1, upH - 1, 16);

        InputImage image = InputImage.fromMediaImage(proxy.getImage(), rotation);
        detector.process(image)
                .addOnSuccessListener(executor, new OnSuccessListener<List<Face>>() {
                    @Override
                    public void onSuccess(List<Face> faces) {
                        Hint lectura;
                        if (faces.size() == 1) {
                            Rect b = faces.get(0).getBoundingBox();
                            double faceLuma = LumaSampler.mean(y, plano.getRowStride(), plano.getPixelStride(),
                                    w, h, rotation, b.left, b.top, b.right, b.bottom, 6);
                            lectura = evaluator.evaluate(1,
                                    b.exactCenterX() / upW, b.exactCenterY() / upH,
                                    (double) b.width() / upW, faceLuma, frameLuma);
                        } else {
                            lectura = evaluator.evaluate(faces.size(), 0, 0, 0, 0, frameLuma);
                        }
                        final Hint estable = stabilizer.aceptar(lectura);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mostrar(estable);
                            }
                        });
                    }
                })
                .addOnCompleteListener(executor, new OnCompleteListener<List<Face>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Face>> task) {
                        proxy.close();
                    }
                });
    }

    private boolean anteojosAvisado = false;

    private void mostrar(Hint hint) {
        if (hint == mostrado) return;
        mostrado = hint;
        String t;
        String m;
        switch (hint) {
            case MULTIPLE_FACES:
                t = "Se detectaron varios rostros";
                m = "Para proteger su identidad, solo debe aparecer una persona frente a la cámara. Asegúrese de estar solo en la imagen para continuar.";
                break;
            case NO_FACE:
                t = "No podemos ver su rostro";
                m = "Ubique su rostro frente a la cámara y manténgalo dentro del área indicada en la pantalla.";
                break;
            case ADJUST_POSITION:
                t = "Ajuste su posición";
                m = "Centre su rostro dentro del marco que aparece en pantalla y mantenga el dispositivo estable.";
                break;
            case TOO_DARK:
                t = "Iluminación insuficiente";
                m = "Su rostro está poco iluminado. Busque un lugar con más luz o ubíquese frente a una fuente de iluminación para continuar.";
                break;
            case TOO_BRIGHT:
                t = "Iluminación excesiva";
                m = "Hay demasiada luz sobre su rostro. Evite la luz directa o cambie de posición para que podamos identificarlo correctamente.";
                break;
            default:
                t = "Todo listo";
                m = "Su rostro se ve bien. Toque \"Comenzar\" y siga las indicaciones en pantalla.";
        }
        titulo.setText(t);
        mensaje.setText(m);
        int voz = hint == Hint.MULTIPLE_FACES ? R.raw.a14 : hint == Hint.NO_FACE ? R.raw.a15
                : hint == Hint.ADJUST_POSITION ? R.raw.a16 : hint == Hint.TOO_DARK ? R.raw.a17
                : hint == Hint.TOO_BRIGHT ? R.raw.a18 : R.raw.a19;
        if (!anteojosAvisado) {
            anteojosAvisado = true;   // la primera vez se anticipa el aviso fijo de anteojos
            com.autenticar.tuid.usuarios.Audio.play(this, R.raw.a13, voz);
        } else {
            com.autenticar.tuid.usuarios.Audio.play(this, voz);
        }
        // listo: tinta oscura; si hay algo para corregir: rojo oscuro (la tarjeta es blanca)
        titulo.setTextColor(hint == Hint.OK ? ContextCompat.getColor(this, R.color.tu_tinta) : 0xFFB3201E);
        continuar.setEnabled(hint == Hint.OK);
        continuar.setAlpha(1f); // el boton deshabilitado ya se ve gris por si mismo
    }

    private void mostrarError() {
        com.autenticar.tuid.usuarios.Audio.play(this, R.raw.a20);
        titulo.setText("No pudimos usar la cámara");
        mensaje.setText("Cierre otras aplicaciones que estén usando la cámara y vuelva a intentarlo.");
        continuar.setEnabled(false);
        continuar.setAlpha(1f);
    }
}
