package com.autenticar.tuid.db;

public class NotificationDBHelper {

    public  static class NotificationDB{
        public static final String TABLE_NAME="NOTIFICATIONTOKEN_VIDA";
        public static final String ID_COL="ID_NO";
        public static final String TITLE_COL="TITLE_NO";
        public static final String MENSSAGER_COL="MENSSAGER_NO";
        public static final String DATE_COL="DATE_NO";
        public static final String TOKEN_COL="TOKEN_NO";
    }
    public static final String NOTI_TABLE_CREATE="CREATE TABLE "+NotificationDB.TABLE_NAME+" ("+NotificationDB.ID_COL+" INTEGER PRIMARY KEY AUTOINCREMENT, "+NotificationDB.TITLE_COL+" TEXT, "+NotificationDB.MENSSAGER_COL+" TEXT, "+NotificationDB.DATE_COL+" TEXT, "+NotificationDB.TOKEN_COL+" TEXT);";
    //public static final String NOTI_TABLE_SELECT_ALL = "SELECT * FROM "+NotificationDB.TABLE_NAME+" WHERE "+NotificationDB.TOKEN_COL+"="+NotificationDB.token+" ORDER BY "+NotificationDB.ID_COL+" DESC";
    public static final String NOTI_TABLE_SELECT_ID = "SELECT "+NotificationDB.TITLE_COL+", "+NotificationDB.MENSSAGER_COL+", "+NotificationDB.DATE_COL+" TABLE "+NotificationDB.TABLE_NAME+" WHERE "+NotificationDB.ID_COL+"=" ;
    public static final String NOTI_TABLE_DROP = "DROP TABLE IF EXISTS "+NotificationDB.TABLE_NAME;
    public static final String NOTI_TABLE_SELECT_ALL(String Token){
        return "SELECT * FROM "+NotificationDB.TABLE_NAME;//+" WHERE "+NotificationDB.TOKEN_COL+" LIKE "+Token;
    }
}