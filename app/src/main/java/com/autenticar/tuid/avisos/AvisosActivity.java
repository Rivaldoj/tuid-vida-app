package com.autenticar.tuid.avisos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.autenticar.tuid.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** "Mis avisos": los mensajes que TuID Vida le envio a este telefono, del mas nuevo al mas viejo. */
public class AvisosActivity extends AppCompatActivity {

    private ListView lista;
    private TextView vacio;
    private AvisosAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avisos);
        com.autenticar.tuid.utils.EdgeInsets.aplicar(findViewById(R.id.raiz));
        lista = findViewById(R.id.avisos_lista);
        vacio = findViewById(R.id.avisos_vacio);
        adapter = new AvisosAdapter();
        lista.setAdapter(adapter);
        findViewById(R.id.avisos_volver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        AvisosStore store = AvisosStore.get(this);
        store.importarActivas(this);
        long visto = store.marcarTodosLeidos(); // lo anterior a esta marca ya estaba leido
        List<AvisosStore.Aviso> avisos = store.listar();
        adapter.cargar(avisos, visto);
        vacio.setVisibility(avisos.isEmpty() ? View.VISIBLE : View.GONE);
        lista.setVisibility(avisos.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private class AvisosAdapter extends BaseAdapter {
        private List<AvisosStore.Aviso> items = new ArrayList<>();
        private long visto;
        private final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("es", "AR"));

        void cargar(List<AvisosStore.Aviso> nuevos, long vistoAntes) {
            items = nuevos;
            visto = vistoAntes;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup parent) {
            View v = convertView != null ? convertView
                    : LayoutInflater.from(AvisosActivity.this).inflate(R.layout.item_aviso, parent, false);
            AvisosStore.Aviso a = items.get(i);
            ((TextView) v.findViewById(R.id.aviso_titulo)).setText(a.titulo.isEmpty() ? "TuID Vida" : a.titulo);
            ((TextView) v.findViewById(R.id.aviso_mensaje)).setText(a.mensaje);
            ((TextView) v.findViewById(R.id.aviso_fecha)).setText(formato.format(new Date(a.fecha)));
            v.findViewById(R.id.aviso_nuevo).setVisibility(a.fecha > visto ? View.VISIBLE : View.GONE);
            return v;
        }
    }
}
