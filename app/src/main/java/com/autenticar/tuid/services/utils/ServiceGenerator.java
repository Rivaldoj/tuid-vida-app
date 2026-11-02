package com.autenticar.tuid.services.utils;

import android.content.Context;

import com.autenticar.tuid.model.AuthResponse;
import com.autenticar.tuid.v2.FormActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

/**
 * Creado por mbatto on 3/11/2016.
 */

public class ServiceGenerator {

    public static Retrofit.Builder builder;
    public static Context context;
    private static HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);

    private static OkHttpClient.Builder httpClient;

    public static <S> S createService(Class<S> serviceClass) {
        return createService(serviceClass, null);
    }

    public static <S> S createService(Class<S> serviceClass, final AuthResponse authResponse) {

        if (httpClient == null) {/*SelfSignInClient(context).getOkHttpClient(); */
            httpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor);
        }
        /* agregar token*/
        //if (authResponse != null) {
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", "7leEC54T3zFPLBTyVfqdR1q+qDnQwbjpAt6eFs3kb80=")
                        // .header("UID", authResponse.UserID)
                        // .header("TPR", authResponse.UserType)
                        .method(original.method(), original.body());
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });
        //}

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }

    public static <S> S createFormService(Class<S> serviceClass,String token) {

        if (httpClient == null) {/*SelfSignInClient(context).getOkHttpClient(); */
            httpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor);
        }
        /* agregar token*/
        //if (authResponse != null) {
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", "7leEC54T3zFPLBTyVfqdR1q+qDnQwbjpAt6eFs3kb80=")
                        .header("X-Auth-Token", token)
                        // .header("UID", authResponse.UserID)
                        // .header("TPR", authResponse.UserType)
                        .method(original.method(), original.body());
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });
        //}

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }
}