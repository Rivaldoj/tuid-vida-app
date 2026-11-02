package com.autenticar.tuid.v2.utils;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.autenticar.tuid.facedetection.IGestureResult;
import com.autenticar.tuid.facedetection.VisionProcessorBase;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;

import com.autenticar.tuid.R;
import com.autenticar.tuid.model.rectagleUser;
import com.autenticar.tuid.ui.vision.CameraImageGraphic;
import com.autenticar.tuid.ui.vision.FrameMetadata;
import com.autenticar.tuid.ui.vision.GraphicOverlay;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;



public class DetectorDeGestosProcessor extends VisionProcessorBase<List<Face>> {

    private static final String TAG = "FaceDetectionProcessor";

    private final com.google.mlkit.vision.face.FaceDetector detector;

    public static final int DETECTAR_SONRISA = 1;
    public static final int DETECTAR_PESTANEO_NEUTRAL = 6;
    public static final int DETECTAR_PESTANEO_CLOSED_EYES = 7;
    public static final int CANTIDAD_DETECCIONES_NECESARIAS = 3;

    private int gestureToDetect = DETECTAR_SONRISA;
    private int contadorDetecciones = 0;
    private IGestureResult gestureResult;
    private Activity activity;
    private Context context;
    private boolean ban = true;
    private ArrayList<rectagleUser> rectangleArray = new ArrayList<>();
    private int valorID = 0;
    private DisplayMetrics display = new DisplayMetrics();
    private int width, medWidth;
    private TextView alertMessager;
    private int alertMessageState = -1;
    private Rect rect;

    public DetectorDeGestosProcessor(int gestureToDetect, IGestureResult gestureResult, Activity activity, Context context, TextView alertMessager) {
        this.context = context;
        //aqui hay q revisar para q funcione en la nueva version
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .setMinFaceSize(0.7f)
                        .enableTracking()
                        .build();
        this.gestureToDetect = gestureToDetect;
        this.activity = activity;
        this.gestureResult = gestureResult;
        detector = FaceDetection.getClient(options);
        activity.getWindowManager().getDefaultDisplay().getMetrics(display);
        width = display.widthPixels;
        medWidth = display.widthPixels / 2;
        this.alertMessager = alertMessager;
        rect = new Rect();
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: " + e);
        }
    }

    @Override
    protected Task<List<Face>> detectInImage(InputImage image) {
        return detector.process(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<Face> faces,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
//        Bitmap displayImage = originalCameraImage.copy(Bitmap.Config.ARGB_8888, true);
//        if (originalCameraImage != null) {
//
//
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
//
            graphicOverlay.clear();
            graphicOverlay.add(imageGraphic);
//        }

        if (faces.size() > 0) {
            if (alertMessageState != 0) {
                alertMessager.setText("Detectando tu fe de vida");
                alertMessager.setBackgroundResource(R.color.colorPrimary);
                alertMessageState = 0;
            }
            for (int i = 0; i < faces.size(); ++i) {
                if (ban || faces.size() == 1) {
                    valorID = faces.get(i).getTrackingId();
                    ban = false;
                }
            }
            for (int i = 0; i < faces.size(); i++) {
                if (faces.get(i).getTrackingId() == valorID && faces.size() == 1) {
                    Face face = faces.get(i);
                    ProcesarGesto(face, originalCameraImage, faces.size());
                } else if (faces.size() > 1) {
                    contadorDetecciones = 0;
                    if (alertMessageState != 1) {
                        alertMessager.setText("Alerta: Se detectan más de dos rostros.");
                        alertMessager.setBackgroundResource(R.color.colorAccentDark);
                        alertMessageState = 1;
                    }

                }
            }
        } else {

            contadorDetecciones = 0;
            if (alertMessageState != 2) {
                alertMessager.setText("Alerta: NO se detectan rostros....");
                alertMessager.setBackgroundResource(R.color.colorAccentDark);
                alertMessageState = 2;
            }
        }
        graphicOverlay.postInvalidate();
        faces.clear();
    }

    private void ProcesarGesto(Face face, Bitmap bitmap, int i) {
        switch (gestureToDetect) {
            case DETECTAR_PESTANEO_CLOSED_EYES:
                if ((face.getLeftEyeOpenProbability() > 0.8)
                        && (face.getRightEyeOpenProbability() > 0.8)
                ) {
                    contadorDetecciones += 0;

                    gestureToDetect = DETECTAR_PESTANEO_NEUTRAL;
                }
                break;
            case DETECTAR_PESTANEO_NEUTRAL:
                if ((face.getLeftEyeOpenProbability() < 0.2)
                        && (face.getRightEyeOpenProbability() < 0.2)
                ) {
                    contadorDetecciones += 1;
                    gestureResult.GestureProgress(contadorDetecciones);
                    gestureToDetect = DETECTAR_PESTANEO_CLOSED_EYES;
                }
                break;
        }


        if (i == 1) {
            if (contadorDetecciones >= CANTIDAD_DETECCIONES_NECESARIAS) {
                alertMessager.setText("Fe de vida Exitoso... ");
                alertMessager.setBackgroundResource(R.color.colorPrimary);
                bitmap = cropBitmap(bitmap, bitmap.getHeight(), bitmap.getWidth());
                gestureResult.GestureDetected(face, bitmap, gestureToDetect);
            }
        } else {
            contadorDetecciones = 0;
            alertMessager.setText("Alerta: se detecta mas de un rostro....");
            alertMessager.setBackgroundResource(R.color.colorAccent);
        }
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }

    public Bitmap cropBitmap(Bitmap original, int heigth, int width) {
        if (rect != null) {
            Bitmap croppedImg = Bitmap.createBitmap(width, heigth, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(croppedImg);
            Rect rectSrc = new Rect(0, 0, original.getWidth(), original.getHeight());
            Rect rectDst = new Rect(0, 0, width, heigth);
            int dx = (rectSrc.width() - rectDst.width()) / 2;
            int dy = (rectSrc.height() - rectDst.height()) / 2;
            rectSrc.inset(Math.max(0, dx), Math.max(0, dy));
            rectDst.inset(Math.max(0, -dx), Math.max(0, -dy));
            canvas.drawBitmap(original, rectSrc, rectDst, null);
            return croppedImg;
        } else {
            return original;
        }
    }
}