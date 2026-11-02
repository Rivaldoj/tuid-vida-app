package com.autenticar.tuid.v2.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;

import com.autenticar.tuid.v2.FeDeVidaActivity;
import com.google.android.gms.internal.vision.zzn;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.zzc;
import com.google.android.gms.vision.face.internal.client.zze;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashSet;


import androidx.annotation.GuardedBy;

public class FaceDetector extends Detector<Face> {
    public static final int NO_LANDMARKS = 0;
    public static final int ALL_LANDMARKS = 1;
    public static final int CONTOUR_LANDMARKS = 2;
    public static final int NO_CLASSIFICATIONS = 0;
    public static final int ALL_CLASSIFICATIONS = 1;
    public static final int FAST_MODE = 0;
    public static final int ACCURATE_MODE = 1;
    public static final int SELFIE_MODE = 2;
    private final zzc zzcj;
    @GuardedBy("lock")
    private final com.google.android.gms.vision.face.internal.client.zzc zzck;
    private final Object lock;
    @GuardedBy("lock")
    private boolean zzcl;

    public boolean getImage = false;

    public final void release() {
        super.release();
        synchronized (this.lock) {
            if (this.zzcl) {
                this.zzck.zzp();
                this.zzcl = false;
            }
        }
    }

    protected final void finalize() throws Throwable {
        try {
            synchronized (this.lock) {
                if (this.zzcl) {
                    Log.w("FaceDetector", "FaceDetector was not released with FaceDetector.release()");
                    this.release();
                }
            }
        } finally {
            super.finalize();
        }

    }

