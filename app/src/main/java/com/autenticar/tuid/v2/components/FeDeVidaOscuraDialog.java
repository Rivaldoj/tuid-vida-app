package com.autenticar.tuid.v2.components;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.autenticar.tuid.R;
import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;

public class FeDeVidaOscuraDialog extends Dialog {

    View.OnClickListener listener;
    public FeDeVidaOscuraDialog(@NonNull Context context, float oscuridad) {
        super(context);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.fe_de_vida_oscura_dialog);
        Log.e("OSCURIDAD", oscuridad + "");

//        ((TextView)findViewById(R.id.text_title_info_empresarial)).append(" " + oscuridad);
    }

    public void setListener(View.OnClickListener lister){
        this.listener = lister;
        findViewById(R.id.novedad_cerrar).setOnClickListener(listener);
    }

    @Override
    public void show(){
        super.show();
        Window window = getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }
}