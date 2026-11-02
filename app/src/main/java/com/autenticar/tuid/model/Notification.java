package com.autenticar.tuid.model;

public class Notification {
    private String id_noti;
    private String title_noti;
    private String messager_noti;
    private String date_noti;

    public Notification() {
    }

    public Notification(String title_noti, String messager_noti, String date_noti) {
        this.title_noti = title_noti;
        this.messager_noti = messager_noti;
        this.date_noti = date_noti;
    }

    public Notification(String id_noti, String title_noti, String messager_noti, String date_noti) {
        this.id_noti = id_noti;
        this.title_noti = title_noti;
        this.messager_noti = messager_noti;
        this.date_noti = date_noti;
    }

    public String getId_noti() {
        return id_noti;
    }

    public void setId_noti(String id_noti) {
        this.id_noti = id_noti;
    }

    public String getTitle_noti() {
        return title_noti;
    }

    public void setTitle_noti(String title_noti) {
        this.title_noti = title_noti;
    }

    public String getMessager_noti() {
        return messager_noti;
    }

    public void setMessager_noti(String messager_noti) {
        this.messager_noti = messager_noti;
    }

    public String getDate_noti() {
        return date_noti;
    }

    public void setDate_noti(String date_noti) {
        this.date_noti = date_noti;
    }
}
