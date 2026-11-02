package com.autenticar.tuid.v2;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.autenticar.tuid.R;
import com.autenticar.tuid.services.TuidService;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.autenticar.tuid.utils.HelperFunctions;
import com.autenticar.tuid.v2.services.Entidad;
import com.autenticar.tuid.v2.services.GetEntidadesRequest;
import com.autenticar.tuid.v2.services.GetEntitiesResponse;
import com.autenticar.tuid.v2.utils.LoadingDialog;
import com.bumptech.glide.Glide;

import java.util.List;

import androidx.appcompat.widget.LinearLayoutCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EntidadesAdheridasActivity extends TuIdActivity{

    @Override
    public void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.v2_entidades_adheridas);
        setTitle("Entidades Adheridas");

        findViewById(R.id.entidades_adheridas_volver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        final GetEntidadesRequest request = new GetEntidadesRequest();
        TuidService service = ServiceGenerator.createService(TuidService.class);
        Call<GetEntitiesResponse> callUser = service.getEntities();
        LoadingDialog.getInstance().show(activity, "Aguarde un momento, procesando la información");
        callUser.enqueue(new Callback<GetEntitiesResponse>() {
            @Override
            public void onResponse(Call<GetEntitiesResponse> call, Response<GetEntitiesResponse> response) {
                LoadingDialog.getInstance().hide();
                LinearLayoutCompat lo = findViewById(R.id.entidades_adheridas_entidades_list_lo);
                LinearLayoutCompat.LayoutParams params = new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(10,10,10,10);
                LayoutInflater flater = getLayoutInflater();
                List<Entidad> entidades = response.body().entidades;
                for(Entidad e: entidades){
                    try{
                        Log.e("ASD", e.img);
                        View v = flater.inflate(R.layout.v2_entidad_adherida_item, null);
                        ImageView iv = v.findViewById(R.id.entidad_adherida_item_image);
                        Glide.with(context).load(e.img).into(iv);
                        lo.addView(v,params);
                    }
                    catch (Exception ex){

                    }
                }
            }

            @Override
            public void onFailure(Call<GetEntitiesResponse> call, Throwable t) {
                LoadingDialog.getInstance().hide();
                HelperFunctions.ShowAlertWithContact(EntidadesAdheridasActivity.this, context.getString(R.string.no_hay_conexion_a_internet), 1, "0");
                Log.e("ERROR", "FAIL");
            }
        });
    }

}
