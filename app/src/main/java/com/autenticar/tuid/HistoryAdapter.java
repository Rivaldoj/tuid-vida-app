package com.autenticar.tuid;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.autenticar.tuid.model.CUIL;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;


public class HistoryAdapter extends BaseAdapter {
    private List<CUIL> item;
    private List<CUIL> itemCopy = new ArrayList<>();
    private Context context;
    private double lat = 0;
    private double lon = 0;
    private GoogleMap mMap;
    private Dialog dialog;


    public HistoryAdapter(Context context, List<CUIL> item,  Dialog dialog, GoogleMap mMap) {
        this.context = context;
        this.item = item;
        this.itemCopy.addAll(item);
        this.mMap=mMap;
        this.dialog=dialog;
    }

    public GoogleMap getmMap() {
        return mMap;
    }

    public void setmMap(GoogleMap mMap) {
        this.mMap = mMap;
    }

    @Override
    public int getCount() {
        return item.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.content_history_cuil, parent, false);
            viewHolder.num_cuil = (TextView) convertView.findViewById(R.id.num_cuil);
            viewHolder.date_cuil = (TextView) convertView.findViewById(R.id.date_cuil);
            viewHolder.img_gps = (ImageView) convertView.findViewById(R.id.ico_gps);
            viewHolder.entidad_pagadora = (TextView) convertView.findViewById(R.id.num_entidad_pagadora);
            viewHolder.entidad_pagadora_label = (TextView) convertView.findViewById(R.id.num_entidad_pagadora_label);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (!item.get(position).getNum_cuil().equals("")) {
            viewHolder.id = position;
            viewHolder.num_cuil.setText(item.get(position).getNum_cuil());
            viewHolder.date_cuil.setText(item.get(position).getDate_cuil());
            viewHolder.latitud = item.get(position).getLatitude_cuil();
            viewHolder.longitud = item.get(position).getLongitude_cuil();

            String entidad_pagadora = item.get(position).getEntidad_pagadora_cuil();
            String entidades = "";


            if(entidad_pagadora!=null && entidad_pagadora.length()>0){
                for(String e: entidad_pagadora.split("  ")){
                    if(e.replace(" ", "").length()>0){
                        if(entidades.length()>0) entidades+="\n";
                        entidades+=e.trim();
                    }
                }
            }

            
            if(entidad_pagadora!=null && entidad_pagadora.length()>0)viewHolder.entidad_pagadora.setText(entidades);
            else{
                viewHolder.entidad_pagadora.setVisibility(View.GONE);
                viewHolder.entidad_pagadora_label.setVisibility(View.GONE);
            }

            if ((viewHolder.longitud != null && viewHolder.latitud != null )&& (!viewHolder.longitud.equals("0") && !viewHolder.latitud.equals("0")) ) {
                viewHolder.img_gps.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        lat = Double.parseDouble(viewHolder.latitud);
                        lon = Double.parseDouble(viewHolder.longitud);
                        LatLng point= new LatLng(lat, lon);
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions()
                                .position(point)
                                .visible(true)
                                .snippet(item.get(position).getDiretion_cuil())
                                .alpha(2)
                                .title(viewHolder.num_cuil.getText().toString()));

                        mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
                        CameraPosition cameraPosition = CameraPosition.builder()
                                .target(point)
                                .zoom(15)
                                .build();
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                marker.setAlpha(3);
                                marker.showInfoWindow();
                            }
                        });
                        dialog.show();
                    }
                });
                viewHolder.img_gps.setVisibility(View.VISIBLE);
            } else {
                viewHolder.img_gps.setVisibility(View.INVISIBLE);
            }
        }
        return convertView;
    }

    public void clear() {
        item.clear();
    }

    public static class ViewHolder {
        int id;
        TextView num_cuil;
        TextView date_cuil;
        String longitud;
        String latitud;
        TextView entidad_pagadora;
        TextView entidad_pagadora_label;
        ImageView img_gps;
    }


    public void filter(String value) {
        item.clear();
        if (value.length() == 0) {
            item.addAll(itemCopy);
        } else {
            for (CUIL item : itemCopy) {
                if (item.getNum_cuil().contains(value)) {
                    this.item.add(item);
                }
            }
        }
    }
}
