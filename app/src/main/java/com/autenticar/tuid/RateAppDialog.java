package com.autenticar.tuid;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RateAppDialog extends Dialog {

    public RateAppDialog(final Context context){
        super(context);
        setContentView(R.layout.rate_app_dialog_layout);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        findViewById(R.id.dialog_text_box_close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        ((Button)findViewById(R.id.dialog_text_box_accept_button)).setText("IR A CALIFICAR");
        findViewById(R.id.dialog_text_box_accept_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    try{
                        SharedPreferences sp = context.getSharedPreferences("TuIDVidaSP", Context.MODE_PRIVATE);
                        sp.edit().putString("AppRated","1").apply();
                    }
                    catch (Exception e){

                    }
                    Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    try {
                        context.startActivity(goToMarket);
                    } catch (ActivityNotFoundException e) {
                    }
                    dismiss();
            }
        });


//        findViewById(R.id.dialog_text_box_reject_button).setVisibility(View.VISIBLE);
        findViewById(R.id.dialog_text_box_reject_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        ((TextView)findViewById(R.id.dialog_text_box_title)).setText("Califica nuestra App");
        ((TextView)findViewById(R.id.dialog_text_box_desc)).setText("¿Te pareció útil nuestra app?\n¿Te interesaría calificarla en playstore?");
        setCanceledOnTouchOutside(true);
        setCancelable(true);
        show();
    }
}
