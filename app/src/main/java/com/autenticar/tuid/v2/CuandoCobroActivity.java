package com.autenticar.tuid.v2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.autenticar.tuid.AmplitudeManager.AmplitudeManager;
import com.autenticar.tuid.R;
import com.autenticar.tuid.services.TuidService;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.autenticar.tuid.utils.HelperFunctions;
import com.autenticar.tuid.v2.services.GetCuandoCobroRequest;
import com.autenticar.tuid.v2.services.GetCuandoCobroResponse;
import com.autenticar.tuid.v2.services.GetEntidadesRequest;
import com.autenticar.tuid.v2.services.GetEntitiesResponse;
import com.autenticar.tuid.v2.services.Pago;
import com.autenticar.tuid.v2.utils.LoadingDialog;

import org.json.JSONObject;

import java.text.SimpleDateFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CuandoCobroActivity extends TuIdActivity{

    @Override
    public void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.cuando_cobro_activity);
        setTitle("Cuando cobro");

        String dni = getIntent().getStringExtra("dni");
        String sexo = getIntent().getStringExtra("sexo");
        String cuil = getIntent().getStringExtra("cuil");

        // por CUIL no se registra ningun dato personal en el analisis de uso
        logAmplitude(cuil != null ? "cuil" : dni, cuil != null ? "" : sexo);
        final GetCuandoCobroRequest request = new GetCuandoCobroRequest();
        TuidService service = ServiceGenerator.createService(TuidService.class);
        Call<GetCuandoCobroResponse> callUser = cuil != null
                ? service.getCuandoCobroPorCuil(cuil)
                : service.getCuandoCobro(dni, sexo);
        LoadingDialog.getInstance().show(this, "Aguarde un momento, procesando la información");
        callUser.enqueue(new Callback<GetCuandoCobroResponse>() {
            @Override
            public void onResponse(Call<GetCuandoCobroResponse> call, Response<GetCuandoCobroResponse> response) {
                LoadingDialog.getInstance().hide();
                LinearLayout lo = findViewById(R.id.cuando_cobro_lo);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0,0,0,(int) (14 * getResources().getDisplayMetrics().density));
                try {
                    for (Pago p : response.body().getPagos()) {
                        View v = getLayoutInflater().inflate(R.layout.cobro_layout, null);
                        String banco = p.getBanco() == null ? "" : p.getBanco();
                        String sucursal = p.getSucursal() == null ? "" : " - " + p.getSucursal();
                        String domicilio = p.getDomicilio() == null ? "" : p.getDomicilio() + ", ";
                        String localidad = p.getLocalidad() == null ? "" : p.getLocalidad() + ", ";
                        String provincia = p.getProvincia() == null ? "" : p.getProvincia();
                        ((TextView) v.findViewById(R.id.cobro_layout_nombre)).setText(p.getNombres());
                        ((TextView) v.findViewById(R.id.cuando_cobro_caja)).setText(p.getCaja());
                        ((TextView) v.findViewById(R.id.cobro_layout_entidad_pagadora)).setText(banco + sucursal);
                        ((TextView) v.findViewById(R.id.cobro_layout_entidad_pagadora_domicilio)).setText(domicilio + localidad + provincia);

                        ((TextView) v.findViewById(R.id.cuando_cobro_actual)).setText("Actual:\n" + p.getFechaActual().substring(0, 10));
                        ((TextView) v.findViewById(R.id.cuando_cobro_proxima)).setText("Próxima:\n" + p.getFechaProxima().substring(0, 10));

                        lo.addView(v, lp);
                    }
                    if (response.body().getPagos().size() == 0)
                        findViewById(R.id.cuando_cobro_no_info).setVisibility(View.VISIBLE);
                }
                catch (Exception e){
                    Log.e("EX", e.getMessage());
                    findViewById(R.id.cuando_cobro_no_info).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<GetCuandoCobroResponse> call, Throwable t) {
                LoadingDialog.getInstance().hide();
                HelperFunctions.ShowAlertWithContact(CuandoCobroActivity.this, context.getString(R.string.no_hay_conexion_a_internet), 1, "0");
                Log.e("ERROR", "FAIL");
            }

        });
    }


    void logAmplitude(String document, String gender){
        try{
            JSONObject data = new JSONObject();
            data.put("document", document);
            data.put("gender", gender);
            new AmplitudeManager().log(this, "Check Payments", data);
        }
        catch (Exception e){

        }

    }
}
