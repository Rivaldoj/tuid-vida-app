package com.autenticar.tuid.utils;

import android.content.Context;
import android.util.Log;

import com.autenticar.tuid.R;
import com.autenticar.tuid.v2.model.TelefonoCodigoPais;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class CountriesUtil {

    static List<TelefonoCodigoPais> codigos_pais = new ArrayList<>();

    List<TelefonoCodigoPais> getTelefonosCodigoPais(Context context){
        if(codigos_pais.size()>0) return codigos_pais;
        else{
            return getCodigosPais(context);
        }
    }


    public static List<TelefonoCodigoPais> getCodigosPais(Context context){
        InputStream is = context.getResources().openRawResource(R.raw.countries);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        }
        catch (Exception e){
            Log.e("EX", e.getMessage());
        }
        finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e("EX2", e.getMessage());
            }
        }

        try {
            JSONObject ret = new JSONObject(writer.toString());
            codigos_pais.clear();
            JSONArray ar = ret.getJSONArray("countries");
            for(int i = 0; i<ar.length(); i++){
                try {
                    JSONObject o = ar.getJSONObject(i);
                    codigos_pais.add(new TelefonoCodigoPais(o.getString("cou_name_es"), o.getString("cou_prefix")));
                }
                catch (Exception e){
                    Log.e("EX3", e.getMessage());
                }
            }
        }
        catch (Exception e){
            Log.e("EX4", e.getMessage());
        }
        return codigos_pais;
    }
}
