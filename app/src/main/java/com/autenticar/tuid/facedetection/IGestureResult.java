package com.autenticar.tuid.facedetection;

import com.google.mlkit.vision.face.Face;
import android.graphics.Bitmap;


public interface IGestureResult {

    void GestureProgress(int valor);

    void GestureDetected(Face face, Bitmap bitmap, int gestureType);
}
