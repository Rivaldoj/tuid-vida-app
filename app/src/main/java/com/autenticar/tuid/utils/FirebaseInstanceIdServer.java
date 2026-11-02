//package com.autenticar.tuid.utils;
//
//
//import android.util.Log;
//
//import com.google.firebase.iid.FirebaseInstanceId;
//import com.google.firebase.iid.FirebaseInstanceIdService;
//
//public class FirebaseInstanceIdServer extends FirebaseInstanceIdService {
//    public static final String TAG = "ID";
//
//    @Override
//    public void onTokenRefresh() {
//        super.onTokenRefresh();
//        String token = FirebaseInstanceId.getInstance().getToken();
//        Log.d(TAG, "Token: "+token);
//    }
//}
