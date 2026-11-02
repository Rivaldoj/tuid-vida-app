package com.autenticar.tuid.services;

import com.autenticar.tuid.model.ChanceRequest;
import com.autenticar.tuid.model.ChanceResponse;
import com.autenticar.tuid.model.ConfirmationRequest;
import com.autenticar.tuid.model.ConfirmationResponse;
import com.autenticar.tuid.model.ContactResponse;
import com.autenticar.tuid.model.LoginRequest;
import com.autenticar.tuid.model.LoginResponse;
import com.autenticar.tuid.model.ReconfirmationRequest;
import com.autenticar.tuid.model.ReconfirmationResponse;
import com.autenticar.tuid.model.RegisterRequest;
import com.autenticar.tuid.model.RegisterResponse;
import com.autenticar.tuid.model.ResponseBase;
import com.autenticar.tuid.model.RevalidationRequest;
import com.autenticar.tuid.model.RevalidationResponse;
import com.autenticar.tuid.model.SearchdniRequest;
import com.autenticar.tuid.model.SearchdniResponse;
import com.autenticar.tuid.model.UploadRequest;
import com.autenticar.tuid.model.ValidationRequest;
import com.autenticar.tuid.model.ValidationResponse;
import com.autenticar.tuid.model.ContactRequest;
import com.autenticar.tuid.v2.services.CheckDocumentRequest;
import com.autenticar.tuid.v2.services.CheckDocumentResponse;
import com.autenticar.tuid.v2.services.ContactRequest2;
import com.autenticar.tuid.v2.services.FormSubmitRequest;
import com.autenticar.tuid.v2.services.GetCuandoCobroRequest;
import com.autenticar.tuid.v2.services.GetCuandoCobroResponse;
import com.autenticar.tuid.v2.services.GetEntitiesResponse;
import com.autenticar.tuid.v2.services.ManualRegisterRequest;
import com.autenticar.tuid.v2.services.ManualRegisterResponse;
import com.autenticar.tuid.v2.services.ManualRegisterSendCodeRequest;
import com.autenticar.tuid.v2.services.ManualRegisterSendCodeResponse;
import com.autenticar.tuid.v2.services.SolicitarCertificadoResponse;
import com.autenticar.tuid.v2.services.ValidationRequest2;
import com.autenticar.tuid.v2.services.ValidationResponse2;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TuidService {

    @POST("register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("chance")
    Call<ChanceResponse> chance(@Body ChanceRequest request);

    @POST("validation")
    Call<ValidationResponse> validation(@Body ValidationRequest request);

    @POST("validation")
    Call<ValidationResponse> validation2(@Body ValidationRequest2 request);

    @POST("confirmation")
    Call<ConfirmationResponse> confirmation(@Body ConfirmationRequest request);

    @POST("upload")
    Call<ResponseBase> upload(@Body UploadRequest request);

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("comentary")
    Call<ContactResponse> comentary(@Body ContactRequest request);

    @POST("comentary")
    Call<ContactResponse> comentary2(@Body ContactRequest2 request);

    @POST("revalidation")
    Call<RevalidationResponse> revalidation(@Body RevalidationRequest request);

    @POST("revalidation")
    Call<ValidationResponse> revalidation2(@Body ValidationRequest2 request);

    @POST("reconfirmation")
    Call<ReconfirmationResponse> reconfirmation(@Body ReconfirmationRequest request);

    @POST("reconfirmation")
    Call<ConfirmationResponse> reconfirmation2(@Body ConfirmationRequest request);

    @POST("searchdni")
    Call<SearchdniResponse> searchdni(@Body SearchdniRequest request);

    @GET("entities")
    Call<GetEntitiesResponse> getEntities();

    @GET("payments")
    Call<GetCuandoCobroResponse> getCuandoCobro(@Query("dni") String dni, @Query("sexo") String sexo);

    @POST("checkDocument")
    Call<CheckDocumentResponse> checkDni(@Body CheckDocumentRequest request);

    @GET("certificate")
    Call<SolicitarCertificadoResponse> getCertificate(@Query("dni") String dni, @Query("sexo") String sexo);

    @POST("manual")
    Call<ManualRegisterResponse> registroManual(@Body ManualRegisterRequest request);

    @POST("manual/validate")
    Call<ManualRegisterSendCodeResponse> registroManualSendCode(@Body ManualRegisterSendCodeRequest request);

    @POST("auth/pendingTask")
    Call<JsonObject> submitForm(@Body FormSubmitRequest request);

}
