package com.autenticar.tuid.v2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import com.autenticar.tuid.R;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Verifica, en este orden, que la app tenga permiso de camara, permiso de ubicacion y la ubicacion del
 * telefono activada, antes de empezar una Fe de Vida. Si falta algo explica en lenguaje simple que
 * falta y para que, ofrece "Permitir acceso" o "Abrir configuracion" (cuando Android ya no deja
 * volver a preguntar) y, al volver desde Ajustes, comprueba solo de nuevo.
 *
 * La ubicacion es obligatoria para certificar (zona desfavorable, residentes en el exterior y
 * trazabilidad ante fraude).
 */
public class PermisosFeDeVida {

    public interface Listener {
        /** Camara y ubicacion disponibles: se puede continuar. */
        void onListo();
    }

    public static final int REQUEST_CODE = 1501;
    private static final String PREFS = "tuid_permisos";

    private final Activity activity;
    private final Listener listener;
    private boolean esperandoAjustes = false;
    private AlertDialog dialogoActual;
    private int iconoPendiente = R.drawable.ic_alerta_camara; // icono del cuadro que se va a mostrar

    public PermisosFeDeVida(Activity activity, Listener listener) {
        this.activity = activity;
        this.listener = listener;
    }

    /** Evalua el estado actual; muestra el aviso que corresponda o llama a onListo(). */
    public void iniciar() {
        cerrarDialogo();
        esperandoAjustes = false;

        if (!tiene(Manifest.permission.CAMERA)) {
            iconoPendiente = R.drawable.ic_alerta_camara;
            pedir(Manifest.permission.CAMERA,
                    "Permiso de cámara necesario",
                    "TuID Vida necesita utilizar la cámara de su dispositivo para verificar su identidad mediante el reconocimiento facial. Por favor, habilite el acceso a la cámara para continuar.",
                    "Cámara");
            return;
        }
        iconoPendiente = R.drawable.ic_alerta_ubicacion;
        if (!tiene(Manifest.permission.ACCESS_FINE_LOCATION)) {
            pedir(Manifest.permission.ACCESS_FINE_LOCATION,
                    "Permiso de ubicación necesario",
                    "TuID Vida necesita acceder a su ubicación para completar las verificaciones de seguridad de esta operación. Por favor, habilite el permiso de ubicación para continuar.",
                    "Ubicación");
            return;
        }
        if (!ubicacionActivada()) {
            mostrar("Ubicación desactivada",
                    "Para certificar su Fe de Vida el teléfono debe tener la ubicación activada. Toque \"Activar ubicación\" y encienda el interruptor de ubicación.",
                    "Activar ubicación",
                    new Runnable() {
                        @Override
                        public void run() {
                            esperandoAjustes = true;
                            activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    });
            return;
        }
        listener.onListo();
    }

    /** Llamar desde Activity.onRequestPermissionsResult. */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != REQUEST_CODE) return;
        iniciar();   // vuelve a evaluar: continua con lo siguiente o explica lo que sigue faltando
    }

    /** Llamar desde Activity.onResume: si el usuario volvio de Ajustes, se comprueba solo. */
    public void onResume() {
        if (esperandoAjustes) {
            esperandoAjustes = false;
            iniciar();
        }
    }

    public void cerrarDialogo() {
        if (dialogoActual != null && dialogoActual.isShowing()) dialogoActual.dismiss();
        dialogoActual = null;
    }

    // ------------------------------------------------------------------

    private boolean tiene(String permiso) {
        return ContextCompat.checkSelfPermission(activity, permiso) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean ubicacionActivada() {
        try {
            LocationManager lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            return lm != null && (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        } catch (Exception e) {
            return false;
        }
    }

    private void pedir(final String permiso, String titulo, String mensaje, final String nombrePermiso) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        boolean preguntadoAntes = prefs.getBoolean("asked_" + permiso, false);
        boolean yaNoPregunta = preguntadoAntes
                && !ActivityCompat.shouldShowRequestPermissionRationale(activity, permiso);

        if (yaNoPregunta) {
            // Android no vuelve a mostrar el cuadro del sistema: hay que habilitarlo en Ajustes
            String guia = mensaje
                    + "\n\nPara habilitarlo:\n1. Toque \"Abrir configuración\".\n2. Entre en \"Permisos\".\n3. Elija \""
                    + nombrePermiso + "\" y seleccione \"Permitir solo con la app en uso\"."
                    + (permiso.equals(Manifest.permission.ACCESS_FINE_LOCATION) ? " Active también \"Usar ubicación precisa\"." : "")
                    + "\n4. Vuelva a TuID Vida.";
            mostrar(titulo, guia, "Abrir configuración", new Runnable() {
                @Override
                public void run() {
                    esperandoAjustes = true;
                    Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", activity.getPackageName(), null));
                    activity.startActivity(i);
                }
            });
        } else {
            mostrar(titulo, mensaje, "Permitir acceso", new Runnable() {
                @Override
                public void run() {
                    activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                            .edit().putBoolean("asked_" + permiso, true).apply();
                    ActivityCompat.requestPermissions(activity, new String[]{permiso}, REQUEST_CODE);
                }
            });
        }
    }

    /** Cuadro con icono de alerta, texto y dos botones tipo chip grandes dentro del cuadro. */
    private void mostrar(String titulo, String mensaje, String botonPositivo, final Runnable accion) {
        if (activity.isFinishing()) return;
        if (titulo.startsWith("Permiso de cámara")) {
            // a11 (mas larga) = con los pasos de Ajustes; a12 = pedido comun. Ver nota de entrega.
            com.autenticar.tuid.usuarios.Audio.play(activity, mensaje.contains("Para habilitarlo") ? R.raw.a11 : R.raw.a12);
        }
        View v = android.view.LayoutInflater.from(activity).inflate(R.layout.dialog_permiso, null);
        ((android.widget.ImageView) v.findViewById(R.id.permiso_icono)).setImageResource(iconoPendiente);
        ((android.widget.TextView) v.findViewById(R.id.permiso_titulo)).setText(titulo);
        ((android.widget.TextView) v.findViewById(R.id.permiso_mensaje)).setText(mensaje);
        android.widget.Button aceptar = (android.widget.Button) v.findViewById(R.id.permiso_aceptar);
        aceptar.setText(botonPositivo);
        dialogoActual = new AlertDialog.Builder(activity, R.style.DialogoTuID).setView(v).setCancelable(true).create();
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogoActual.dismiss();
                accion.run();
            }
        });
        v.findViewById(R.id.permiso_ahora_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogoActual.dismiss();
            }
        });
        dialogoActual.show();
    }
}
