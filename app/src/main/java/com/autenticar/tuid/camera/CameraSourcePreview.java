/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.autenticar.tuid.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import androidx.core.app.ActivityCompat;

import com.autenticar.tuid.IPushSize;
import com.autenticar.tuid.RegistrarScanUserActivity;
import com.autenticar.tuid.model.Persona;
import com.autenticar.tuid.utils.HelperFunctions;
import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.CameraSource;

import java.io.IOException;
import java.util.List;



public class CameraSourcePreview extends ViewGroup implements IPushSize {
    private static final String TAG = "CameraSourcePreview";

    private Context mContext;
    private SurfaceView mSurfaceView;
    private boolean mStartRequested;
    private boolean mSurfaceAvailable;
    private CameraSource mCameraSource;

    private RectF docSize;
    private GraphicOverlay mOverlay;
    private List<Camera.Size> mSupportedPreviewSizes;
    private String ERROR_COMARE_SOURCE="Se ha detectado una falla en la registración, intente nuevamente.";

    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mStartRequested = false;
        mSurfaceAvailable = false;


        mSurfaceView = new SurfaceView(context);
        mSurfaceView.getHolder().addCallback(new SurfaceCallback());

        addView(mSurfaceView);

    }

    private Bitmap rotateImage(Bitmap bm, int i) {
        Matrix matrix = new Matrix();
        switch (i) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bm;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bm;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
            bm.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    //1 face 2 front dni  3 back dni
    public void takePicture(final Persona person, final int destin, final RectF size) {

        mCameraSource.takePicture(null, new
                CameraSource.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes) {
                        if (bytes != null) {
                            //Log.v(TAG, "Error bytes == null");
                            //HelperFunctions.ShowAlert((Activity) mContext, "Error bytes == null");
                            //Intent intnt = new Intent(mContext, RegistrarScanUserActivity.class);
                            //mContext.startActivity(intnt);

                            Bitmap bitmapOrigen = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            Bitmap croppedImage = null;
                            try {
                                //achico la imagen
                                if (bitmapOrigen != null) {
                                    croppedImage = scaleBitmap(bitmapOrigen);
                                } else {
                                    Log.v(TAG, "Error bitmapOrigen == null");
                                    errorCamera();
                                }
                            } catch (Exception e) {
                                Log.v(TAG, "Error scaleBitmap: " + e);
                                errorCamera();
                            }

                            assert bitmapOrigen != null;
                            bitmapOrigen.recycle();
                            boolean rotated = false;
                            if (croppedImage != null && getOrientation(croppedImage) == 90) {
                                croppedImage = rotateImage(croppedImage, ExifInterface.ORIENTATION_ROTATE_90); // invierto los lados
                            } else if (croppedImage != null && getOrientation(croppedImage) == 180) {
                                croppedImage = rotateImage(croppedImage, ExifInterface.ORIENTATION_ROTATE_180);
                            } else if (croppedImage != null && getOrientation(croppedImage) == 270) {
                                croppedImage = rotateImage(croppedImage, ExifInterface.ORIENTATION_ROTATE_270);
                            } else if (croppedImage == null) {
                                Log.v(TAG, "Error croppedImage == null");
                                HelperFunctions.ShowAlert((Activity) mContext, "Error croppedImage == null");
                                //Intent intnt = new Intent(mContext, RegistrarScanUserActivity.class);
                                //mContext.startActivity(intnt);
                            }
                            Bitmap bitmap = croppedImage;
                            //la llevo al tamano de la pantalla
                            double factorW = 1;
                            double factorH = 1;
                            if (bitmap != null) {
                                if (rotated) {
                                    factorW = ((double) bitmap.getWidth()) / ((double) mCameraSource.getPreviewSize().getWidth());
                                    factorH = ((double) bitmap.getHeight()) / ((double) mCameraSource.getPreviewSize().getHeight());
                                } else {
                                    factorW = ((double) bitmap.getWidth()) / ((double) mCameraSource.getPreviewSize().getHeight());
                                    factorH = ((double) bitmap.getHeight()) / ((double) mCameraSource.getPreviewSize().getWidth());
                                }
                            }
                            int left = 0;
                            int top = 0;
                            int width = 0;
                            int height = 0;

                            if (size != null) {
                                if (destin == 1) {
                                    left = (int) (size.left * factorW);
                                    top = (int) (size.top * factorH);
                                    width = (int) (size.right * factorW);
                                    height = (int) (size.bottom * factorH);
                                } else {
                                    left = (int) (size.left * factorW);
                                    top = (int) (size.top * factorH);
                                    width = (int) (size.right * factorW + 300);
                                    height = (int) (size.bottom * factorH + 100);
                                }
                            } else {
                            /*
                            if(docSize != null) {
                                //si la imagen es mas chica que el documento tomo la imagen de base
                                left =  (int) (docSize.left * factorW);
                                System.out.println("docSize(left): "+(docSize.left * factorW));
                                top =   (int) (docSize.top * factorH);
                                System.out.println("docSize(top): "+(docSize.top * factorH));
                                width = (int) ((docSize.right - docSize.left) * factorW);
                                System.out.println("docSize(width): "+((docSize.right - docSize.left) * factorW));
                                if (width > bitmap.getWidth()) {
                                    //left = left - (width - bitmap.getWidth()); //me corro a la izquierda
                                    width = bitmap.getWidth();
                                }

                                height = (int) ((docSize.bottom - docSize.top) * factorH);
                                System.out.println("docSize(height): "+((docSize.bottom - docSize.top) * factorH));
                                if (height > bitmap.getHeight()) {
                                    //top = top- (height-bitmap.getHeight()); //subo
                                    height = bitmap.getHeight(); //subo
                                }
                            }*/
                            }
                            try {
                                if (width <= 0) {
                                    width = (int) size.right;
                                }
                                if (height <= 0) {
                                    height = (int) size.bottom;
                                }
                                croppedImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                            } catch (Exception e) {
                                Log.v(TAG, "Error  " + e);
                                errorCamera();
                            }
                            if (croppedImage == null) {
                                Log.v(TAG, "Error croppedImage == null");
                                errorCamera();
                            }

                            Rect r = new Rect();
                            r.left = left;
                            r.top = top;
                            r.right = left + width;
                            r.bottom = top + height;

                            Canvas canvas = new Canvas(croppedImage);
                            Rect dstRect = new Rect(0, 0, width, height);
                            if (bitmap != null) {
                                canvas.drawBitmap(bitmap, r, dstRect, null);
                            }

                            switch (destin) {
                                case 1:
                                    //right y bottom son width y height
                                    person.SetFaceDetected(croppedImage);
                                    break;
                                case 2:
                                    person.dniFront = croppedImage;
                                    break;
                                case 3:
                                    person.dniBack = croppedImage;
                                    break;
                            }
                        }else{
                            HelperFunctions.ShowAlert((Activity) mContext,  "No se pudo procesar la imagen del DNI");
                        }
                    }
                });

    }

    private void errorCamera() {
        Intent intnt = new Intent(mContext, RegistrarScanUserActivity.class);
        mContext.startActivity(intnt);
        HelperFunctions.ShowAlert((Activity) mContext, ERROR_COMARE_SOURCE);
    }

    private int getOrientation(Bitmap bitmap) {
        if (bitmap.getWidth() > bitmap.getHeight()) {
            return 90;
        }
        return 0;
    }

    private Bitmap scaleBitmap(Bitmap bm) {
        int width =  bm.getWidth();
        int height = bm.getHeight();
        Log.v("Pictures", "Width and height are " + width + "--" + height);

        if (width > height) {
            // landscape
            float ratio = (float) width / 660;
            width = 660;
            height = (int) (height / ratio);
        } else if (height > width) {
            // portrait
            float ratio = (float) height / 660;
            height = 660;
            width = (int) (width / ratio);
        } else {
            // square
            height = 660;
            width = 660;
        }

        Log.v("Pictures", "after scaling Width and height are " + width + "--" + height);

        bm = Bitmap.createScaledBitmap(bm, width, height, false);
        return bm;
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

    /*public void release() {
        if (mCameraSource != null) {
            mCameraSource.release();
            mCameraSource = null;
        }
    }*/



    public void setCamera(Camera mCamera) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
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

    private void startIfReady() throws IOException {
        if (mStartRequested && mSurfaceAvailable) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if (mCameraSource != null) {
                try {
                    mCameraSource.start(mSurfaceView.getHolder());
                } catch (Exception e) {
                    Log.e(TAG, "mCameraSource.star", e);
                    errorCamera();
                }
//                if (mOverlay != null) {
//                    Size size = mCameraSource.getPreviewSize();
//                    int min = Math.min(size.getWidth(), size.getHeight());
//                    int max = Math.max(size.getWidth(), size.getHeight());
//                    if (isPortraitMode()) {
//                        // Swap width and height sizes when in portrait, since it will be rotated by
//                        // 90 degrees
//                        mOverlay.setCameraInfo(min, max, mCameraSource.getCameraFacing());
//                    } else {
//                        mOverlay.setCameraInfo(max, min, mCameraSource.getCameraFacing());
//                    }
//                    mOverlay.clear();
//                }
//                mStartRequested = false;
            }
        }
    }

    public void setPreviewSize(int _width, int _height){
        width = _width;
        height = _height;
    }

    int width = 320;
    int height = 240;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

