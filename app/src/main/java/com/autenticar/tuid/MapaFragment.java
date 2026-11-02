package com.autenticar.tuid;

import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapaFragment extends SupportMapFragment   {

    public static double latitud;
    public static double longitud;
    public static String cuil;
    public static GoogleMap googleMap;
    public String TAG="MapaFragment";
    public SupportMapFragment mapFragment;

    public MapaFragment() {
    }

    public static MapaFragment newInstance(double mLatitud, double mLongitud, String mCuil) {
        latitud = mLatitud;
        longitud = mLongitud;
        cuil = mCuil;
        MapaFragment fragment = new MapaFragment();
        return fragment;
    }

    /*@Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.frag_map);
        mapFragment.getMapAsync(this);
        System.out.println(" onCreateView: ");
        return view;
    }*/

    /*@Override
    public void onMapReady(GoogleMap googleMap) {
        System.out.println("Entro en mapa al fin fragmento");
        LatLng point= new LatLng(7.7785362, -72.2391548);
        googleMap.addMarker(new MarkerOptions()
                .position(point)
                .title("Cali la Sucursal del cielo"));
        CameraPosition cameraPosition = CameraPosition.builder()
                .target(point)
                .zoom(10)
                .build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }*/


   /* @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        System.out.println(" onAttach: ");
        try {
            mCallback = (FragmentInteractionListener) context;
        }catch (Exception e){
            Log.e(TAG, String.valueOf(e));
        }
    }

    @Override
    public void onDetach() {
        System.out.println(" onDetach: ");
        mCallback=null;
        super.onDetach();
    }

    public interface FragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
   */

}
