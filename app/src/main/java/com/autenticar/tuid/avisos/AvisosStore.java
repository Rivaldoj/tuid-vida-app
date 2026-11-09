package com.autenticar.tuid.avisos;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Historial de avisos (notificaciones push) recibidos en este telefono. Se guarda solo en el telefono, en el
 * almacenamiento privado de la app; no se sincroniza con el servidor.
 *
 * Un aviso puede entrar por tres caminos (no se duplican entre si):
 *  1) la app esta abierta y llega el mensaje (MyFirebaseMessagingService),
 *  2) la persona toca la notificacion y se abre la app (SplashActivity),
 *  3) al abrir la app se leen las notificaciones de TuID Vida que siguen en la barra del telefono.
 */
public final class AvisosStore {

    private static final String PREFS = "avisos_tuid";
    private static final String KEY_LISTA = "lista";
    private static final String KEY_VISTO = "visto";
    private static final int MAXIMO = 200;
    private static final long MISMO_AVISO_MS = 5 * 60 * 1000L;

    public static final class Aviso {
        public final String titulo;
        public final String mensaje;
        public final long fecha;

        Aviso(String titulo, String mensaje, long fecha) {
            this.titulo = titulo;
            this.mensaje = mensaje;
            this.fecha = fecha;
        }
    }

    private static AvisosStore instancia;
    private final SharedPreferences prefs;

    private AvisosStore(Context c) {
        prefs = c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static synchronized AvisosStore get(Context c) {
        if (instancia == null) instancia = new AvisosStore(c);
        return instancia;
    }

    /** Devuelve true si se agrego (false si ya estaba o venia vacio). */
    public synchronized boolean agregar(String titulo, String mensaje, long fechaMs) {
        if (mensaje == null || mensaje.trim().isEmpty()) return false;
        if (titulo == null) titulo = "";
        if (fechaMs <= 0) fechaMs = System.currentTimeMillis();
        List<Aviso> actuales = listar();
        for (Aviso a : actuales) {
            if (a.titulo.equals(titulo) && a.mensaje.equals(mensaje) && Math.abs(a.fecha - fechaMs) < MISMO_AVISO_MS) {
                return false; // ya lo teniamos (llego por otro camino)
            }
        }
        actuales.add(0, new Aviso(titulo, mensaje, fechaMs));
        guardar(actuales);
        return true;
    }

    /** Mas nuevos primero. */
    public synchronized List<Aviso> listar() {
        List<Aviso> out = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs.getString(KEY_LISTA, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                out.add(new Aviso(o.optString("t"), o.optString("m"), o.optLong("f")));
            }
        } catch (Exception e) {
            // lista danada: se empieza de nuevo
        }
        java.util.Collections.sort(out, new java.util.Comparator<Aviso>() {
            @Override
            public int compare(Aviso a, Aviso b) {
                return Long.compare(b.fecha, a.fecha);
            }
        });
        return out;
    }

    private void guardar(List<Aviso> lista) {
        JSONArray arr = new JSONArray();
        int n = Math.min(lista.size(), MAXIMO);
        try {
            for (int i = 0; i < n; i++) {
                Aviso a = lista.get(i);
                JSONObject o = new JSONObject();
                o.put("t", a.titulo);
                o.put("m", a.mensaje);
                o.put("f", a.fecha);
                arr.put(o);
            }
        } catch (Exception ignorada) {
        }
        prefs.edit().putString(KEY_LISTA, arr.toString()).apply();
    }

    public synchronized int sinLeer() {
        long visto = prefs.getLong(KEY_VISTO, 0L);
        int n = 0;
        for (Aviso a : listar()) {
            if (a.fecha > visto) n++;
        }
        return n;
    }

    /** Marca como leidos todos los avisos actuales. Devuelve la marca anterior para poder resaltar los nuevos. */
    public synchronized long marcarTodosLeidos() {
        long anterior = prefs.getLong(KEY_VISTO, 0L);
        prefs.edit().putLong(KEY_VISTO, System.currentTimeMillis()).apply();
        return anterior;
    }

    public synchronized long vistoHasta() {
        return prefs.getLong(KEY_VISTO, 0L);
    }

    /** Camino 2: la persona toco la notificacion y se abrio la app. */
    public void guardarDeIntent(Intent intent) {
        try {
            if (intent == null || intent.getExtras() == null) return;
            Bundle b = intent.getExtras();
            String titulo = b.getString("titulo");
            String mensaje = b.getString("mensaje");
            if (mensaje == null) return;
            long fecha = 0L;
            Object enviado = b.get("google.sent_time");
            if (enviado != null) fecha = Long.parseLong(String.valueOf(enviado));
            agregar(titulo, mensaje, fecha);
        } catch (Exception ignorada) {
        }
    }

    /** Camino 3: avisos de TuID Vida que siguen en la barra de notificaciones del telefono. */
    public void importarActivas(Context c) {
        try {
            NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) return;
            for (StatusBarNotification sbn : nm.getActiveNotifications()) {
                String tag = sbn.getTag();
                if (tag == null || !tag.startsWith("FCM-Notification")) continue;
                Bundle e = sbn.getNotification().extras;
                CharSequence titulo = e.getCharSequence(Notification.EXTRA_TITLE);
                CharSequence texto = e.getCharSequence(Notification.EXTRA_TEXT);
                agregar(titulo == null ? "" : titulo.toString(), texto == null ? null : texto.toString(), sbn.getPostTime());
            }
        } catch (Exception ignorada) {
        }
    }
}
