package com.autenticar.tuid.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.autenticar.tuid.AmplitudeManager.AmplitudeManager;
import com.autenticar.tuid.R;
import com.autenticar.tuid.model.ContactRequest;
import com.autenticar.tuid.model.ContactResponse;
import com.autenticar.tuid.model.ContactSubject;
import com.autenticar.tuid.services.TuidService;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.autenticar.tuid.v2.EntidadesAdheridasActivity;
import com.autenticar.tuid.v2.HomeActivity;
import com.autenticar.tuid.v2.ValidarTelefonoMail;
import com.autenticar.tuid.v2.components.SoporteDialog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//import android.support.v4.app.NotificationCompat;


public class HelperFunctions {
    public static ProgressDialog mProgressDialog;
    int notificationID = 434224;
    public static boolean sound = true;
    public static boolean BIENVENIDA = true;
    private static MediaPlayer mp;
    private static String codeMovil = "";
    private static boolean codeban = false;
    private static boolean start = false;
    private static boolean error = false;

    public static AlertDialog CreateAlert(final Activity context, String title, String message, View.OnClickListener onClickListener) {


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

    public static AlertDialog CreateAlertWithLoginAndPhoneOptions(final Activity context, String title, String message, boolean showPhone) {


        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        View view = context.getLayoutInflater().inflate(R.layout.custom_alert_with_phone_or_login_options, null);
        dialog.setView(view);
        final AlertDialog alert = dialog.create();
        ((TextView) view.findViewById(R.id.dialog_text)).setText(message);
        ((TextView)view.findViewById(R.id.dialog_ir_a_dar_fe_de_vida)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HomeActivity.irAFeDeVidaOnResume = true;
                alert.dismiss();
                context.finish();
            }
        });

