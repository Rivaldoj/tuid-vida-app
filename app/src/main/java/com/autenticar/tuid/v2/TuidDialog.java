package com.autenticar.tuid.v2;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.autenticar.tuid.R;

public class TuidDialog {

    public static void show(Activity context, String titulo, String msg, int code){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        dialog.setContentView(R.layout.v2_tuid_dialog);
        ((TextView)dialog.findViewById(R.id.tuid_dialog_title)).setText(titulo);
        ((TextView)dialog.findViewById(R.id.tuid_dialog_text)).setText(msg);
        dialog.findViewById(R.id.tuid_dialog_aceptar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.dialog_ir_a_dar_fe_de_vida).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.tuid_dialog_ir_a_registro).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });


        dialog.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height =  WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);
    }


    public static void show(Activity context, String titulo, String msg,String error_code, View.OnClickListener listener){
        final Dialog dialog = new Dialog(context);
        Log.e("ERROR CODE", error_code +"");
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        dialog.setContentView(R.layout.v2_tuid_dialog);
        ((TextView)dialog.findViewById(R.id.tuid_dialog_title)).setText(titulo);
        ((TextView)dialog.findViewById(R.id.tuid_dialog_text)).setText(msg);

        dialog.findViewById(R.id.tuid_dialog_ir_a_fe_de_vida).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                HomeActivity.irAFeDeVidaOnResume = true;
                context.finish();
            }
        });

        dialog.findViewById(R.id.tuid_dialog_ir_a_registro).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                HomeActivity.irARegistroOnResume = true;
                context.finish();
            }
        });

        dialog.findViewById(R.id.tuid_dialog_aceptar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if(listener!=null) listener.onClick(view);
            }
        });

        if(error_code.equalsIgnoreCase( "10012")) dialog.findViewById(R.id.tuid_dialog_ir_a_registro).setVisibility(View.VISIBLE);
        if(error_code.equalsIgnoreCase("10009")) dialog.findViewById(R.id.tuid_dialog_ir_a_fe_de_vida).setVisibility(View.VISIBLE);


        dialog.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height =  WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);
    }
}
