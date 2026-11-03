package com.autenticar.tuid.services.utils;

import android.content.Context;
import android.util.Log;

import com.autenticar.tuid.BuildConfig;
import com.google.android.gms.tasks.Tasks;
import com.google.android.play.core.integrity.IntegrityManagerFactory;
import com.google.android.play.core.integrity.StandardIntegrityManager;
import com.google.android.play.core.integrity.StandardIntegrityManager.PrepareIntegrityTokenRequest;
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityToken;
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenProvider;
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

/**
 * Agrega el header X-Integrity-Token (Google Play Integrity, API estandar) a las llamadas criticas.
 *
 * El token queda atado al pedido: requestHash = hex(SHA-256(ultimo segmento del path + "\n" + body)).
 * El servidor debe recalcular ese hash con el body recibido y compararlo con el que viene dentro del
 * token descifrado (requestDetails.requestHash), ademas de revisar appIntegrity y deviceIntegrity.
 *
 * Si no se pudo obtener el token (sin Play Services, sin red, proyecto no configurado) el pedido sale
 * igual sin el header; es el servidor quien decide si lo exige.
 */
public class IntegrityInterceptor implements Interceptor {

    private static final String TAG = "INTEGRITY";
    public static final String HEADER = "X-Integrity-Token";
    private static final long TIMEOUT_S = 10;

    private static final Set<String> PROTECTED = new HashSet<>(Arrays.asList(
            "searchdni", "login", "register", "chance", "validation", "revalidation",
            "confirmation", "reconfirmation", "manual", "validate", "signup"));

    private static StandardIntegrityTokenProvider provider;
    private static final Object lock = new Object();

    private final Context context;

    public IntegrityInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    /** Pide el "calentamiento" del token. Llamar fuera del hilo principal (p. ej. al iniciar la app). */
    public static void warmUp(Context ctx) {
        if (BuildConfig.PLAY_CLOUD_PROJECT == 0L) return;
        getProvider(ctx.getApplicationContext());
    }

    private static StandardIntegrityTokenProvider getProvider(Context ctx) {
        synchronized (lock) {
            if (provider != null) return provider;
            try {
                StandardIntegrityManager manager = IntegrityManagerFactory.createStandard(ctx);
                provider = Tasks.await(manager.prepareIntegrityToken(
                        PrepareIntegrityTokenRequest.builder()
                                .setCloudProjectNumber(BuildConfig.PLAY_CLOUD_PROJECT)
                                .build()), TIMEOUT_S, TimeUnit.SECONDS);
            } catch (Exception e) {
                Log.w(TAG, "No se pudo preparar Play Integrity: " + e);
            }
            return provider;
        }
    }

    private static String sha256Hex(byte[] data) throws Exception {
        byte[] d = MessageDigest.getInstance("SHA-256").digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : d) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        if (BuildConfig.PLAY_CLOUD_PROJECT == 0L || !PROTECTED.contains(original.url().pathSegments().get(original.url().pathSegments().size() - 1))) {
            return chain.proceed(original);
        }
        try {
            StandardIntegrityTokenProvider p = getProvider(context);
            if (p == null) return chain.proceed(original);
            Buffer buffer = new Buffer();
            RequestBody body = original.body();
            if (body != null) body.writeTo(buffer);
            String last = original.url().pathSegments().get(original.url().pathSegments().size() - 1);
            String hash = sha256Hex((last + "\n" + buffer.readUtf8()).getBytes("UTF-8"));
            StandardIntegrityToken token = Tasks.await(p.request(
                    StandardIntegrityTokenRequest.builder().setRequestHash(hash).build()), TIMEOUT_S, TimeUnit.SECONDS);
            return chain.proceed(original.newBuilder().header(HEADER, token.token()).build());
        } catch (Exception e) {
            Log.w(TAG, "Sin token de integridad: " + e);
            return chain.proceed(original);
        }
    }
}
