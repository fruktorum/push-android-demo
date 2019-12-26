package com.devinotele.devinosdk.sdk;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class RetrofitClientInstance {

    private static Retrofit retrofit;
    private static Retrofit firebaseRetrofit;
    private static final String BASE_URL = "https://integrationapi.net/push/sdk/";
    private static final String FIREBASE_URL = "https://fcm.googleapis.com/fcm/";
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();


    static Retrofit getRetrofitInstance(final String apiKey) {
        if (retrofit == null) {

            httpClient.addInterceptor(chain -> {
                Request original = chain.request();

                Request request = original.newBuilder()
                        .header("Authorization", apiKey)
                        .header("Content-Type", "application/json")
                        .method(original.method(), original.body())
                        .build();

                return chain.proceed(request);
            });

            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient.build())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    static Retrofit getFirebaseInstance(final String pushKey) {
        if (firebaseRetrofit == null) {

            httpClient.addInterceptor(chain -> {
                Request original = chain.request();

                Request request = original.newBuilder()
                        .header("Authorization", pushKey)
                        .header("Content-Type", "application/json")
                        .method(original.method(), original.body())
                        .build();

                return chain.proceed(request);
            });

            firebaseRetrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(FIREBASE_URL)
                    .client(httpClient.build())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }


}
