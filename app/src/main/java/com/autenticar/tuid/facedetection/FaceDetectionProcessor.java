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
package com.autenticar.tuid.facedetection;

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
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions.ALL_CONTOURS;

/**
 * Face Detector Demo.
 */
public class FaceDetectionProcessor extends VisionProcessorBase<List<FirebaseVisionFace>> {

    private static final String TAG = "FaceDetectionProcessor";

    private final FirebaseVisionFaceDetector detector;

    public static final int DETECTAR_SIN_GESTOS = 0;
    public static final int DETECTAR_SONRISA = 1;
    public static final int DETECTAR_OJOSCERRADOS_SONRIENDO = 2;
    public static final int DETECTAR_OJO_DIESTRO_CERRADO_SONRIENDO = 3;
    public static final int DETECTAR_OJO_IZQ_CERRADO_SONRIENDO = 4;
    public static final int DETECTAR_OJOSCERRADOS = 5;
    public static final int DETECTAR_PESTANEO_NEUTRAL = 6;
    public static final int DETECTAR_PESTANEO_CLOSED_EYES = 7;
    public static final int CANTIDAD_DETECCIONES_NECESARIAS = 3; // detecta 3 pestañeso cuenta 1 cuando abre y uno cuando cierra el ojo

    private int gestureToDetect = DETECTAR_SONRISA;
    private int contadorDetecciones = 0;
    private IGestureResult gestureResult;
    private Activity activity;
    private Context context;
    private boolean ban = true;
    private ArrayList<rectagleUser> rectangleArray = new ArrayList<>();
    private int valorID = 0;
    private Rectangle rectangle_respaldo;
    private DisplayMetrics display = new DisplayMetrics();
    private int width, medWidth;
    private TextView alertMessager;
    private int alertMessageState = -1;
    private Rect rect;

