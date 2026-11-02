// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.autenticar.tuid.facedetection;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceLandmark;
import android.graphics.PointF;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.autenticar.tuid.ui.vision.GraphicOverlay;
import com.autenticar.tuid.ui.vision.GraphicOverlay.Graphic;
import com.google.android.gms.vision.CameraSource;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
public class FaceGraphic extends Graphic {
    private static final float FACE_POSITION_RADIUS = 4.0f;
    private static final float ID_TEXT_SIZE = 30.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;
    private final Paint facePositionPaint;
    private final Paint idPaint;
    private final Paint boxPaint;
    private int facing;
    private volatile Face firebaseVisionFace;

    public FaceGraphic(GraphicOverlay overlay, Face face, int facing) {
        super(overlay);

        firebaseVisionFace = face;
        this.facing = facing;
        final int selectedColor = Color.WHITE;

        facePositionPaint = new Paint();
        facePositionPaint.setColor(selectedColor);

        idPaint = new Paint();
        idPaint.setColor(selectedColor);
        idPaint.setTextSize(ID_TEXT_SIZE);

        boxPaint = new Paint();
        boxPaint.setColor(selectedColor);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = firebaseVisionFace;
        if (face == null) {
            return;
        }

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getBoundingBox().centerX());
        float y = translateY(face.getBoundingBox().centerY());
        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, facePositionPaint);
        canvas.drawText("id: " + face.getTrackingId(), x + ID_X_OFFSET, y + ID_Y_OFFSET, idPaint);
        canvas.drawText(
                "happiness: " + String.format("%.2f", face.getSmilingProbability()),
                x + ID_X_OFFSET * 3,
                y - ID_Y_OFFSET,
                idPaint);
        if (facing == CameraSource.CAMERA_FACING_FRONT) {
            canvas.drawText(
                    "right eye: " + String.format("%.2f", face.getRightEyeOpenProbability()),
                    x - ID_X_OFFSET,
                    y,
                    idPaint);
            canvas.drawText(
                    "left eye: " + String.format("%.2f", face.getLeftEyeOpenProbability()),
                    x + ID_X_OFFSET * 6,
                    y,
                    idPaint);
        } else {
            canvas.drawText(
                    "left eye: " + String.format("%.2f", face.getLeftEyeOpenProbability()),
                    x - ID_X_OFFSET,
                    y,
                    idPaint);
            canvas.drawText(
                    "right eye: " + String.format("%.2f", face.getRightEyeOpenProbability()),
                    x + ID_X_OFFSET * 6,
                    y,
                    idPaint);
        }

        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getBoundingBox().width() / 2.0f);
        float yOffset = scaleY(face.getBoundingBox().height() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        canvas.drawRect(left, top, right, bottom, boxPaint);

        // draw landmarks
        drawLandmarkPosition(canvas, face, FaceLandmark.MOUTH_BOTTOM);
        drawLandmarkPosition(canvas, face, FaceLandmark.LEFT_CHEEK);
        drawLandmarkPosition(canvas, face, FaceLandmark.LEFT_EAR);
        drawLandmarkPosition(canvas, face, FaceLandmark.MOUTH_LEFT);
        drawLandmarkPosition(canvas, face, FaceLandmark.LEFT_EYE);
        drawLandmarkPosition(canvas, face, FaceLandmark.NOSE_BASE);
        drawLandmarkPosition(canvas, face, FaceLandmark.RIGHT_CHEEK);
        drawLandmarkPosition(canvas, face, FaceLandmark.RIGHT_EAR);
        drawLandmarkPosition(canvas, face, FaceLandmark.RIGHT_EYE);
        drawLandmarkPosition(canvas, face, FaceLandmark.MOUTH_RIGHT);
    }

    private void drawLandmarkPosition(Canvas canvas, Face face, int landmarkID) {
        FaceLandmark landmark = face.getLandmark(landmarkID);
        if (landmark != null) {
            PointF point = landmark.getPosition();
            canvas.drawCircle(
                    translateX(point.x),
                    translateY(point.y),
                    10f, idPaint);
        }
    }
}
