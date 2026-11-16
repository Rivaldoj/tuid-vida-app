package com.autenticar.tuid.usuarios;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.autenticar.tuid.model.CUIL;
import com.autenticar.tuid.model.HistoryRequest;
import com.autenticar.tuid.model.HistoryResponse;
import com.autenticar.tuid.services.TuidServiceV2;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.autenticar.tuid.utils.SQLHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * "Actualizar historial": pide al servidor las ultimas 20 certificaciones de UNA persona y reemplaza su
 * historial local de forma transaccional. El servidor es la fuente oficial; el telefono guarda una copia
 * de consulta. Un error de red o una respuesta vacia nunca borran lo que ya hay en el telefono.
 * Es manual: solo se ejecuta cuando la persona toca el boton.
 */
public class HistorialSync {

    public interface Resultado {
        /** @param ok true si se actualizo; @param mensaje texto para mostrar a la persona */
        void onTerminado(boolean ok, String mensaje);
    }

    public static final String MSG_OK = "Su historial de Fe de Vida se actualizó correctamente.";
    public static final String MSG_VACIO = "No encontramos certificaciones anteriores para este usuario.";
    public static final String MSG_SIN_CONEXION = "No pudimos actualizar su historial. Verifique su conexión a Internet e inténtelo nuevamente.";

    /** Segundos maximos de espera antes de avisar que no hay conexion (el cliente de red espera mucho mas). */
    private static final long ESPERA_MAXIMA_MS = 20000;

    public static void actualizar(final Activity activity, final String cuil, final Resultado cb0) {
        // El resultado se informa UNA sola vez: lo que llegue primero (respuesta o fin de la espera)
        final java.util.concurrent.atomic.AtomicBoolean terminado = new java.util.concurrent.atomic.AtomicBoolean(false);
        final Resultado cb = new Resultado() {
            @Override
            public void onTerminado(boolean ok, String mensaje) {
                if (terminado.compareAndSet(false, true)) cb0.onTerminado(ok, mensaje);
            }
        };
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                cb.onTerminado(false, MSG_SIN_CONEXION);
            }
        }, ESPERA_MAXIMA_MS);
        try {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        cb.onTerminado(false, MSG_SIN_CONEXION);
                        return;
                    }
                    pedir(activity, cuil, task.getResult(), cb);
                }
            });
        } catch (Exception e) {
            cb.onTerminado(false, MSG_SIN_CONEXION);
        }
    }

    private static void pedir(final Activity activity, final String cuil, final String idDevice, final Resultado cb) {
        ServiceGenerator.createV2Service(TuidServiceV2.class)
                .history(new HistoryRequest(cuil, idDevice))
                .enqueue(new Callback<HistoryResponse>() {
                    @Override
                    public void onResponse(Call<HistoryResponse> call, Response<HistoryResponse> response) {
                        HistoryResponse r = response.body();
                        if (!response.isSuccessful() || r == null) {
                            cb.onTerminado(false, MSG_SIN_CONEXION);
                            return;
                        }
                        if (!r.IsOk()) {
                            cb.onTerminado(false, r.Message != null ? r.Message : MSG_SIN_CONEXION);
                            return;
                        }
                        // El servidor acepto este token: es el que tiene guardado para esta persona
                        DeviceTokenSync.recordarEnviado(activity, idDevice);
                        if (r.certificaciones == null || r.certificaciones.isEmpty()) {
                            cb.onTerminado(true, MSG_VACIO);   // no se toca el historial local
                            return;
                        }
                        SQLHelper sql = new SQLHelper(activity);
                        boolean guardado = sql.reemplazarHistorial(cuil, convertir(r.certificaciones));
                        cb.onTerminado(guardado, guardado ? MSG_OK : MSG_SIN_CONEXION);
                    }

                    @Override
                    public void onFailure(Call<HistoryResponse> call, Throwable t) {
                        cb.onTerminado(false, MSG_SIN_CONEXION);
                    }
                });
    }

    /** De la mas antigua a la mas reciente, para que el orden de carga coincida con el de la lista. */
    static List<CUIL> convertir(List<HistoryResponse.Certificacion> certs) {
        List<HistoryResponse.Certificacion> l = new ArrayList<>(certs);
        if (l.size() > SQLHelper.MAX_HISTORIAL_POR_USUARIO) l = l.subList(0, SQLHelper.MAX_HISTORIAL_POR_USUARIO);
        Collections.reverse(l);   // el servidor las manda de la mas reciente a la mas antigua
        SimpleDateFormat salida = new SimpleDateFormat("E dd LLL yyyy. h:m a", Locale.getDefault());
        List<CUIL> out = new ArrayList<>();
        for (HistoryResponse.Certificacion c : l) {
            String fecha = c.fecha == null ? "" : c.fecha;
            try {
                SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                iso.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date d = iso.parse(fecha.length() >= 19 ? fecha.substring(0, 19) : fecha);
                if (d != null) fecha = salida.format(d);
            } catch (Exception ignored) {
                // se deja el texto tal cual lo informo el servidor
            }
            StringBuilder entidades = new StringBuilder();
            if (c.empresas != null) {
                for (String e : c.empresas) {
                    if (entidades.length() > 0) entidades.append(", ");
                    entidades.append(e);
                }
            }
            out.add(new CUIL(null, "", fecha, c.longitud == null ? "0.0" : c.longitud,
                    c.latitud == null ? "0.0" : c.latitud, c.direccion == null ? "" : c.direccion, entidades.toString()));
        }
        return out;
    }
}
