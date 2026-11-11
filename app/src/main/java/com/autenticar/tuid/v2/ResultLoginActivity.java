package com.autenticar.tuid.v2;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.autenticar.tuid.MainApp;
import com.autenticar.tuid.R;
import com.autenticar.tuid.RateAppDialog;
import com.autenticar.tuid.model.ConfirmationRequest;
import com.autenticar.tuid.model.ReconfirmationRequest;
import com.autenticar.tuid.model.ReconfirmationResponse;
import com.autenticar.tuid.model.RevalidationRequest;
import com.autenticar.tuid.model.RevalidationResponse;
import com.autenticar.tuid.model.ValidationResponse;
import com.autenticar.tuid.model.itemError;
import com.autenticar.tuid.services.TuidService;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.autenticar.tuid.utils.CountriesUtil;
import com.autenticar.tuid.utils.HelperFunctions;
import com.autenticar.tuid.v2.components.NovedadesDialog;
import com.autenticar.tuid.v2.model.Phone;
import com.autenticar.tuid.v2.model.TelefonoCodigoPais;
import com.autenticar.tuid.v2.services.ValidationRequest2;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.autenticar.tuid.utils.HelperFunctions.CreateAlert;
import static com.autenticar.tuid.utils.HelperFunctions.hideProgressDialog;
import static com.autenticar.tuid.utils.HelperFunctions.mProgressDialog;

public class ResultLoginActivity extends TuIdActivity{

    CircleImageView profileImage;
    Bitmap imagen_ico;
    private TextView fe_vida;
    private ImageView img_fe_vida;
    public ArrayList<itemError> errorItems;
    private TextView main_cuil;
    private TextView main_email;
    private TextView main_telefono;
    private ListView list_error;
    private static LoginResultErrorsListAdapter MainListValidationAdapter;

    String email_1 = "";
    String email_2 = "";
    String telefono_1 = "";
    private boolean newDialog = false;
    private int mode = -1;
    private Dialog dialog = null;

    public static String dni ="";
    public static String sexo = "";
    public static String cuil = "";   // se completa cuando la Fe de Vida se hizo identificando por CUIL
    boolean avoidRateAppDialog = false;

    @Override
    public void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.result_login_activity);
        com.autenticar.tuid.usuarios.Audio.play(this, R.raw.a26);

        check_novedades();
        initView();

        checkPendingTasks();
    }

    void check_novedades(){
        String image = getIntent().getStringExtra("novedad_img");
        String link = getIntent().getStringExtra("novedad_link");
        if(image!=null && image.length()>0 && image.startsWith("http")){
            avoidRateAppDialog=true;
            new NovedadesDialog(this, image, link);
        }
    }

    void checkPendingTasks(){
        if(getIntent().hasExtra("pendingTasks") && getIntent().hasExtra("token")){
            try{
                Log.e("ASD", "Result activity trae pending tasks y token!");
                String token = getIntent().getStringExtra("token");
                JSONArray ar = new JSONArray(getIntent().getStringExtra("pendingTasks"));
                Intent intent = new Intent(HomeActivity.self, TareasPendientesActivity.class);
                intent.putExtra("pendingTasks", ar.toString());
                intent.putExtra("token", token);
                startActivity(intent);
            }
            catch (Exception e){
                Log.e("EX", e.getMessage());
            }
        }

    }

    void initView(){
        findViewById(R.id.result_login_activity_get_certificate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCertificate();
            }
        });
        
        final String banco = getIntent().getStringExtra("Banco").toUpperCase();
        if (banco != null) {
            LayoutInflater flater = getLayoutInflater();
            LinearLayout lo = findViewById(R.id.result_login_activity_entidades_lo);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0,20,0,0);
            for(String ent: banco.split("  ")){
                if(ent.replaceAll(" ", "").length()==0 ) continue;
                View v = flater.inflate(R.layout.result_entidad_lo, null);
                ((TextView)v.findViewById(R.id.main_aviso_instituto)).setText(ent.trim());
                lo.addView(v,lp);
            }
