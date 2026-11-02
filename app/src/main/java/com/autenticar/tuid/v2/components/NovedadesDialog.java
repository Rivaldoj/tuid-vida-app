package com.autenticar.tuid.v2.components;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.autenticar.tuid.R;
import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;

public class NovedadesDialog extends Dialog {
    public NovedadesDialog(@NonNull Context context, String image_url, String link) {
        super(context);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.novedades_dialog);

        ImageView image = findViewById(R.id.novedades_image);
        Glide.with(context).load(image_url).into(image);

        if(link!=null && link.length()>0 && link.startsWith("http")){
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
                }
            });
        }

        findViewById(R.id.novedad_cerrar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        findViewById(R.id.novedad_cerrar_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        show();
        Window window = getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }
}
