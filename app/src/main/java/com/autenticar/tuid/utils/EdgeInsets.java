package com.autenticar.tuid.utils;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Deja libre la barra de estado y la de navegacion del telefono y conserva el relleno propio de la pantalla. */
public final class EdgeInsets {
    private EdgeInsets() {
    }

    public static void aplicar(View raiz) {
        final int l = raiz.getPaddingLeft();
        final int t = raiz.getPaddingTop();
        final int r = raiz.getPaddingRight();
        final int b = raiz.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(raiz, new androidx.core.view.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
                v.setPadding(l + bars.left, t + bars.top, r + bars.right, b + bars.bottom);
                return insets;
            }
        });
        ViewCompat.requestApplyInsets(raiz);
    }
}