//        if (mCameraSource != null) {
//            Size size;
//            size = mCameraSource.getPreviewSize();
//            if(size == null){
//                size = new Size(1920, 1080);
//            }
//            Log.e("CamperaPreviewSize", size.getWidth() + " " + size.getHeight());
//            if (size != null && mSupportedPreviewSizes != null) {
//                Camera.Size size2 = getOptimalPreviewSize(mSupportedPreviewSizes, size.getWidth(), size.getHeight());
//                width = size2.width;
//                height = size2.height;
//            } else {
//                size = mCameraSource.getPreviewSize();
//                if (size != null) {
//                    width = size.getWidth();
//                    height = size.getHeight();
//                }
//            }
//        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
//        if (isPortraitMode()) {
//            int tmp = width;
//            width = height;
//            height = tmp;
//        }
//
//        final int layoutWidth = right - left;
//        final int layoutHeight = bottom - top;
//
//        // Computes height and width for potentially doing fit width.
//        int childWidth = layoutWidth;
//        int childHeight = (int) (((float) layoutWidth / (float) width) * height);
//
//        // If height is too tall using fit width, does fit height instead.
//        if (childHeight > layoutHeight) {
//            childHeight = layoutHeight;
//            childWidth = (int) (((float) layoutHeight / (float) height) * width);
//        }

        for (int i = 0; i < getChildCount(); ++i) {
//            getChildAt(i).layout(0, 0, childWidth, childHeight); //childHeight
            getChildAt(i).layout(left, top, right, bottom); //childHeight
        }

//        try {
//            startIfReady();
//        } catch (IOException e) {
//            Log.e(TAG, "Could not start camera source.", e);
//            errorCamera();
//        }
    }

    private boolean isPortraitMode() {
        int orientation = mContext.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }
        Log.d(TAG, "isPortraitMode returning false by default");
        return false;
    }

    @Override
    public void SetSize(RectF size) {
        docSize = size;
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



