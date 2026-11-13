package com.autenticar.tuid.v2;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.autenticar.tuid.AmplitudeManager.AmplitudeManager;
import com.autenticar.tuid.MainApp;
import com.autenticar.tuid.R;
import com.autenticar.tuid.model.ConfirmationRequest;
import com.autenticar.tuid.model.ConfirmationResponse;
import com.autenticar.tuid.model.ValidationRequest;
import com.autenticar.tuid.model.ValidationResponse;
import com.autenticar.tuid.services.TuidService;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.autenticar.tuid.utils.CountriesUtil;
import com.autenticar.tuid.utils.HelperFunctions;
import com.autenticar.tuid.v2.model.Phone;
import com.autenticar.tuid.v2.model.TelefonoCodigoPais;
import com.autenticar.tuid.v2.services.ValidationRequest2;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.text.TextUtils.isEmpty;
import static com.autenticar.tuid.utils.HelperFunctions.setSound;
import static com.autenticar.tuid.utils.HelperFunctions.sound;

public class ValidarTelefonoMail extends TuIdActivity{


    private ProgressDialog mProgressDialog;
    boolean isPhone = false;

    @Override
    public void onBackPressed(){
        AlertDialog.Builder dialogo = new AlertDialog.Builder(activity);
        dialogo.setTitle("Alerta");
        dialogo.setMessage("¿Está seguro que desea salir?");
        dialogo.setCancelable(true);
        dialogo.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo, int id) {
                dialogo.dismiss();
            }
        });
        dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo, int id) {
                dialogo.dismiss();
                backPressed();
            }
        });
        AlertDialog alert = dialogo.create();
        alert.show();
        Button btn_negativo = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        btn_negativo.setTextColor(Color.BLACK);
        Button btn_positivo = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        btn_positivo.setTextColor(Color.BLACK);
    }

    public void backPressed(){
        super.onBackPressed();
    }
    @Override
    public void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.v2_validar_telefono_mail_activity);
        ((TextView) findViewById(R.id.v2_validar_titulo)).setText("Registrar correo electrónico");

        try {
            if (getIntent().getExtras().containsKey("isPhone") && getIntent().getExtras().getBoolean("isPhone", false)) {
                isPhone = true;
                ((TextView) findViewById(R.id.v2_validar_titulo)).setText("Registrar celular");
                ((TextView) findViewById(R.id.v2_validar_telefono_mail_text)).setText("Para completar tu registro debes indicar un teléfono celular. Recibirás un código de verificación por SMS.");
                ((TextView) findViewById(R.id.v2_validar_aviso_codigo)).setText("Le enviaremos un código para comprobar que es correcto.");
                findViewById(R.id.v2_validar_telefono_mail_telefono_telefono_container).setVisibility(View.VISIBLE);
                findViewById(R.id.v2_validar_telefono_mail_telefono_mail_container).setVisibility(View.GONE);
                ((RadioButton) findViewById(R.id.optSendSMS)).setChecked(true);
                initPhoneSpinner();

            }
        }
        catch (Exception e){
            
        }
//        playGuideSounds(new int[]{R.raw.emailtelefono, R.raw.iniciarvalidacion}, 0, null);
        playGuideSounds(isPhone ? new int[]{R.raw.a41, R.raw.enviarcodigo} : new int[]{R.raw.a39, R.raw.a40}, 0, null);
        findViewById(R.id.btnSendValidation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processValidation();
            }
        });


