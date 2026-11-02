package com.autenticar.tuid.model;

public class rectagleUser {
    private  int left;
    private int rigth;
    private int top;
    private int button;
    private int id;

    public rectagleUser(int left, int rigth, int top, int button, int id) {
        this.left = left;
        this.rigth = rigth;
        this.top = top;
        this.button = button;
        this.id = id;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getRigth() {
        return rigth;
    }

    public void setRigth(int rigth) {
        this.rigth = rigth;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getButton() {
        return button;
    }

    public void setButton(int button) {
        this.button = button;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
