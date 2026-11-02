package com.autenticar.tuid.v2.components;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.autenticar.tuid.RegistrarScanUserActivity;
import com.autenticar.tuid.camera.GraphicOverlay;
import com.autenticar.tuid.utils.HelperFunctions;
import com.autenticar.tuid.v2.FeDeVidaActivity;
import com.autenticar.tuid.v2.HomeActivity;
import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.CameraSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import androidx.core.app.ActivityCompat;


public class CameraSourcePreview extends ViewGroup  {
    private static final String TAG = "CameraSourcePreview";

    private Context mContext;
    private SurfaceView mSurfaceView;
    private boolean mStartRequested;
    private boolean mSurfaceAvailable;
    private CameraSource mCameraSource;

    private GraphicOverlay mOverlay;
    private List<Camera.Size> mSupportedPreviewSizes;
    private String ERROR_COMARE_SOURCE="Se ha detectado una falla en la registración, intente nuevamente.";

    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mStartRequested = false;
        mSurfaceAvailable = false;
        mSurfaceView = new SurfaceView(context);

//        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mSurfaceView.getHolder().addCallback(new SurfaceCallback());
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mSurfaceView, lp);
    }

    private void errorCamera() {
        Intent intnt = new Intent(mContext, RegistrarScanUserActivity.class);
        mContext.startActivity(intnt);
        HelperFunctions.ShowAlert((Activity) mContext, ERROR_COMARE_SOURCE);
    }

    public void start(CameraSource cameraSource) throws IOException {
        if (cameraSource == null) {
            stop();
        }

        mCameraSource = cameraSource;

        if (mCameraSource != null) {
            mStartRequested = true;
            startIfReady();
        }
    }

    public void start(CameraSource cameraSource, GraphicOverlay overlay) throws IOException {
        mOverlay = overlay;
        start(cameraSource);
    }

    public void stop() {
        if (mCameraSource != null) {
            mCameraSource.stop();
        }
    }

    public void setCamera(Camera mCamera) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        }
    }

    private void startIfReady() throws IOException {
        if (mStartRequested && mSurfaceAvailable) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (mCameraSource != null) {
                try {
                    mCameraSource.start(mSurfaceView.getHolder());
                } catch (Exception e) {
                    Log.e(TAG, "mCameraSource.star", e);
                    errorCamera();
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        FeDeVidaActivity.self.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        int offset =  width/5;
        Log.e("left", left +":");
        Log.e("right", right +":");
        int l = -offset;
        int r = right+offset;
        int t = top;
        int b = bottom;
        Log.e("l", l  +":");
        Log.e("r", r  +":");
        Log.e("t", t  +":");
        Log.e("b", b  +":");
        for (int i = 0; i < getChildCount(); ++i) {
//            getChildAt(i).layout(0, 0, right-left, bottom-top);
//            getChildAt(i).layout(l, t, r, b); //childHeight
            getChildAt(i).layout(l,t,r,b); //childHeight
        }


    }




    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;


        for (Camera.Size size : sizes) {
            double ratio = (double) size.height / size.width;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - h);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }
        }

        return optimalSize;
    }


    
    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surface) {
            mSurfaceAvailable = true;
            try {
                startIfReady();
            } catch (IOException e) {
                Log.e(TAG, "Could not start camera source.", e);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surface) {
            mSurfaceAvailable = false;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }
    }
}



