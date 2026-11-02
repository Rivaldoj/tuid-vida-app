package com.autenticar.tuid.facedetection;

        import android.content.Context;
        import android.graphics.Canvas;
        import android.graphics.Color;
        import android.graphics.Paint;
        import android.graphics.Rect;
        import android.view.View;

        import com.autenticar.tuid.model.rectagleUser;

        import java.util.ArrayList;

public class Rectangle extends View {

    ArrayList<rectagleUser> ru;
    int id;

    public Rectangle(Context context, ArrayList<rectagleUser> ru, int id) {
        super(context);
        this.ru = ru;
        this.id=id;
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        canvas.drawColor(Color.argb(0, 0, 0, 0));
//
//        Paint paintPrimary = new Paint();
//        paintPrimary.setStrokeWidth(2);
//        paintPrimary.setColor(Color.rgb(100, 100, 100));
//        paintPrimary.setStyle(Paint.Style.STROKE);
//
//        Paint paintSecond = new Paint();
//        paintSecond.setStrokeWidth(2);
//        paintSecond.setColor(Color.RED);
//        paintSecond.setStyle(Paint.Style.STROKE);
//
//        Paint paintLines = new Paint();
//        paintLines.setStrokeWidth(5);
//        paintLines.setColor(Color.WHITE);
//        paintLines.setStyle(Paint.Style.STROKE);
//
//        int valor = 30;
//        System.out.println("TAMAÑO: " + (ru.size()-1));
//        for (int i = 0; i <ru.size(); i++) {
//            Rect rect = new Rect();
//            rect.top = ru.get(i).getTop();
//            rect.bottom = ru.get(i).getButton();
//            rect.left = ru.get(i).getLeft();
//            rect.right = ru.get(i).getRigth();
//            if (id == ru.get(i).getId()) {
//                canvas.drawRect(rect, paintPrimary);
//            } else {
//                canvas.drawRect(rect, paintSecond);
//            }
//
//            canvas.drawLine(rect.centerX() - (rect.width() / 2), rect.centerY() - (rect.height() / 2), rect.centerX() - (rect.width() / 2), (rect.centerY() - (rect.height() / 2)) + valor, paintLines);
//            canvas.drawLine(rect.centerX() - (rect.width() / 2), rect.centerY() - (rect.height() / 2), (rect.centerX() - (rect.width() / 2)) + valor, rect.centerY() - (rect.height() / 2), paintLines);
//
//            canvas.drawLine(rect.centerX() + (rect.width() / 2), rect.centerY() + (rect.height() / 2), rect.centerX() + (rect.width() / 2), (rect.centerY() + (rect.height() / 2)) - valor, paintLines);
//            canvas.drawLine(rect.centerX() + (rect.width() / 2), rect.centerY() + (rect.height() / 2), (rect.centerX() + (rect.width() / 2)) - valor, rect.centerY() + (rect.height() / 2), paintLines);
//
//            canvas.drawLine(rect.centerX() - (rect.width() / 2), rect.centerY() + (rect.height() / 2), rect.centerX() - (rect.width() / 2), (rect.centerY() + (rect.height() / 2)) - valor, paintLines);
//            canvas.drawLine(rect.centerX() - (rect.width() / 2), rect.centerY() + (rect.height() / 2), (rect.centerX() - (rect.width() / 2)) + valor, rect.centerY() + (rect.height() / 2), paintLines);
//
//            canvas.drawLine(rect.centerX() + (rect.width() / 2), rect.centerY() - (rect.height() / 2), rect.centerX() + (rect.width() / 2), (rect.centerY() - (rect.height() / 2)) + valor, paintLines);
//            canvas.drawLine(rect.centerX() + (rect.width() / 2), rect.centerY() - (rect.height() / 2), (rect.centerX() + (rect.width() / 2)) - valor, rect.centerY() - (rect.height() / 2), paintLines);
//        }
    }
}