    public FaceDetectionProcessor(int gestureToDetect, IGestureResult gestureResult, Activity activity, Context context, TextView alertMessager) {
        this.context = context;
        //aqui hay q revisar para q funcione en la nueva version
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setContourMode(ALL_CONTOURS)
                        .setMinFaceSize(0.7f)
                        .enableTracking()
                        .build();
        this.gestureToDetect = gestureToDetect;
        this.activity = activity;
        this.gestureResult = gestureResult;
        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
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
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionFace>> detectInImage(FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionFace> faces,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
//        Bitmap displayImage = originalCameraImage.copy(Bitmap.Config.ARGB_8888, true);
        if (originalCameraImage != null) {
//            //-------  ESTEBAN
//            if(faces.size()>0) {
//                Canvas canvas = new Canvas(displayImage);
//                for(FirebaseVisionFace f : faces) {
//
//
//                    canvas.drawColor(Color.argb(0, 0, 0, 0));
//
//                    Paint paintPrimary = new Paint();
//                    paintPrimary.setStrokeWidth(2);
//                    paintPrimary.setColor(Color.rgb(100, 100, 100));
//                    paintPrimary.setStyle(Paint.Style.STROKE);
//
//                    Paint paintSecond = new Paint();
//                    paintSecond.setStrokeWidth(2);
//                    paintSecond.setColor(Color.RED);
//                    paintSecond.setStyle(Paint.Style.STROKE);
//
//                    Paint paintLines = new Paint();
//                    paintLines.setStrokeWidth(5);
//                    paintLines.setColor(Color.WHITE);
//                    paintLines.setStyle(Paint.Style.STROKE);
//
//                    int valor = 30;
//                    Rect rect = new Rect();
//                    rect.top = f.getBoundingBox().top;
//                    rect.bottom = f.getBoundingBox().bottom;
//                    rect.left = (originalCameraImage.getWidth() - f.getBoundingBox().left) - f.getBoundingBox().width();
//                    rect.right = rect.left + f.getBoundingBox().width();
//
//
//                    canvas.drawLine(rect.centerX() - (rect.width() / 2), rect.centerY() - (rect.height() / 2), rect.centerX() - (rect.width() / 2), (rect.centerY() - (rect.height() / 2)) + valor, paintLines);
//                    canvas.drawLine(rect.centerX() - (rect.width() / 2), rect.centerY() - (rect.height() / 2), (rect.centerX() - (rect.width() / 2)) + valor, rect.centerY() - (rect.height() / 2), paintLines);
//
//                    canvas.drawLine(rect.centerX() + (rect.width() / 2), rect.centerY() + (rect.height() / 2), rect.centerX() + (rect.width() / 2), (rect.centerY() + (rect.height() / 2)) - valor, paintLines);
//                    canvas.drawLine(rect.centerX() + (rect.width() / 2), rect.centerY() + (rect.height() / 2), (rect.centerX() + (rect.width() / 2)) - valor, rect.centerY() + (rect.height() / 2), paintLines);
//
//                    canvas.drawLine(rect.centerX() - (rect.width() / 2), rect.centerY() + (rect.height() / 2), rect.centerX() - (rect.width() / 2), (rect.centerY() + (rect.height() / 2)) - valor, paintLines);
//                    canvas.drawLine(rect.centerX() - (rect.width() / 2), rect.centerY() + (rect.height() / 2), (rect.centerX() - (rect.width() / 2)) + valor, rect.centerY() + (rect.height() / 2), paintLines);
//
//                    canvas.drawLine(rect.centerX() + (rect.width() / 2), rect.centerY() - (rect.height() / 2), rect.centerX() + (rect.width() / 2), (rect.centerY() - (rect.height() / 2)) + valor, paintLines);
//                    canvas.drawLine(rect.centerX() + (rect.width() / 2), rect.centerY() - (rect.height() / 2), (rect.centerX() + (rect.width() / 2)) - valor, rect.centerY() - (rect.height() / 2), paintLines);
//                }
//            }
//            //-------

//            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, displayImage);


            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);

            graphicOverlay.clear();
            graphicOverlay.add(imageGraphic);
        }
//        rectangleArray.clear();

        if (faces.size() > 0) {
            if(alertMessageState!=0) {
                alertMessager.setText("Detectando tu fe de vida");
                alertMessager.setBackgroundResource(R.color.colorPrimary);
                alertMessageState = 0;
            }
            for (int i = 0; i < faces.size(); ++i) {
                if (ban || faces.size() == 1) {
                    valorID = faces.get(i).getTrackingId();
                    ban = false;
                }
//                rectagleUser ru = new rectagleUser(
//                        (int) width - (faces.get(i).getBoundingBox().right * 2),
//                        (int) width - (faces.get(i).getBoundingBox().right * 2) + (faces.get(i).getBoundingBox().width() * 2),
//                        (int) (faces.get(i).getBoundingBox().top * 2),
//                        (int) ((faces.get(i).getBoundingBox().bottom * 2) + 10),
//                        faces.get(i).getTrackingId());
//



//
//
//                rectangleArray.add(ru);
//                rect.top = ru.getTop();
//                rect.left = ru.getLeft();
//                rect.right = ru.getRigth();
//                rect.bottom = ru.getButton();



                /*if (faces.get(i).getBoundingBox().left * 2 < width && faces.get(i).getBoundingBox().right * 2 < width) {
                    System.out.println("1");
                    rectagleUser ru = new rectagleUser(
                            faces.get(i).getBoundingBox().left * 2 + width + (faces.get(i).getBoundingBox().width() / 2),
                            faces.get(i).getBoundingBox().right * 2 + width +(faces.get(i).getBoundingBox().width() / 2),
                            faces.get(i).getBoundingBox().top * 2,
                            (faces.get(i).getBoundingBox().bottom * 2) + 10,
                            faces.get(i).getTrackingId());
                    rectangleArray.add(ru);
                } else if (faces.get(i).getBoundingBox().left * 2 > width && faces.get(i).getBoundingBox().right * 2 > width) {
                    System.out.println("2");
                    rectagleUser ru = new rectagleUser(
                            (int) (faces.get(i).getBoundingBox().left * 2) - width - faces.get(i).getBoundingBox().width() / 2,
                            (int) (faces.get(i).getBoundingBox().right * 2) - width - faces.get(i).getBoundingBox().width() / 2,
                            (int) (faces.get(i).getBoundingBox().top * 2),
                            (int) ((faces.get(i).getBoundingBox().bottom * 2) + 10),
                            faces.get(i).getTrackingId());
                    rectangleArray.add(ru);
                } else
               if (faces.get(i).getBoundingBox().left * 2 < width && faces.get(i).getBoundingBox().right * 2 > width) {
                if(faces.get(i).getBoundingBox().left*2  < medWidth) {
                        rectagleUser ru = new rectagleUser(
                                (int) width-(faces.get(i).getBoundingBox().right * 2) ,
                                (int) width-(faces.get(i).getBoundingBox().right * 2)+ (faces.get(i).getBoundingBox().width()*2 ),
                                (int) (faces.get(i).getBoundingBox().top * 2),
                                (int) ((faces.get(i).getBoundingBox().bottom * 2) + 10),
                                faces.get(i).getTrackingId());
                        rectangleArray.add(ru);
                   // }else{
                      //  System.out.println("left mayor");
                   /*} else if (faces.get(i).getBoundingBox().width() / 2 < width) {
                        rectagleUser ru = new rectagleUser(
                                (int) (faces.get(i).getBoundingBox().left * 2 + faces.get(i).getBoundingBox().width() / 2),
                                (int) (faces.get(i).getBoundingBox().right * 2 + faces.get(i).getBoundingBox().width() / 2),
                                (int) (faces.get(i).getBoundingBox().top * 2),
                                (int) ((faces.get(i).getBoundingBox().bottom * 2) + 10),
                                faces.get(i).getTrackingId());
                        rectangleArray.add(ru);
                    } else {
                        rectagleUser ru = new rectagleUser(
                                (int) (faces.get(i).getBoundingBox().left * 2),
                                (int) (faces.get(i).getBoundingBox().right * 2),
                                (int) (faces.get(i).getBoundingBox().top * 2),
                                (int) ((faces.get(i).getBoundingBox().bottom * 2) + 10),
                                faces.get(i).getTrackingId());
                        rectangleArray.add(ru);
                    }*/

                // } else {
                //     System.out.println("4");
                //}
            }
            for (int i = 0; i < faces.size(); i++) {
                if (faces.get(i).getTrackingId() == valorID && faces.size() == 1) {
                    FirebaseVisionFace face = faces.get(i);
                    ProcesarGesto(face, originalCameraImage, faces.size());
                } else if (faces.size() > 1) {
                    contadorDetecciones = 0;
                    if(alertMessageState!=1) {
                        alertMessager.setText("Alerta: Se detectan más de dos rostros.");
                        alertMessager.setBackgroundResource(R.color.colorAccentDark);
                        alertMessageState = 1;
                    }

                }
            }
        } else {
//            rectagleUser ru = new rectagleUser(0, 0, 0, 0, 0);
//            ban = true;
//            valorID = 0;
//            contadorDetecciones = 0;
//            rectangleArray.add(ru);

            contadorDetecciones = 0;
            if(alertMessageState!=2) {
                alertMessager.setText("Alerta: NO se detectan rostros....");
                alertMessager.setBackgroundResource(R.color.colorAccentDark);
                alertMessageState=2;
            }
        }
//        Rectangle rectangle = new Rectangle(context, rectangleArray, valorID);
//        if (rectangle_respaldo != null) {
//            fireTopLayout.removeView(rectangle_respaldo);
//        }
//        rectangle_respaldo = rectangle;
//        fireTopLayout.addView(rectangle);
        graphicOverlay.postInvalidate();
        faces.clear();
    }

