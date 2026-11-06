package com.autenticar.tuid.usuarios;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.autenticar.tuid.R;

/**
 * Cuadro de aviso con botones grandes y bien visibles (los botones de los cuadros estandar de Android
 * salen chicos y grises en esta app y se confunden con texto). Un boton principal y, opcionalmente, "Cancelar".
 */
public final class Cuadro {
    private Cuadro() {
    }

    /**
     * @param principal       texto del boton grande (siempre visible)
     * @param accionPrincipal se ejecuta al tocar el boton principal (puede ser null: solo cierra)
     * @param cancelar        texto del boton de abajo, o null para no mostrarlo
     */
    public static AlertDialog mostrar(Activity activity, String titulo, String mensaje,
                                      String principal, final Runnable accionPrincipal, String cancelar) {
        View v = LayoutInflater.from(activity).inflate(R.layout.dialog_selector_usuario, null);
        ((TextView) v.findViewById(R.id.picker_titulo)).setText(titulo);
        ((TextView) v.findViewById(R.id.picker_mensaje)).setText(mensaje);
        final AlertDialog d = new AlertDialog.Builder(activity).setView(v).setCancelable(false).create();
        d.setCanceledOnTouchOutside(false);

        Button p = (Button) v.findViewById(R.id.picker_principal);
        p.setText(principal);
        p.setTextColor(Color.WHITE);
        p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                d.dismiss();
                if (accionPrincipal != null) accionPrincipal.run();
            }
        });
        Button c = (Button) v.findViewById(R.id.picker_cancelar);
        if (cancelar == null) {
            c.setVisibility(View.GONE);
        } else {
            c.setText(cancelar);
            c.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    d.dismiss();
                }
            });
        }
        if (!activity.isFinishing()) d.show();
        return d;
    }
}
