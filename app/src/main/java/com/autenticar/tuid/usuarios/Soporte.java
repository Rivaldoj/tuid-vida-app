package com.autenticar.tuid.usuarios;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.autenticar.tuid.HistoryAdapter;
import com.autenticar.tuid.R;
import com.autenticar.tuid.utils.CuilValidator;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/** Contacto con Soporte por WhatsApp, con un mensaje ya escrito que dice quien es la persona y por que se comunica. */
public final class Soporte {
    /** Enlace de WhatsApp de Soporte de TuID Vida. */
    public static final String WHATSAPP_URL = "https://wa.me/5491123443283";

    private Soporte() {
    }

    /**
     * Texto que le llega a Soporte. Si no se conoce el nombre se deja "Nombre y apellido:" para que la persona
     * lo complete antes de enviar.
     */
    public static String mensaje(String nombreYApellido, String cuil, String motivo) {
        String nombre = nombreYApellido == null ? "" : nombreYApellido.trim();
        return "Hola, necesito ayuda de Soporte de TuID Vida.\n"
                + "Nombre y apellido: " + nombre + "\n"
                + "CUIL: " + CuilValidator.formatear(cuil) + "\n"
                + "Motivo: " + motivo;
    }

    /** wa.me/numero?text=...  (codificado para URL; los espacios como %20). */
    public static String urlWhatsApp(String texto) {
        try {
            return WHATSAPP_URL + "?text=" + URLEncoder.encode(texto, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            return WHATSAPP_URL;
        }
    }

    /** Busca el nombre entre las personas guardadas en este telefono (el servidor no lo informa antes de verificar identidad). */
    private static String nombreGuardado(Activity activity, String cuil) {
        try {
            String c = CuilValidator.normalizar(cuil);
            for (LocalUser u : LocalUserStore.get(activity).list()) {
                if (u.cuil.equals(c)) return HistoryAdapter.nombreYApellido(u.nombre);
            }
        } catch (Exception ignored) {
            // sin nombre: la persona lo completa en el mensaje
        }
        return null;
    }

    public static void abrirWhatsApp(Activity activity, String texto) {
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlWhatsApp(texto))));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "No pudimos abrir WhatsApp. Escriba al +54 9 11 2344-3283.", Toast.LENGTH_LONG).show();
        }
    }

    /** Registro desaprobado o sin completar: se lo manda con Soporte (no al formulario de contacto). */
    public static void mostrarRegistroIncompleto(final Activity activity, final String cuil) {
        final String conocido = nombreGuardado(activity, cuil);
        if (conocido != null) {
            // el nombre ya esta en el telefono: se manda directo
            Cuadro.mostrar(activity, "Registración incompleta",
                    "Su registración no fue completada o fue desaprobada. Comuníquese con Soporte.",
                    "Comunicarse con Soporte", new Runnable() {
                        @Override
                        public void run() {
                            abrirWhatsApp(activity, mensaje(conocido, cuil, "Registración incompleta"));
                        }
                    }, "Volver");
            return;
        }
        // No se conoce el nombre (el servidor no lo informa antes de verificar identidad): se pide, asi Soporte sabe quien escribe
        View v = LayoutInflater.from(activity).inflate(R.layout.dialog_selector_usuario, null);
        ((TextView) v.findViewById(R.id.picker_titulo)).setText("Registración incompleta");
        ((TextView) v.findViewById(R.id.picker_mensaje)).setText(
                "Su registración no fue completada o fue desaprobada. Escriba su nombre y apellido para que Soporte pueda identificarlo.");
        final EditText nombre = (EditText) v.findViewById(R.id.picker_cuil);   // se reutiliza el campo, pero como texto
        nombre.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        nombre.setFilters(new InputFilter[]{new InputFilter.LengthFilter(60)});
        nombre.setHint("Nombre y apellido");
        nombre.setVisibility(View.VISIBLE);
        final TextView error = (TextView) v.findViewById(R.id.picker_error);
        final AlertDialog d = new AlertDialog.Builder(activity, R.style.DialogoTuID).setView(v).setCancelable(false).create();
        d.setCanceledOnTouchOutside(false);

        Button enviar = (Button) v.findViewById(R.id.picker_principal);
        enviar.setText("Comunicarse con Soporte");
        enviar.setTextColor(Color.WHITE);
        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String n = nombre.getText().toString().trim();
                if (n.length() < 3) {
                    error.setText("Escriba su nombre y apellido para continuar.");
                    error.setVisibility(View.VISIBLE);
                    return;
                }
                d.dismiss();
                abrirWhatsApp(activity, mensaje(n, cuil, "Registración incompleta"));
            }
        });
        ((Button) v.findViewById(R.id.picker_cancelar)).setText("Volver");
        v.findViewById(R.id.picker_cancelar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                d.dismiss();
            }
        });
        if (!activity.isFinishing()) {
            d.show();
            d.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }
}
