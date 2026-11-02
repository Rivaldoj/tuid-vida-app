package com.autenticar.tuid.v2;

import android.app.Activity;
import com.google.firebase.messaging.FirebaseMessaging;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.autenticar.tuid.AmplitudeManager.AmplitudeManager;
import com.autenticar.tuid.BuildConfig;
import com.autenticar.tuid.MainApp;
import com.autenticar.tuid.R;
import com.autenticar.tuid.services.TuidService;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.autenticar.tuid.utils.CountriesUtil;
import com.autenticar.tuid.utils.HelperFunctions;
import com.autenticar.tuid.v2.components.SoporteDialog;
import com.autenticar.tuid.v2.model.Phone;
import com.autenticar.tuid.v2.model.TelefonoCodigoPais;
import com.autenticar.tuid.v2.services.GetCuandoCobroRequest;
import com.autenticar.tuid.v2.services.GetCuandoCobroResponse;
import com.autenticar.tuid.v2.services.ManualRegisterRequest;
import com.autenticar.tuid.v2.services.ManualRegisterResponse;
import com.autenticar.tuid.v2.services.ManualRegisterSendCodeRequest;
import com.autenticar.tuid.v2.services.ManualRegisterSendCodeResponse;
import com.autenticar.tuid.v2.services.Pago;
import com.autenticar.tuid.v2.utils.LoadingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.autenticar.tuid.v2.FeDeVidaActivity.GESTURE_TO_DETECT;

