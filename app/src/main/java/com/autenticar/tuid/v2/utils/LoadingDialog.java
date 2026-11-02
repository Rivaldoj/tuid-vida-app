package com.autenticar.tuid.v2.utils;

import android.app.Activity;
import android.app.ProgressDialog;

public class LoadingDialog {

    ProgressDialog mProgressDialog;
    private static LoadingDialog self;


    public static LoadingDialog getInstance(){
        if(self == null) self = new LoadingDialog();
        return self;
    }
    
    public void show(Activity context, String message){
        if(mProgressDialog!=null){
            try{
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            catch (Exception e){

            }
        }
        if (mProgressDialog == null) {
            try{
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(message);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setIndeterminate(true);
            }
            catch (Exception e){

            }

        }
        if(mProgressDialog!=null) mProgressDialog.show();
    }

    public void hide(){
        try {
            if (mProgressDialog != null) mProgressDialog.dismiss();
        }
        catch (Exception e){

        }
    }




}
