package com.autenticar.tuid.db;

public class CUILDBHelper{
    public  static class CUIL{
        public static final String TABLE_NAME="CUIL_VIDA";
        public static final String ID_COL="ID_CU";
        public static final String DATE_COL="DATE_CU";
        public static final String NUMER_COL="NUMBER_CU";
        public static final String LATITUDE_COL="LATITUD_CU";
        public static final String LONGITUDE_COL="LONGITUD_CU";
        public static final String DIRECTION_COL="DIRECTION_CU";
        public static final String ENTIDAD_COL="ENTIDAD_CU";
    }
    public static final String CUIL_TABLE_CREATE="CREATE TABLE "+CUIL.TABLE_NAME+" ("+CUIL.ID_COL+" INTEGER PRIMARY KEY AUTOINCREMENT, "+CUIL.DATE_COL+" TEXT, "+CUIL.NUMER_COL+" TEXT);";
    public static final String CUIL_TABLE_SELECT_ALL = "SELECT  * FROM "+CUIL.TABLE_NAME+" ORDER BY "+CUIL.ID_COL+" DESC";
    public static final String CUIL_TABLE_DROP = "DROP TABLE IF EXISTS "+CUIL.TABLE_NAME;
    public static final String CUIL_TABLE_UPDATE_LATITUDE = "ALTER TABLE "+CUIL.TABLE_NAME+" ADD COLUMN "+CUIL.LATITUDE_COL+" TEXT ;";
    public static final String CUIL_TABLE_UPDATE_LONGITUDE = "ALTER TABLE "+CUIL.TABLE_NAME+" ADD COLUMN "+CUIL.LONGITUDE_COL+" TEXT ;";
    public static final String CUIL_TABLE_UPDATE_DIRECTION = "ALTER TABLE "+CUIL.TABLE_NAME+" ADD COLUMN "+CUIL.DIRECTION_COL+" TEXT ;";
    public static final String CUIL_TABLE_UPDATE_ENTIDAD = "ALTER TABLE "+CUIL.TABLE_NAME+" ADD COLUMN "+CUIL.ENTIDAD_COL+" TEXT ;";

}