        ((TextView)view.findViewById(R.id.dialog_finalizar)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
                context.finish();
            }
        });
        if(showPhone) {
            ((TextView) view.findViewById(R.id.dialog_ir_a_validar_telefono)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, ValidarTelefonoMail.class);
                    i.putExtra("isPhone", true);
                    context.startActivity(i);
                    context.finish();
                }
            });
        }
        else {
            ((TextView) view.findViewById(R.id.dialog_ir_a_validar_telefono)).setVisibility(View.GONE);
        }

        return alert;

    }

    public static AlertDialog CreateAlertWithContact(final Activity context, String title, String message, View.OnClickListener onClickListener, int type, String response_code){
        boolean finishActivityAfter = !response_code.equalsIgnoreCase("DONTFINISHACTIVITY");
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        View view = context.getLayoutInflater().inflate(R.layout.custom_alert_with_contact, null);
        dialog.setView(view);
        final AlertDialog alert = dialog.create();
        ((TextView) view.findViewById(R.id.dialog_text)).setText(message);
        ImageView ok = (ImageView) view.findViewById(R.id.ok_action);
        if (onClickListener == null) {
            onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alert.dismiss();
                    if(finishActivityAfter) context.finish();
                }
            };
        }

        view.findViewById(R.id.dialog_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
                if(finishActivityAfter) context.finish();
            }
        });

        view.findViewById(R.id.dialog_ir_a_contacto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
                if(finishActivityAfter) context.finish();
//                HelperFunctions.showDialog(R.string.nav_titulo_contacto, R.string.nav_content_contacto, 4, HomeActivity.self, HomeActivity.self);
                new SoporteDialog( HomeActivity.self, HomeActivity.self,R.string.nav_content_contacto);
            }
        });

        if(message.toUpperCase().contains("USTED YA SE ENCUENTRA REGISTRADO")){
            view.findViewById(R.id.dialog_ir_a_dar_fe_de_vida).setVisibility(View.VISIBLE);
            view.findViewById(R.id.dialog_ir_a_dar_fe_de_vida).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HomeActivity.irAFeDeVidaOnResume = true;
                    alert.dismiss();
                    if(finishActivityAfter) context.finish();
                }
            });
        }

        if(response_code.equalsIgnoreCase("10009")){
            view.findViewById(R.id.dialog_ir_a_dar_fe_de_vida).setVisibility(View.VISIBLE);
            view.findViewById(R.id.dialog_ir_a_dar_fe_de_vida).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HomeActivity.irAFeDeVidaOnResume = true;
                    alert.dismiss();
                    if(finishActivityAfter) context.finish();
                }
            });
        }

       
        if(response_code.equalsIgnoreCase("10011")){
            view.findViewById(R.id.dialog_ver_entidades_adheridas).setVisibility(View.VISIBLE);
            view.findViewById(R.id.dialog_ver_entidades_adheridas).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alert.dismiss();
                    HomeActivity.self.startActivity(new Intent(HomeActivity.self, EntidadesAdheridasActivity.class));
                    if(finishActivityAfter) context.finish();
                }
            });
        }

        ok.setOnClickListener(onClickListener);
        return alert;
    }

    public static AlertDialog CreateAlert(Activity context, String title, String message) {

        return CreateAlert(context, title, message, null);

    }

    public static void setHTMLFormmatedText(String formattedText, TextView view) {

        //String formattedText = "This <i>is</i> a <b>test</b> of <a href='http://foo.com'>html</a>";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.setText(Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY));
        } else {
            view.setText(Html.fromHtml(formattedText));
        }
    }

    public static Bitmap getBitmapFromAsset(String strName, Context context) {
        AssetManager assetManager = context.getAssets();
        InputStream istr = null;
        try {
            istr = assetManager.open(strName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        return bitmap;
    }

    public static String GetDeviceID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public static void ShowAlert(Activity context, String message) {
        CreateAlert(context, context.getString(R.string.app_name), message).show();
    }

    public static void ShowAlertWithContact(Activity context, String message, int type, String response_code) {
        CreateAlertWithContact(context, context.getString(R.string.app_name), message,null, type, response_code).show();
    }

    public static void ShowAlert(Activity context, String message, View.OnClickListener onClickListener) {
        CreateAlert(context, context.getString(R.string.app_name), message, onClickListener).show();
    }

    public static String encodeTobase64(Bitmap image) {
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        return imageEncoded;
    }

    public static void Vibrate(Context context) {
        Vibrator vibrator;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static String conctactDialogSelectedSubjectId = "";

    public static List<ContactSubject> getListadoDeTopicsParaContacto(){

        List<ContactSubject> asuntos = new ArrayList<>();
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
        return asuntos;
    }

    public static void setDialogOtroModo(Dialog dialog, int titulo, int msg){
        dialog.setContentView(R.layout.dialog_info_empresarial);
        TextView text_title_info_empresarialdialog = (TextView) dialog.findViewById(R.id.text_title_info_empresarial);
        TextView text_content_info_empresarial = (TextView) dialog.findViewById(R.id.text_content_info_empresarial);
        text_title_info_empresarialdialog.setText(titulo);
        text_content_info_empresarial.setText(msg);
        ImageView btn_cancel = (ImageView) dialog.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        ImageView btn_return = (ImageView) dialog.findViewById(R.id.btn_return);
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        text_content_info_empresarial.setText(msg);
    }
    
    public final static void showDialog(final int titulo, int msg, int item, final Context context, final Activity activity) {
        conctactDialogSelectedSubjectId = "";
        List<ContactSubject> asuntos = getListadoDeTopicsParaContacto();
        
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        if (item == 4) {
            // Dialog para soporte
            dialog.setContentView(R.layout.dialog_contact_subject_select);
            new AmplitudeManager().log(activity, "Going to Contact", null);
            dialog.findViewById(R.id.btn_return).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            dialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            LinearLayout lo = dialog.findViewById(R.id.dialog_contact_subject_select);
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
                        conctactDialogSelectedSubjectId = c.id;
                        dialog.setContentView(R.layout.dialog_contact);
                        TextView text_title_info_empresarialdialog = (TextView) dialog.findViewById(R.id.text_title_info_empresarial);
                        text_title_info_empresarialdialog.setText(titulo);
                        ImageView btn_cancel = (ImageView) dialog.findViewById(R.id.btn_cancel);
                        ImageView btn_return = (ImageView) dialog.findViewById(R.id.btn_return);
                        btn_return.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                        Button btn_send = (Button) dialog.findViewById(R.id.btn_send);
                        final EditText text_content_info_cuil_text = (EditText) dialog.findViewById(R.id.text_content_info_CUIL_text);
                        final EditText text_content_info_email_text = (EditText) dialog.findViewById(R.id.text_content_info_email_text);
                        final EditText text_content_info_name_text = (EditText) dialog.findViewById(R.id.text_content_info_name_text);
                        final EditText text_content_info_phone_text = (EditText) dialog.findViewById(R.id.text_content_info_phone_text);
                        final EditText text_content_info_asunto_text = (EditText) dialog.findViewById(R.id.text_content_info_asunto_text);
                        if(c.id.length()>0) {
                            text_content_info_asunto_text.setText(c.text);
                            text_content_info_asunto_text.setEnabled(false);
                        }
                        final EditText text_content_info_msn_text = (EditText) dialog.findViewById(R.id.text_content_info_msn_text);
                        btn_cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                        btn_send.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ContactRequest request = new ContactRequest();
                                request.cuil = String.valueOf(text_content_info_cuil_text.getText()).trim().replace("-", "");
                                request.nombre = String.valueOf(text_content_info_name_text.getText());
                                request.telefono = String.valueOf(text_content_info_phone_text.getText());
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
                                    CreateAlert(activity, "Alerta", activity.getString(R.string.contact_validar_dni)).show();
                                    //Toast.makeText(context,R.string.contact_validar_dni , Toast.LENGTH_LONG).show();
                                }
                                else if (request.cuil.length()!=11) {
                                    text_content_info_cuil_text.requestFocus();
                                    CreateAlert(activity, "Alerta", "El cuil ingresado es inválido").show();
                                    //Toast.makeText(context,R.string.contact_validar_dni , Toast.LENGTH_LONG).show();
                                } else if (request.nombre.trim().equals("")) {
                                    text_content_info_name_text.requestFocus();
                                    CreateAlert(activity, "Alerta", activity.getString(R.string.contact_validar_dni)).show();
                                    //Toast.makeText(context,R.string.contact_validar_dni , Toast.LENGTH_LONG).show();
                                } else if (request.telefono.trim().equals("")) {
                                    text_content_info_phone_text.requestFocus();
                                    CreateAlert(activity, "Alerta", activity.getString(R.string.contact_validar_dni)).show();
                                    //Toast.makeText(context,R.string.contact_validar_dni , Toast.LENGTH_LONG).show();
                                } else if (request.asunto.trim().equals("")) {
                                    text_content_info_asunto_text.requestFocus();
                                    CreateAlert(activity, "Alerta", activity.getString(R.string.contact_validar_asunto)).show();
                                    //Toast.makeText(context,R.string.contact_validar_asunto , Toast.LENGTH_LONG).show();
                                } else if (request.email.length() < 3) {
                                    text_content_info_email_text.requestFocus();
                                    CreateAlert(activity, "Alerta", activity.getString(R.string.contact_validar_email)).show();
                                    //Toast.makeText(context,R.string.contact_validar_email , Toast.LENGTH_LONG).show();
                                } else if (request.mensaje.trim().equals("")) {
                                    text_content_info_msn_text.requestFocus();
                                    CreateAlert(activity, "Alerta", activity.getString(R.string.contact_validar_mensaje)).show();
                                    //Toast.makeText(context,R.string.contact_validar_mensaje, Toast.LENGTH_LONG).show();
                                } else {
                                    send_contact(request, dialog, activity, context);
                                }
                            }
                        });
                    }
                });
                lo.addView(b,lp);

            }



            ScrollView scrollView =  dialog.findViewById(R.id.dialog_contact_scroll_view);
            Animation fade_in = AnimationUtils.loadAnimation(context, R.anim.fade_in);
            scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    try {
                        int childHeight = ((LinearLayout) dialog.findViewById(R.id.dialog_contact_subject_select)).getHeight();
                        boolean isScrollableDown = scrollView.getScrollY() < childHeight - scrollView.getHeight();
                        boolean isScrollableUp = scrollView.getScrollY() > 0;
                        if (isScrollableDown) {
                            dialog.findViewById(R.id.dialog_contact_go_down).setVisibility(View.VISIBLE);
                            dialog.findViewById(R.id.dialog_contact_go_down).startAnimation(fade_in);
                        } else {
                            dialog.findViewById(R.id.dialog_contact_go_down).clearAnimation();
                            dialog.findViewById(R.id.dialog_contact_go_down).setVisibility(View.INVISIBLE);
                        }
                        if (isScrollableUp) {
                            dialog.findViewById(R.id.dialog_contact_go_up).setVisibility(View.VISIBLE);
                            dialog.findViewById(R.id.dialog_contact_go_up).startAnimation(fade_in);
                        } else {
                            dialog.findViewById(R.id.dialog_contact_go_up).clearAnimation();
                            dialog.findViewById(R.id.dialog_contact_go_up).setVisibility(View.INVISIBLE);
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
                        int childHeight = ((LinearLayout) dialog.findViewById(R.id.dialog_contact_subject_select)).getHeight();
                        boolean isScrollableDown = scrollView.getScrollY() < childHeight - scrollView.getHeight();
                        boolean isScrollableUp = scrollView.getScrollY() > 0;
                        if (isScrollableDown) {
                            dialog.findViewById(R.id.dialog_contact_go_down).setVisibility(View.VISIBLE);
                            dialog.findViewById(R.id.dialog_contact_go_down).startAnimation(fade_in);
                        } else {
                            dialog.findViewById(R.id.dialog_contact_go_down).clearAnimation();
                            dialog.findViewById(R.id.dialog_contact_go_down).setVisibility(View.INVISIBLE);
                        }
                        if (isScrollableUp) {
                            dialog.findViewById(R.id.dialog_contact_go_up).setVisibility(View.VISIBLE);
                            dialog.findViewById(R.id.dialog_contact_go_up).startAnimation(fade_in);
                        } else {
                            dialog.findViewById(R.id.dialog_contact_go_up).clearAnimation();
                            dialog.findViewById(R.id.dialog_contact_go_up).setVisibility(View.INVISIBLE);
                        }
                    }
                    catch (Exception e){

                    }
                }
            });
        } else {
            // OTRO TIPO DE DIALOG
            setDialogOtroModo(dialog,titulo,msg);
        }
        dialog.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height =  WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);

    }

    public static void send_contact(ContactRequest request, final Dialog dialog, final Activity activity, Context context) {
        try{
            Button btn_send = (Button) dialog.findViewById(R.id.btn_send);
            btn_send.setEnabled(false);
        }
        catch (Exception e){

        }
        TuidService apiServiceContact = ServiceGenerator.createService(TuidService.class);
        Call<ContactResponse> callContact = apiServiceContact.comentary(request);
        showProgressDialog(activity.getString(R.string.alert_verificando_info), context);
        callContact.enqueue(new Callback<ContactResponse>() {
            @Override
            public void onResponse(Call<ContactResponse> call, Response<ContactResponse> response) {
                try{
                    Button btn_send = (Button) dialog.findViewById(R.id.btn_send);
                    btn_send.setEnabled(false);
                }
                catch (Exception e){

                }
                hideProgressDialog();
                if (response.isSuccessful()) {
                    if (response.body().IsOk()) {
                        HelperFunctions.ShowAlert(activity, "Su mensaje se ha enviado correctamente");
                    } else {
                        HelperFunctions.ShowAlert(activity, response.body().Message);
                    }
                    dialog.dismiss();
                } else {
                    HelperFunctions.ShowAlert(activity, response.message());
                }
            }

            @Override
            public void onFailure(Call<ContactResponse> call, Throwable t) {
                hideProgressDialog();
                HelperFunctions.ShowAlert(activity, "Error al enviar formulario de contacto");
                try{
                    Button btn_send = (Button) dialog.findViewById(R.id.btn_send);
                    btn_send.setEnabled(false);
                }
                catch (Exception e){

                }
            }
        });
    }

    public static void showProgressDialog(String message, Context context) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage(message);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    public static void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    /**
     * Función que elimina acentos y caracteres especiales de
     * una cadena de texto.
     *
     * @param input
     * @return cadena de texto limpia de acentos y caracteres especiales.
     */
    public static String chararterSpece(String input) {
        String original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇûùüÛÙÜ";
        String ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcCuuuUUU";
        String output = input;
        output = output.replace("'", "");
        for (int i = 0; i < original.length(); i++) {
            output = output.replace(original.charAt(i), ascii.charAt(i));
        }
        return output;
    }


    public static int setSound2(String resource, Context context){
        //by steve
        if(!sound) return -1;
        if (mp != null) mp.stop();
        switch (resource) {
            case "ayuda": mp = MediaPlayer.create(context, R.raw.ayuda);
                break;
            case "bieniluminado"://listo
                mp = MediaPlayer.create(context, R.raw.iluminacion);
                break;
            case "bienvenido_vida"://listo
                mp = MediaPlayer.create(context, R.raw.bienvenido_vida);
                break;
            case "cancelarregistro"://listo
                mp = MediaPlayer.create(context, R.raw.cancelarregistro);
                break;
            case "completaformulario": //listo
                mp = MediaPlayer.create(context, R.raw.completaformulario);
                break;
            case "continuar"://listo
                mp = MediaPlayer.create(context, R.raw.continuar);
                break;
            case "correoelectronico"://listo
                mp = MediaPlayer.create(context, R.raw.correoelectronico);
                break;
            case "emailtelefono"://listo
                mp = MediaPlayer.create(context, R.raw.emailtelefono);
                break;
            case "enfocafrente"://listo
                mp = MediaPlayer.create(context, R.raw.enfocafrente);
                break;
            case "enviarcodigo"://listo
                mp = MediaPlayer.create(context, R.raw.enviarcodigo);
                break;
            case "espera":
                mp = MediaPlayer.create(context, R.raw.espera);
                break;
            case "fedevida": //listo
                mp = MediaPlayer.create(context, R.raw.fedevida);
                break;
            case "fedevidaexito"://listo
                mp = MediaPlayer.create(context, R.raw.fedevidaexito);
                break;
            case "fondoclaro"://listo
                mp = MediaPlayer.create(context, R.raw.fondoclaro);
                break;
            case "gestodevida":
                mp = MediaPlayer.create(context, R.raw.gestodevida);
                break;
            case "giradni":// listo
                mp = MediaPlayer.create(context, R.raw.giradni);
                break;
            case "ingresacodigo"://listo
                mp = MediaPlayer.create(context, R.raw.ingresacodigo);
                break;
            case "ingresar": //pendiente
                mp = MediaPlayer.create(context, R.raw.ingresar);
                break;
            case "iniciarscan"://listo
                mp = MediaPlayer.create(context, R.raw.iniciarscan);
                break;
            case "iniciarvalidacion"://listo
                mp = MediaPlayer.create(context, R.raw.iniciarvalidacion);
                break;
            case "masdeunrostro":
                mp = MediaPlayer.create(context, R.raw.masdeunrostro);
                break;
            case "movil"://listo
                mp = MediaPlayer.create(context, R.raw.movil);
                break;
            case "noreconocecara":
                mp = MediaPlayer.create(context, R.raw.noreconocecara);
                break;
            case "noreconocedni"://listo
                mp = MediaPlayer.create(context, R.raw.noreconocedni);
                break;
            case "noreconocedniycara":
                mp = MediaPlayer.create(context, R.raw.noreconocedniycara);
                break;
            case "noreconocepersona":
                mp = MediaPlayer.create(context, R.raw.noreconocepersona);
                break;
            case "pestania":
                mp = MediaPlayer.create(context, R.raw.pestania);
                break;
            case "precionaprueba"://listo
                mp = MediaPlayer.create(context, R.raw.precionaprueba);
                break;
            case "registrate": //listo
                mp = MediaPlayer.create(context, R.raw.registrate);
                break;
            case "reingresacorreo"://listo
                mp = MediaPlayer.create(context, R.raw.reingresacorreo);
                break;
            case "reingresamovil"://listo
                mp = MediaPlayer.create(context, R.raw.reingresamovil);
                break;
            case "satisfactorio": //listo
                mp = MediaPlayer.create(context, R.raw.satisfactorio);
                break;
            case "scan"://listo
                mp = MediaPlayer.create(context, R.raw.scan);
                break;
            case "segundo":
                mp = MediaPlayer.create(context, R.raw.segundo);
                break;
            case "sinanteojos":
                mp = MediaPlayer.create(context, R.raw.sinanteojos);
                break;
            case "terminosycondiciones": //listo
                mp = MediaPlayer.create(context, R.raw.terminosycondiciones);
                break;
            case "tres":
                mp = MediaPlayer.create(context, R.raw.tres);
                break;
            case "uno":
                mp = MediaPlayer.create(context, R.raw.uno);
                break;
            case "play":
                return 0;
            case "pause":
                return 0;
            case "stop":
                if (mp != null) {
                    mp.stop();
                }
                return 0;
            case "getDuration":
                if (mp != null) return mp.getDuration();
        }

        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });
        return mp.getDuration();
    }

    public static int setSound(String layoutID, Context context) {
        try {
            if (sound) {
                start = true;
                //mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                switch (layoutID) {
                    case "ayuda": //listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.ayuda);
                        start = true;
                        break;
                    case "bieniluminado"://listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.iluminacion);
                        start = true;
                        break;
                    case "bienvenido_vida"://listo
                        mp = MediaPlayer.create(context, R.raw.bienvenido_vida);
                        start = true;
                        break;
                    case "cancelarregistro"://listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.cancelarregistro);
                        start = true;
                        break;
                    case "completaformulario": //listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.completaformulario);
                        start = true;
                        break;
                    case "continuar"://listo
                        mp.stop();
                        mp = MediaPlayer.create(context, R.raw.continuar);
                        start = true;
                        break;
                    case "correoelectronico"://listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.correoelectronico);
                        start = true;
                        break;
                    case "emailtelefono"://listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.emailtelefono);
                        start = true;
                        break;
                    case "enfocafrente"://listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.enfocafrente);
                        start = true;
                        break;
                    case "enviarcodigo"://listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.enviarcodigo);
                        start = true;
                        break;
                    case "espera":
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.espera);
                        start = true;
                        break;
                    case "fedevida": //listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.fedevida);
                        start = true;
                        break;
                    case "fedevidaexito"://listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.fedevidaexito);
                        start = true;
                        break;
                    case "fondoclaro"://listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.fondoclaro);
                        start = true;
                        break;
                    case "gestodevida":
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.gestodevida);
                        start = true;
                        break;
                    case "giradni":// listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.giradni);
                        start = true;
                        break;
                    case "ingresacodigo"://listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.ingresacodigo);
                        start = true;
                        break;
                    case "ingresar": //pendiente
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.ingresar);
                        start = true;
                        break;
                    case "iniciarscan"://listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.iniciarscan);
                        start = true;
                        break;
                    case "iniciarvalidacion"://listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.iniciarvalidacion);
                        start = true;
                        break;
                    case "masdeunrostro":
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.masdeunrostro);
                        start = true;
                        break;
                    case "movil"://listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.movil);
                        start = true;
                        break;
                    case "noreconocecara":
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.noreconocecara);
                        start = true;
                        break;
                    case "noreconocedni"://listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.noreconocedni);
                        start = true;
                        break;
                    case "noreconocedniycara":
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.noreconocedniycara);
                        start = true;
                        break;
                    case "noreconocepersona":
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.noreconocepersona);
                        start = true;
                        break;
                    case "pestania":
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.pestania);
                        start = true;
                        break;
                    case "precionaprueba"://listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.precionaprueba);
                        start = true;
                        break;
                    case "registrate": //listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.registrate);
                        start = true;
                        break;
                    case "reingresacorreo"://listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.reingresacorreo);
                        start = true;
                        break;
                    case "reingresamovil"://listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.reingresamovil);
                        start = true;
                        break;
                    case "satisfactorio": //listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.satisfactorio);
                        start = true;
                        break;
                    case "scan"://listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.scan);
                        start = true;
                        break;
                    case "segundo":
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.segundo);
                        start = true;
                        break;
                    case "sinanteojos":
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.sinanteojos);
                        start = true;
                        break;
                    case "terminosycondiciones": //listo
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.terminosycondiciones);
                        start = true;
                        break;
                    case "tres":
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.tres);
                        start = true;
                        break;
                    case "uno":
                        if (mp != null)
                            mp.stop();
                        mp = MediaPlayer.create(context, R.raw.uno);
                        start = true;
                        break;
                    case "play":
                        if (mp != null) {
                            mp.start();
                        }
                        start = false;
                        return 0;
                    case "pause":
                        if (mp != null) {
                            mp.pause();
                        }
                        start = false;
                        return 0;
                    case "stop":
                        if (mp != null) {
                            mp.stop();
                        }
                        start = false;
                        return 0;
                    case "getDuration":
                        if (mp != null)
                            return mp.getDuration();
                }
                if (start) {
                    error = false;
                    mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                            //System.out.println("setOnErrorListener onError i:"+i+" i1:"+i1);
                            error = true;
                            return false;
                        }
                    });
                    if (!error) {
                        mp.start();
                        return mp.getDuration();
                    } else {
                        return 0;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error reproducir sonido: " + e);
        }
        return 0;
    }

    public static String[] desconcatenar(String value) {
        return value.split("");
    }

    public static void setCodeMovil(String code) {
        codeMovil = code;
        codeban = true;
    }

    public static String getCodeMovil() {
        return codeMovil;
    }

    public static boolean isCodeban() {
        return codeban;
    }

    public static String chararterAlfa(String input) {
        input = input.replace("M", "");
        input = input.replace("F", "");
        return input;
    }



}
