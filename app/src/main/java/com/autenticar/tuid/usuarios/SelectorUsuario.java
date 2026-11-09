package com.autenticar.tuid.usuarios;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.autenticar.tuid.R;
import com.autenticar.tuid.utils.CuilValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Pregunta "quien" va a usar la funcion (Fe de Vida, Cuando cobro, Certificado).
 *  - Un solo usuario guardado: queda seleccionado, solo se confirma; hay un boton visible "Nuevo usuario".
 *  - Varios: se elige de una lista (nombre + CUIL), tambien con "Nuevo usuario".
 *  - Ninguno: se explica y se ofrece Registrarse o Vincular usuario (ingresar el CUIL).
 * Elegir un nombre NO autoriza nada por si solo: cada Fe de Vida repite la prueba de vida en el servidor.
 */
public class SelectorUsuario {

    public interface Callback {
        void onCuil(String cuil);

        void onRegistrar();
    }

    private final Activity activity;
    private final LocalUserStore store;
    private final String accion;
    private final Callback callback;
    private AlertDialog dialog;
    private View contenido;
    private List<LocalUser> usuarios;

    /** @param accion texto corto de la funcion, por ejemplo "su Fe de Vida" */
    public SelectorUsuario(Activity activity, LocalUserStore store, String accion, Callback callback) {
        this.activity = activity;
        this.store = store;
        this.accion = accion;
        this.callback = callback;
    }

    public void mostrar() {
        usuarios = store.list();
        if (usuarios.isEmpty()) mostrarVacio();
        else mostrarSeleccion();
    }

    /** Arma un cuadro nuevo con el contenido base y lo muestra en lugar del anterior. */
    private void nuevoCuadro(String titulo, String mensaje) {
        contenido = LayoutInflater.from(activity).inflate(R.layout.dialog_selector_usuario, null);
        texto(R.id.picker_titulo).setText(titulo);
        texto(R.id.picker_mensaje).setText(mensaje);
        AlertDialog nuevo = new AlertDialog.Builder(activity, R.style.DialogoTuID).setView(contenido).setCancelable(true).create();
        nuevo.setCanceledOnTouchOutside(false);   // un toque fuera del cuadro no lo cierra (se cierra con Cancelar)
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
        dialog = nuevo;
        dialog.show();
        ((Button) contenido.findViewById(R.id.picker_cancelar)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private TextView texto(int id) {
        return (TextView) contenido.findViewById(id);
    }

    private void principal(String etiqueta, View.OnClickListener l) {
        Button b = (Button) contenido.findViewById(R.id.picker_principal);
        b.setText(etiqueta);
        b.setTextColor(Color.WHITE);
        b.setOnClickListener(l);
    }

    private void secundario(String etiqueta, View.OnClickListener l) {
        Button b = (Button) contenido.findViewById(R.id.picker_secundario);
        b.setText(etiqueta);
        b.setBackgroundResource(R.drawable.btn_outline);
        b.setTextColor(activity.getResources().getColor(R.color.colorPrimaryDark));
        b.setVisibility(View.VISIBLE);
        b.setOnClickListener(l);
    }

    // ---- sin usuarios guardados
    private void mostrarVacio() {
        nuevoCuadro("No hay usuarios en este teléfono",
                "No hay usuarios disponibles en este dispositivo. Para realizar una Fe de Vida, primero debe registrarse o, si ya se encuentra registrado en TuID Vida, vincular un usuario.");
        principal("Registrarse", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                callback.onRegistrar();
            }
        });
        secundario("Vincular usuario", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarCuil();
            }
        });
    }

    // ---- uno o varios usuarios guardados
    private void mostrarSeleccion() {
        final boolean unico = usuarios.size() == 1;
        nuevoCuadro(unico ? "Confirme el usuario" : "Seleccione el usuario",
                unico ? "Va a realizar " + accion + " para:" : "Seleccione el usuario que realizará " + accion + ".");
        final Spinner spinner = (Spinner) contenido.findViewById(R.id.picker_spinner);
        if (unico) {
            TextView u = texto(R.id.picker_unico);
            u.setVisibility(View.VISIBLE);
            u.setText(usuarios.get(0).nombre + "\nCUIL: " + CuilValidator.formatear(usuarios.get(0).cuil));
        } else {
            List<String> etiquetas = new ArrayList<>();
            etiquetas.add("Seleccione una persona…");
            for (LocalUser u : usuarios) {
                etiquetas.add(u.nombre + " — CUIL: " + CuilValidator.formatear(u.cuil));
            }
            spinner.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, etiquetas));
            spinner.setVisibility(View.VISIBLE);
        }
        principal("Continuar", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = unico ? 0 : spinner.getSelectedItemPosition() - 1;
                if (pos < 0) {
                    error("Seleccione una persona para continuar.");
                    return;
                }
                dialog.dismiss();
                callback.onCuil(usuarios.get(pos).cuil);
            }
        });
        secundario("Nuevo usuario", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarCuil();
            }
        });
    }

    // ---- ingreso manual de CUIL (primera vez o otra persona)
    private void mostrarCuil() {
        nuevoCuadro("Ingrese el CUIL",
                "Ingrese el CUIL de la persona que realizará " + accion + ".");
        final EditText cuil = (EditText) contenido.findViewById(R.id.picker_cuil);
        cuil.setVisibility(View.VISIBLE);
        texto(R.id.picker_ayuda).setVisibility(View.VISIBLE);
        principal("Continuar", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String n = CuilValidator.normalizar(cuil.getText().toString());
                String problema = CuilValidator.mensajeError(n);
                if (problema != null) {
                    error(problema);
                    return;
                }
                dialog.dismiss();
                callback.onCuil(n);
            }
        });
        cuil.requestFocus();
        dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void error(String mensaje) {
        TextView e = texto(R.id.picker_error);
        e.setText(mensaje);
        e.setVisibility(View.VISIBLE);
    }
}