public class RegistroManualActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;
    private static final int REQUEST_LIVE_PREVIEW = 151;
    private static final int CAMERA_REQUEST_FONT = 1888;
    private static final int CAMERA_REQUEST_BACK = 1889;

    public int step = 1;
    Bitmap frente;
    Bitmap dorso;
    Bitmap selfie;
    String token="";
    String version = BuildConfig.VERSION_CODE + "(" + BuildConfig.VERSION_NAME + ")";
    String os = "Android";
    Phone phone = new Phone();

    public int id_usuario = -1;

    String mImagePath;
    Uri imageUri;

    @Override
    public void onCreate(Bundle s){
        super.onCreate(s);
        Uri data = this.getIntent().getData();
        if (data != null && data.isHierarchical()) {
            String uri = this.getIntent().getDataString();
            String param1 = data.getQueryParameter("param1");
        }

        initFirebase();

        try {
            if(getIntent().getBooleanExtra("scanFailed", false)) step = 0;
        }
        catch (Exception e){
            Log.e("EX", e.getMessage());
        }

        setStep(step);
    }

    private void initFirebase(){
        try {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (task.isSuccessful()) {
                                token = task.getResult();
                                Log.e("Token",token);
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e("EX", "FirebaseInstanceId.getInstance error: " + e);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        setStep(step);
    }

    void getImage(int i){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 100){
            Uri uri = data.getData();
            ((ImageView)findViewById(R.id.frente)).setImageURI(uri);
            frente = scaleBitmap(((BitmapDrawable)((ImageView)findViewById(R.id.frente)).getDrawable()).getBitmap());
        }
        if (resultCode == RESULT_OK && requestCode == 101){
            Uri uri = data.getData();
            ((ImageView)findViewById(R.id.dorso)).setImageURI(uri);
            dorso = scaleBitmap(((BitmapDrawable)((ImageView)findViewById(R.id.dorso)).getDrawable()).getBitmap());
        }
        if (resultCode == RESULT_OK && requestCode == REQUEST_LIVE_PREVIEW){
            selfie = ((MainApp) getApplication()).GetSelfie();
            setStep(5);
        }
        if (requestCode == CAMERA_REQUEST_FONT && resultCode == Activity.RESULT_OK) {
            ((ImageView)findViewById(R.id.frente)).setImageURI(imageUri);
            frente = scaleBitmap(((BitmapDrawable)((ImageView)findViewById(R.id.frente)).getDrawable()).getBitmap());
        }

        if (requestCode == CAMERA_REQUEST_BACK && resultCode == Activity.RESULT_OK) {
            ((ImageView)findViewById(R.id.dorso)).setImageURI(imageUri);
            dorso =scaleBitmap(((BitmapDrawable)((ImageView)findViewById(R.id.dorso)).getDrawable()).getBitmap());
        }
    }

    public byte[] getBitmapBytes(Bitmap bitmap) {
//        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageInByte = baos.toByteArray();
        return imageInByte;
    }

    protected void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(message);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    @Override
    public void onBackPressed(){
        if(step<=1 || step==7) super.onBackPressed();
        else setStep(--step);
    }

    void setStep(int step){
//        Log.e("STEP","Step: " + step);
        this.step = step;
        if(step == 0)setStep0();
        if(step == 1)setStep1();
        if(step == 2)setStep2();
        if(step == 3)setStep3();
        if(step == 4)setStep4();
        if(step == 5)setStep5();
        if(step == 6)setStep6();
        if(step == 7)setStep7();
    }

    void setStep0(){
        setContentView(R.layout.v2_registro_manual_activity_paso_0);
        findViewById(R.id.continuar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setStep(1);
            }
        });

        findViewById(R.id.ayuda).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
            }
        });
    }

    void setStep1(){
        // terminos y condiciones
        setContentView(R.layout.v2_registro_manual_activity_paso_1);
        CheckBox politicas = findViewById(R.id.checkbox_politicas);
        CheckBox terminos = findViewById(R.id.checkbox_terminos);
        findViewById(R.id.continuar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(politicas.isChecked() && terminos.isChecked()) setStep(2);
            }
        });

        findViewById(R.id.checkbox_terminos_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelperFunctions.showDialog(R.string.nav_titulo_termino_condiciones, R.string.nav_content_termino_condiciones, 1, view.getContext(), RegistroManualActivity.this);
            }
        });

        findViewById(R.id.checkbox_politicas_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelperFunctions.showDialog(R.string.nav_titulo_politicas, R.string.nav_content_politicas, 1, view.getContext(), RegistroManualActivity.this);
            }
        });
    }

    void setStep2(){
        // FRENTE
        setContentView(R.layout.v2_registro_manual_activity_paso_2);
        if(frente!=null)((ImageView)findViewById(R.id.frente)).setImageBitmap(frente);

        findViewById(R.id.continuar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(frente!=null) setStep(++step);
            }
        });

        findViewById(R.id.subir_imagen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImage(100);
            }
        });

        findViewById(R.id.tomar_foto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture(CAMERA_REQUEST_FONT);
            }
        });

    }

    void setStep3(){
        // DORSO
        setContentView(R.layout.v2_registro_manual_activity_paso_3);
        if(dorso!=null)((ImageView)findViewById(R.id.dorso)).setImageBitmap(dorso);
        findViewById(R.id.continuar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dorso!=null)setStep(++step);
            }
        });

        findViewById(R.id.subir_imagen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImage(101);
            }
        });

        findViewById(R.id.tomar_foto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture(CAMERA_REQUEST_BACK);
            }
        });
    }

    void setStep4(){
        // PRE GESTOS
        setContentView(R.layout.v2_registro_manual_activity_paso_4);

        findViewById(R.id.continuar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), FeDeVidaActivity.class);
                intent.putExtra("manual", true);
                intent.putExtra(GESTURE_TO_DETECT, FeDeVidaActivity.DETECTAR_PESTANEO_CLOSED_EYES);
                startActivityForResult(intent,REQUEST_LIVE_PREVIEW);
            }
        });
    }

    void setStep5(){
        //validar telefono
        setContentView(R.layout.v2_registro_manual_activity_paso_5);
        initPhoneSpinner();

        Spinner spinner_phone_country = findViewById(R.id.text_content_info_phone_cod_pais_spinner);
        EditText text_content_info_phone_area = findViewById(R.id.text_content_info_phone_cod_area);
        EditText text_content_info_phone_text = findViewById(R.id.text_content_info_phone_text);

        findViewById(R.id.continuar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Phone telefono = new Phone();
                telefono.country = ((TelefonoCodigoPais) spinner_phone_country.getSelectedItem()).codigo;
                telefono.area = String.valueOf(text_content_info_phone_area.getText());
                telefono.phone = String.valueOf(text_content_info_phone_text.getText());
                if(telefono.isValid()){
                    phone = telefono;
                    submitInfo();
                }
            }
        });
    }

    void setStep6(){
        // codigo sms
        setContentView(R.layout.v2_registro_manual_activity_paso_6);
        ((ImageView)findViewById(R.id.imgEmailSms)).setBackgroundResource(R.drawable.enviar_sms);
        ((TextView)findViewById(R.id.textCodigoEnvio)).setText("Se ha enviado el CODIGO mediante un mensaje de texto al número " + phone.country + " " + phone.area + " " + phone.phone);
        findViewById(R.id.textCodigoEnvioAviso).setVisibility(View.INVISIBLE);
        findViewById(R.id.continuar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText code_et = findViewById(R.id.editCodigoValidacion);
                String code = code_et.getText().toString();
                if(code.length()==4){
                    submitCode(code);
                }

            }
        });
    }

    void setStep7(){
        // SUCCESS
        setContentView(R.layout.v2_registro_manual_activity_paso_7);
        findViewById(R.id.continuar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    void submitInfo(){
        findViewById(R.id.continuar).setEnabled(false);
        LoadingDialog.getInstance().show(this, "Aguarde un momento, procesando la información");
        final ManualRegisterRequest request = new ManualRegisterRequest();
        request.front = b64Encode(getBitmapBytes( frente));
        request.back = b64Encode(getBitmapBytes(dorso));
        request.selfie = b64Encode(getBitmapBytes(selfie));
        request.phone = phone;
        request.os = os;
        request.version = version;
        request.id_device = token;
        JSONObject req = new JSONObject();
        try{
            JSONObject phone = new JSONObject();
            phone.put("country", request.phone.country);
            phone.put("area", request.phone.area);
            phone.put("phone", request.phone.phone);
            req.put("front", request.front);
            req.put("back", request.back);
            req.put("selfie", request.selfie);
            req.put("phone", phone);
            req.put("os", request.os);
            req.put("version", request.version);
            req.put("id_device", request.id_device);
            Log.e("REQUEST", req.toString(2));
        }
        catch (Exception e){

        }
        TuidService service = ServiceGenerator.createService(TuidService.class);
        Call<ManualRegisterResponse> callUser = service.registroManual(request);

        callUser.enqueue(new Callback<ManualRegisterResponse>() {
            @Override
            public void onResponse(Call<ManualRegisterResponse> call, Response<ManualRegisterResponse> response) {
                findViewById(R.id.continuar).setEnabled(true);
                LoadingDialog.getInstance().hide();

                if(response.isSuccessful() && response.body().IsOk() && response.body().id>0){
                    Log.e("Reponse","success");
                    id_usuario = response.body().id;
                    setStep(6);
                    return;
                }
                Log.e("Reponse","fail");
//                HelperFunctions.ShowAlertWithContact(RegistroManualActivity.this, response.body().Message, 1,"DONTFINISHACTIVITY");
                showError(response.body().Message, response.body().url);
            }

            @Override
            public void onFailure(Call<ManualRegisterResponse> call, Throwable t) {
                LoadingDialog.getInstance().hide();
                findViewById(R.id.continuar).setEnabled(true);
                HelperFunctions.ShowAlertWithContact(RegistroManualActivity.this, getString(R.string.no_hay_conexion_a_internet), 1, "0");
                showError("Error con servicio", "");
                Log.e("ERROR", "FAIL");
            }

        });
    }

    void submitCode(String code){
        LoadingDialog.getInstance().show(this, "Aguarde un momento, procesando la información");
        final ManualRegisterSendCodeRequest request = new ManualRegisterSendCodeRequest();
        try {
            LoadingDialog.getInstance().hide();
            request.code = Integer.parseInt(code);
            request.usuario_id = id_usuario;
        }
        catch (Exception e){
            Log.e("EX", e.getMessage());
            showError("Error", "");
            return;
        }
        TuidService service = ServiceGenerator.createService(TuidService.class);
        Call<ManualRegisterSendCodeResponse> callUser = service.registroManualSendCode(request);

        callUser.enqueue(new Callback<ManualRegisterSendCodeResponse>() {
            @Override
            public void onResponse(Call<ManualRegisterSendCodeResponse> call, Response<ManualRegisterSendCodeResponse> response) {
                LoadingDialog.getInstance().hide();
                if(response.body().Response.equalsIgnoreCase("OK")){
                    setStep(7);
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegistroManualActivity.this);
                    builder.setTitle("Validar Telefono");
                    builder.setMessage(response.body().Message);
                    builder.create().show();

                }
            }

            @Override
            public void onFailure(Call<ManualRegisterSendCodeResponse> call, Throwable t) {
                LoadingDialog.getInstance().hide();
                HelperFunctions.ShowAlertWithContact(RegistroManualActivity.this, getString(R.string.no_hay_conexion_a_internet), 1, "0");
                showError("Error con servicio", "");
                Log.e("ERROR", "FAIL");
            }

        });
    }

    void initPhoneSpinner(){
        List<TelefonoCodigoPais> codigos_pais = CountriesUtil.getCodigosPais(this);

        Spinner spinner = findViewById(R.id.text_content_info_phone_cod_pais_spinner);
        ArrayAdapter<TelefonoCodigoPais> adapter = new ArrayAdapter<TelefonoCodigoPais>(this, android.R.layout.simple_spinner_item, codigos_pais);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView) adapterView.getChildAt(0)).setText(((TelefonoCodigoPais)adapterView.getItemAtPosition(i)).codigo);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        for(TelefonoCodigoPais t: codigos_pais){
            if(t.pais.equalsIgnoreCase("Argentina")){
                spinner.setSelection(adapter.getPosition(t));
            }
        }

    }

    private void takePicture(int request){ //you can call this every 5 seconds using a timer or whenever you want
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg";
                photoFile = new File(getExternalFilesDir(null), fileName);
            } catch (Exception ignored) {}
            if (photoFile != null) {
                mImagePath = photoFile.getAbsolutePath();
                imageUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", photoFile);;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, request);
            }
        }
    }

    String b64Encode(byte[] data){
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    void showError(String mensaje, String whatsapp){
//        Log.e("ASD", mensaje);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(mensaje);
        boolean irAFeDeVida = mensaje.equalsIgnoreCase("El beneficiario ya se encuentra registrado");
//        builder.setMessage(mensaje);
        builder.setItems(new CharSequence[]{"Contactar soporte", "Ir a whatsapp",irAFeDeVida?"Ir a fe de vida":"OK"},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which) {
                            case 0:
                                new SoporteDialog( RegistroManualActivity.this, RegistroManualActivity.this,R.string.nav_content_contacto);
                                break;
                            case 1:
                                if(whatsapp.length()>5)startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(whatsapp)));
                                break;
                            case 2:
                                dialog.dismiss();
                                if(irAFeDeVida){
                                    HomeActivity.irAFeDeVidaOnResume = true;
                                    if(HomeActivity.self!=null) HomeActivity.self.finish();
                                    Intent i = new Intent(RegistroManualActivity.this, HomeActivity.class);
                                    startActivity(i);
                                }
                                finish();
                                break;
                        }
                    }
                });
        builder.create().show();

    }

    public Bitmap scaleBitmap(Bitmap bm){
        Log.e("SCALIING", "RESULT: " + bm.getWidth() + ": " + bm.getHeight() + " : " + byteSizeOf(bm));
        if(byteSizeOf(bm)<15000000){
            Log.e("SCALING...", "READY");
            return bm;
        }
        int width = bm.getWidth();
        int height = bm.getHeight();
        int dstWidth = (int)(width*0.8f);
        int dstHeight = (int)(height*0.8f);
        Bitmap dstBitmap = Bitmap.createScaledBitmap(bm, dstWidth, dstHeight, true);
//        bm = Bitmap.createScaledBitmap(bm, width, height, false);
        Log.e("SCALE", "RESULT: " + dstBitmap.getWidth() + ": " + dstBitmap.getHeight() + " : " + byteSizeOf(dstBitmap));
        return scaleBitmap(dstBitmap);
    }

    public int byteSizeOf(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return bitmap.getAllocationByteCount();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        } else {
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }

    public  Bitmap scaleBitmap2(Bitmap bm) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        int scale_size = width;


        if (width > height) {
            // landscape
            float ratio = (float) width / height;
            width = scale_size;
            height = (int) (height / ratio);
        } else if (height > width) {
            // portrait
            float ratio = (float) height / width;
            height = scale_size;
            width = (int) (width / ratio);
        } else {
            // square
            height = scale_size;
            width = scale_size;
        }

        if(width<scale_size || height<scale_size) return bm;
        
        bm = Bitmap.createScaledBitmap(bm, width, height, false);
        Log.e("SCALE", "RESULT: " + width + ": " + height + " : " + (bm.getRowBytes()/1024)/1024);
        return bm;
    }
}
