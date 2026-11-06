package com.autenticar.tuid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.autenticar.tuid.model.CUIL;
import com.autenticar.tuid.utils.SQLHelper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;

import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.autenticar.tuid.usuarios.HistorialSync;
import com.autenticar.tuid.usuarios.LocalUser;
import com.autenticar.tuid.usuarios.LocalUserStore;
import com.autenticar.tuid.utils.CuilValidator;


public class HistoryActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ListView list_cuil;
    public ArrayList<CUIL> item;
    private static HistoryAdapter historyAdapter;
    final Activity activity = this;
    private SQLHelper sql;
    private GoogleMap mMap;
    private String TAG="HistoryActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_cuil);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        /*MAPA*/
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.fragment_map);
        TextView text_title_info_empresarialdialog = (TextView) dialog.findViewById(R.id.text_title_info_empresarial);
       // TextView text_content_info_empresarial = (TextView) dialog.findViewById(R.id.text_content_info_empresarial);
        text_title_info_empresarialdialog.setText("Ubicación de registro");
        ImageView btn_cancel = (ImageView) dialog.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        ImageView btn_return = (ImageView) dialog.findViewById(R.id.btn_return);
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.frag_map);
            mapFragment.getMapAsync(this);
        }catch (Exception e){
            Log.i(TAG, "SupportMapFragment Error: "+e);
        }

        list_cuil = findViewById(R.id.list_cuil);
        sql = new SQLHelper(this);
        mapaDialog = dialog;
        if (BuildConfig.USE_CUIL_API) {
            configurarPorUsuario();
        } else {
            cargarLista(null);
        }
    }

    private Dialog mapaDialog;
    private String cuilSeleccionado;

    /** Carga la lista local; con cuilFiltro muestra solo las certificaciones de esa persona (nunca se mezclan). */
    private void cargarLista(String cuilFiltro) {
        ArrayList<CUIL> todos = sql.getAllCUIL();
        item = new ArrayList<>();
        for (CUIL c : todos) {
            if (cuilFiltro == null || cuilFiltro.equals(CuilValidator.normalizar(c.getNum_cuil()))) item.add(c);
        }
        historyAdapter = new HistoryAdapter(this, item, mapaDialog, mMap);
        list_cuil.setAdapter(historyAdapter);
        TextView vacio = findViewById(R.id.no_registro);
        if (historyAdapter.getCount() == 0) {
            if (BuildConfig.USE_CUIL_API && cuilFiltro != null) {
                vacio.setText("No hay certificaciones guardadas en este dispositivo. Puede recuperar sus últimas 20 certificaciones desde el sistema.");
            }
            vacio.setVisibility(View.VISIBLE);
        } else {
            vacio.setVisibility(View.GONE);
        }
    }

    /** Selector de usuario (o usuario unico) y boton "Actualizar historial". */
    private void configurarPorUsuario() {
        final java.util.List<LocalUser> usuarios = LocalUserStore.get(this).list();
        findViewById(R.id.history_barra).setVisibility(View.VISIBLE);
        final TextView mensaje = findViewById(R.id.history_mensaje);
        final Button actualizar = findViewById(R.id.history_actualizar);
        if (usuarios.isEmpty()) {
            actualizar.setVisibility(View.GONE);
            mensaje.setVisibility(View.VISIBLE);
            mensaje.setText("Solo podrá actualizar su historial luego de la próxima certificación.");
            cargarLista(null);
            return;
        }
        // arranca con la persona que hizo la Fe de Vida mas reciente (la ultima de la lista)
        cuilSeleccionado = usuarios.get(usuarios.size() - 1).cuil;
        if (usuarios.size() == 1) {
            TextView unico = findViewById(R.id.history_usuario_unico);
            unico.setVisibility(View.VISIBLE);
            unico.setText(usuarios.get(0).nombre);
        } else {
            Spinner spinner = findViewById(R.id.history_usuario);
            java.util.List<String> etiquetas = new ArrayList<>();
            for (LocalUser u : usuarios) etiquetas.add(u.nombre + " — CUIL: " + CuilValidator.formatear(u.cuil));
            spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, etiquetas));
            spinner.setSelection(usuarios.size() - 1);
            findViewById(R.id.history_usuario_label).setVisibility(View.VISIBLE);
            spinner.setVisibility(View.VISIBLE);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    cuilSeleccionado = usuarios.get(position).cuil;
                    mensaje.setVisibility(View.GONE);
                    cargarLista(cuilSeleccionado);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
        cargarLista(cuilSeleccionado);
        actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String cuil = cuilSeleccionado;
                actualizar.setEnabled(false);
                mensaje.setVisibility(View.VISIBLE);
                mensaje.setText("Actualizando su historial…");
                HistorialSync.actualizar(HistoryActivity.this, cuil, new HistorialSync.Resultado() {
                    @Override
                    public void onTerminado(final boolean ok, final String texto) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                actualizar.setEnabled(true);
                                mensaje.setText(texto);
                                if (ok && cuil.equals(cuilSeleccionado)) cargarLista(cuil);
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history_curl, menu);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
            search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
            search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    //if (!s.isEmpty()) {
                        //historyAdapter.filter(s);
                    //}
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    //if(!s.isEmpty()){
                    historyAdapter.filter(s);
                    historyAdapter.notifyDataSetChanged();
                    if (historyAdapter.getCount() == 0) {
                        findViewById(R.id.no_registro).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.no_registro).setVisibility(View.GONE);
                    }
                    //}
                    return false;
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_delete) {
            AlertDialog.Builder dialogo = new AlertDialog.Builder(activity);
            dialogo.setTitle(R.string.alert_titulo_alerta);
            dialogo.setMessage("¿Desea eliminar todo el historial de registro?");
            dialogo.setCancelable(true);
            dialogo.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo, int id) {
                    dialogo.cancel();
                }
            });
            dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo, int id) {
                    sql = new SQLHelper(activity);
                    sql.deleteCUILAll();
                    historyAdapter.clear();
                    list_cuil.setAdapter(historyAdapter);
                    list_cuil.deferNotifyDataSetChanged();
                    historyAdapter.notifyDataSetChanged();
                    if (historyAdapter.getCount() == 0) {
                        findViewById(R.id.no_registro).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.no_registro).setVisibility(View.GONE);
                    }
                }
            });
            /*Cambio de color los botones del alert*/
            AlertDialog alert = dialogo.create();
            alert.show();
            Button btn_negativo = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
            btn_negativo.setTextColor(Color.BLACK);
            Button btn_positivo = alert.getButton(DialogInterface.BUTTON_POSITIVE);
            btn_positivo.setTextColor(Color.BLACK);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        historyAdapter.setmMap(mMap);
        historyAdapter.notifyDataSetChanged();
    }
}