//            ((TextView) findViewById(R.id.main_aviso_instituto)).setText(banco);
        } else {
            ((TextView) findViewById(R.id.main_aviso_instituto)).setText("Por Definir");
        }

        TextView main_aviso_certificado = (TextView) findViewById(R.id.main_aviso_certificado);
        if (getIntent().getStringExtra("Fecha_valida").equals("")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd LLL yyyy", Locale.getDefault());
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_YEAR, 30);
            String fecha = String.valueOf(dateFormat.format(calendar.getTime()));
            System.out.println(" calendar.getTime() " + fecha);
            main_aviso_certificado.setText(fecha);

            main_aviso_certificado.setVisibility(View.GONE);
            findViewById(R.id.main_aviso_certificado_label).setVisibility(View.GONE);

        } else {
            String fecha_valida = getIntent().getStringExtra("Fecha_valida");
            try{
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
                fecha_valida= sdf2.format(sdf.parse(fecha_valida));
            }
            catch (Exception e){

            }
            main_aviso_certificado.setText(fecha_valida);
        }


        fe_vida = (TextView) findViewById(R.id.fe_vida);
        img_fe_vida = (ImageView) findViewById(R.id.img_fe_vida);
        errorItems = new ArrayList<>();


        list_error = findViewById(R.id.lista_error);
        MainListValidationAdapter = new LoginResultErrorsListAdapter(errorItems, activity, String.valueOf(getIntent().getIntExtra("userID", -1)), activity);
        list_error.setAdapter(MainListValidationAdapter);
        
        if (getIntent().getStringExtra("UserName") != null) {
            ((TextView) findViewById(R.id.main_name)).setText(getIntent().getStringExtra("UserName"));
        } else {
            ((TextView) findViewById(R.id.main_name)).setVisibility(View.GONE);
        }
        main_cuil = (TextView) findViewById(R.id.main_cuil);
        if (getIntent().getStringExtra("Cuil") != null) {
            main_cuil.setText(getIntent().getStringExtra("Cuil"));
        } else {
            main_cuil.setText("No disponible");
            img_fe_vida.setBackground(getDrawable(R.drawable.ic_alert));
            fe_vida.setTextColor(getColor(R.color.colorRed));
            fe_vida.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            errorItems.add(new itemError("Su fe de vida no ha sido registrada correctamente", "CUIL"));
        }
        main_email = (TextView) findViewById(R.id.main_email);
        if (getIntent().getStringExtra("Email") != null) {
            main_email.setText(getIntent().getStringExtra("Email"));
        } else {
            main_email.setText("No disponible");
            img_fe_vida.setBackground(getDrawable(R.drawable.ic_alert));
            fe_vida.setTextColor(getColor(R.color.colorRed));
            fe_vida.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            errorItems.add(new itemError("Se debe validar su email", "EMAIL"));
        }
        main_telefono = (TextView) findViewById(R.id.main_telefono);
        if (getIntent().getStringExtra("Telefono") != null) {
            main_telefono.setText(getIntent().getStringExtra("Telefono"));
        } else {
            main_telefono.setText("No disponible");
            img_fe_vida.setBackground(getDrawable(R.drawable.ic_alert));
            fe_vida.setTextColor(getColor(R.color.colorRed));
            fe_vida.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            errorItems.add(new itemError("Se debe validar su teléfono", "TELEFONO"));
        }
        if (errorItems.size() == 0){
            if(!avoidRateAppDialog) rateApp();
        }
        else{
            // FALTAN VALIDAR DATOS
            for(itemError e : errorItems){
                if(e.getType().equals("TELEFONO")){
                    // si falta vaildar telefono llevar a pantalla completa
                    editphone(null);
                }
            }
        }
        setProfileImage();
    }

    void setProfileImage(){
        profileImage = (CircleImageView) findViewById(R.id.ico_perfil);
        imagen_ico = ((MainApp) getApplication()).GetSelfie();
        if (imagen_ico != null) {
            profileImage.setImageBitmap(imagen_ico);
        }
    }

    public void rateApp(){
        final Context context = this;
        try{
            SharedPreferences sp = this.getSharedPreferences("TuIDVidaSP", MODE_PRIVATE);
            if(!sp.contains("AppRated")){
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            new RateAppDialog(context);
                        }
                        catch (Exception e){

                        }
                    }
                }, 5000);

            }
        }
        catch (Exception e){

        }
    }

    public void editmail(View v){
        if(mode==2)newDialog=false;
        actualizarData(1);
    }

    public void editphone(View v){
        telefono_1 ="";
        if(mode==1) newDialog=false;
        actualizarData(2);
    }


    public void actualizarData(final int mode) {
        this.mode = mode;
        if (!newDialog) {
            newDialog=true;
            dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.dialog_actualizar2);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            TextView text_title_info_empresarialdialog = (TextView) dialog.findViewById(R.id.text_title_info_empresarial);
            ImageView btn_cancel = (ImageView) dialog.findViewById(R.id.btn_cancel);
            ImageView btn_return = (ImageView) dialog.findViewById(R.id.btn_return);
            final EditText text_email_1 = (EditText) dialog.findViewById(R.id.text_email_1);
            final EditText text_email_2 = (EditText) dialog.findViewById(R.id.text_email_2);

            final EditText text_content_info_phone_text = (EditText) dialog.findViewById(R.id.text_content_info_phone_text);
            final EditText text_content_info_phone_area = (EditText) dialog.findViewById(R.id.text_content_info_phone_cod_area);
            final Spinner spinner_phone_country = dialog.findViewById(R.id.text_content_info_phone_cod_pais_spinner);

            final EditText edit_validacion_email = (EditText) dialog.findViewById(R.id.edit_validacion_email);
            final EditText edit_validacion_telefono = (EditText) dialog.findViewById(R.id.edit_validacion_telefono);

            if(mode==1){
                dialog.findViewById(R.id.fragmet_actualizar_data2_telefono_container).setVisibility(View.GONE);
            }
            if(mode==2){
                text_email_1.setVisibility(View.GONE);
                text_email_2.setVisibility(View.GONE);
                dialog.findViewById(R.id.text_info_email_1_lo).setVisibility(View.GONE);
                dialog.findViewById(R.id.text_info_email_2_lo).setVisibility(View.GONE);
                edit_validacion_email.setVisibility(View.GONE);
            }

            final Button btn_update_code = (Button) dialog.findViewById(R.id.btn_update_code);

            text_title_info_empresarialdialog.setText("Actualizar " + (mode==1?"Email":"Telefono"));
            btn_return.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.findViewById(R.id.fragment_actualizar).setVisibility(View.VISIBLE);
                    dialog.findViewById(R.id.fragment_validate).setVisibility(View.GONE);
                }
            });
            Button btn_update = (Button) dialog.findViewById(R.id.btn_update);
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            btn_update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mode==2) ((TextView)dialog.findViewById(R.id.info_envio)).setText("Se envió un SMS con el código de validación.");
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    email_1 = String.valueOf(text_email_1.getText());
                    email_2 = String.valueOf(text_email_2.getText());

                    Phone phone = new Phone();
                    if(mode==2) {
                        phone.country = ((TelefonoCodigoPais) spinner_phone_country.getSelectedItem()).codigo;
                        phone.area = String.valueOf(text_content_info_phone_area.getText());
                        phone.phone = String.valueOf(text_content_info_phone_text.getText());
                        if(phone.country.length()>0 && phone.area.length()>0 && phone.phone.length()>0) telefono_1 = phone.country + phone.area + phone.phone;
                    }

                    if(!email_1.equals("") && telefono_1.equals("")){
                        if(!HelperFunctions.isValidEmail(email_1)){
                            CreateAlert(activity, "Alerta", getString(R.string.corre_invalido)).show();
                        }else if (!email_1.trim().equals(email_2.trim())) {
                            CreateAlert(activity, "Alerta", getString(R.string.validacion_conirmacion_email)).show();
                        }else{
                            sendUpdateMail();
                            if(email_1.equals("")){
                                edit_validacion_email.setVisibility(View.GONE);
                                ((TextView)dialog.findViewById(R.id.text_code_email)).setVisibility(View.GONE);
                            }
                            if(telefono_1.equals("")){
                                edit_validacion_telefono.setVisibility(View.GONE);
                                ((TextView)dialog.findViewById(R.id.text_code_sms)).setVisibility(View.GONE);
                            }
                        }
                    }else if(email_1.equals("") && !telefono_1.equals("")){
                        if(telefono_1.length() < 8){
                            CreateAlert(activity, "Alerta", getString(R.string.telefono_invalido)).show();
                        }else{
                            sendUpdatePhone();
                            if(email_1.equals("")){
                                edit_validacion_email.setVisibility(View.GONE);
                                ((TextView)dialog.findViewById(R.id.text_code_email)).setVisibility(View.GONE);
                            }
                            if(telefono_1.equals("")){
                                edit_validacion_telefono.setVisibility(View.GONE);
                                ((TextView)dialog.findViewById(R.id.text_code_sms)).setVisibility(View.GONE);
                            }
                        }
                    }
                }
            });


            btn_update_code.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (edit_validacion_telefono.getText().length() == 4 || edit_validacion_email.getText().length() == 4) {
                        showProgressDialog("Enviando código de validación...");
                        final TuidService service = ServiceGenerator.createService(TuidService.class);
                        final ReconfirmationRequest request_reconfirmation = new ReconfirmationRequest();
                        request_reconfirmation.userID = String.valueOf(getIntent().getIntExtra("userID", -1));
                        request_reconfirmation.CodeTel = String.valueOf(edit_validacion_telefono.getText());
                        request_reconfirmation.CodeEmail = String.valueOf(edit_validacion_email.getText());
                        final Call<ReconfirmationResponse> callUserReconfirmation = service.reconfirmation(request_reconfirmation);
                        callUserReconfirmation.enqueue(new Callback<ReconfirmationResponse>() {
                            @Override
                            public void onResponse(Call<ReconfirmationResponse> call, Response<ReconfirmationResponse> response) {
                                hideProgressDialog();
                                if (response.isSuccessful()) {
                                    if (response.body().IsOk()) {
                                        ReconfirmationResponse reconfirmationResponse = response.body();

                                        //TELEFONO Y MAIL
                                        if(!telefono_1.equals("") && !email_1.equals("")) {
                                            if(reconfirmationResponse.Email.equals("true") && reconfirmationResponse.Telefono.equals("true")) {
                                                ((TextView) activity.findViewById(R.id.main_telefono)).setText(telefono_1);
                                                ((TextView) activity.findViewById(R.id.main_email)).setText(email_1);
                                                newDialog = false;
                                                HelperFunctions.ShowAlert(activity, "El cambió de Email y Número teléfonico se realizó correctamente. ");
                                                MainListValidationAdapter.removeErrorFromList("EMAIL");
                                                MainListValidationAdapter.removeErrorFromList("TELEFONO");
                                                dialog.dismiss();
                                            }else if(!reconfirmationResponse.Email.equals("true") && reconfirmationResponse.Telefono.equals("true")){
                                                newDialog=true;
                                                ((TextView) activity.findViewById(R.id.main_telefono)).setText(telefono_1);
                                                edit_validacion_telefono.setText("");
                                                edit_validacion_telefono.setVisibility(View.GONE);
                                                ((TextView)dialog.findViewById(R.id.text_code_sms)).setText("Su código SMS fue validado correctamente");
                                                MainListValidationAdapter.removeErrorFromList("TELEFONO");
                                            }else if(reconfirmationResponse.Email.equals("true") || !reconfirmationResponse.Telefono.equals("true")){
                                                newDialog=true;
                                                ((TextView) activity.findViewById(R.id.main_email)).setText(email_1);
                                                edit_validacion_email.setText("");
                                                ((TextView)dialog.findViewById(R.id.text_code_email)).setText("Su Email fue validado correctamente");
                                                edit_validacion_email.setVisibility(View.GONE);
                                                MainListValidationAdapter.removeErrorFromList("EMAIL");
                                            }

                                            //NI TELEFONO NI MAIL
                                        }else if(telefono_1.equals("") && email_1.equals("")) {
                                            HelperFunctions.ShowAlert(activity, "Ocurrio un problema con la verificación");
                                            // TELEFONO
                                        }else if(!telefono_1.equals("")  && email_1.equals("")) {
                                            if(reconfirmationResponse.Telefono.equals("true")){
                                                newDialog=false;
                                                ((TextView) activity.findViewById(R.id.main_telefono)).setText(telefono_1);
                                                edit_validacion_telefono.setText("");
                                                edit_validacion_telefono.setVisibility(View.GONE);
                                                dialog.dismiss();
                                                ((TextView)dialog.findViewById(R.id.text_code_sms)).setText("Su código SMS fue validado correctamente");
                                                MainListValidationAdapter.removeErrorFromList("TELEFONO");
                                            }else{
                                                HelperFunctions.ShowAlert(activity, "Código suministrado no es valido ");
                                            }

                                            //EMAIL
                                        }else if(telefono_1.equals("") && !email_1.equals("")) {
                                            if(reconfirmationResponse.Email.equals("true")){
                                                newDialog=false;
                                                ((TextView) activity.findViewById(R.id.main_email)).setText(email_1);
                                                edit_validacion_email.setText("");
                                                ((TextView)dialog.findViewById(R.id.text_code_email)).setText("Su Email fue validado correctamente");
                                                edit_validacion_email.setVisibility(View.GONE);
                                                MainListValidationAdapter.removeErrorFromList("EMAIL");
                                                dialog.dismiss();
                                            }else{
                                                HelperFunctions.ShowAlert(activity, "Código suministrado no es valido ");
                                            }
                                        }
                                    } else {
                                        newDialog=true;
                                        HelperFunctions.ShowAlert(activity, response.body().Message);
                                    }
                                } else {
                                    newDialog=true;
                                    HelperFunctions.ShowAlert(activity, "Ocurrio un error con la validación ");
                                }
                            }

                            @Override
                            public void onFailure(Call<ReconfirmationResponse> call, Throwable t) {
                                hideProgressDialog();
                            }
                        });
                    }
                }
            });
            dialog.show();
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }else {
            try {
                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }catch (Exception e){
                Log.e("EX", " dialog.show(): "+e);
            }
        }
        if(mode==2) initCodPaisSpinner(dialog);
    }

    // Antes los errores del servidor o de red se ignoraban en silencio y la pantalla parecia colgada.
    private void mostrarErrorActualizacion(String mensaje) {
        String texto = (mensaje != null && !mensaje.trim().isEmpty() && !"chance".equals(mensaje))
                ? mensaje
                : "No se pudo actualizar los datos. Verificá tu conexión e intentá nuevamente.";
        HelperFunctions.ShowAlert(activity, texto);
    }

    private  void sendUpdatePhone(){
        final TuidService service = ServiceGenerator.createService(TuidService.class);
        final ValidationRequest2 request = new ValidationRequest2();

        final EditText text_content_info_phone_text = (EditText) dialog.findViewById(R.id.text_content_info_phone_text);
        final EditText text_content_info_phone_area = (EditText) dialog.findViewById(R.id.text_content_info_phone_cod_area);
        final Spinner spinner_phone_country = dialog.findViewById(R.id.text_content_info_phone_cod_pais_spinner);

        Phone phone = new Phone();
        if(mode==2) {
            phone.country = ((TelefonoCodigoPais) spinner_phone_country.getSelectedItem()).codigo;
            phone.area = String.valueOf(text_content_info_phone_area.getText());
            phone.phone = String.valueOf(text_content_info_phone_text.getText());
            if(phone.country.length()>0 && phone.area.length()>0 && phone.phone.length()>0) telefono_1 = phone.country + phone.area + phone.phone;
        }



        request.userID = getIntent().getIntExtra("userID", -1);
        request.phone = phone;
        Call<ValidationResponse> callUser = service.revalidation2(request);
        showProgressDialog("Enviando Actualización de datos...");
        callUser.enqueue(new Callback<ValidationResponse>() {
            @Override
            public void onResponse(Call<ValidationResponse> call, Response<ValidationResponse> response) {
                hideProgressDialog();
                ValidationResponse revalidationResponse = response.body();
                if (response.isSuccessful() && revalidationResponse != null) {
                    if (revalidationResponse.IsOk()) {
                        dialog.findViewById(R.id.fragment_actualizar).setVisibility(View.GONE);
                        dialog.findViewById(R.id.fragment_validate).setVisibility(View.VISIBLE);
                    } else {
                        mostrarErrorActualizacion(revalidationResponse.Message);
                    }
                } else {
                    mostrarErrorActualizacion(null);
                }
            }

            @Override
            public void onFailure(Call<ValidationResponse> call, Throwable t) {
                hideProgressDialog();
                mostrarErrorActualizacion(null);
            }
        });
    }

    private void sendUpdateMail() {
        final TuidService service = ServiceGenerator.createService(TuidService.class);
        final RevalidationRequest request_revalidation = new RevalidationRequest();
        request_revalidation.userID = String.valueOf(getIntent().getIntExtra("userID", -1));
        request_revalidation.email = email_1;
        request_revalidation.telefono = telefono_1;
        final Call<RevalidationResponse> callUser = service.revalidation(request_revalidation);
        showProgressDialog("Enviando Actualización de datos...");
        callUser.enqueue(new Callback<RevalidationResponse>() {
            @Override
            public void onResponse(Call<RevalidationResponse> call, Response<RevalidationResponse> response) {
                hideProgressDialog();
                RevalidationResponse revalidationResponse = response.body();
                if (response.isSuccessful() && revalidationResponse != null) {
                    if (revalidationResponse.IsOk()) {
                        dialog.findViewById(R.id.fragment_actualizar).setVisibility(View.GONE);
                        dialog.findViewById(R.id.fragment_validate).setVisibility(View.VISIBLE);
                    } else {
                        mostrarErrorActualizacion(revalidationResponse.Message);
                    }
                } else {
                    mostrarErrorActualizacion(null);
                }
            }

            @Override
            public void onFailure(Call<RevalidationResponse> call, Throwable t) {
                hideProgressDialog();
                mostrarErrorActualizacion(null);
            }
        });
    }

    protected void showProgressDialog(String message) {
        try {
            if (mProgressDialog == null || true) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage(message);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setIndeterminate(true);
            }
            mProgressDialog.show();
        }
        catch (Exception e){

        }
    }

    void getCertificate(){
        Intent i = new Intent(context, CertificadoActivity.class);
        if (cuil != null && !cuil.isEmpty()) {
            i.putExtra("cuil", cuil);
        } else {
            i.putExtra("sexo", sexo);
            i.putExtra("dni",dni);
        }
        startActivity(i);
    }

    void initCodPaisSpinner(Dialog d){
        List<TelefonoCodigoPais> codigos_pais = CountriesUtil.getCodigosPais(d.getContext());

        Spinner spinner = d.findViewById(R.id.text_content_info_phone_cod_pais_spinner);
        ArrayAdapter<TelefonoCodigoPais> adapter = new ArrayAdapter<TelefonoCodigoPais>(d.getContext(), android.R.layout.simple_spinner_item, codigos_pais);
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
}
