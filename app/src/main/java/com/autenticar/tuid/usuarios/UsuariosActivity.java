package com.autenticar.tuid.usuarios;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.autenticar.tuid.R;
import com.autenticar.tuid.utils.CuilValidator;
import com.autenticar.tuid.v2.TerminosYCondicionesActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Lista de personas guardadas en este telefono. Permite quitarlas del telefono (con confirmacion) y
 * llevar a la registracion de otra persona. Las personas se agregan solas despues de una Fe de Vida.
 * Quitar a alguien NO borra su registro ni sus certificaciones en el sistema.
 */
public class UsuariosActivity extends AppCompatActivity {

    private LocalUserStore store;
    private ListView lista;
    private TextView vacio;
    private UsuariosAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios);
        com.autenticar.tuid.utils.EdgeInsets.aplicar(findViewById(R.id.raiz));
        store = LocalUserStore.get(this);
        lista = findViewById(R.id.usuarios_lista);
        vacio = findViewById(R.id.usuarios_vacio);
        adapter = new UsuariosAdapter(new ArrayList<LocalUser>());
        lista.setAdapter(adapter);
        Button agregar = findViewById(R.id.usuarios_agregar);
        agregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UsuariosActivity.this, TerminosYCondicionesActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refrescar();
    }

    private void refrescar() {
        List<LocalUser> usuarios = store.list();
        adapter.clear();
        adapter.addAll(usuarios);
        adapter.notifyDataSetChanged();
        vacio.setVisibility(usuarios.isEmpty() ? View.VISIBLE : View.GONE);
        lista.setVisibility(usuarios.isEmpty() ? View.GONE : View.VISIBLE);
    }

    /**
     * Elegir un nombre NO certifica nada: devuelve el CUIL a la pantalla principal, que arranca la Fe de Vida
     * completa (controles de camara, prueba de vida y comparacion facial en el servidor).
     */
    private void iniciarFeDeVida(LocalUser u) {
        Intent r = new Intent();
        r.putExtra("cuil", u.cuil);
        setResult(RESULT_OK, r);
        finish();
    }

    private void confirmarQuitar(final LocalUser u) {
        new AlertDialog.Builder(this)
                .setTitle("Quitar usuario")
                .setMessage("¿Desea quitar este usuario del dispositivo? Su registración y sus certificaciones permanecerán en el sistema.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Quitar usuario", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface d, int w) {
                        if (!store.remove(u.cuil)) {
                            Toast.makeText(UsuariosActivity.this, "No pudimos quitar al usuario. Intente nuevamente.", Toast.LENGTH_LONG).show();
                        }
                        refrescar();
                    }
                })
                .show();
    }

    private class UsuariosAdapter extends ArrayAdapter<LocalUser> {
        UsuariosAdapter(List<LocalUser> items) {
            super(UsuariosActivity.this, 0, items);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = convertView != null ? convertView
                    : LayoutInflater.from(getContext()).inflate(R.layout.item_usuario, parent, false);
            final LocalUser u = getItem(position);
            ((TextView) v.findViewById(R.id.usuario_nombre)).setText(u.nombre);
            ((TextView) v.findViewById(R.id.usuario_cuil)).setText("CUIL: " + CuilValidator.formatear(u.cuil));
            View feDeVida = v.findViewById(R.id.usuario_fe_de_vida);
            // La Fe de Vida por seleccion de usuario solo existe cuando la app identifica por CUIL
            feDeVida.setVisibility(com.autenticar.tuid.BuildConfig.USE_CUIL_API ? View.VISIBLE : View.GONE);
            View.OnClickListener iniciar = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    iniciarFeDeVida(u);
                }
            };
            feDeVida.setOnClickListener(iniciar);
            v.setOnClickListener(com.autenticar.tuid.BuildConfig.USE_CUIL_API ? iniciar : null);
            v.findViewById(R.id.usuario_quitar).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    confirmarQuitar(u);
                }
            });
            return v;
        }
    }
}
