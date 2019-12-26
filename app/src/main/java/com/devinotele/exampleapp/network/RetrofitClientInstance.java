package com.devinotele.exampleapp.network;

import com.devinotele.exampleapp.BuildConfig;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class RetrofitClientInstance {

    private static Retrofit retrofitFirebase;
    private static Retrofit retrofitDevino;
    private static final String FIREBASE_URL = "https://fcm.googleapis.com/fcm/";
    private static final String DEVINO_PUSH_SERVICE_URL = BuildConfig.DEVINO_PUSH_SERVICE_URL;
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    static Retrofit getRetrofitInstance() {
        if (retrofitFirebase == null) {

            httpClient.addInterceptor(chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("Authorization", BuildConfig.DEVINO_API_KEY)
                        .header("Content-Type", "application/json")
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            });

            retrofitFirebase = new Retrofit.Builder()
                    .baseUrl(FIREBASE_URL)
                    .client(httpClient.build())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitFirebase;
    }

    static Retrofit getRetrofitInstanceForDevinoPush() {
        if (retrofitDevino == null) {

            httpClient.addInterceptor(chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("Authotization", BuildConfig.DEVINO_SEND_API_KEY)
                        .header("Content-Type", "application/json")
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            });

            retrofitDevino = new Retrofit.Builder()
                    .baseUrl(DEVINO_PUSH_SERVICE_URL)
                    .client(httpClient.build())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitDevino;
    }

}
