package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class ResponseBase {

    @SerializedName("res")
    public String Response;

    @SerializedName("status")
    public int Status;

    @SerializedName("message")
    public String Message;

    @SerializedName("details")
    public String Details;


    public boolean IsOk() {
        return ("ok".equals(this.Response) && Status == 200 && !"chance".equals(Message));
    }

    public boolean IsChance() {
        return ("ok".equals(this.Response) && Status == 200 && "chance".equals(Message));
    }
}


/*
appData = {
        res: 'ok',
        status: 200,
        result: 'image recibida',
    };
 */
