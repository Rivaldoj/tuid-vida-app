package com.autenticar.tuid.v2;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.autenticar.tuid.R;
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

public class LoginResultErrorsListAdapter extends BaseAdapter {
    private List<itemError> item;
    private Context context;
    private String userID;
    private Activity activity;
    private String valor_email_telefono;


    public LoginResultErrorsListAdapter(List<itemError> item, Context context, String userID, Activity activity) {
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
        final LoginResultErrorsListAdapter.ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new LoginResultErrorsListAdapter.ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.content_list_error2, viewGroup, false);
            viewHolder.img_error_list = (TextView) convertView.findViewById(R.id.img_error_list);
            viewHolder.error_desc = (TextView) convertView.findViewById(R.id.img_error_list_desc);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (LoginResultErrorsListAdapter.ViewHolder) convertView.getTag();
        }
        if (!item.get(position).getText_error().equals("")) {
            viewHolder.id = position;
            viewHolder.img_error_list.setText(item.get(position).getText_error());
            if(item.get(position).getType().equals("TELEFONO")){
                viewHolder.error_desc.setVisibility(View.VISIBLE);
                viewHolder.error_desc.setText("Registrar el número de Móvil le permitirá recibir notificaciones, recordatorios y comunicaciones de su interés.");
            }
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

    void showDialogActualizarDatosContacto(String type){
        try {
            if (type.equalsIgnoreCase("email")) {
                ((ResultLoginActivity)context).editmail(null);
            }
            if (type.equalsIgnoreCase("telefono")) {
                ((ResultLoginActivity)context).editphone(null);
            }
        }
        catch (Exception e){

        }
    }

    public static class ViewHolder {
        int id;
        TextView img_error_list;
        TextView error_desc;
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
