package com.devinotele.exampleapp.network;

import android.util.Log;

import com.devinotele.exampleapp.BuildConfig;
import com.devinotele.exampleapp.R;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

class RetrofitClientInstance {

    private static Retrofit retrofitDevino;
    private static final String DEVINO_PUSH_SERVICE_URL = BuildConfig.DEVINO_PUSH_SERVICE_URL;
    private static final OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    private static volatile String CURRENT_REQUEST_URL = "";

    public String getCurrentRequestUrl() {
        return CURRENT_REQUEST_URL;
    }

    static Retrofit getRetrofitInstanceForDevinoPush() {
        if (retrofitDevino == null) {
            httpClient.addInterceptor(chain -> {
                Log.d("DevinoPush", "chain = " + chain);
                Request original = chain.request();
                CURRENT_REQUEST_URL = chain.request().url().toString();
                Request request = original.newBuilder()
                        .header("Authorization", BuildConfig.DEVINO_SEND_API_KEY)
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