package com.autenticar.tuid.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.autenticar.tuid.db.CUILDBHelper;
import com.autenticar.tuid.db.NotificationDBHelper;
import com.autenticar.tuid.db.TuIDVidaDBHelper;
import com.autenticar.tuid.model.CUIL;
import com.autenticar.tuid.model.Notification;

import java.util.ArrayList;

public class SQLHelper extends SQLiteOpenHelper {
    public SQLHelper(@Nullable Context context) {
        super(context, TuIDVidaDBHelper.DATABASE_NAME, null, TuIDVidaDBHelper.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.e("SQL", "onCreate");
        sqLiteDatabase.execSQL(CUILDBHelper.CUIL_TABLE_CREATE);
        sqLiteDatabase.execSQL(NotificationDBHelper.NOTI_TABLE_CREATE);
        this.onUpgrade(sqLiteDatabase, 1, TuIDVidaDBHelper.DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVers, int newVers) {
        Log.e("SQL", "onUpgrade");
        //this.onCreate(sqLiteDatabase);
        //sqLiteDatabase.execSQL(CUILDBHelper.CUIL_TABLE_DROP);
        //sqLiteDatabase.execSQL(NotificationDBHelper.NOTI_TABLE_DROP);
        if (oldVers == 1 && newVers == 4) {
            Log.e("SQL", "ADD LONG");
            sqLiteDatabase.execSQL(CUILDBHelper.CUIL_TABLE_UPDATE_LONGITUDE);
            Log.e("SQL", "ADD LAT");
            sqLiteDatabase.execSQL(CUILDBHelper.CUIL_TABLE_UPDATE_LATITUDE);
            Log.e("SQL", "ADD DIRE");
            sqLiteDatabase.execSQL(CUILDBHelper.CUIL_TABLE_UPDATE_DIRECTION);
        }
        if (oldVers == 1 && newVers == 3) {
            Log.e("SQL", "ADD LONG");
            sqLiteDatabase.execSQL(CUILDBHelper.CUIL_TABLE_UPDATE_LONGITUDE);
            Log.e("SQL", "ADD LAT");
            sqLiteDatabase.execSQL(CUILDBHelper.CUIL_TABLE_UPDATE_LATITUDE);
            //sqLiteDatabase.execSQL(CUILDBHelper.CUIL_TABLE_UPDATE_DIRECTION);
        }
        if (oldVers == 3 && newVers == 4) {
            Log.e("SQL", "ADD DIR");
            sqLiteDatabase.execSQL(CUILDBHelper.CUIL_TABLE_UPDATE_DIRECTION);
        }
        if (oldVers <  6) {
            Log.e("SQL" , "Version < 6");
            try { sqLiteDatabase.execSQL(CUILDBHelper.CUIL_TABLE_UPDATE_LONGITUDE); } catch (Exception e){ Log.e("EX", e.getMessage()); }
            try { sqLiteDatabase.execSQL(CUILDBHelper.CUIL_TABLE_UPDATE_LATITUDE); } catch (Exception e){ Log.e("EX", e.getMessage()); }
            try { sqLiteDatabase.execSQL(CUILDBHelper.CUIL_TABLE_UPDATE_DIRECTION); } catch (Exception e){ Log.e("EX", e.getMessage()); }
            try { sqLiteDatabase.execSQL(CUILDBHelper.CUIL_TABLE_UPDATE_ENTIDAD); } catch (Exception e){ Log.e("EX", e.getMessage()); }
        }

    }

    public void insertCUIL(CUIL cuil) {
        SQLiteDatabase db = this.getWritableDatabase();
        try{ this.onUpgrade(db, 1, TuIDVidaDBHelper.DATABASE_VERSION); } catch (Exception e){}
        ContentValues values = new ContentValues();
        values.put(CUILDBHelper.CUIL.NUMER_COL, cuil.getNum_cuil());
        values.put(CUILDBHelper.CUIL.DATE_COL, cuil.getDate_cuil());
        values.put(CUILDBHelper.CUIL.LATITUDE_COL, cuil.getLatitude_cuil());
        values.put(CUILDBHelper.CUIL.LONGITUDE_COL, cuil.getLongitude_cuil());
        values.put(CUILDBHelper.CUIL.DIRECTION_COL, cuil.getDiretion_cuil());
        values.put(CUILDBHelper.CUIL.ENTIDAD_COL, cuil.getEntidad_pagadora_cuil());
        db.insert(CUILDBHelper.CUIL.TABLE_NAME, null, values);
        db.close();
    }

    /** Maximo de certificaciones que se guardan en el telefono por persona. */
    public static final int MAX_HISTORIAL_POR_USUARIO = 20;

    /** Deja solo las {@code max} certificaciones mas recientes de esa persona; las demas se borran SOLO del telefono. */
    public void recortarHistorial(String cuil, int max) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("DELETE FROM " + CUILDBHelper.CUIL.TABLE_NAME
                    + " WHERE " + CUILDBHelper.CUIL.NUMER_COL + " = ? AND " + CUILDBHelper.CUIL.ID_COL + " NOT IN ("
                    + "SELECT " + CUILDBHelper.CUIL.ID_COL + " FROM " + CUILDBHelper.CUIL.TABLE_NAME
                    + " WHERE " + CUILDBHelper.CUIL.NUMER_COL + " = ? ORDER BY " + CUILDBHelper.CUIL.ID_COL + " DESC LIMIT " + max + ")",
                    new Object[]{cuil, cuil});
        } finally {
            db.close();
        }
    }

