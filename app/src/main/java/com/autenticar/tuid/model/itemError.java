package com.autenticar.tuid.model;

public class itemError {
    private String text_error;
    private String type;

    public itemError(String text_error, String type) {
        this.text_error = text_error;
        this.type = type;
    }

    public itemError() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText_error() {
        return text_error;
    }

    public void setText_error(String text_error) {
        this.text_error = text_error;
    }
}
