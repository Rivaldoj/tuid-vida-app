package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

/** GET {base}/vida/v2/session: sesion de Face Liveness (si falla, vienen res/status/message de ResponseBase). */
public class LivenessSessionResponse extends ResponseBase {

    @SerializedName("sessionId")
    public String sessionId;
}
