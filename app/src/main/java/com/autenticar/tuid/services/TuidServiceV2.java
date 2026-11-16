package com.autenticar.tuid.services;

import com.autenticar.tuid.model.CheckRegistrationRequest;
import com.autenticar.tuid.model.CheckRegistrationResponse;
import com.autenticar.tuid.model.HistoryRequest;
import com.autenticar.tuid.model.HistoryResponse;
import com.autenticar.tuid.model.LoginRequest;
import com.autenticar.tuid.model.LoginResponse;
import com.autenticar.tuid.model.SearchdniRequest;
import com.autenticar.tuid.model.SearchdniResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/** Backend Vida v2 ({base}/vida/v2): Fe de Vida con AWS Face Liveness. */
public interface TuidServiceV2 {

    @POST("searchdni")
    Call<SearchdniResponse> searchdni(@Body SearchdniRequest request);

    @POST("checkregistration")
    Call<CheckRegistrationResponse> checkRegistration(@Body CheckRegistrationRequest request);

    @POST("history")
    Call<HistoryResponse> history(@Body HistoryRequest request);

    /** Aviso de token de notificaciones renovado por Firebase (el servidor lo acepta solo con el token anterior como prueba). */
    @POST("refreshdevice")
    Call<com.autenticar.tuid.model.ResponseBase> refreshDevice(@Body com.autenticar.tuid.model.DeviceRefreshRequest request);

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    /** Registro: la sesion de Face Liveness se crea aca y luego se envia en signup. */
    @retrofit2.http.GET("session")
    Call<com.autenticar.tuid.model.LivenessSessionResponse> session();

    @POST("signup")
    Call<com.autenticar.tuid.model.RegisterResponse> signup(@Body com.autenticar.tuid.model.SignupRequest request);
}
