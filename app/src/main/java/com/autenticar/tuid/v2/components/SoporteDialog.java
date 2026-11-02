package com.autenticar.tuid.v2.components;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.autenticar.tuid.AmplitudeManager.AmplitudeManager;
import com.autenticar.tuid.R;
import com.autenticar.tuid.model.ContactRequest;
import com.autenticar.tuid.model.ContactResponse;
import com.autenticar.tuid.model.ContactSubject;
import com.autenticar.tuid.services.TuidService;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.autenticar.tuid.utils.CountriesUtil;
import com.autenticar.tuid.utils.HelperFunctions;
import com.autenticar.tuid.v2.model.Phone;
import com.autenticar.tuid.v2.model.TelefonoCodigoPais;
import com.autenticar.tuid.v2.services.ContactRequest2;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SoporteDialog extends Dialog {

    Context context;
    Activity activity;
    int titulo;
    static List<ContactSubject> asuntos = new ArrayList<>();
    String conctactDialogSelectedSubjectId ="";


    static{
        asuntos.add(new ContactSubject("Solicito Ayuda modo de uso", "100"));
        asuntos.add(new ContactSubject("Confirmar Registración o Fe de Vida", "101"));
        asuntos.add(new ContactSubject("Quiero saber si mi Fe de Vida está Vigente", "102"));
        asuntos.add(new ContactSubject("Tengo dificultades con el Escaneo", "103"));
        asuntos.add(new ContactSubject("Envió de Código de validación", "104"));
//        asuntos.add(new ContactSubject("Solicito Certificado de Fe de Vida", "105"));
        asuntos.add(new ContactSubject("Cuando debo Renovar mi Fe de Vida", "106"));
        asuntos.add(new ContactSubject("Me indica que debo otorgar permisos", "107"));
        asuntos.add(new ContactSubject("No puedo realizar Fe de Vida", "108"));
        asuntos.add(new ContactSubject("No me acreditacion beneficio previsional", "109"));
        asuntos.add(new ContactSubject("Cambio de Dispositivo", "110"));
        asuntos.add(new ContactSubject("Contactar soporte", ""));
        
    } 
    
    
    public SoporteDialog(@NonNull Context _context,Activity _activity, int _titulo) {
        super(_context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(false);
        context = _context;
        activity = _activity;
        titulo = _titulo;

        setContentView(R.layout.dialog_contact_subject_select);

        new AmplitudeManager().log(activity, "Going to Contact", null);
        findViewById(R.id.btn_return).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        initAsuntoLayout();

        show();
        WindowManager.LayoutParams lp2 = new WindowManager.LayoutParams();
        lp2.copyFrom(getWindow().getAttributes());
        lp2.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp2.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(lp2);
    }

    public void goToContactForm(ContactSubject c){
        conctactDialogSelectedSubjectId = c.id;
        setContentView(R.layout.dialog_contact);
        initCodPaisSpinner(this);
        ((TextView) findViewById(R.id.text_title_info_empresarial)).setText("Formulario de contacto");
        ImageView btn_cancel = (ImageView) findViewById(R.id.btn_cancel);
        ImageView btn_return = (ImageView) findViewById(R.id.btn_return);
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        Button btn_send = (Button) findViewById(R.id.btn_send);
        final EditText text_content_info_cuil_text = (EditText) findViewById(R.id.text_content_info_CUIL_text);
        final EditText text_content_info_email_text = (EditText) findViewById(R.id.text_content_info_email_text);
        final EditText text_content_info_name_text = (EditText) findViewById(R.id.text_content_info_name_text);
        final EditText text_content_info_phone_text = (EditText) findViewById(R.id.text_content_info_phone_text);
        final EditText text_content_info_phone_area = (EditText) findViewById(R.id.text_content_info_phone_cod_area);
        final Spinner spinner_phone_country = findViewById(R.id.text_content_info_phone_cod_pais_spinner);
        final EditText text_content_info_asunto_text = (EditText) findViewById(R.id.text_content_info_asunto_text);
        if(c.id.length()>0) {
            text_content_info_asunto_text.setText(c.text);
            text_content_info_asunto_text.setEnabled(false);
        }
        final EditText text_content_info_msn_text = (EditText) findViewById(R.id.text_content_info_msn_text);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContactRequest2 request = new ContactRequest2();
                request.cuil = String.valueOf(text_content_info_cuil_text.getText()).trim().replace("-", "");
                request.nombre = String.valueOf(text_content_info_name_text.getText());
                Phone phone = new Phone();
                phone.country = ((TelefonoCodigoPais)spinner_phone_country.getSelectedItem()).codigo;
                phone.area = String.valueOf(text_content_info_phone_area.getText());
                phone.phone = String.valueOf(text_content_info_phone_text.getText());
                request.phone = phone;
//                request.telefono = String.valueOf(text_content_info_phone_text.getText());
                request.email = String.valueOf(text_content_info_email_text.getText());
                request.asunto = String.valueOf(text_content_info_asunto_text.getText());
                request.mensaje = String.valueOf(text_content_info_msn_text.getText());
                request.code = conctactDialogSelectedSubjectId;
                try {
                    request.appVersion = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                request.os = "Android (" + Build.MANUFACTURER + " " + Build.MODEL +")";
                request.version = Build.VERSION.RELEASE;
                if (request.cuil.trim().equals("")) {
                    text_content_info_cuil_text.requestFocus();
                    createAlert(activity, "Alerta", activity.getString(R.string.contact_validar_dni)).show();
                }
                else if (request.cuil.length()!=11) {
                    text_content_info_cuil_text.requestFocus();
                    createAlert(activity, "Alerta", "El cuil ingresado es inválido").show();
                    
                } else if (request.nombre.trim().equals("")) {
                    text_content_info_name_text.requestFocus();
                    createAlert(activity, "Alerta", activity.getString(R.string.contact_validar_dni)).show();
                    
                } else if (request.phone.phone.trim().equals("")) {
                    text_content_info_phone_text.requestFocus();
                    createAlert(activity, "Alerta", activity.getString(R.string.contact_validar_dni)).show();
                    
                } else if (request.phone.country.trim().equals("")) {
                    text_content_info_phone_text.requestFocus();
                    createAlert(activity, "Alerta", activity.getString(R.string.contact_validar_dni)).show();

                } else if (request.phone.area.trim().equals("")) {
                    text_content_info_phone_text.requestFocus();
                    createAlert(activity, "Alerta", activity.getString(R.string.contact_validar_dni)).show();

                }
                else if (request.asunto.trim().equals("")) {
                    text_content_info_asunto_text.requestFocus();
                    createAlert(activity, "Alerta", activity.getString(R.string.contact_validar_asunto)).show();
                    //Toast.makeText(context,R.string.contact_validar_asunto , Toast.LENGTH_LONG).show();
                } else if (request.email.length() < 3 || !isValidEmail(request.email)) {
                    text_content_info_email_text.requestFocus();
                    createAlert(activity, "Alerta", activity.getString(R.string.contact_validar_email)).show();
                    //Toast.makeText(context,R.string.contact_validar_email , Toast.LENGTH_LONG).show();
                } else if (request.mensaje.trim().equals("")) {
                    text_content_info_msn_text.requestFocus();
                    createAlert(activity, "Alerta", activity.getString(R.string.contact_validar_mensaje)).show();
                    //Toast.makeText(context,R.string.contact_validar_mensaje, Toast.LENGTH_LONG).show();
                } else {
                    send_contact(request);
                }
            }
        });
    }

    void initAsuntoLayout(){
        LinearLayout lo = findViewById(R.id.dialog_contact_subject_select);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(10,10,10,10);
        for(final ContactSubject c: asuntos){
            Button b = new Button(context);
            b.setText(c.text);
            b.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            b.setTextColor(context.getColor(R.color.colorPrimary));

            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToContactForm(c);
                }
            });
            lo.addView(b,lp);

        }

        ScrollView scrollView =  findViewById(R.id.dialog_contact_scroll_view);
        Animation fade_in = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                try {
                    int childHeight = ((LinearLayout) findViewById(R.id.dialog_contact_subject_select)).getHeight();
                    boolean isScrollableDown = scrollView.getScrollY() < childHeight - scrollView.getHeight();
                    boolean isScrollableUp = scrollView.getScrollY() > 0;
                    if (isScrollableDown) {
                        findViewById(R.id.dialog_contact_go_down).setVisibility(View.VISIBLE);
                        findViewById(R.id.dialog_contact_go_down).startAnimation(fade_in);
                    } else {
                        findViewById(R.id.dialog_contact_go_down).clearAnimation();
                        findViewById(R.id.dialog_contact_go_down).setVisibility(View.INVISIBLE);
                    }
                    if (isScrollableUp) {
                        findViewById(R.id.dialog_contact_go_up).setVisibility(View.VISIBLE);
                        findViewById(R.id.dialog_contact_go_up).startAnimation(fade_in);
                    } else {
                        findViewById(R.id.dialog_contact_go_up).clearAnimation();
                        findViewById(R.id.dialog_contact_go_up).setVisibility(View.INVISIBLE);
                    }
                }
                catch (Exception e){

                }

            }
        });

        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                    scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int childHeight = ((LinearLayout) findViewById(R.id.dialog_contact_subject_select)).getHeight();
                    boolean isScrollableDown = scrollView.getScrollY() < childHeight - scrollView.getHeight();
                    boolean isScrollableUp = scrollView.getScrollY() > 0;
                    if (isScrollableDown) {
                        findViewById(R.id.dialog_contact_go_down).setVisibility(View.VISIBLE);
                        findViewById(R.id.dialog_contact_go_down).startAnimation(fade_in);
                    } else {
                        findViewById(R.id.dialog_contact_go_down).clearAnimation();
                        findViewById(R.id.dialog_contact_go_down).setVisibility(View.INVISIBLE);
                    }
                    if (isScrollableUp) {
                        findViewById(R.id.dialog_contact_go_up).setVisibility(View.VISIBLE);
                        findViewById(R.id.dialog_contact_go_up).startAnimation(fade_in);
                    } else {
                        findViewById(R.id.dialog_contact_go_up).clearAnimation();
                        findViewById(R.id.dialog_contact_go_up).setVisibility(View.INVISIBLE);
                    }
                }
                catch (Exception e){

                }
            }
        });


    }

    void send_contact(ContactRequest2 request) {
        Log.e("request", request.toString());
        try{
            Button btn_send = (Button) findViewById(R.id.btn_send);
            btn_send.setEnabled(false);
        }
        catch (Exception e){

        }
        TuidService apiServiceContact = ServiceGenerator.createService(TuidService.class);
        Call<ContactResponse> callContact = apiServiceContact.comentary2(request);
        HelperFunctions.showProgressDialog(activity.getString(R.string.alert_verificando_info), context);
        callContact.enqueue(new Callback<ContactResponse>() {
            @Override
            public void onResponse(Call<ContactResponse> call, Response<ContactResponse> response) {
                try{
                    Button btn_send = (Button) findViewById(R.id.btn_send);
                    btn_send.setEnabled(false);
                }
                catch (Exception e){

                }
                HelperFunctions.hideProgressDialog();
                if (response.isSuccessful()) {
                    if (response.body().IsOk()) {
                        HelperFunctions.ShowAlert(activity, "Su mensaje se ha enviado correctamente");
                    } else {
                        HelperFunctions.ShowAlert(activity, response.body().Message);
                    }
                    dismiss();
                } else {
                    try{
                        Button btn_send = (Button) findViewById(R.id.btn_send);
                        btn_send.setEnabled(true);
                    }
                    catch (Exception e){

                    }
                    String errorMessage = response.message();
                    try{
                        JSONObject r = new JSONObject(response.errorBody().string());
                        errorMessage = r.getString("message");
                    }
                    catch (Exception e){

                    }

                    HelperFunctions.ShowAlert(activity, errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ContactResponse> call, Throwable t) {
                HelperFunctions.hideProgressDialog();
                HelperFunctions.ShowAlert(activity, "Error al enviar formulario de contacto");
                try{
                    Button btn_send = (Button) findViewById(R.id.btn_send);
                    btn_send.setEnabled(true);
                }
                catch (Exception e){

                }
            }
        });
    }

    AlertDialog createAlert(final Activity context, String title, String message) {
        return createAlert(context,title,message,null);
    }

    AlertDialog createAlert(final Activity context, String title, String message, View.OnClickListener onClickListener) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        View view = context.getLayoutInflater().inflate(R.layout.custom_alert, null);
        dialog.setView(view);
        final AlertDialog alert = dialog.create();
        ((TextView) view.findViewById(R.id.dialog_text)).setText(message);
        ImageView ok = (ImageView) view.findViewById(R.id.ok_action);
        if (onClickListener == null) {
            onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //whatever you want
                    alert.dismiss();
                }
            };
        }
        ok.setOnClickListener(onClickListener);
        return alert;
    }

    public static boolean isValidEmail(String text) {
        return !TextUtils.isEmpty(text) && android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches();
    }


    static void initCodPaisSpinner(Dialog d){
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