//        findViewById(R.id.optSendMail).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (((RadioButton) findViewById(R.id.optSendMail)).isChecked()) {
//                    ((TextInputLayout) findViewById(R.id.txtTelefono)).getEditText().setText(null);
//                    findViewById(R.id.txtTelefono).setEnabled(false);
//                    findViewById(R.id.txtEmail).setEnabled(true);
//                    findViewById(R.id.txtEmailConfirmar).setEnabled(true);
//                    ((RadioButton) findViewById(R.id.optSendSMS)).setChecked(false);
//                }
//            }
//        });

        findViewById(R.id.optSendSMS).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((RadioButton) findViewById(R.id.optSendSMS)).isChecked()) {
                    ((TextInputLayout) findViewById(R.id.txtEmail)).getEditText().setText(null);
                    ((TextInputLayout) findViewById(R.id.txtEmailConfirmar)).getEditText().setText(null);
                    findViewById(R.id.txtEmail).setEnabled(false);
                    findViewById(R.id.txtEmailConfirmar).setEnabled(false);
                    findViewById(R.id.txtTelefono).setEnabled(true);
                    ((RadioButton) findViewById(R.id.optSendMail)).setChecked(false);
                }
            }
        });

    }

    private void processValidation() {

        TextInputLayout txtEMail = findViewById(R.id.txtEmail);
        TextInputLayout txtEMailConfirmacion = findViewById(R.id.txtEmailConfirmar);
        txtEMail.setError(null);
        txtEMailConfirmacion.setError(null);

        Spinner spinner_phone_country = findViewById(R.id.text_content_info_phone_cod_pais_spinner);
        EditText text_content_info_phone_area = findViewById(R.id.text_content_info_phone_cod_area);
        EditText text_content_info_phone_text = findViewById(R.id.text_content_info_phone_text);
        text_content_info_phone_text.setError(null);

        boolean cancel = false;
        View focusView = null;

        String email = txtEMail.getEditText().getText().toString();
        String emailConfirmacion = txtEMailConfirmacion.getEditText().getText().toString();

        Phone telefono = new Phone();
        if(isPhone) {
            telefono.country = ((TelefonoCodigoPais) spinner_phone_country.getSelectedItem()).codigo;
            telefono.area = String.valueOf(text_content_info_phone_area.getText());
            telefono.phone = String.valueOf(text_content_info_phone_text.getText());
        }

        if (isEmpty(email) && !telefono.isValid()) {
            HelperFunctions.ShowAlert(activity, getString(R.string.datos_envio_vacios));
        } else {
            if (!isEmpty(email) && !HelperFunctions.isValidEmail(email)) {
                txtEMail.setError(getString(R.string.corre_invalido));
                focusView = txtEMail;
                cancel = true;
            } else if (!email.equals(emailConfirmacion)) {
                txtEMail.setError(getString(R.string.validacion_conirmacion_email));
                focusView = txtEMailConfirmacion;
                cancel = true;
            }
            if (isPhone && !telefono.isValid()) {
                text_content_info_phone_text.setError(getString(R.string.telefono_invalido));
                focusView = text_content_info_phone_text;
                cancel = true;
            }

            if (cancel) {
                // There was an error; don't attempt login and focus the first
                // form field with an error.
                focusView.requestFocus();

            } else {
                requestValidation(email, telefono);
            }
        }

        
    }

    private void requestValidation(String email, Phone telefono) {
        ValidationRequest2 request = new ValidationRequest2();
        TuidService service = ServiceGenerator.createService(TuidService.class);

        request.userID = ((MainApp) getApplication()).GetUserSession().userID;
        if (!TextUtils.isEmpty(email))
            request.EMail = email;
        if (telefono.isValid())
            request.phone = telefono;

        Call<ValidationResponse> callUser = isPhone? service.revalidation2(request) :service.validation2(request);
        showProgressDialog("Enviando validación...");

        callUser.enqueue(new Callback<ValidationResponse>() {
            @Override
            public void onResponse(Call<ValidationResponse> call, Response<ValidationResponse> response) {

                hideProgressDialog();
                ValidationResponse validationResponse = response.body();
                if (response.isSuccessful()) {

                    if (validationResponse.IsOk()) {
                        //go to validacion
                        boolean isEmail = !isPhone;
                        goToValidacion(isEmail, isEmail? ((((TextInputLayout) findViewById(R.id.txtEmail)).getEditText().getText().toString())) : ((((TextInputLayout) findViewById(R.id.txtTelefono)).getEditText().getText().toString())));
                        com.autenticar.tuid.usuarios.Cuadro.mostrar(activity, "Código en camino", getString(R.string.alert_recibiras_codigo_verificacion) + ".", "Entendido", null, null, R.drawable.ic_ayuda_email);
                    } else {
                        HelperFunctions.ShowAlert(activity, validationResponse.Message);
                    }
                } else {
                    HelperFunctions.ShowAlert(activity, response.message());
                }

            }

            @Override
            public void onFailure(Call<ValidationResponse> call, Throwable t) {

                hideProgressDialog();
            }
        });
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

    protected void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    void goToValidacion(boolean isEmail, String dato){
        setContentView(R.layout.v2_validar_telefono_mail_verificacion_activity);
        playGuideSounds(new int[]{isEmail ? R.raw.a40_3 : R.raw.ingresacodigo}, 0, null);

        if (!isEmail) {
            ((ImageView)findViewById(R.id.imgEmailSms)).setImageResource(R.drawable.enviar_sms);
            ((TextView)findViewById(R.id.textCodigoEnvio)).setText("Se ha enviado el CODIGO mediante un mensaje de texto al número " + dato);
            findViewById(R.id.textCodigoEnvioAviso).setVisibility(View.INVISIBLE);
        } else {
            ((ImageView)findViewById(R.id.imgEmailSms)).setImageResource(R.drawable.enviar_email);
            findViewById(R.id.textCodigoEnvioAviso).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.textCodigoEnvio)).setText("Se ha enviado el CODIGO mediante un EMAIL al correo " + dato);
        }
        ((EditText)findViewById(R.id.editCodigoValidacion)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length()==4){
                    playGuideSounds(new int[]{R.raw.a40_4}, 0, null);
                }
            }
        });

        findViewById(R.id.btnSendCodeValidation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendValidationCode(isEmail);
            }
        });

    }

    private void sendValidationCode(boolean isMail) {
        boolean cancel = false;
        findViewById(R.id.btnSendCodeValidation).setEnabled(false);
        String codigo = ((EditText) findViewById(R.id.editCodigoValidacion)).getText().toString();
        if (isEmpty(codigo)) {
            HelperFunctions.ShowAlert(activity, getString(R.string.alert_ingresa_codigo_enviado));
            //txtCodigoValidaction.setError(getString(R.string.alert_ingresa_codigo_enviado));
            if (sound) {
                setSound("ingresacodigo", activity);
            }
            cancel = true;
            findViewById(R.id.btnSendCodeValidation).setEnabled(true);
        }
        if (!cancel) {
            ConfirmationRequest request = new ConfirmationRequest();
            TuidService service = ServiceGenerator.createService(TuidService.class);
            request.userID = ((MainApp) getApplication()).GetUserSession().userID;
            if (isMail) {
                request.MailCode = codigo;
            } else {
                request.SMSCode = codigo;
            }
            Call<ConfirmationResponse> callUser = isPhone? service.reconfirmation2(request) : service.confirmation(request);
            showProgressDialog(getString(R.string.alert_enviando_codigo_ver));
            callUser.enqueue(new Callback<ConfirmationResponse>() {
                @Override
                public void onResponse(Call<ConfirmationResponse> call, Response<ConfirmationResponse> response) {
                    hideProgressDialog();
                    ConfirmationResponse validationResponse = response.body();
                    if (response.isSuccessful()) {
                        if (validationResponse.IsOk()) {
                            guardarUsuarioRegistrado();
                            AlertDialog ad = HelperFunctions.CreateAlertWithLoginAndPhoneOptions(activity, getString(R.string.app_name), getString(R.string.registro_satisfactorio), !isPhone);
                            ad.setCancelable(false);
                            ad.show();
                        } else {
                            try {
                                JSONObject error = new JSONObject();
                                error.put("error", response.body().Message);
                                new AmplitudeManager().log(activity, "Registration Failed", error);
                            }
                            catch (Exception e){

                            }
                            HelperFunctions.ShowAlert(activity, validationResponse.Message);
                        }
                    } else {
                        HelperFunctions.ShowAlert(activity, response.message());
                    }
                    findViewById(R.id.btnSendCodeValidation).setEnabled(true);
                }

                @Override
                public void onFailure(Call<ConfirmationResponse> call, Throwable t) {
                    hideProgressDialog();
                    findViewById(R.id.btnSendCodeValidation).setEnabled(true);
                    HelperFunctions.ShowAlert(activity, "Requiere una conexión internet estable");
                }
            });
        }
    }

    /**
     * Registro completo: la persona queda en "Usuarios" de este telefono. Solo si se conoce su CUIL (lo escribio al
     * empezar) y es coherente con el DNI escaneado (el CUIL contiene el numero de documento).
     */
    private void guardarUsuarioRegistrado() {
        try {
            String cuil = com.autenticar.tuid.utils.CuilValidator.normalizar(com.autenticar.tuid.usuarios.ComprobarRegistro.cuilEnRegistro);
            String dni = com.autenticar.tuid.v2.utils.Scanner.dni == null ? "" : com.autenticar.tuid.v2.utils.Scanner.dni.replaceAll("[^0-9]", "");
            if (!com.autenticar.tuid.utils.CuilValidator.esValido(cuil) || dni.isEmpty()) return;
            String dni8 = ("00000000" + dni);
            dni8 = dni8.substring(dni8.length() - 8);
            if (!cuil.substring(2, 10).equals(dni8)) return;   // CUIL de otra persona: no se guarda
            String apellido = com.autenticar.tuid.v2.utils.Scanner.apellido == null ? "" : com.autenticar.tuid.v2.utils.Scanner.apellido.trim();
            String nombre = com.autenticar.tuid.v2.utils.Scanner.nombre == null ? "" : com.autenticar.tuid.v2.utils.Scanner.nombre.trim();
            String completo = (apellido + ", " + nombre).trim();
            com.autenticar.tuid.usuarios.LocalUserStore.get(this).addOrUpdate(cuil, completo.equals(",") ? "" : completo);
            HomeActivity.cuilRecienRegistrado = cuil;
            com.autenticar.tuid.usuarios.ComprobarRegistro.cuilEnRegistro = null;
        } catch (Exception e) {
            Log.e("USUARIOS", "No se pudo guardar el usuario registrado", e);
        }
    }

    void initPhoneSpinner(){
        List<TelefonoCodigoPais> codigos_pais = CountriesUtil.getCodigosPais(context);

        Spinner spinner = findViewById(R.id.text_content_info_phone_cod_pais_spinner);
        ArrayAdapter<TelefonoCodigoPais> adapter = new ArrayAdapter<TelefonoCodigoPais>(context, android.R.layout.simple_spinner_item, codigos_pais);
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
