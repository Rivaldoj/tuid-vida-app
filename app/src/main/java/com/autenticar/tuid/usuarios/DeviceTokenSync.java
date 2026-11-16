package com.autenticar.tuid.usuarios;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.autenticar.tuid.model.DeviceRefreshRequest;
import com.autenticar.tuid.model.ResponseBase;
import com.autenticar.tuid.services.TuidServiceV2;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Mantiene al dia en el servidor el token de notificaciones (Firebase) de este telefono.
 *
 * El token cambia solo (Firebase lo renueva) y el servidor solo lo aprendia en la proxima Fe de Vida: mientras tanto
 * las notificaciones no llegaban. Aca se avisa el cambio apenas ocurre (al renovarse y al abrir la app), mostrando
 * como prueba el token ANTERIOR, el que el servidor tiene guardado. Sin esa prueba el servidor no cambia nada:
 * un telefono nuevo o una reinstalacion se vinculan, como siempre, con una Fe de Vida.
 *
 * Es silencioso y nunca interrumpe a la persona: ante cualquier falla de red reintenta mas tarde.
 */
public final class DeviceTokenSync {

    private static final String PREFS = "tuid_dispositivo";
    /** Ultimo token que el servidor conoce de este telefono (el que se envio en la ultima Fe de Vida / registro / refresco). */
    private static final String K_CONOCIDO = "token_servidor";
    private static final String K_INTENTO = "ultimo_intento";
    /** Entre un intento fallido de red y el siguiente. */
    static final long REINTENTO_MS = 6L * 60L * 60L * 1000L;

    private static final AtomicBoolean EN_CURSO = new AtomicBoolean(false);

    private DeviceTokenSync() { }

    /** Que hacer con el token actual. Funcion pura (se prueba sin Android). */
    public enum Decision { NADA, PUNTO_DE_PARTIDA, REFRESCAR, ESPERAR }

    /**
     * @param conocido     ultimo token que el servidor conoce (null si esta version nunca lo guardo)
     * @param actual       token vigente de Firebase
     * @param hayPersonas  hay personas guardadas en este telefono (sin ellas no hay a quien refrescar)
     * @param ahora        hora actual (ms)
     * @param ultimoIntento hora del ultimo intento fallido (ms, 0 si no hubo)
     */
    public static Decision decidir(String conocido, String actual, boolean hayPersonas, long ahora, long ultimoIntento) {
        if (actual == null || actual.isEmpty()) return Decision.NADA;
        // Primera vez con esta version: se toma el token actual como punto de partida. Si el servidor tenia otro,
        // no hay forma segura de enterarse: se corrige en la proxima Fe de Vida.
        if (conocido == null || conocido.isEmpty()) return Decision.PUNTO_DE_PARTIDA;
        if (conocido.equals(actual)) return Decision.NADA;
        if (!hayPersonas) return Decision.PUNTO_DE_PARTIDA;
        if (ultimoIntento > 0 && ahora - ultimoIntento < REINTENTO_MS) return Decision.ESPERAR;
        return Decision.REFRESCAR;
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** El servidor acaba de recibir este token (Fe de Vida, registro o historial confirmados): es el que conoce. */
    public static void recordarEnviado(Context ctx, String token) {
        if (ctx == null || token == null || token.isEmpty()) return;
        prefs(ctx).edit().putString(K_CONOCIDO, token).remove(K_INTENTO).apply();
    }

    /** Despues de una Fe de Vida o un registro confirmados por el servidor: el token que se envio es el vigente. */
    public static void recordarVigente(final Context ctx) {
        try {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (task.isSuccessful() && task.getResult() != null) recordarEnviado(ctx, task.getResult());
                }
            });
        } catch (Exception e) {
            // sin Firebase no hay nada que recordar
        }
    }

    /** Al abrir la app: si el token cambio y el servidor no se entero, se lo avisa. */
    public static void sincronizar(final Context ctx) {
        try {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (task.isSuccessful() && task.getResult() != null) sincronizarConToken(ctx, task.getResult());
                }
            });
        } catch (Exception e) {
            // sin Firebase no hay nada que sincronizar
        }
    }

    /** Firebase entrego un token nuevo (onNewToken) o se lo leyo al abrir la app. */
    public static void sincronizarConToken(Context context, final String nuevo) {
        if (context == null || nuevo == null || nuevo.isEmpty()) return;
        final Context ctx = context.getApplicationContext();
        final SharedPreferences p = prefs(ctx);
        final String conocido = p.getString(K_CONOCIDO, null);
        final List<LocalUser> personas;
        try {
            personas = LocalUserStore.get(ctx).list();
        } catch (Exception e) {
            return;
        }
        switch (decidir(conocido, nuevo, !personas.isEmpty(), System.currentTimeMillis(), p.getLong(K_INTENTO, 0L))) {
            case PUNTO_DE_PARTIDA:
                p.edit().putString(K_CONOCIDO, nuevo).remove(K_INTENTO).apply();
                return;
            case REFRESCAR:
                refrescar(ctx, p, conocido, nuevo, personas);
                return;
            default:
                return;
        }
    }

    private static void refrescar(final Context ctx, final SharedPreferences p, final String anterior, final String nuevo, List<LocalUser> personas) {
        if (!EN_CURSO.compareAndSet(false, true)) return;
        final AtomicInteger pendientes = new AtomicInteger(personas.size());
        final AtomicBoolean algunOk = new AtomicBoolean(false);
        final AtomicBoolean fallaDeRed = new AtomicBoolean(false);
        // Al terminar con todas las personas: si el servidor acepto alguna, o todas respondieron (aunque fuera "sin prueba"),
        // no se insiste con este par de tokens. Si hubo falla de red, se reintenta mas tarde.
        final Runnable terminar = new Runnable() {
            @Override
            public void run() {
                if (pendientes.decrementAndGet() != 0) return;
                EN_CURSO.set(false);
                if (algunOk.get() || !fallaDeRed.get()) p.edit().putString(K_CONOCIDO, nuevo).remove(K_INTENTO).apply();
            }
        };
        p.edit().putLong(K_INTENTO, System.currentTimeMillis()).apply();
        TuidServiceV2 servicio;
        try {
            servicio = ServiceGenerator.createV2Service(TuidServiceV2.class);
        } catch (Exception e) {
            EN_CURSO.set(false);
            return;
        }
        for (LocalUser u : personas) {
            try {
                servicio.refreshDevice(new DeviceRefreshRequest(u.cuil, anterior, nuevo)).enqueue(new Callback<ResponseBase>() {
                    @Override
                    public void onResponse(Call<ResponseBase> call, Response<ResponseBase> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().IsOk()) algunOk.set(true);
                        } else {
                            fallaDeRed.set(true);
                        }
                        terminar.run();
                    }

                    @Override
                    public void onFailure(Call<ResponseBase> call, Throwable t) {
                        fallaDeRed.set(true);
                        terminar.run();
                    }
                });
            } catch (Exception e) {
                fallaDeRed.set(true);
                terminar.run();
            }
        }
    }
}
