package com.autenticar.tuid.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.autenticar.tuid.R;


public class TuIdTextBox extends Dialog {

//    ******************************************************************************************
//    ********                                                                          ********
//    ********                           VERSION 1.0                                    ********
//    ********                                                                          ********
//    ******************************************************************************************

    public TuIdTextBox(Context context, String title, String text){
        super(context);
        setContentView(R.layout.dialog_text_box);
        try {
            getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        }
        catch (Exception e){}
        findViewById(R.id.dialog_text_box_close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        findViewById(R.id.dialog_text_box_accept_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        try{
        ((TextView)findViewById(R.id.dialog_text_box_title)).setText(title);
        ((TextView)findViewById(R.id.dialog_text_box_desc)).setText(text);
        }
        catch (Exception e){}

        try {
            setCanceledOnTouchOutside(true);
            setCancelable(true);
            show();
        }
        catch (Exception e){

        }
    }
}
