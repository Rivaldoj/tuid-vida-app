package com.autenticar.tuid;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
//import android.support.design.widget.TextInputLayout;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.autenticar.tuid.model.ReconfirmationRequest;
import com.autenticar.tuid.model.ReconfirmationResponse;
import com.autenticar.tuid.model.RevalidationRequest;
import com.autenticar.tuid.model.RevalidationResponse;
import com.autenticar.tuid.model.itemError;
import com.autenticar.tuid.services.TuidService;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.autenticar.tuid.utils.HelperFunctions;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.autenticar.tuid.utils.HelperFunctions.CreateAlert;
import static com.autenticar.tuid.utils.HelperFunctions.hideProgressDialog;
import static com.autenticar.tuid.utils.HelperFunctions.mProgressDialog;

public class MainListValidationAdapter extends BaseAdapter {
    private List<itemError> item;
    private Context context;
    private String userID;
    private Activity activity;
    private String valor_email_telefono;


    public MainListValidationAdapter(List<itemError> item, Context context, String userID, Activity activity) {
        this.item = item;
        this.context = context;
        this.userID = userID;
        this.activity=activity;
    }

    @Override
    public int getCount() {
        return item.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        final ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.content_list_error, viewGroup, false);
            viewHolder.img_error_list = (TextView) convertView.findViewById(R.id.img_error_list);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (!item.get(position).getText_error().equals("")) {
            viewHolder.id = position;
            viewHolder.img_error_list.setText(item.get(position).getText_error());
            if (!item.get(position).getType().equals("CUIL")) {
                viewHolder.img_error_list.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showDialogActualizarDatosContacto(item.get(position).getType());
                    }
                });
                viewHolder.img_error_list.setVisibility(View.VISIBLE);
            } else {
                viewHolder.img_error_list.setVisibility(View.INVISIBLE);
            }
        }
        return convertView;
    }

    public static class ViewHolder {
        int id;
        TextView img_error_list;
    }

    public final void showDialogActualizarDatosContacto(final String Type) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_dato_contacto);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView text_title_info_empresarialdialog = (TextView) dialog.findViewById(R.id.text_title_info_empresarial);

        ImageView btn_cancel = (ImageView) dialog.findViewById(R.id.btn_cancel);
        ImageView btn_return = (ImageView) dialog.findViewById(R.id.btn_return);
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.findViewById(R.id.fragment_data).setVisibility(View.VISIBLE);
                dialog.findViewById(R.id.fragment_validate).setVisibility(View.GONE);
            }
        });
        Button btn_update = (Button) dialog.findViewById(R.id.btn_update);
        final EditText text_content_1 = (EditText) dialog.findViewById(R.id.text_content_1);
        final TextView text_content_info_1 = (TextView) dialog.findViewById(R.id.text_content_info_1);
        final EditText text_content_2 = (EditText) dialog.findViewById(R.id.text_content_2);
        final TextView text_content_info_2 = (TextView) dialog.findViewById(R.id.text_content_info_2);

        if (Type.toUpperCase().equals("EMAIL")) {
            text_title_info_empresarialdialog.setText("Actualizar Email");
            text_content_1.setInputType(InputType.TYPE_CLASS_TEXT);
            text_content_2.setInputType(InputType.TYPE_CLASS_TEXT);
            text_content_info_1.setText("Email");
            text_content_1.setHint("Email");
            text_content_info_2.setText("Repite tu email");
            text_content_2.setHint("Repite tu email");
        } else if (Type.toUpperCase().equals("TELEFONO")) {
            text_title_info_empresarialdialog.setText("Actualizar Teléfono");
            text_content_1.setInputType(InputType.TYPE_CLASS_PHONE);
            text_content_2.setInputType(InputType.TYPE_CLASS_PHONE);
            text_content_info_1.setText("Teléfono");
            text_content_1.setHint("Teléfono");
            text_content_info_2.setText("Repite tu teléfono");
            text_content_2.setHint("Repite tu teléfono");
        }

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text_1 = String.valueOf(text_content_1.getText());
                String text_2 = String.valueOf(text_content_2.getText());
                valor_email_telefono=text_1;
                if (Type.toUpperCase().equals("EMAIL")) {
                    if (text_1.equals("") && !HelperFunctions.isValidEmail(text_1)) {
                        CreateAlert((Activity) context, "Alerta", context.getString(R.string.corre_invalido)).show();
                    } else if (!text_1.trim().equals(text_2.trim())) {
                        CreateAlert((Activity) context, "Alerta", context.getString(R.string.validacion_conirmacion_email)).show();
                    } else {
                        sendEmailTelefono("EMAIL", text_content_1.getText().toString(), dialog);
                    }

                } else if (Type.toUpperCase().equals("TELEFONO")) {
                    if (text_1.equals("") && (text_1.length() < 8)) {
                        CreateAlert((Activity) context, "Alerta", context.getString(R.string.telefono_invalido)).show();
                    } else if (!text_1.trim().equals(text_2.trim())) {
                        CreateAlert((Activity) context, "Alerta", context.getString(R.string.validacion_conirmacion_telefono)).show();
                    } else {
                        sendEmailTelefono("TELEFONO", text_content_1.getText().toString(), dialog);
                    }
                }
            }
        });
        dialog.show();
    }


    public void sendEmailTelefono(final String type, final String valor, final Dialog dialog) {
        final TuidService service = ServiceGenerator.createService(TuidService.class);
        final TextView info_envio = (TextView) dialog.findViewById(R.id.info_envio);
        final RevalidationRequest request_revalidation = new RevalidationRequest();
        request_revalidation.userID = this.userID;

        if (type.equals("EMAIL")) {
            request_revalidation.telefono = "";
            request_revalidation.email = valor;
        } else if (type.equals("TELEFONO")) {
            request_revalidation.telefono = valor;
            request_revalidation.email = "";
        }
        final Call<RevalidationResponse> callUser = service.revalidation(request_revalidation);
        if (type.equals("EMAIL")) {
            showProgressDialog("Enviando Actualización de Email...");
        } else if (type.equals("TELEFONO")) {
            showProgressDialog("Enviando Actualizacion de Número Telefonico...");
        }

        callUser.enqueue(new Callback<RevalidationResponse>() {
            @Override
            public void onResponse(Call<RevalidationResponse> call, Response<RevalidationResponse> response) {
                hideProgressDialog();
                RevalidationResponse revalidationResponse = response.body();
                if (response.isSuccessful()) {
                    if (revalidationResponse.IsOk()) {
                        HelperFunctions.ShowAlert((Activity) context, context.getString(R.string.alert_recibiras_codigo_verificacion));
                        dialog.findViewById(R.id.fragment_data).setVisibility(View.GONE);
                        dialog.findViewById(R.id.fragment_validate).setVisibility(View.VISIBLE);
                        if (type.equals("TELEFONO")) {
                            info_envio.setText("Se ha enviado el CÓDIGO mediante un mensaje de texto al número " + valor);
                        } else if (type.equals("EMAIL")) {
                            info_envio.setText("Se ha enviado el CÓDIGO mediante un EMAIL al correo " + valor);
                        }
                    } else {
                        HelperFunctions.ShowAlert((Activity) context, revalidationResponse.Message);
                    }
                } else {
                    HelperFunctions.ShowAlert((Activity) context, "Ocurrio un error con la validación");
                }
            }

            @Override
            public void onFailure(Call<RevalidationResponse> call, Throwable t) {
                hideProgressDialog();
                //findViewById(R.id.btnSendValidation).setEnabled(true);
                //HelperFunctions.ShowAlert(activity, "Requiere una conexión internet estable");
            }
        });
        dialog.findViewById(R.id.btn_validate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText edit_validacion = (EditText) dialog.findViewById(R.id.edit_validacion);
                final String valor = edit_validacion.getText().toString();
                if (valor.length() == 4) {
                    final ReconfirmationRequest request_reconfirmation = new ReconfirmationRequest();
                    request_reconfirmation.userID = userID;
                    if (type.equals("EMAIL")) {
                        request_reconfirmation.CodeEmail = valor;
                        request_reconfirmation.CodeTel = "";
                    } else if (type.equals("TELEFONO")) {
                        request_reconfirmation.CodeTel = valor;
                        request_reconfirmation.CodeEmail = "";
                    } else {
                        request_reconfirmation.CodeTel = "";
                        request_reconfirmation.CodeEmail = "";
                    }
                    final Call<ReconfirmationResponse> callUserReconfirmation = service.reconfirmation(request_reconfirmation);
                    callUserReconfirmation.enqueue(new Callback<ReconfirmationResponse>() {
                        @Override
                        public void onResponse(Call<ReconfirmationResponse> call, Response<ReconfirmationResponse> response) {
                            hideProgressDialog();
                            ReconfirmationResponse reconfirmationResponse = response.body();
                            if (response.isSuccessful()) {
                                if (reconfirmationResponse.IsOk()) {
                                    if (type.equals("EMAIL")) {
                                        ((TextView) activity.findViewById(R.id.main_email)).setText(valor_email_telefono);
                                        for(int i=0; i<item.size();i++){
                                            if(item.get(i).getType().equals("EMAIL")){
                                                item.remove(i);
                                            }
                                        }
                                    } else if (type.equals("TELEFONO")) {
                                        ((TextView)activity.findViewById(R.id.main_telefono)).setText(valor_email_telefono);
                                        for(int i=0; i<item.size();i++){
                                            if(item.get(i).getType().equals("TELEFONO")){
                                                item.remove(i);
                                            }
                                        }
                                    }
                                    dialog.dismiss();
                                    if(item.size()==0) {
                                        ((TextView) activity.findViewById(R.id.fe_vida)).setText("Fe de Vida Valida");
                                    }
                                    ((ImageView)activity.findViewById(R.id.img_fe_vida)).setBackground(activity.getDrawable(R.drawable.ic_validado));
                                    notifyDataSetChanged();


                                } else {
                                    HelperFunctions.ShowAlert((Activity) context, reconfirmationResponse.Message);
                                }
                            } else {
                                HelperFunctions.ShowAlert((Activity) context, "Ocurrio un error con la validación");
                            }
                        }

                        @Override
                        public void onFailure(Call<ReconfirmationResponse> call, Throwable t) {
                            hideProgressDialog();
                            //findViewById(R.id.btnSendValidation).setEnabled(true);
                            //HelperFunctions.ShowAlert(activity, "Requiere una conexión internet estable");
                        }
                    });
                } else {
                    HelperFunctions.ShowAlert((Activity) context, "El código no es valido intente nuevamente.");
                }

            }
        });
    }

    protected void showProgressDialog(String message) {
        try {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(message);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setIndeterminate(true);
            }
            mProgressDialog.show();
        }
        catch (Exception e){
            
        }
    }

    public void removeErrorFromList(String type){
        for(int i=0; i<item.size();i++){
            if(item.get(i).getType().equals(type)){
                item.remove(i);
            }
        }

        notifyDataSetChanged();
    }
}
