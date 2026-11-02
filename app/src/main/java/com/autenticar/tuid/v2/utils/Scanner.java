package com.autenticar.tuid.v2.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import com.autenticar.tuid.v2.model.Persona;
import com.autenticar.tuid.v2.ScanActivity;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Scanner {

    Activity activity;
    public boolean isFrontDetection = true;
    int mode = 0;
    public volatile static boolean busy = false;
    public static boolean preventTimeOut = false;

    public Bitmap result;

    // scan results
    public static Bitmap frontResult;
    public static Bitmap backResult;
    public static Bitmap faceResult;
    public static String dni;
    public static String apellido;
    public static Date expiry;
    public static String nombre;
    public static Date fechaNacimiento;
    public static String sexo;
    public static String tramite;
//    public static String domicilio = "";

    // detectors
    FirebaseVisionObjectDetector objectDetector;
    FirebaseVisionFaceDetector faceDetector;
    FirebaseVisionTextRecognizer textDetector;
    FirebaseVisionBarcodeDetector pdfDetector;

    // timeout vars
    public boolean faceFoundOnce = false;
    public boolean pdfFoundOnce = false;
    public boolean mrzFoundOnce = false;
    public boolean backDataEqualsFrontOnce = false;

    public long startTime;
    public long maxScanTime = 50000; //50 segundos

    public String getTimeoutDetails() {
        if (isFrontDetection) {
            if (!faceFoundOnce)
                return "La calidad de la imagen no permite identificar un rostro en el documento, compruebe que tenga buena iluminación y no se produzcan reflejos en el documento";
            else
                return "La calidad de la imagen no permite identificar un código de barras o código MRZ en el documento. Compruebe que tenga buena iluminación y no se produzcan reflejos en el documento";
        } else {
            if (mode == 2 && !pdfFoundOnce)
                return "La calidad de la imagen no permite identificar un código de barras en el documento, compruebe que tenga buena iluminación y no se produzcan reflejos en el documento";
            if (mode == 1 && !mrzFoundOnce)
                return "La calidad de la imagen no permite identificar un código MRZ en el documento, compruebe que tenga buena iluminación y no se produzcan reflejos en el documento";
            if (!backDataEqualsFrontOnce)
                return "No coincide el frente con el dorso (Los datos del frente no coinciden con el dorso, comuníquese por medio de formulario de contacto (!) para solicitar ayuda )";
        }

        return "La calidad de la imagen no permite identificar un documento, compruebe que tenga buena iluminación y no se produzcan reflejos en el documento";
    }

    public Scanner(Activity activity) {
        startTime = System.currentTimeMillis();
        preventTimeOut = false;
        busy = false;
        frontResult = null;
        backResult = null;
        faceResult = null;

//        Log.e("Scanner", "Started");
        this.activity = activity;

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
                        .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_PDF417)
                        .build();

        pdfDetector = FirebaseVision.getInstance().getVisionBarcodeDetector(pdfDetectoroptions);

    }

    public Bitmap scaleBitmap(Bitmap bm) {
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

    public static int getVerificationDigit(String number) {
        try {
//            Log.e("Verificando", "." + number + ".");
            int suma = 0;
            int[] aux = new int[]{7, 3, 1};
            int position = 0;
            for (char c : number.toCharArray()) {
                if (!Character.isDigit(c)) {

                    suma += ((int) c - 55) * aux[position++];

                } else {
                    suma += Integer.parseInt("" + c) * aux[position++];
                }
//                Log.e("Char", c+"");
                if (position == aux.length) position = 0;

            }
//            Log.e("DigitoVerif", suma % 10 + "");
            return suma % 10;
        } catch (Exception e) {
//            Log.e("VerifDigitEx", e.getMessage());
        }
        return -1;
    }

    private static Date parseDateDDMMYYYY(String date) {

//        Log.e("Parseando", date);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try {
            String d = date.split("/")[0];
            String m = date.split("/")[1];
            String a = date.split("/")[2];

            return sdf.parse(d + "-" + m + "-" + a);
        } catch (Exception e) {
//            Log.e("ERROR", "PARSE DE FECHA DE NACIMIENTO");
        }
        return new Date();
    }

    public void process(Bitmap _frame) {
        if (isTimeout() || busy) {
//            Log.e("HEY!", "IM BUSSY RIGHT NOW!");
            return;
        }
        busy = true;
//        Log.e("Scanner", "Processing :" + ((startTime + maxScanTime) - new Date().getTime()) );

        if(_frame.getWidth()>_frame.getHeight()){
            Matrix matrix = new Matrix();
            matrix.setRotate(90);
            _frame = Bitmap.createBitmap(_frame, 0, 0, _frame.getWidth(), _frame.getHeight(), matrix, true);

        }

        FirebaseVisionImage frame = FirebaseVisionImage.fromBitmap(_frame);
        final ProcessResult result = new ProcessResult(_frame);

        objectDetector.processImage(frame).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionObject>>() {
            @Override
            public void onSuccess(List<FirebaseVisionObject> firebaseVisionObjects) {
                for (FirebaseVisionObject fo : firebaseVisionObjects) {
                    if (result.frame == null || result.frame.getBoundingBox().width() < fo.getBoundingBox().width())
                        result.frame = fo;
                }
                result.taskFinished("object");
            }
        });

        textDetector.processImage(frame).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                result.text = firebaseVisionText;
                result.taskFinished("text");
            }
        });

        pdfDetector.detectInImage(frame).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
            @Override
            public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                if (firebaseVisionBarcodes.size() == 1) result.barcode = firebaseVisionBarcodes.get(0);
                result.taskFinished("pdf");
            }
        });

        if(isFrontDetection) {
            faceDetector.detectInImage(frame).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                @Override
                public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                    if (firebaseVisionFaces.size() > 0){
                        result.face = firebaseVisionFaces.get(0);
                        faceFoundOnce = true;
                    }
                    result.taskFinished("face");

                }
            });
        }
    }

    public class ProcessResult {

        int tasksResults = 0;


        public Bitmap image;
        public FirebaseVisionObject frame;
        public FirebaseVisionFace face;
        public FirebaseVisionBarcode barcode;
        public FirebaseVisionText text;

        public synchronized void taskFinished(String what) {
            tasksResults++;

            if(isFrontDetection) {
                if(tasksResults!=4) return;
                getMode();
                if(mode==1 && validateFrontMode1()) return;
                if(mode==2 && validateFrontMode2()) return;
            }
            else{
                if(tasksResults!=3) return;
                if(mode==1 && validateBackMode1()) return;
                if(mode==2 && validateBackMode2()) return;
            }

            busy = false;
        }

        public ProcessResult(Bitmap image) {
            this.image = image;
        }

        void getMode() {
            if (frame == null || face == null || text == null){
                mode = 0;
                return;
            }

            if (barcode != null) {
                String[] PDFresult = barcode.getRawValue().split("@");
                if (PDFresult.length > 7) {
                    mode = 1;
//                    Log.e("Scanner", "Detected Mode 1");
                    return;
                }
            }


            for (FirebaseVisionText.TextBlock tb : text.getTextBlocks()) {
                for (FirebaseVisionText.Line l : tb.getLines()) {
                    if(l.getText().contains("<<")){
                        mode = 2;
//                        Log.e("Scanner", "Detected Mode 2");
                        return;
                    }
                }
            }
            mode = 0;
        }

        boolean validateFrontMode1(){
//            Log.e("Scanner", "Validating front mode 1");

            if(!frame.getBoundingBox().contains(face.getBoundingBox())) return false;
            if(!frame.getBoundingBox().contains(barcode.getBoundingBox())) return false;

            Log.e("BARCODE", barcode.getRawValue());
            String[] PDFresult = barcode.getRawValue().split("@");
            if (PDFresult.length > 7) {
                apellido = PDFresult[1];
                nombre = PDFresult[2];
                sexo = PDFresult[3];
                dni = PDFresult[4];
                fechaNacimiento = parseDateDDMMYYYY(PDFresult[6]);
                expiry = parseDateDDMMYYYY(PDFresult[7]);
                Calendar c = Calendar.getInstance();
                c.setTime(expiry);
                c.add(Calendar.YEAR, 15);
                expiry = c.getTime();
                tramite = PDFresult[0];
            }

            for (FirebaseVisionText.TextBlock tb : text.getTextBlocks()) {
                for (FirebaseVisionText.Line l : tb.getLines()) {
                    if(l.getText().replace(".","").contains(dni) && textIsAligned(tb)) {
                        ((ScanActivity)activity).checkDniIsOk();
                        startTime = new Date().getTime();

                        Bitmap faceBMP = Bitmap.createBitmap(image, face.getBoundingBox().left,face.getBoundingBox().top,face.getBoundingBox().width(), face.getBoundingBox().height());
                        image = Bitmap.createBitmap(image, frame.getBoundingBox().left,frame.getBoundingBox().top,frame.getBoundingBox().width(), frame.getBoundingBox().height());
                        storeFront(image);
                        storeFaceBitmap(faceBMP);
                        return true;
                    }
                }
            }

            return false;

        }

        boolean validateBackMode1(){
//            Log.e("Scanner", "Validating back mode 1");

            if(text==null || frame==null) return false;

            boolean foundDNI = false;
            List<FirebaseVisionText.Line> mrzLines = new ArrayList<>();

            for (FirebaseVisionText.TextBlock tb : text.getTextBlocks()) {
                if(tb.getBoundingBox().width() > frame.getBoundingBox().width()) continue;
                if(tb.getBoundingBox().width()*1.5<frame.getBoundingBox().width()) continue;
                if(!textIsAligned(tb)) break;
                for (FirebaseVisionText.Line l : tb.getLines()) {
                    if(l.getText().contains("<<")){
                        mrzLines.add(l);
                        mrzFoundOnce = true;


                        // Obtengo fecha vencimiento del mrz
                        try{
                            String line = l.getText();
                            if(isNumber(line.substring(0, 6))){
                                if("MF".contains(line.charAt(7) +"" )){
                                    try {
                                        expiry =  new SimpleDateFormat("yyMMdd").parse(line.substring(8, 14));
                                    }
                                    catch (Exception e){
                                    }
                                }
                            }
                        }
                        catch (Exception e){

                        }
                        // fin fecha vencimiento
                    }
                }
            }

//            for (FirebaseVisionText.TextBlock tb : text.getTextBlocks()) {
//                String t = tb.getText().toLowerCase();
//                if(!(t.contains("pulgar") || t.contains("cuil") || t.contains(dni.toLowerCase()) || t.contains("<")) || t.contains("ministro") || t.contains("domicilio") || t.contains("nacimiento") ){
//                    domicilio+=t + " ";
//                }
//            }


            for(FirebaseVisionText.Line l : mrzLines){
                if (!foundDNI && l.getText().replace("MO","M0").contains(dni + "<")){
                    foundDNI = true;
                    backDataEqualsFrontOnce = true;
                }
            }

            if(foundDNI && mrzLines.size()==3) {
                image = Bitmap.createBitmap(image, frame.getBoundingBox().left, frame.getBoundingBox().top, frame.getBoundingBox().width(), frame.getBoundingBox().height());
                storeBack(image);
                persistResult();
                return true;
            }


            return false;
        }

        boolean validateFrontMode2(){
//            Log.e("Scanner", "Validating front mode 2");

            if(!frame.getBoundingBox().contains(face.getBoundingBox())) return false;

            boolean foundDNI = false;
            boolean foundBithdayAndGender = false;
            List<FirebaseVisionText.Line> mrzLines = new ArrayList<>();

            for (FirebaseVisionText.TextBlock tb : text.getTextBlocks()) {
                if(!textIsAligned(tb)) break;
                for (FirebaseVisionText.Line l : tb.getLines()) {
                    if(l.getText().contains("<<")) mrzLines.add(l);
                    mrzFoundOnce = true;
                }
            }


            for(FirebaseVisionText.Line l : mrzLines){
                String line = l.getText().toString();
                if (!foundDNI && line.contains("IDARG")){
                    String dniAux = line.substring(5, l.getText().indexOf("<"));
                    if(line.contains("<" + getVerificationDigit(dniAux) + "<")){
                        dni = dniAux;
                        foundDNI = true;
                    }
                }
                if(!foundBithdayAndGender && line.contains("<<<") && line.length()>15){
                    if(isNumber(line.substring(0, 6))){
                        if("MF".contains(line.charAt(7) +"" )){
                            try {
                                sexo = line.substring(7, 8);
                                fechaNacimiento = new SimpleDateFormat("yyMMdd").parse(line.substring(0, 6));
                                expiry =  new SimpleDateFormat("yyMMdd").parse(line.substring(8, 14));
                                Log.e("ASDEXPIRY2", expiry.toString());
                                foundBithdayAndGender = true;

                            }
                            catch (Exception e){

                            }
                        }
                    }
                }
            }

            if(foundDNI && foundBithdayAndGender && mrzLines.size()==3) {
                Bitmap faceBMP = Bitmap.createBitmap(image, face.getBoundingBox().left,face.getBoundingBox().top,face.getBoundingBox().width(), face.getBoundingBox().height());
                image = Bitmap.createBitmap(image, frame.getBoundingBox().left,frame.getBoundingBox().top,frame.getBoundingBox().width(), frame.getBoundingBox().height());
                storeFront(image);
                storeFaceBitmap(faceBMP);
                ((ScanActivity)activity).checkDniIsOk();
                startTime = new Date().getTime();
                return true;
            }


            return false;
        }

        boolean validateBackMode2(){
//            Log.e("Scanner", "Validating back mode 2");

            if(barcode==null || frame==null) return false;
            if(frame.getBoundingBox().width()<barcode.getBoundingBox().width()*2) return false;
            String[] PDFresult = barcode.getRawValue().split("@");
            if (PDFresult.length > 7) {
                pdfFoundOnce = true;
                if(!dni.equalsIgnoreCase(PDFresult[1].replace(" ", ""))){
                   return false;
                }
                backDataEqualsFrontOnce = true;
                if (PDFresult.length < 10) return false;
                dni = PDFresult[1].replace(" ", "");
                if(!Character.isDigit(dni.charAt(0))) dni = dni.substring(1);
                apellido = PDFresult[4];
                nombre = PDFresult[5];
                sexo = PDFresult[8];
                fechaNacimiento = parseDateDDMMYYYY(PDFresult[7]);
                tramite = PDFresult[10];

                image = Bitmap.createBitmap(image, frame.getBoundingBox().left, frame.getBoundingBox().top, frame.getBoundingBox().width(), frame.getBoundingBox().height());
                storeBack(image);
                persistResult();
                return  true;
            }

            return false;

        }

        boolean textIsAligned(FirebaseVisionText.TextBlock line){
            double angle = Math.atan2(line.getCornerPoints()[1].y - line.getCornerPoints()[0].y, line.getCornerPoints()[1].x - line.getCornerPoints()[0].x) * 180 / Math.PI;
            boolean result =  Math.abs(angle) < 10;
            return result;
        }

        void storeFaceBitmap(Bitmap b){
            faceResult = b;
        }

        void storeFront(Bitmap b){
            frontResult = b.copy(b.getConfig(), b.isMutable());
            Persona.dniFront = frontResult;
        }

        void storeBack(Bitmap b){
            backResult = b;
        }
    }

    void persistResult(){
        Persona persona = new Persona();
        Persona.dniBack = backResult;
        Persona.face = faceResult;
        persona.nombre = nombre;
        persona.apellido = apellido;
        if(!Character.isDigit(dni.charAt(0))) dni = dni.substring(1);
        persona.dni = dni;
        persona.nacimiento = fechaNacimiento;
        persona.sexo = sexo;
        persona.tramite = tramite;
        ((ScanActivity)activity).stopScanning(persona);
    }

    boolean isTimeout(){
        if(preventTimeOut) return false;
        if(startTime + maxScanTime < new Date().getTime()){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ScanActivity)activity).stopScanningTimeout();
                    busy = true;
                }
            });
            return  true;
        }
        return false;
    }

    boolean isNumber(String text){
        if(text.length()<1) return false;
        for(int i =0; i<text.length(); i++){
            if(!Character.isDigit(text.charAt(i))) return false;
        }
        return true;
    }
}