    private void ProcesarGesto(FirebaseVisionFace face, Bitmap bitmap, int i) {
        switch (gestureToDetect) {
//            case DETECTAR_SONRISA:
//                if ((face.getSmilingProbability() > 0.8)
//                        && face.getLeftEyeOpenProbability() > 0.5
//                        && face.getRightEyeOpenProbability() > 0.5) {
//                    contadorDetecciones += 1;
//                } else {
//                    contadorDetecciones = 0;
//                }
//                break;
//            case DETECTAR_OJO_IZQ_CERRADO_SONRIENDO:
//                if ((face.getLeftEyeOpenProbability() > 0.8)
//                        && (face.getRightEyeOpenProbability() < 0.3)
//                        && (face.getSmilingProbability() > 0.8)) {
//                    contadorDetecciones += 1;
//                } else {
//                    contadorDetecciones = 0;
//                }
//                break;
//            case DETECTAR_OJO_DIESTRO_CERRADO_SONRIENDO:
//                if ((face.getLeftEyeOpenProbability() < 0.3)
//                        && (face.getRightEyeOpenProbability() > 0.8)
//                        && (face.getSmilingProbability() > 0.8)) {
//                    contadorDetecciones += 1;
//                } else {
//                    contadorDetecciones = 0;
//                }
//
//                break;
//            case DETECTAR_OJOSCERRADOS_SONRIENDO:
//                if ((face.getLeftEyeOpenProbability() < 0.3)
//                        && (face.getRightEyeOpenProbability() < 0.3)
//                        && (face.getSmilingProbability() > 0.8)) {
//                    contadorDetecciones += 1;
//                } else {
//                    contadorDetecciones = 0;
//                }
//                break;
//
//            case DETECTAR_OJOSCERRADOS:
//                if ((face.getLeftEyeOpenProbability() < 0.3)
//                        && (face.getRightEyeOpenProbability() < 0.3)
//                        && (face.getSmilingProbability() < 0.3)) {
//                    contadorDetecciones += 1;
//                } else {
//                    contadorDetecciones = 0;
//                    gestureResult.GestureProgress(contadorDetecciones);
//                }
//                break;
//            case DETECTAR_SIN_GESTOS:
//                if ((face.getLeftEyeOpenProbability() > 0.8)
//                        && (face.getRightEyeOpenProbability() > 0.8)
//                        && (face.getSmilingProbability() < 0.3)) {
//                    contadorDetecciones += 1;
//                } else {
//                    contadorDetecciones = 0;
//                    gestureResult.GestureProgress(contadorDetecciones);
//                }
//                break;

            /* manejo de pestaneo */
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
            Rect rectSrc= new Rect(0,0,original.getWidth(), original.getHeight());
            Rect rectDst= new Rect(0,0,width,heigth);
            int dx=(rectSrc.width()-rectDst.width())/2;
            int dy=(rectSrc.height()-rectDst.height())/2;
            rectSrc.inset(Math.max(0,dx), Math.max(0, dy));
            rectDst.inset(Math.max(0,-dx), Math.max(0, -dy));
           // Rect rectFin = new Rect(rect.left, rect.top-20, rect.right, rect.bottom+20);
            canvas.drawBitmap(original, rectSrc, rectDst, null);
            return croppedImg;
        } else {
            return original;
        }
    }
}
