package com.autenticar.tuid.utils.cropimage;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.autenticar.tuid.DNIDetector;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.CameraSource;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CropperDetector {

    boolean faceDetected = false;
    boolean pdfDetected = false;
    boolean targetTextDetected = false;

    CameraSource mCameraSource;
    String[] targetText;

    volatile boolean faceDetectionEnded = true;
    volatile boolean pdfDetectionEnded = true;
    volatile boolean textDetectionEnded = true;
    volatile boolean objectDetectionEnded = true;
    volatile boolean getNewFrameEnded = true;

    boolean isFrontDetection = true;
    boolean modelDetected = false;

    public static int model = 2;

    public Bitmap currentFrame;
    public Bitmap result;

    public static Bitmap frontResult;
    public static Bitmap backResult;
    public static Bitmap faceResult;

    public static String dni;
    public static String apellido;
    public static String nombre;
    public static Date fechaNacimiento;
    public static String sexo;

    FirebaseVisionObjectDetector objectDetector;
    FirebaseVisionFaceDetector faceDetector;
    FirebaseVisionTextRecognizer textDetector;
    FirebaseVisionBarcodeDetector pdfDetector;

    public boolean faceFoundOnce = false;
    public boolean pdfFoundOnce = false;
    public boolean mrzFoundOnce = false;
    public boolean backDataEqualsFrontOnce = false;

    public static boolean processing = false;

    Activity activity;

    public long startTime;
    public long maxScanTime = 30000; //30 segundos



    public String getTimeoutDetails(){
        if(isFrontDetection){
            if(!faceFoundOnce) return "La calidad de la imagen no permite identificar un rostro en el documento, compruebe que tenga buena iluminación y no se produzcan reflejos en el documento";
            else return "La calidad de la imagen no permite identificar un código de barras o código MRZ en el documento. Compruebe que tenga buena iluminación y no se produzcan reflejos en el documento";
        }
        else{
            if(model == 2 && !pdfFoundOnce) return "La calidad de la imagen no permite identificar un código de barras en el documento, compruebe que tenga buena iluminación y no se produzcan reflejos en el documento";
            if(model == 1 && !mrzFoundOnce) return "La calidad de la imagen no permite identificar un código MRZ en el documento, compruebe que tenga buena iluminación y no se produzcan reflejos en el documento";
            if(!backDataEqualsFrontOnce) return "No coincide el frente con el dorso (Los datos del frente no coinciden con el dorso, comuníquese por medio de formulario de contacto (!) para solicitar ayuda )";
        }

        return "La calidad de la imagen no permite identificar un documento, compruebe que tenga buena iluminación y no se produzcan reflejos en el documento";
    }

    public CropperDetector(Activity activity , CameraSource cameraSource, boolean isFrontDetection, String[] targetText) {
        startTime = System.currentTimeMillis();
        String target = "";
        for(String s : targetText) target+=" " + s;
//        Log.e("CropperDetector", "Looking for " + target);
        this.activity = activity;
//        Log.e("CropperDetector", "Started for " + (isFrontDetection ? "front" : "back") + " model:" + model);
        this.mCameraSource = cameraSource;
        this.targetText = targetText;
        this.isFrontDetection = isFrontDetection;
        if(isFrontDetection) {
            frontResult = null;
            model = 2;
        }
        else{
            backResult = null;
            modelDetected = true;
        }


        FirebaseVisionObjectDetectorOptions objectDetectorOptions =
                new FirebaseVisionObjectDetectorOptions.Builder()
                        .setDetectorMode(FirebaseVisionObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .build();

        objectDetector = FirebaseVision.getInstance().getOnDeviceObjectDetector(objectDetectorOptions);

        faceDetector = FirebaseVision.getInstance().getVisionFaceDetector();

        textDetector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

        FirebaseVisionBarcodeDetectorOptions pdfDetectoroptions =
                new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(
                                FirebaseVisionBarcode.FORMAT_PDF417)
                        .build();

        pdfDetector = FirebaseVision.getInstance().getVisionBarcodeDetector(pdfDetectoroptions);
//        Log.e("CropperDet", "Instance ready");

    }


    private boolean isFilled() {
        return faceDetectionEnded && pdfDetectionEnded && textDetectionEnded && objectDetectionEnded && getNewFrameEnded && faceDetected && pdfDetected && targetTextDetected;
    }

    public boolean isCompleted() {

        if(isFrontDetection && frontResult!=null) return true;
        if(!isFrontDetection && backResult!=null) return true;
        if(!processing)revalidatePreview();
        return false;


    }

    public void saveImage(final Bitmap finalBitmap, String image_name) {
        String fname = image_name + ".png";
//        Log.e("Guardando imagen", Environment.getExternalStorageDirectory().toString() + fname);
        MediaStore.Images.Media.insertImage(activity.getContentResolver(), finalBitmap, fname , "ASD");
//        try {
//
//            File path = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "tuIdVida" + File.separator);
//            if(!path.exists())  path.mkdir();
//            File file = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "tuIdVida" + File.separator, fname);
//            if (file.exists()) file.delete();
//            final FileOutputStream out = new FileOutputStream(file);
//            AsyncTask.execute(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        finalBitmap.compress(Bitmap.CompressFormat.PNG, 0, out);
//                        out.flush();
//                        out.close();
//                    } catch (Exception e) {
////                        Log.e("Error", e.getMessage());
//                    }
//                }
//            });
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }


    private Bitmap getGrayscale(Bitmap src) {
        //Custom color matrix to convert to GrayScale
//        float[] matrix = new float[]{
//                0.3f, 0.59f, 0.11f, 0, 0,
//                0.3f, 0.59f, 0.11f, 0, 0,
//                0.3f, 0.59f, 0.11f, 0, 0,
//                0, 0, 0, 1, 0,};

        Bitmap dest = Bitmap.createBitmap(
                src.getWidth(),
                src.getHeight(),
                src.getConfig());


        //MONOCHROME MATRIX
        ColorMatrix ma = new ColorMatrix();
        ma.setSaturation(0);


        Canvas canvas = new Canvas(dest);
        Paint paint = new Paint();
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(ma);
        paint.setColorFilter(filter);
        canvas.drawBitmap(src, 0, 0, paint);

        return dest;
    }


    public  Bitmap scaleBitmap(Bitmap bm) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        if (width > height) {
            // landscape
            float ratio = (float) width / 660;
            width = 660;
            height = (int) (height / ratio);
        } else if (height > width) {
            // portrait
            float ratio = (float) height / 660;
            height = 660;
            width = (int) (width / ratio);
        } else {
            // square
            height = 660;
            width = 660;
        }


        bm = Bitmap.createScaledBitmap(bm, width, height, false);
        return bm;
    }



    private void revalidatePreview() {
        if(DNIDetector.tempBitmap!=null){
            currentFrame = DNIDetector.tempBitmap;
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(currentFrame);
           if (isFrontDetection)
               processImageFront(image);
           else {
               processImageBack(image);
           }
           getNewFrameEnded = true;
        }
    }



    public static int getVerificationDigit(String number){
        try {
//            Log.e("Verificando", "." + number + ".");
            int suma = 0;
            int[] aux = new int[]{7, 3, 1};
            int position = 0;
            for (char c : number.toCharArray()) {
                if (!Character.isDigit(c)) {

                    suma += ((int) c - 55) * aux[position++];

                }
                else{
                    suma += Integer.parseInt("" + c) * aux[position++];
                }
//                Log.e("Char", c+"");
                if (position == aux.length) position = 0;

            }
//            Log.e("DigitoVerif", suma % 10 + "");
            return suma % 10;
        }
        catch (Exception e){
//            Log.e("VerifDigitEx", e.getMessage());
        }
        return -1;
    }



    private static Date parseDateDDMMYYYY(String date) {

//        Log.e("Parseando", date);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try{
            String d = date.split("/")[0];
            String m = date.split("/")[1];
            String a = date.split("/")[2];

            return sdf.parse(d+"-"+m+"-"+a);
        }
        catch (Exception e){
//            Log.e("ERROR", "PARSE DE FECHA DE NACIMIENTO");
        }
        return new Date();
    }


    public void processImageFront(final FirebaseVisionImage source) {
        if (!processing) {
            processing = true;
            objectDetector.processImage(source).addOnSuccessListener(
                    new OnSuccessListener<List<FirebaseVisionObject>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionObject> detectedObjects) {
                            if (detectedObjects.size() > 0) {
//                                Log.e("CropperD", "Objetos detectados :" + detectedObjects.size());
                                List<FirebaseVisionObject> detectedPrincipalObjects = new ArrayList<FirebaseVisionObject>();
                                for (FirebaseVisionObject o : detectedObjects) {
//                                    Log.e("CropperD", o.getBoundingBox().width() + " : " + o.getBoundingBox().height());
                                    if (o.getBoundingBox().width() > 1000 || o.getBoundingBox().height()> 1000 || true) {
                                        detectedPrincipalObjects.add(o);
                                    }
                                }
                                    if (detectedPrincipalObjects.size()>0) {
                                        FirebaseVisionObject o = detectedPrincipalObjects.get(0);
                                        result = Bitmap.createBitmap(o.getBoundingBox().width(), o.getBoundingBox().height(), Bitmap.Config.ARGB_8888);
                                        Canvas canvas = new Canvas(result);
                                        Rect dstRect = new Rect(0, 0, o.getBoundingBox().width(), o.getBoundingBox().height());
                                        Rect src = new Rect(o.getBoundingBox().left, o.getBoundingBox().top, o.getBoundingBox().right, o.getBoundingBox().bottom);
                                        canvas.drawBitmap(source.getBitmap(), src, dstRect, null);


                                        if (o.getBoundingBox().width() < o.getBoundingBox().height()) {
                                            Matrix matrix = new Matrix();
                                            matrix.setRotate(90);
                                            result = Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, true);
                                        }

                                        final FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(result);

                                        //DETECTAR CARA EN OBJETO DETECTADO (ESTAMOS  EN EL FRENTE)
                                        faceDetector.detectInImage(image).addOnSuccessListener(
                                                new OnSuccessListener<List<FirebaseVisionFace>>() {
                                                    @Override
                                                    public void onSuccess(List<FirebaseVisionFace> detections) {
//                                                        Log.e("FaceDetector", "Detected faces:" + detections.size());
                                                        if (detections.size() > 0) {
                                                            faceFoundOnce = true;
//                                                            Log.e("CropperD", "CARA detectada");
//                                                            saveImage(image.getBitmap(), "cara-detectado-" + System.currentTimeMillis());
                                                            FirebaseVisionFace o = detections.get(0);
                                                            Bitmap faceResult = Bitmap.createBitmap(o.getBoundingBox().width(), o.getBoundingBox().height(), Bitmap.Config.ARGB_8888);
                                                            Canvas canvas = new Canvas(faceResult);
                                                            Rect dstRect = new Rect(0, 0, o.getBoundingBox().width(), o.getBoundingBox().height());
                                                            Rect src = new Rect(o.getBoundingBox().left, o.getBoundingBox().top, o.getBoundingBox().right, o.getBoundingBox().bottom);
                                                            canvas.drawBitmap(image.getBitmap(), src, dstRect, null);
//                                                            CropperDetector.faceResult =  scaleBitmap(getGrayscale(faceResult));
                                                            CropperDetector.faceResult =  faceResult;

                                                            pdfDetector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                                                                @Override
                                                                public void onSuccess(List<FirebaseVisionBarcode> BARCODEdetections) {
                                                                    if (BARCODEdetections.size() > 0) {
//                                                                        Log.e("CropperD", "QR detectado");
                                                                        model = 1;
                                                                        FirebaseVisionBarcode o = BARCODEdetections.get(0);
//                                                                        Log.e("CropperD", "BARCODE: " + o.getRawValue());
                                                                        String[] PDFresult = o.getRawValue().split("@");
                                                                        if (PDFresult.length > 7) {
                                                                            dni = PDFresult[4];
                                                                            apellido = PDFresult[1];
                                                                            nombre = PDFresult[2];
                                                                            sexo = PDFresult[3];
                                                                            fechaNacimiento = parseDateDDMMYYYY(PDFresult[6]);
                                                                            textDetector.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                                                                @Override
                                                                                public void onSuccess(FirebaseVisionText OCRdetection) {
                                                                                    boolean PDFdetected = false;
                                                                                    for (FirebaseVisionText.TextBlock tb : OCRdetection.getTextBlocks()) {
                                                                                        for (FirebaseVisionText.Line l : tb.getLines()) {
                                                                                            if (l.getText().replace(".","").replace("MO", "M0").contains(dni)){
                                                                                                PDFdetected = true;
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                    if(PDFdetected){
//                                                                                        Log.e("CropperD", "PDF detectado");
                                                                                        frontResult =  scaleBitmap(result);
                                                                                    }
                                                                                    else{
                                                                                        Log.e("CropperD", "DNI IGUAL AL DEL QR NO DETECTADO " + dni);
                                                                                    }
                                                                                    processing = false;
//                                                                                    Log.e("CropperD", "Processing ended");

                                                                                }
                                                                            });
                                                                        }
                                                                    } else {
//                                                                        Log.e("CropperD", "QR NO detectado");
                                                                        textDetector.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                                                            @Override
                                                                            public void onSuccess(FirebaseVisionText OCRdetection) {
                                                                                boolean PDFdetected = false;
                                                                                int detectedPDFLines = 0;
                                                                                dni = "";
                                                                                for (FirebaseVisionText.TextBlock tb : OCRdetection.getTextBlocks()) {
                                                                                    for (FirebaseVisionText.Line l : tb.getLines()) {
                                                                                        if (l.getText().contains("<<")){
                                                                                            PDFdetected = true;
                                                                                            detectedPDFLines++;
                                                                                            if(l.getText().toUpperCase().contains("IDARG")){
                                                                                                String dniAux = l.getText().substring(5, l.getText().indexOf("<"));
//                                                                                                Log.e("DniAux", dniAux);
                                                                                                if(l.getText().contains("<" + getVerificationDigit(dniAux) + "<")){
                                                                                                    dni = dniAux;
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                                if(PDFdetected && detectedPDFLines>2 && dni.length()>0){
//                                                                                    Log.e("CropperD", "PDF detectado");
                                                                                    frontResult =  scaleBitmap(result);
                                                                                    model = 2;
                                                                                }
                                                                                else{
//                                                                                    Log.e("CropperD", "PDF NO DETECTADO");
                                                                                }
                                                                                processing = false;
//                                                                                Log.e("CropperD", "Processing ended");

                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                        //NO HAY CARA
                                                        else{
//                                                            Log.e("CropperD", "NO HAY CARA");
                                                            processing = false;
//                                                            Log.e("CropperD", "Processing ended");
                                                        }
                                                    }
                                                });

                                    }
                                    else {
                                        processing = false;
//                                        Log.e("CropperD", "Processing ended");
                                    }
                            }
                            else {
                                processing = false;
//                                Log.e("CropperD", "Processing ended");
                            }
                        }
                    });
        }
    }

    public void processImageBack(final FirebaseVisionImage source) {
        if (!processing) {
//            Log.e("CropperD", "PROCESANDO BACK " + model + (backResult==null?"":"ERORRRORORORRRR"));
            processing = true;
            objectDetector.processImage(source).addOnSuccessListener(
                    new OnSuccessListener<List<FirebaseVisionObject>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionObject> detectedObjects) {
                            if (detectedObjects.size() > 0) {
//                                Log.e("CropperD", "Objeto detectado");
                                List<FirebaseVisionObject> detectedPrincipalObjects = new ArrayList<FirebaseVisionObject>();
                                for (FirebaseVisionObject o : detectedObjects) {
                                    if (o.getBoundingBox().width() > 1000 || o.getBoundingBox().height() > 1000 || true) {
                                        detectedPrincipalObjects.add(o);
                                    }
                                }
                                if (detectedPrincipalObjects.size()>0) {
                                    FirebaseVisionObject o = detectedPrincipalObjects.get(0);
                                    result = Bitmap.createBitmap(o.getBoundingBox().width(), o.getBoundingBox().height(), Bitmap.Config.ARGB_8888);
                                    Canvas canvas = new Canvas(result);
                                    Rect dstRect = new Rect(0, 0, o.getBoundingBox().width(), o.getBoundingBox().height());
                                    Rect src = new Rect(o.getBoundingBox().left, o.getBoundingBox().top, o.getBoundingBox().right, o.getBoundingBox().bottom);
                                    canvas.drawBitmap(source.getBitmap(), src, dstRect, null);
//                                    saveImage(result, "LASD"+System.currentTimeMillis());

                                    if (o.getBoundingBox().width() < o.getBoundingBox().height()) {
                                        Matrix matrix = new Matrix();
                                        matrix.setRotate(90);
                                        result = Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, true);
                                    }


//                                        saveImage(result, "objeto-detectado-" + System.currentTimeMillis());

                                    final FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(result);

                                        if(model==1){
                                            textDetector.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                                @Override
                                                public void onSuccess(FirebaseVisionText OCRdetection) {
                                                    //saveImage(result, "objeto-detectado-text" + System.currentTimeMillis());
//                                                    Log.e("CropperD", "Detected text: " + OCRdetection.getText());
                                                    boolean PDFdetected = false;
                                                    boolean dniDetected = false;
                                                    int detectedPDFLines = 0;
                                                     for (FirebaseVisionText.TextBlock tb : OCRdetection.getTextBlocks()) {
                                                         for (FirebaseVisionText.Line l : tb.getLines()) {
//                                                             Log.e("CropperD", "Detected text: " + l.getText());
                                                             if (l.getText().contains("<<")){
                                                                 PDFdetected = true;
                                                                 mrzFoundOnce = true;
                                                                 detectedPDFLines++;
                                                             }
                                                             if (l.getText().replace("MO","M0").contains(dni + "<")){
                                                                 dniDetected = true;
                                                             }
                                                         }

                                                     }
                                                    if(PDFdetected && detectedPDFLines>2 && dniDetected){
                                                        backResult =  scaleBitmap(result);
                                                    }
                                                    processing = false;
                                                }
                                            });
                                        }
                                        else{
                                            // MODEL 2
//                                            Log.e("Looking", dni);
                                            pdfDetector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                                                @Override
                                                public void onSuccess(List<FirebaseVisionBarcode> detections) {

                                                    if (detections.size() > 0) {
                                                        pdfFoundOnce = true;
                                                        FirebaseVisionBarcode o = detections.get(0);
//                                                        Log.e("CropperD", "BARCODE: " + o.getRawValue());
                                                        String[] PDFresult = o.getRawValue().split("@");
//                                                        Log.e("Comparing", "." + dni +". ." + PDFresult[1] +".");
                                                        if (PDFresult.length > 7 && dni.equalsIgnoreCase(PDFresult[1].replace(" ", ""))) {
                                                            backDataEqualsFrontOnce = true;
                                                            dni = PDFresult[1].replace(" ", "");
                                                            if(!Character.isDigit(dni.charAt(0))) dni = dni.substring(1);
                                                            apellido = PDFresult[4];
                                                            nombre = PDFresult[5];
                                                            sexo = PDFresult[8];
                                                            fechaNacimiento = parseDateDDMMYYYY(PDFresult[7]);
                                                            backResult =  scaleBitmap(result);
                                                        }
                                                    }
                                                    processing = false;
                                                }
                                            });
                                        }
                                    }
                                else{
//                                    Log.e("CropperD", "Processing ended");
                                    processing = false;
                                }
                            }
                            else {
                                processing = false;
//                                Log.e("CropperD", "Processing ended");
                            }
                        }
                    });
        }
    }
}