    public final SparseArray<Face> detect(Frame var1) {
        if (var1 == null) {
            throw new IllegalArgumentException("No frame supplied.");
        } else {
            ByteBuffer var2;
            if (var1.getBitmap() != null) {
                Bitmap var13;
                int var14 = (var13 = var1.getBitmap()).getWidth();
                int var15 = var13.getHeight();
                int var16 = var14 * var15;
                int var17 = (var14 + 1) / 2 * ((var15 + 1) / 2) << 1;
                ByteBuffer var18 = ByteBuffer.allocateDirect(var16 + var17);
                int var19 = var16;

                for (int var20 = 0; var20 < var16; ++var20) {
                    int var21;
                    int var22 = Color.red(var21 = var13.getPixel(var20 % var14, var20 / var14));
                    int var23 = Color.green(var21);
                    int var24 = Color.blue(var21);
                    float var25 = 0.299F * (float) var22 + 0.587F * (float) var23 + 0.114F * (float) var24;
                    var18.put(var20, (byte) ((int) var25));
                    if (var20 / var14 % 2 == 0 && var20 % var14 % 2 == 0) {
                        float var26 = -0.169F * (float) var22 + -0.331F * (float) var23 + 0.5F * (float) var24 + 128.0F;
                        float var27 = 0.5F * (float) var22 + -0.419F * (float) var23 + -0.081F * (float) var24 + 128.0F;
                        var18.put(var19++, (byte) ((int) var26));
                        var18.put(var19++, (byte) ((int) var27));
                    }
                }

                var2 = var18;
            } else {
                var2 = var1.getGrayscaleImageData();
            }

            Face[] var3;
            synchronized (this.lock) {
                if (!this.zzcl) {
                    throw new RuntimeException("Cannot use detector after release()");
                }

                var3 = this.zzck.zzb(var2, zzn.zzc(var1));
            }

            int var4 = 0;
            HashSet var5 = new HashSet();
            SparseArray var6 = new SparseArray(var3.length);
            Face[] var7 = var3;
            int var8 = var3.length;

            for (int var9 = 0; var9 < var8; ++var9) {
                Face var10;
                int var11 = (var10 = var7[var9]).getId();
                var4 = Math.max(var4, var11);
                if (var5.contains(var11)) {
                    ++var4;
                    var11 = var4;
                }

                var5.add(var11);
                int var12 = this.zzcj.zzb(var11);
                var6.append(var12, var10);
            }

            if(getImage && FeDeVidaActivity.resultBitmap==null){
                YuvImage yuvImage = new YuvImage(var1.getGrayscaleImageData().array(), ImageFormat.NV21, var1.getMetadata().getWidth(), var1.getMetadata().getHeight(), null);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                yuvImage.compressToJpeg(new Rect(0, 0, var1.getMetadata().getWidth(), var1.getMetadata().getHeight()), 100, byteArrayOutputStream);
                byte[] jpegArray = byteArrayOutputStream.toByteArray();
                Bitmap result = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);
                int degrees = 0;

                switch (var1.getMetadata().getRotation()) {
                    case Surface.ROTATION_0:
                        degrees = 0;
                        break;
                    case Surface.ROTATION_90:
                        degrees = 90;
                        break;
                    case Surface.ROTATION_180:
                        degrees = 180;
                        break;
                    case Surface.ROTATION_270:
                        degrees = 270;
                        break;
                }
                FeDeVidaActivity.resultBitmap = BitmapsHelper.RotateBitmap(result,degrees);
                getImage = false;
//                FeDeVidaActivity.self.gestureDetected();
            }

            return var6;
        }
    }

    public final boolean setFocus(int var1) {
        int var2 = this.zzcj.zzc(var1);
        synchronized (this.lock) {
            if (!this.zzcl) {
                throw new RuntimeException("Cannot use detector after release()");
            } else {
                return this.zzck.zzd(var2);
            }
        }
    }

    public final boolean isOperational() {
        return this.zzck.isOperational();
    }

    private FaceDetector() {
        this.zzcj = new zzc();
        this.lock = new Object();
        this.zzcl = true;
        throw new IllegalStateException("Default constructor called");
    }

    private FaceDetector(com.google.android.gms.vision.face.internal.client.zzc var1) {
        this.zzcj = new zzc();
        this.lock = new Object();
        this.zzcl = true;
        this.zzck = var1;
    }

    private static boolean zza(zze var0) {
        boolean var1 = true;
        if (var0.mode != 2 && var0.landmarkType == 2) {
            Log.e("FaceDetector", "Contour is not supported for non-SELFIE mode.");
            var1 = false;
        }

        if (var0.landmarkType == 2 && var0.zzcn == 1) {
            Log.e("FaceDetector", "Classification is not supported with contour.");
            var1 = false;
        }

        return var1;
    }

    public static class Builder {
        private final Context zze;
        private int landmarkType = 0;
        private boolean zzcm = false;
        private int zzcn = 0;
        private boolean trackingEnabled = true;
        private int mode = 0;
        private float proportionalMinFaceSize = -1.0F;

        public Builder(Context var1) {
            this.zze = var1;
        }

        public FaceDetector.Builder setLandmarkType(int var1) {
            if (var1 != 0 && var1 != 1 && var1 != 2) {
                throw new IllegalArgumentException((new StringBuilder(34)).append("Invalid landmark type: ").append(var1).toString());
            } else {
                this.landmarkType = var1;
                return this;
            }
        }

        public FaceDetector.Builder setProminentFaceOnly(boolean var1) {
            this.zzcm = var1;
            return this;
        }

        public FaceDetector.Builder setClassificationType(int var1) {
            if (var1 != 0 && var1 != 1) {
                throw new IllegalArgumentException((new StringBuilder(40)).append("Invalid classification type: ").append(var1).toString());
            } else {
                this.zzcn = var1;
                return this;
            }
        }

        public FaceDetector.Builder setTrackingEnabled(boolean var1) {
            this.trackingEnabled = var1;
            return this;
        }

        public FaceDetector.Builder setMode(int var1) {
            switch (var1) {
                case 0:
                case 1:
                case 2:
                    this.mode = var1;
                    return this;
                default:
                    throw new IllegalArgumentException((new StringBuilder(25)).append("Invalid mode: ").append(var1).toString());
            }
        }

        public FaceDetector.Builder setMinFaceSize(float var1) {
            if (!(var1 < 0.0F) && !(var1 > 1.0F)) {
                this.proportionalMinFaceSize = var1;
                return this;
            } else {
                throw new IllegalArgumentException((new StringBuilder(47)).append("Invalid proportional face size: ").append(var1).toString());
            }
        }

        public FaceDetector build() {
            com.google.android.gms.vision.face.internal.client.zze var1;
            (var1 = new zze()).mode = this.mode;
            var1.landmarkType = this.landmarkType;
            var1.zzcn = this.zzcn;
            var1.zzcm = this.zzcm;
            var1.trackingEnabled = this.trackingEnabled;
            var1.proportionalMinFaceSize = this.proportionalMinFaceSize;
            if (!FaceDetector.zza(var1)) {
                throw new IllegalArgumentException("Invalid build options");
            } else {
                com.google.android.gms.vision.face.internal.client.zzc var2 = new com.google.android.gms.vision.face.internal.client.zzc(this.zze, var1);
                return new FaceDetector(var2);
            }
        }
    }
}