    /**
     * Reemplaza el historial local de una persona por el que informo el servidor, en una sola transaccion:
     * si algo falla, no se pierde nada. {@code nuevas} va de la mas antigua a la mas reciente (el orden de
     * carga define el orden de lista). Devuelve false si no se pudo guardar (el historial anterior queda igual).
     */
    public boolean reemplazarHistorial(String cuil, java.util.List<CUIL> nuevas) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            db.delete(CUILDBHelper.CUIL.TABLE_NAME, CUILDBHelper.CUIL.NUMER_COL + "=?", new String[]{cuil});
            for (CUIL c : nuevas) {
                ContentValues values = new ContentValues();
                values.put(CUILDBHelper.CUIL.NUMER_COL, cuil);
                values.put(CUILDBHelper.CUIL.DATE_COL, c.getDate_cuil());
                values.put(CUILDBHelper.CUIL.LATITUDE_COL, c.getLatitude_cuil());
                values.put(CUILDBHelper.CUIL.LONGITUDE_COL, c.getLongitude_cuil());
                values.put(CUILDBHelper.CUIL.DIRECTION_COL, c.getDiretion_cuil());
                values.put(CUILDBHelper.CUIL.ENTIDAD_COL, c.getEntidad_pagadora_cuil());
                if (db.insert(CUILDBHelper.CUIL.TABLE_NAME, null, values) < 0) return false;
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e("SQL", "reemplazarHistorial", e);
            return false;
        } finally {
            try { db.endTransaction(); } catch (Exception ignored) { }
            db.close();
        }
    }

    public CUIL getCUILByID(int id) {
        CUIL cuil = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String[] COLUMNS = {
                CUILDBHelper.CUIL.ID_COL,
                CUILDBHelper.CUIL.NUMER_COL,
                CUILDBHelper.CUIL.DATE_COL,
                CUILDBHelper.CUIL.LATITUDE_COL,
                CUILDBHelper.CUIL.LONGITUDE_COL,
                CUILDBHelper.CUIL.DIRECTION_COL
        };
        Cursor cursor = db.query(CUILDBHelper.CUIL.TABLE_NAME, COLUMNS, "id=?", null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            cuil = new CUIL(cursor.getString(0), cursor.getString(2), cursor.getString(1), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6));
        }
        return cuil;
    }

    public ArrayList<CUIL> getAllCUIL() {
        ArrayList<CUIL> list_cuil = new ArrayList();
        SQLiteDatabase db = this.getWritableDatabase();
        this.onUpgrade(db, 1, TuIDVidaDBHelper.DATABASE_VERSION);
        Cursor cursor = db.rawQuery(CUILDBHelper.CUIL_TABLE_SELECT_ALL, null);
        CUIL cuil = null;

        if (cursor.moveToFirst()) {
            Log.e("cursor", cursor.getColumnCount() + "");
            do {
                if(cursor.getColumnCount()==4){
                    cuil = new CUIL(cursor.getString(0), cursor.getString(2), cursor.getString(1),"0.0", "0.0", "", "");
                }else if(cursor.getColumnCount()==5){
                    cuil = new CUIL(cursor.getString(0), cursor.getString(2), cursor.getString(1), cursor.getString(4),  cursor.getString(3), "","");
                }else if(cursor.getColumnCount()==6){
                    cuil = new CUIL(cursor.getString(0), cursor.getString(2), cursor.getString(1), cursor.getString(4), cursor.getString(3), cursor.getString(5), "");
                }else if(cursor.getColumnCount()==7){
                    cuil = new CUIL(cursor.getString(0), cursor.getString(2), cursor.getString(1), cursor.getString(4), cursor.getString(3), cursor.getString(5), cursor.getString(6));
                }
                list_cuil.add(cuil);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list_cuil;
    }

    public void deleteCUIL(CUIL cuil) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(CUILDBHelper.CUIL.TABLE_NAME, CUILDBHelper.CUIL.ID_COL + "=?", new String[]{
                cuil.getId_cuil()
        });
        db.close();
    }

    public void deleteCUILAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(CUILDBHelper.CUIL.TABLE_NAME, "", null);
        db.close();
    }

    public void insertNotification(Notification noti) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NotificationDBHelper.NotificationDB.TITLE_COL, noti.getTitle_noti());
        values.put(NotificationDBHelper.NotificationDB.MENSSAGER_COL, noti.getMessager_noti());
        values.put(NotificationDBHelper.NotificationDB.DATE_COL, noti.getDate_noti());
        db.insert(NotificationDBHelper.NotificationDB.TABLE_NAME, null, values);
        db.close();
    }

    public Notification getNotificationByID(int id) {
        Notification noti = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String[] COLUMNS = {
                NotificationDBHelper.NotificationDB.ID_COL,
                NotificationDBHelper.NotificationDB.TITLE_COL,
                NotificationDBHelper.NotificationDB.MENSSAGER_COL,
                NotificationDBHelper.NotificationDB.DATE_COL
        };
        Cursor cursor = db.query(NotificationDBHelper.NotificationDB.TABLE_NAME, COLUMNS, "id=?", null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            noti = new Notification(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
        }
        return noti;
    }

    public ArrayList<Notification> getAllNotification(String Token) {
        ArrayList<Notification> list = new ArrayList();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(NotificationDBHelper.NOTI_TABLE_SELECT_ALL(Token), null);
        Notification noti = null;
        if (cursor.moveToFirst()) {
            do {
                noti = new Notification(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
                list.add(noti);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public void deleteNotification(Notification noti) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(NotificationDBHelper.NotificationDB.TABLE_NAME, NotificationDBHelper.NotificationDB.ID_COL + "=?", new String[]{
                noti.getId_noti()
        });
        db.close();
    }

    public void dropNotification() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(NotificationDBHelper.NOTI_TABLE_DROP);
        db.close();
    }
}
