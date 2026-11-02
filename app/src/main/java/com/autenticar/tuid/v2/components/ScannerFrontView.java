package com.autenticar.tuid.v2.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.autenticar.tuid.v2.HomeActivity;

public class ScannerFrontView extends View {
    private Context mContext;
    private Paint mPaint;
    private Paint mStrokePaint;
    private Path mPath = new Path();

    public ScannerFrontView(Context context) {
        super(context);
        mContext = context;
        initPaints();
    }

    public ScannerFrontView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    public ScannerFrontView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaints();
    }


    private void initPaints() {
        mPaint = new Paint();
        mPaint.setColor(Color.parseColor("#A6000000"));

        mStrokePaint = new Paint();
        mStrokePaint.setColor(Color.parseColor("#00ABC0"));
        mStrokePaint.setStrokeWidth(2);
        mStrokePaint.setStyle(Paint.Style.STROKE);

    }

    RectF r;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPath.reset();

//        float strokeWidth = 0;
//        if (canvas.getWidth() < canvas.getHeight()) {
//            strokeWidth = (canvas.getHeight() - canvas.getWidth())/2;
//        } else {
//            strokeWidth = (canvas.getWidth() - canvas.getHeight())/2;
//        }
//
//        mPaint.setStrokeWidth(strokeWidth);

        if(r==null) r = new RectF(canvas.getWidth()*0.01f,canvas.getHeight()*0.1f,canvas.getWidth()*0.99f,canvas.getHeight()*0.9f);
//        mPath.addCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, radius, Path.Direction.CW);
        mPath.addOval(r,Path.Direction.CW);
        mPath.setFillType(Path.FillType.INVERSE_EVEN_ODD);

//        canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, radius, mStrokePaint);
        canvas.drawOval(r,mStrokePaint);

        canvas.drawPath(mPath, mPaint);
    }
}