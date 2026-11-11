package com.autenticar.tuid.usuarios;

import android.content.Context;
import android.media.MediaPlayer;

import com.autenticar.tuid.utils.HelperFunctions;

/**
 * Reproduce un audio guia (res/raw/aNN) respetando el boton de sonido de la app.
 * Un solo reproductor: cada audio nuevo corta el anterior; los audios en cadena se pasan juntos.
 */
public final class Audio {
    private static MediaPlayer mp;

    private Audio() {
    }

    public static void play(Context context, int... audios) {
        detener();
        if (!HelperFunctions.sound || context == null || audios == null || audios.length == 0) return;
        siguiente(context.getApplicationContext(), audios, 0);
    }

    private static void siguiente(final Context app, final int[] audios, final int pos) {
        if (pos >= audios.length) return;
        try {
            MediaPlayer m = MediaPlayer.create(app, audios[pos]);
            if (m == null) return;
            mp = m;
            m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer p) {
                    try {
                        p.release();
                    } catch (Exception ignored) {
                    }
                    if (mp == p) mp = null;
                    siguiente(app, audios, pos + 1);
                }
            });
            m.start();
        } catch (Exception ignored) {
            // un audio que falla no debe frenar la pantalla
        }
    }

    public static void detener() {
        try {
            if (mp != null) {
                mp.stop();
                mp.release();
            }
        } catch (Exception ignored) {
        }
        mp = null;
    }
}
